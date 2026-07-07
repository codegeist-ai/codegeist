# minio-oidc-storage-smoke.ps1 - local authentik/MinIO OIDC STS smoke.
#
# Why this exists:
# - Verifies the T008_04 storage foundation without Java Codegeist Server code.
# - Starts a disposable Docker Compose stack where authentik issues a test OIDC
#   token, MinIO exchanges it through STS, and a Dockerized S3 client uses the
#   short-lived credentials for SigV4 object access.
# - The authentik blueprint creates the user, provider, and application. This
#   script creates a local app-password token through authentik's supported API
#   for the non-interactive client-credentials token exchange.
# - The script applies the needed shipped OAuth blueprints plus the mounted
#   fixture blueprint explicitly; authentik's background discovery can be too
#   delayed for a deterministic short smoke timeout.
#
# Inputs:
# - Docker and Docker Compose v2 must be available.
# - The fixture under scripts/tests/fixtures/minio-oidc-storage/ defines local
#   test-only credentials, authentik bootstrap data, and MinIO policy.
# - Use -KeepRunning to leave containers and volumes up for manual inspection.
#
# Related files:
# - scripts/tests/smoke-common.ps1
# - scripts/tests/fixtures/minio-oidc-storage/compose.yml

[CmdletBinding()]
param(
    [string]$FixtureDir = "",

    [string]$ProjectName = "",

    [switch]$KeepRunning,

    [int]$TimeoutSeconds = 180,

    [string]$StatusFile = $env:CODEGEIST_SMOKE_STATUS_FILE
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

if (-not $FixtureDir) {
    $FixtureDir = Join-Path $PSScriptRoot "fixtures/minio-oidc-storage"
}
$FixtureDir = Resolve-SmokePath $FixtureDir

if (-not $ProjectName) {
    $ProjectName = "codegeist-minio-oidc-$PID"
}

$composeFile = Join-Path $FixtureDir "compose.yml"
$composeNetwork = "${ProjectName}_default"
$repoRoot = Resolve-SmokePath (Join-Path $PSScriptRoot ".." "..")
$workDir = Join-Path $repoRoot "app/codegeist/target/smoke-test/minio-oidc-storage/$ProjectName"
$curlImage = "curlimages/curl:8.11.1"
$awsCliImage = "amazon/aws-cli:2.17.50"
$authentikBootstrapToken = "codegeist-smoke-bootstrap-token"
$authentikAppPasswordIdentifier = "codegeist-smoke-app-password"
$authentikAppPassword = "codegeist-smoke-app-password"
$allowedKey = "accounts/codegeist-smoke/artifacts/commands/smoke-command/versions/v1/content"
$deniedKey = "accounts/other-user/artifacts/commands/smoke-command/versions/v1/content"
$bucket = "codegeist-artifacts"
$smokeText = "codegeist minio oidc smoke"

function Write-MinioOidcStatus {
    param(
        [string]$Status,
        [string]$Reason
    )

    Write-SmokeStatusFile $StatusFile ([ordered]@{
        status = $Status
        platform = 'minio-oidc-storage'
        transport = 'docker-compose-authentik-minio-sts'
        reason = $Reason
    })
}

function Invoke-Compose {
    param([string[]]$Arguments)

    & docker compose --project-name $ProjectName --file $composeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Invoke-ComposeQuiet {
    param([string[]]$Arguments)

    $output = & docker compose --project-name $ProjectName --file $composeFile @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        $text = ($output | Out-String).Trim()
        if ($text) {
            throw "docker compose $($Arguments -join ' ') failed with exit code ${LASTEXITCODE}: $text"
        }
        throw "docker compose $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Invoke-ContainerCapture {
    param(
        [string]$Image,
        [string[]]$Arguments,
        [hashtable]$Environment = @{},
        [string[]]$Volumes = @(),
        [bool]$AllowFailure = $false
    )

    $dockerArgs = @("run", "--rm", "--network", $composeNetwork)
    foreach ($volume in $Volumes) {
        $dockerArgs += @("--volume", $volume)
    }
    foreach ($key in $Environment.Keys) {
        $dockerArgs += @("--env", "$key=$($Environment[$key])")
    }
    $dockerArgs += @($Image)
    $dockerArgs += $Arguments

    $output = & docker @dockerArgs 2>&1
    $exitCode = $LASTEXITCODE
    $text = ($output | Out-String).Trim()
    if (-not $AllowFailure -and $exitCode -ne 0) {
        if ($text) {
            throw "docker $($dockerArgs[0..([Math]::Min($dockerArgs.Length - 1, 5))] -join ' ') failed with exit code ${exitCode}: $text"
        }
        throw "docker run failed with exit code $exitCode"
    }

    return [pscustomobject]@{
        ExitCode = $exitCode
        Output = $text
    }
}

function Invoke-CurlCapture {
    param(
        [string[]]$Arguments,
        [bool]$AllowFailure = $false
    )

    return Invoke-ContainerCapture -Image $curlImage -Arguments $Arguments -AllowFailure $AllowFailure
}

function Wait-SmokeEndpoint {
    param(
        [string]$Description,
        [string[]]$CurlArguments
    )

    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        $result = Invoke-CurlCapture -Arguments $CurlArguments -AllowFailure $true
        if ($result.ExitCode -eq 0) {
            return $result.Output
        }
        Start-Sleep -Seconds 2
    } while ([DateTimeOffset]::UtcNow -lt $deadline)

    throw "Timed out waiting for $Description"
}

function ConvertFrom-JwtPayload {
    param([string]$Jwt)

    $parts = $Jwt.Split('.')
    if ($parts.Length -lt 2) {
        throw 'OIDC access token is not a JWT'
    }

    $payload = $parts[1].Replace('-', '+').Replace('_', '/')
    switch ($payload.Length % 4) {
        2 { $payload += '==' }
        3 { $payload += '=' }
        1 { throw 'OIDC access token payload has invalid base64url padding' }
    }

    $json = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload))
    return $json | ConvertFrom-Json
}

function Select-StsXmlValue {
    param(
        [xml]$Xml,
        [string]$Name
    )

    $node = Select-Xml -Xml $Xml -XPath "//*[local-name()='$Name']" | Select-Object -First 1
    if (-not $node) {
        throw "STS response did not contain $Name"
    }
    return $node.Node.InnerText
}

function Invoke-AwsCli {
    param(
        [string[]]$Arguments,
        [hashtable]$Credentials,
        [bool]$AllowFailure = $false
    )

    $environment = @{
        AWS_ACCESS_KEY_ID = $Credentials.AccessKeyId
        AWS_SECRET_ACCESS_KEY = $Credentials.SecretAccessKey
        AWS_SESSION_TOKEN = $Credentials.SessionToken
        AWS_DEFAULT_REGION = 'us-east-1'
        AWS_EC2_METADATA_DISABLED = 'true'
    }
    $volume = "${workDir}:/work"
    return Invoke-ContainerCapture -Image $awsCliImage -Arguments $Arguments -Environment $environment -Volumes @($volume) -AllowFailure $AllowFailure
}

function Remove-SmokeStack {
    if ($KeepRunning) {
        Write-Host "Keeping MinIO OIDC smoke stack running: docker compose --project-name $ProjectName --file $composeFile ps"
        return
    }

    & docker compose --project-name $ProjectName --file $composeFile down --volumes --remove-orphans *> $null
}

$smokeStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

try {
    if (-not (Test-Path -LiteralPath $composeFile)) {
        throw "Compose fixture not found: $composeFile"
    }

    New-SmokeDirectory $workDir
    [System.IO.File]::WriteAllText((Join-Path $workDir "upload.txt"), $smokeText, [System.Text.Encoding]::UTF8)

    Write-Host "MinIO OIDC storage smoke: authentik + MinIO STS"

    Invoke-SmokeStep `
        "docker compose up (MinIO OIDC storage fixture)" `
        "minio oidc compose up" `
        { Invoke-Compose @("up", "--detach") }

    $null = Wait-SmokeEndpoint `
        -Description "authentik health" `
        -CurlArguments @("-fsS", "http://authentik-server:9000/-/health/live/")

    Invoke-SmokeStep `
        "authentik blueprint apply (MinIO OIDC storage fixture)" `
        "authentik minio oidc blueprint apply" `
        { Invoke-ComposeQuiet @(
            "exec", "-T", "authentik-worker", "ak", "apply_blueprint", "-v", "0",
            "/blueprints/system/providers-oauth2.yaml",
            "/blueprints/default/flow-default-provider-authorization-implicit-consent.yaml",
            "/blueprints/default/flow-default-provider-invalidation.yaml",
            "/blueprints/custom/minio-oidc-smoke.yaml"
        ) }

    $discoveryUrl = "http://authentik-server:9000/application/o/minio-smoke/.well-known/openid-configuration"
    $discoveryJson = Wait-SmokeEndpoint `
        -Description "authentik OIDC discovery and blueprint import" `
        -CurlArguments @("-fsS", $discoveryUrl)
    $discovery = $discoveryJson | ConvertFrom-Json
    if (-not $discovery.token_endpoint) {
        throw 'authentik discovery did not expose token_endpoint'
    }

    $null = Wait-SmokeEndpoint `
        -Description "MinIO readiness" `
        -CurlArguments @("-fsS", "http://minio:9000/minio/health/ready")

    Write-Host "authentik discovery: ready"
    Write-Host "MinIO health: ready"

    $userResponse = Invoke-CurlCapture -Arguments @(
        "-fsS",
        "--header", "Authorization: Bearer $authentikBootstrapToken",
        "http://authentik-server:9000/api/v3/core/users/?search=codegeist-smoke"
    )
    $userResult = $userResponse.Output | ConvertFrom-Json
    $userId = $userResult.results[0].pk
    if (-not $userId) {
        throw 'authentik blueprint did not create the codegeist-smoke user'
    }

    $null = Invoke-CurlCapture -Arguments @(
        "-fsS",
        "--request", "POST",
        "--header", "Authorization: Bearer $authentikBootstrapToken",
        "--header", "Content-Type: application/json",
        "--data", "{`"identifier`":`"$authentikAppPasswordIdentifier`",`"intent`":`"app_password`",`"user`":$userId,`"description`":`"Codegeist MinIO OIDC smoke app password`",`"expiring`":false}",
        "http://authentik-server:9000/api/v3/core/tokens/"
    )
    $null = Invoke-CurlCapture -Arguments @(
        "-fsS",
        "--request", "POST",
        "--header", "Authorization: Bearer $authentikBootstrapToken",
        "--header", "Content-Type: application/json",
        "--data", "{`"key`":`"$authentikAppPassword`"}",
        "http://authentik-server:9000/api/v3/core/tokens/$authentikAppPasswordIdentifier/set_key/"
    )
    Write-Host "authentik fixture app password: set"

    $tokenResponse = Invoke-CurlCapture -Arguments @(
        "-fsS",
        "--request", "POST",
        "--url", $discovery.token_endpoint,
        "--data-urlencode", "grant_type=client_credentials",
        "--data-urlencode", "client_id=minio-smoke-client",
        "--data-urlencode", "username=codegeist-smoke",
        "--data-urlencode", "password=$authentikAppPassword",
        "--data-urlencode", "scope=openid profile email minio-policies"
    )
    $tokenJson = $tokenResponse.Output | ConvertFrom-Json
    if (-not $tokenJson.access_token) {
        throw 'authentik token response did not contain access_token'
    }

    $payload = ConvertFrom-JwtPayload $tokenJson.access_token
    if ($payload.'minio-policies' -ne 'codegeist-artifacts-smoke') {
        throw 'OIDC access token did not include the expected minio-policies claim'
    }
    Write-Host "authentik token: contains minio-policies claim"

    $stsResponse = Wait-SmokeEndpoint `
        -Description "MinIO STS temporary credentials" `
        -CurlArguments @(
            "-fsS",
            "--request", "POST",
            "--url", "http://minio:9000/",
            "--data-urlencode", "Action=AssumeRoleWithWebIdentity",
            "--data-urlencode", "Version=2011-06-15",
            "--data-urlencode", "DurationSeconds=900",
            "--data-urlencode", "WebIdentityToken=$($tokenJson.access_token)"
        )

    [xml]$stsXml = $stsResponse
    $credentials = @{
        AccessKeyId = Select-StsXmlValue -Xml $stsXml -Name 'AccessKeyId'
        SecretAccessKey = Select-StsXmlValue -Xml $stsXml -Name 'SecretAccessKey'
        SessionToken = Select-StsXmlValue -Xml $stsXml -Name 'SessionToken'
        Expiration = Select-StsXmlValue -Xml $stsXml -Name 'Expiration'
    }
    Write-Host "MinIO STS: temporary credentials issued"

    $s3EndpointArgs = @("--endpoint-url", "http://minio:9000", "s3")
    $null = Invoke-AwsCli -Credentials $credentials -Arguments ($s3EndpointArgs + @(
        "cp", "/work/upload.txt", "s3://$bucket/$allowedKey"
    ))
    Write-Host "S3 upload allowed prefix: passed"

    $null = Invoke-AwsCli -Credentials $credentials -Arguments ($s3EndpointArgs + @(
        "cp", "s3://$bucket/$allowedKey", "/work/download.txt"
    ))
    $downloaded = [System.IO.File]::ReadAllText((Join-Path $workDir "download.txt"), [System.Text.Encoding]::UTF8)
    if ($downloaded -ne $smokeText) {
        throw 'Downloaded S3 object content did not match uploaded content'
    }
    Write-Host "S3 download allowed prefix: passed"

    $deniedResult = Invoke-AwsCli -Credentials $credentials -AllowFailure $true -Arguments ($s3EndpointArgs + @(
        "cp", "/work/upload.txt", "s3://$bucket/$deniedKey"
    ))
    if ($deniedResult.ExitCode -eq 0) {
        throw 'Forbidden write to another account prefix unexpectedly succeeded'
    }
    Write-Host "S3 upload forbidden prefix: denied as expected"

    $null = Invoke-AwsCli -Credentials $credentials -AllowFailure $true -Arguments ($s3EndpointArgs + @(
        "rm", "s3://$bucket/$allowedKey"
    ))

    Write-MinioOidcStatus 'passed' 'none'
    Write-Host "MinIO OIDC storage smoke status: passed"
    Write-SmokeDuration "minio oidc storage smoke total" $smokeStopwatch
}
catch {
    $reason = $_.Exception.Message
    Write-MinioOidcStatus 'failed' $reason
    Write-Host "MinIO OIDC storage smoke status: failed"
    Write-Host "Reason: $reason"
    exit 1
}
finally {
    Remove-SmokeStack
}

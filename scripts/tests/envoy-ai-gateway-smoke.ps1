# envoy-ai-gateway-smoke.ps1 - local Envoy AI Gateway to Ollama smoke.
#
# Why this exists:
# - Verifies the T008_05 local AI gateway posture without remote provider calls or
#   fake OpenAI-compatible servers.
# - Starts the existing Codegeist Ollama container through app/codegeist/cli's
#   `ollama-start` task, connects it to the disposable Compose network as `ollama`,
#   bootstraps the authentik OIDC apps, starts oauth2-proxy in front of Envoy AI
#   Gateway through compose.yml plus compose.ai.yml, and sends one authenticated
#   OpenAI-compatible chat completion request through Envoy to Ollama.
# - With -StartAllServices and -StartOnly, also starts Open WebUI for manual browser
#   testing: the browser signs into Open WebUI through authentik, then Open WebUI
#   calls Envoy AI Gateway as its internal OpenAI-compatible provider.
# - Uses an additional Compose file so the optional AI gateway stays separate from
#   the base authentik/MinIO fixture.
#
# Inputs:
# - Docker, Docker Compose v2, PowerShell, and Taskfile's `task` command.
# - The local Ollama model is the fixed Codegeist test model `llama3.2:1b`.
# - Use -KeepRunning to leave the Compose stack and Ollama network attachment up.
# - Use -StartOnly with -KeepRunning for an interactive local environment without
#   sending the smoke request.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - app/codegeist/server/Taskfile.yml
# - scripts/tests/fixtures/minio-oidc-storage/compose.yml
# - scripts/tests/fixtures/minio-oidc-storage/compose.ai.yml

[CmdletBinding()]
param(
    [string]$FixtureDir = "",

    [string]$ProjectName = "",

    [switch]$KeepRunning,

    [switch]$StartOnly,

    [switch]$StartAllServices,

    [int]$TimeoutSeconds = 300,

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
    $ProjectName = "codegeist-ai-gateway-$PID"
}

$composeFile = Join-Path $FixtureDir "compose.yml"
$composeAiFile = Join-Path $FixtureDir "compose.ai.yml"
$composeNetwork = "${ProjectName}_default"
$repoRoot = Resolve-SmokePath (Join-Path $PSScriptRoot ".." "..")
$cliDir = Join-Path $repoRoot "app/codegeist/cli"
$curlImage = "curlimages/curl:8.11.1"
$ollamaContainerName = if ($env:OLLAMA_CONTAINER_NAME) { $env:OLLAMA_CONTAINER_NAME } else { "codegeist-ollama" }
$ollamaNetworkAlias = "ollama"
$model = "llama3.2:1b"
$authentikBootstrapToken = "codegeist-smoke-bootstrap-token"
$authentikAppPasswordIdentifier = "codegeist-smoke-app-password"
$authentikAppPassword = "codegeist-smoke-app-password"
$envoyClientId = "envoy-ai-gateway-smoke-client"
$envoyIssuerUrl = "http://authentik-server:9000/"
$envoyDiscoveryUrl = "http://authentik-server:9000/application/o/envoy-ai-gateway-smoke/.well-known/openid-configuration"
$openWebUiPublicUrl = if ($env:CODEGEIST_OPEN_WEBUI_URL) { $env:CODEGEIST_OPEN_WEBUI_URL } else { "http://172.30.198.40:8080" }
$script:ollamaConnectedByScript = $false

function Write-EnvoyAiGatewayStatus {
    param(
        [string]$Status,
        [string]$Reason
    )

    Write-SmokeStatusFile $StatusFile ([ordered]@{
        status = $Status
        platform = 'envoy-ai-gateway'
        transport = 'docker-compose-envoy-ollama'
        reason = $Reason
    })
}

function Invoke-Compose {
    param([string[]]$Arguments)

    & docker compose --project-name $ProjectName --file $composeFile --file $composeAiFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Invoke-ComposeQuiet {
    param([string[]]$Arguments)

    $output = & docker compose --project-name $ProjectName --file $composeFile --file $composeAiFile @Arguments 2>&1
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
        [bool]$AllowFailure = $false
    )

    $dockerArgs = @("run", "--rm", "--network", $composeNetwork)
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

function Apply-AuthentikBlueprints {
    Invoke-SmokeStep `
        "authentik blueprint apply (Envoy AI Gateway fixture)" `
        "authentik envoy ai gateway blueprint apply" `
        { Invoke-ComposeQuiet @(
            "exec", "-T", "authentik-worker", "ak", "apply_blueprint", "-v", "0",
            "/blueprints/system/providers-oauth2.yaml",
            "/blueprints/default/flow-default-provider-authorization-implicit-consent.yaml",
            "/blueprints/default/flow-default-provider-invalidation.yaml",
            "/blueprints/custom/minio-oidc-smoke.yaml",
            "/blueprints/custom/envoy-ai-gateway-smoke.yaml",
            "/blueprints/custom/open-webui-smoke.yaml"
        ) }
}

function Set-SmokeAppPassword {
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

    $null = Invoke-CurlCapture -AllowFailure $true -Arguments @(
        "-fsS",
        "--request", "POST",
        "--header", "Authorization: Bearer $authentikBootstrapToken",
        "--header", "Content-Type: application/json",
        "--data", "{`"identifier`":`"$authentikAppPasswordIdentifier`",`"intent`":`"app_password`",`"user`":$userId,`"description`":`"Codegeist Envoy AI Gateway smoke app password`",`"expiring`":false}",
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
}

function Get-EnvoyAccessToken {
    param([object]$Discovery)

    $tokenResponse = Invoke-CurlCapture -Arguments @(
        "-fsS",
        "--request", "POST",
        "--url", $Discovery.token_endpoint,
        "--data-urlencode", "grant_type=client_credentials",
        "--data-urlencode", "client_id=$envoyClientId",
        "--data-urlencode", "username=codegeist-smoke",
        "--data-urlencode", "password=$authentikAppPassword",
        "--data-urlencode", "scope=openid profile email"
    )
    $tokenJson = $tokenResponse.Output | ConvertFrom-Json
    if (-not $tokenJson.access_token) {
        throw 'authentik token response did not contain access_token'
    }

    $payload = ConvertFrom-JwtPayload $tokenJson.access_token
    if ($payload.iss.TrimEnd('/') -ne $envoyIssuerUrl.TrimEnd('/')) {
        throw "OIDC access token issuer was '$($payload.iss)', expected '$($envoyIssuerUrl.TrimEnd('/'))'"
    }
    $audiences = @($payload.aud)
    if ($audiences -notcontains $envoyClientId) {
        throw "OIDC access token audience did not include $envoyClientId"
    }

    Write-Host "authentik token: valid issuer and audience"
    return $tokenJson.access_token
}

function Get-DockerNetworkContainerEntry {
    param(
        [string]$NetworkName,
        [string]$ContainerName
    )

    $json = & docker network inspect $NetworkName --format '{{json .Containers}}' 2>&1
    if ($LASTEXITCODE -ne 0) {
        $text = ($json | Out-String).Trim()
        throw "docker network inspect $NetworkName failed: $text"
    }

    $containers = ($json | Out-String).Trim() | ConvertFrom-Json
    if (-not $containers) {
        return $null
    }

    foreach ($property in $containers.PSObject.Properties) {
        if ($property.Value.Name -eq $ContainerName) {
            return $property.Value
        }
    }

    return $null
}

function Connect-OllamaToComposeNetwork {
    $null = & docker container inspect $ollamaContainerName 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Ollama container was not found after ollama-start: $ollamaContainerName"
    }

    $entry = Get-DockerNetworkContainerEntry -NetworkName $composeNetwork -ContainerName $ollamaContainerName
    if ($entry) {
        $aliasesProperty = $entry.PSObject.Properties['Aliases']
        $aliases = if ($aliasesProperty) { @($aliasesProperty.Value) } else { @() }
        if ($aliases -contains $ollamaNetworkAlias) {
            Write-Host "Ollama network alias: already connected as $ollamaNetworkAlias"
            return
        }

        & docker network disconnect $composeNetwork $ollamaContainerName *> $null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to refresh Ollama attachment on network $composeNetwork"
        }
    }

    & docker network connect --alias $ollamaNetworkAlias $composeNetwork $ollamaContainerName
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to connect $ollamaContainerName to network $composeNetwork"
    }
    $script:ollamaConnectedByScript = $true
    Write-Host "Ollama network alias: $ollamaNetworkAlias"
}

function Disconnect-OllamaFromComposeNetwork {
    if (-not $script:ollamaConnectedByScript) {
        return
    }

    & docker network disconnect $composeNetwork $ollamaContainerName *> $null
}

function Start-Ollama {
    Invoke-SmokeStep `
        "task ollama-start" `
        "ollama start" `
        {
            Invoke-SmokeWithEnvironment @{ OLLAMA_ENTER = 'false' } {
                & task --dir $cliDir ollama-start
                if ($LASTEXITCODE -ne 0) {
                    throw "task ollama-start failed with exit code $LASTEXITCODE"
                }
            }
        }
}

function Remove-SmokeStack {
    if ($KeepRunning) {
        Write-Host "Keeping Envoy AI Gateway smoke stack running: docker compose --project-name $ProjectName --file $composeFile --file $composeAiFile ps"
        if ($StartAllServices) {
            Write-Host "Open WebUI URL: $openWebUiPublicUrl"
            Write-Host "authentik smoke login: codegeist-smoke / codegeist-smoke-password"
        }
        return
    }

    Disconnect-OllamaFromComposeNetwork
    & docker compose --project-name $ProjectName --file $composeFile --file $composeAiFile down --volumes --remove-orphans *> $null
}

$smokeStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

try {
    foreach ($requiredFile in @($composeFile, $composeAiFile)) {
        if (-not (Test-Path -LiteralPath $requiredFile)) {
            throw "Compose fixture not found: $requiredFile"
        }
    }

    Write-Host "Envoy AI Gateway smoke: Envoy AI Gateway + local Ollama"

    Invoke-SmokeStep `
        "docker compose up (authentik fixture)" `
        "authentik compose up" `
        { Invoke-Compose @("up", "--detach", "authentik-server", "authentik-worker") }

    $null = Wait-SmokeEndpoint `
        -Description "authentik health" `
        -CurlArguments @("-fsS", "http://authentik-server:9000/-/health/live/")
    Write-Host "authentik health: ready"

    Apply-AuthentikBlueprints

    $discoveryJson = Wait-SmokeEndpoint `
        -Description "authentik Envoy AI Gateway OIDC discovery and blueprint import" `
        -CurlArguments @("-fsS", $envoyDiscoveryUrl)
    $discovery = $discoveryJson | ConvertFrom-Json
    if (-not $discovery.token_endpoint) {
        throw 'authentik Envoy AI Gateway discovery did not expose token_endpoint'
    }
    Write-Host "authentik Envoy AI Gateway discovery: ready"

    Start-Ollama

    Connect-OllamaToComposeNetwork

    $upArguments = if ($StartAllServices) {
        @("up", "--detach")
    } else {
        @("up", "--detach", "envoy-ai-gateway", "envoy-ai-auth-proxy")
    }

    Invoke-SmokeStep `
        "docker compose up (Envoy AI Gateway auth fixture)" `
        "envoy ai gateway auth compose up" `
        { Invoke-Compose $upArguments }

    $null = Wait-SmokeEndpoint `
        -Description "Envoy AI Gateway health" `
        -CurlArguments @("-fsS", "http://envoy-ai-gateway:1064/health")
    Write-Host "Envoy AI Gateway health: ready"

    $null = Wait-SmokeEndpoint `
        -Description "Envoy AI Gateway auth proxy health" `
        -CurlArguments @("-fsS", "http://envoy-ai-auth-proxy:4180/ping")
    Write-Host "Envoy AI Gateway auth proxy health: ready"

    if ($StartAllServices) {
        $null = Wait-SmokeEndpoint `
            -Description "Open WebUI health" `
            -CurlArguments @("-fsS", "http://open-webui:8080/health")
        Write-Host "Open WebUI health: ready"
        Write-Host "Open WebUI URL: $openWebUiPublicUrl"
        Write-Host "authentik smoke login: codegeist-smoke / codegeist-smoke-password"
    }

    if ($StartOnly) {
        Write-EnvoyAiGatewayStatus 'passed' 'started-only'
        Write-Host "Envoy AI Gateway smoke status: started"
        Write-SmokeDuration "envoy ai gateway smoke total" $smokeStopwatch
        return
    }

    $requestJson = ConvertTo-Json -Compress ([ordered]@{
        model = $model
        messages = @(
            [ordered]@{
                role = 'user'
                content = 'Answer with exactly one short sentence: Codegeist Envoy Ollama smoke passed.'
            }
        )
        stream = $false
        temperature = 0
    })

    Set-SmokeAppPassword
    $accessToken = Get-EnvoyAccessToken -Discovery $discovery

    $unauthenticatedResult = Invoke-CurlCapture -AllowFailure $true -Arguments @(
        "-sS",
        "-o", "/dev/null",
        "-w", "%{http_code}",
        "--request", "POST",
        "--url", "http://envoy-ai-auth-proxy:4180/v1/chat/completions",
        "--header", "Content-Type: application/json",
        "--data", $requestJson
    )
    if ($unauthenticatedResult.Output -eq '200') {
        throw 'Unauthenticated Envoy AI Gateway request unexpectedly succeeded'
    }
    Write-Host "Envoy AI Gateway unauthenticated request: rejected with HTTP $($unauthenticatedResult.Output)"

    $chatResponse = Invoke-SmokeStep `
        "curl authenticated Envoy AI Gateway chat completions" `
        "envoy ai gateway authenticated ollama chat" `
        {
            Wait-SmokeEndpoint `
                -Description "authenticated Envoy AI Gateway Ollama chat completion" `
                -CurlArguments @(
                    "-fsS",
                    "--max-time", "180",
                    "--request", "POST",
                    "--url", "http://envoy-ai-auth-proxy:4180/v1/chat/completions",
                    "--header", "Authorization: Bearer $accessToken",
                    "--header", "Content-Type: application/json",
                    "--header", "x-codegeist-user-id: codegeist-smoke-user",
                    "--header", "x-codegeist-account-id: codegeist-smoke-account",
                    "--header", "x-codegeist-request-id: envoy-ollama-smoke",
                    "--data", $requestJson
                )
        }

    $chatJson = $chatResponse | ConvertFrom-Json
    $content = $chatJson.choices[0].message.content
    if (-not $content) {
        throw 'Envoy AI Gateway chat response did not contain assistant content'
    }
    Write-Host "Envoy AI Gateway chat completion: passed"

    Write-EnvoyAiGatewayStatus 'passed' 'none'
    Write-Host "Envoy AI Gateway smoke status: passed"
    Write-SmokeDuration "envoy ai gateway smoke total" $smokeStopwatch
}
catch {
    $reason = $_.Exception.Message
    Write-EnvoyAiGatewayStatus 'failed' $reason
    Write-Host "Envoy AI Gateway smoke status: failed"
    Write-Host "Reason: $reason"
    exit 1
}
finally {
    Remove-SmokeStack
}

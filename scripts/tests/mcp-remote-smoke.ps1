# mcp-remote-smoke.ps1 - Docker-backed remote MCP smoke entrypoint.
#
# Why this exists:
# - Keeps the remote MCP smoke workflow on the same PowerShell helper surface as
#   platform artifact smokes while still using Docker for the local fixture.
# - Proves both the direct streamable_http MCP callback path and an ask/Ollama/MCP
#   path without relying on hosted networks.
#
# Inputs:
# - Docker must be available and able to build/run Linux containers.
# - Maven must be available on the host to package the fixture jar.
# - task ollama-start must be able to start or reuse local Ollama.
# - Optional CODEGEIST_MCP_REMOTE_SMOKE_PORT fixes the host port.
# - Optional CODEGEIST_SMOKE_STATUS_FILE writes a key-value status summary.
#
# Related files:
# - scripts/tests/smoke-common.ps1
# - app/codegeist/cli/Taskfile.yml
# - scripts/tests/fixtures/mcp-remote-server/

[CmdletBinding()]
param(
    [string]$CliDir = "",

    [string]$FixtureDir = "",

    [string]$ImageName = "codegeist-mcp-remote-smoke:local",

    [string]$ContainerName = "",

    [string]$RequestedPort = $env:CODEGEIST_MCP_REMOTE_SMOKE_PORT,

    [string]$StatusFile = $env:CODEGEIST_SMOKE_STATUS_FILE
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

if (-not $CliDir) {
    $CliDir = Join-Path $PSScriptRoot "../../app/codegeist/cli"
}
$CliDir = Resolve-SmokePath $CliDir

if (-not $FixtureDir) {
    $FixtureDir = Join-Path $PSScriptRoot "fixtures/mcp-remote-server"
}
$FixtureDir = Resolve-SmokePath $FixtureDir

if (-not $ContainerName) {
    $ContainerName = "codegeist-mcp-remote-smoke-$PID"
}

function Write-McpStatus {
    param(
        [string]$Status,
        [string]$Reason
    )

    Write-SmokeStatusFile $StatusFile ([ordered]@{
        status = $Status
        platform = 'mcp-remote'
        transport = 'docker-streamable-http'
        reason = $Reason
    })
}

function Remove-McpContainer {
    $inspectOutput = & docker container inspect $ContainerName 2>$null
    if ($LASTEXITCODE -eq 0 -and $inspectOutput) {
        & docker rm -f $ContainerName *> $null
    }
}

$smokeStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

try {
    if (-not (Test-Path -LiteralPath $CliDir)) {
        throw "CLI module directory not found: $CliDir"
    }
    if (-not (Test-Path -LiteralPath $FixtureDir)) {
        throw "MCP fixture directory not found: $FixtureDir"
    }

    Write-Host "MCP remote smoke: streamable_http"

    Invoke-SmokeStep `
        "mvn --batch-mode --no-transfer-progress -DskipTests package (remote MCP fixture)" `
        "mcp remote fixture package" `
        { & mvn --batch-mode --no-transfer-progress -DskipTests -f (Join-Path $FixtureDir "pom.xml") package }

    Invoke-SmokeStep `
        "docker build $ImageName" `
        "mcp remote docker build" `
        { & docker build -t $ImageName $FixtureDir }

    $publish = if ($RequestedPort) { "127.0.0.1:${RequestedPort}:3000" } else { "127.0.0.1::3000" }
    Invoke-SmokeStep `
        "docker run $ImageName" `
        "mcp remote container start" `
        { & docker run --detach --name $ContainerName --publish $publish $ImageName *> $null }

    $portMapping = (& docker port $ContainerName "3000/tcp").Trim()
    $hostPort = ($portMapping -split ':')[-1]
    if (-not $hostPort) {
        throw 'Could not determine remote MCP fixture host port'
    }

    $ready = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (Test-SmokeTcpPort "127.0.0.1" ([int]$hostPort)) {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 1
    }

    if (-not $ready) {
        & docker logs --tail 80 $ContainerName 2>$null | Write-Error
        throw "Remote MCP fixture did not open port $hostPort"
    }

    $remoteSmokeUrl = "http://127.0.0.1:$hostPort"
    Invoke-SmokeStep `
        "mvn --batch-mode --no-transfer-progress -Dtest=CodegeistMcpRemoteSmokeIT test" `
        "mcp remote streamable_http test" `
        {
            & mvn --batch-mode --no-transfer-progress `
                -Dtest=CodegeistMcpRemoteSmokeIT `
                "-Dcodegeist.mcp.remote-smoke.url=$remoteSmokeUrl" `
                -f (Join-Path $CliDir "pom.xml") test
        }

    Invoke-SmokeStep `
        "OLLAMA_ENTER=false task ollama-start" `
        "mcp remote ollama start" `
        {
            Push-Location -LiteralPath $CliDir
            try {
                Invoke-SmokeWithEnvironment @{ OLLAMA_ENTER = 'false' } { & task ollama-start }
            }
            finally {
                Pop-Location
            }
        }

    Invoke-SmokeStep `
        "CODEGEIST_TEST_PROVIDER_CATEGORY=local mvn --batch-mode --no-transfer-progress -Dtest=AskCommandsMcpRemoteSmokeIT test" `
        "mcp remote ask ollama test" `
        {
            Invoke-SmokeWithEnvironment @{ CODEGEIST_TEST_PROVIDER_CATEGORY = 'local' } {
                & mvn --batch-mode --no-transfer-progress `
                    -Dtest=AskCommandsMcpRemoteSmokeIT `
                    "-Dcodegeist.mcp.remote-smoke.url=$remoteSmokeUrl" `
                    -f (Join-Path $CliDir "pom.xml") test
            }
        }

    Write-McpStatus 'passed' 'none'
    Write-Host "MCP remote smoke status: passed"
    Write-SmokeDuration "mcp remote smoke total" $smokeStopwatch
}
catch {
    $reason = $_.Exception.Message
    Write-McpStatus 'failed' $reason
    Write-Host "MCP remote smoke status: failed"
    Write-Host "Reason: $reason"
    exit 1
}
finally {
    Remove-McpContainer
}

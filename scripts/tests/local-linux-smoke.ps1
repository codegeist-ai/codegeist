# local-linux-smoke.ps1 - local Linux build gates and native smoke entrypoint.
#
# Why this exists:
# - Keeps the Linux smoke workflow on the same PowerShell codebase used by the
#   native artifact harness and Windows-side smoke checks.
# - Runs JVM tests and jar packaging as local build gates, then delegates native
#   package verification to scripts/tests/native-smoke.ps1 when native-image is
#   available or required.
#
# Inputs:
# - Run from anywhere inside the repository checkout.
# - Optional CODEGEIST_SMOKE_STATUS_FILE writes a key-value status summary.
# - Optional CODEGEIST_SMOKE_REQUIRE_NATIVE=1 turns missing native-image into a
#   failure instead of a skipped native subcheck.
#
# Related files:
# - scripts/tests/smoke-common.ps1
# - scripts/tests/native-smoke.ps1
# - app/codegeist/cli/Taskfile.yml

[CmdletBinding()]
param(
    [string]$CliDir = "",

    [string]$SmokeRoot = "",

    [string]$ReleaseVersion = "",

    [string]$StatusFile = $env:CODEGEIST_SMOKE_STATUS_FILE,

    [switch]$RequireNative
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

if (-not $CliDir) {
    $CliDir = Join-Path $PSScriptRoot "../../app/codegeist/cli"
}
$CliDir = Resolve-SmokePath $CliDir

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $CliDir "target/smoke-test"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot

$requireNative = $RequireNative.IsPresent -or (Test-SmokeEnvFlag "CODEGEIST_SMOKE_REQUIRE_NATIVE")
$mavenRevisionArgs = @()
if ($ReleaseVersion) {
    $mavenRevisionArgs += "-Drevision=$ReleaseVersion"
}

function Write-LocalLinuxStatus {
    param(
        [string]$Status,
        [string]$JarStatus,
        [string]$NativeStatus,
        [string]$NativeReason
    )

    Write-SmokeStatusFile $StatusFile ([ordered]@{
        status = $Status
        platform = 'linux'
        jar_status = $JarStatus
        native_status = $NativeStatus
        native_reason = $NativeReason
    })
}

$platformStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$nativeStatus = 'skipped'
$nativeReason = 'native-image is not available on PATH'

try {
    if (-not (Test-Path -LiteralPath $CliDir)) {
        throw "CLI module directory not found: $CliDir"
    }

    Push-Location -LiteralPath $CliDir
    try {
        Write-Host "Platform: linux"
        Write-Host "Artifact: build"

        $mavenTestCommand = (@("mvn", "--batch-mode", "--no-transfer-progress") + $mavenRevisionArgs + @("test")) -join " "
        Invoke-SmokeStep `
            $mavenTestCommand `
            "linux maven tests" `
            { & mvn --batch-mode --no-transfer-progress @mavenRevisionArgs test }

        $mavenPackageCommand = (@("mvn", "--batch-mode", "--no-transfer-progress") + $mavenRevisionArgs + @("-DskipTests", "clean", "package")) -join " "
        Invoke-SmokeStep `
            $mavenPackageCommand `
            "linux jar package" `
            { & mvn --batch-mode --no-transfer-progress @mavenRevisionArgs -DskipTests clean package }

        Remove-Item -Recurse -Force -LiteralPath $SmokeRoot -ErrorAction SilentlyContinue
        New-SmokeDirectory $SmokeRoot

        if (Get-Command native-image -ErrorAction SilentlyContinue) {
            Write-Host "Artifact: native"
            Write-Host "Command: pwsh scripts/tests/native-smoke.ps1 -BuildNative"
            & (Join-Path $PSScriptRoot "native-smoke.ps1") `
                -CliDir $CliDir `
                -SmokeRoot $SmokeRoot `
                -ReleaseVersion $ReleaseVersion `
                -BuildNative
            $nativeStatus = 'passed'
            $nativeReason = 'none'
        }
        elseif ($requireNative) {
            throw 'native-image is required but not available on PATH'
        }
        else {
            Write-Host "Native status: skipped"
            Write-Host "Reason: $nativeReason"
        }
    }
    finally {
        Pop-Location
    }

    Write-LocalLinuxStatus 'passed' 'skipped' $nativeStatus $nativeReason
    Write-Host "Platform smoke status: passed"
    Write-Host "Platform: linux"
    Write-Host "Jar status: skipped"
    Write-Host "Native status: $nativeStatus"
    Write-Host "Native reason: $nativeReason"
    Write-SmokeDuration "linux platform smoke total" $platformStopwatch
}
catch {
    $reason = $_.Exception.Message
    Write-LocalLinuxStatus 'failed' 'failed' 'failed' $reason
    Write-Host "Platform smoke status: failed"
    Write-Host "Platform: linux"
    Write-Host "Reason: $reason"
    exit 1
}

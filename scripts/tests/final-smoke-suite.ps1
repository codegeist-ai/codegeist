# final-smoke-suite.ps1 - local final smoke suite for Linux and Windows.
#
# Why this exists:
# - Runs platform smoke entrypoints through one PowerShell orchestration layer so
#   Linux and Windows status handling, skip policy, and final reporting stay shared.
# - Keeps QEMU VM lifecycle in scripts/tests/qemu-windows-vm.sh, while the smoke
#   entrypoints it calls use the shared PowerShell codebase.
#
# Inputs:
# - Optional AllowSkips allows missing platform prerequisites to report skipped.
# - Windows VM inputs are still consumed by scripts/tests/qemu-windows-vm.sh.
#
# Related files:
# - scripts/tests/smoke-common.ps1
# - scripts/tests/local-linux-smoke.ps1
# - scripts/tests/qemu-windows-vm.sh
# - app/codegeist/cli/Taskfile.yml

[CmdletBinding()]
param(
    [switch]$AllowSkips,

    [string]$SuiteDir = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

$repoRoot = Resolve-SmokePath (Join-Path $PSScriptRoot "../..")
if (-not $SuiteDir) {
    $SuiteDir = Join-Path $repoRoot "app/codegeist/cli/target/smoke-test/final-smoke-suite"
}
$SuiteDir = Resolve-SmokePath $SuiteDir

function Invoke-PlatformSmoke {
    param(
        [string]$Name,
        [scriptblock]$Command
    )

    $statusFile = Join-Path $SuiteDir "$Name.status"
    New-SmokeDirectory $SuiteDir
    Write-Host "Suite platform: $Name"

    $exitCode = 0
    $script:LastPlatformSmokePassed = $false
    try {
        Invoke-SmokeWithEnvironment @{ CODEGEIST_SMOKE_STATUS_FILE = $statusFile } $Command
        if ($LASTEXITCODE -ne 0) {
            $exitCode = $LASTEXITCODE
        }
    }
    catch {
        $exitCode = 1
        Write-Host "Suite platform error: $Name $($_.Exception.Message)"
    }

    New-SmokeDirectory $SuiteDir
    $status = Read-SmokeStatusFile $statusFile
    Write-Host "Suite platform status: $Name $status"

    if ($exitCode -ne 0) {
        return
    }
    if (-not $AllowSkips -and $status -eq 'skipped') {
        Write-Error "Final suite requires $Name smoke to pass."
        return
    }
    if ($status -eq 'failed') {
        return
    }

    $script:LastPlatformSmokePassed = $true
}

if (-not $AllowSkips) {
    if (-not $env:CODEGEIST_SMOKE_REQUIRE_NATIVE) {
        $env:CODEGEIST_SMOKE_REQUIRE_NATIVE = '1'
    }
    if (-not $env:CODEGEIST_WINDOWS_NATIVE_MODE) {
        $env:CODEGEIST_WINDOWS_NATIVE_MODE = 'required'
    }
    $env:CODEGEIST_WINDOWS_ALLOW_SKIP = '0'
}
else {
    $env:CODEGEIST_WINDOWS_ALLOW_SKIP = '1'
}

Remove-Item -Recurse -Force -LiteralPath $SuiteDir -ErrorAction SilentlyContinue
New-SmokeDirectory $SuiteDir

$failures = 0

Invoke-PlatformSmoke "linux" { & pwsh -NoProfile -File (Join-Path $PSScriptRoot "local-linux-smoke.ps1") }
if (-not $script:LastPlatformSmokePassed) {
    $failures++
}

Invoke-PlatformSmoke "windows-x64" { & bash (Join-Path $PSScriptRoot "qemu-windows-vm.sh") smoke }
if (-not $script:LastPlatformSmokePassed) {
    $failures++
}

Write-Host -NoNewline "Final smoke suite status: "
if ($failures -eq 0) {
    Write-Host "passed"
    exit 0
}

Write-Host "failed"
Write-Host "Failed platform checks: $failures"
exit 1

# qemu-windows-smoke.ps1 - Windows QEMU VM smoke entrypoint over SSH.
#
# Why this exists:
# - Keeps host-side Windows smoke orchestration on the same PowerShell helper
#   surface as Linux and MCP smokes.
# - Runs scripts/tests/windows-smoke.ps1 inside the Windows VM while reporting the
#   same status-file and duration contract as other platform smoke entrypoints.
#
# Inputs:
# - CODEGEIST_WINDOWS_SSH_TARGET, CODEGEIST_WINDOWS_REPO_DIR, and optional SSH
#   port/key settings identify the already prepared Windows VM.
# - CODEGEIST_WINDOWS_NATIVE_MODE controls native validation: auto, skip, required.
# - CODEGEIST_WINDOWS_ALLOW_SKIP=1 converts missing host prerequisites to skipped.
#
# Related files:
# - scripts/tests/smoke-common.ps1
# - scripts/tests/qemu-windows-vm.sh
# - scripts/tests/windows-smoke.ps1

[CmdletBinding()]
param(
    [string]$StatusFile = $env:CODEGEIST_SMOKE_STATUS_FILE,

    [string]$SshTarget = $env:CODEGEIST_WINDOWS_SSH_TARGET,

    [string]$RepoDir = $env:CODEGEIST_WINDOWS_REPO_DIR,

    [ValidateSet("auto", "skip", "required")]
    [string]$NativeMode = $(if ($env:CODEGEIST_WINDOWS_NATIVE_MODE) { $env:CODEGEIST_WINDOWS_NATIVE_MODE } else { "auto" }),

    [string]$SshPort = $env:CODEGEIST_WINDOWS_SSH_PORT,

    [string]$SshKey = $env:CODEGEIST_WINDOWS_SSH_KEY,

    [string]$SshConnectTimeout = $(if ($env:CODEGEIST_WINDOWS_SSH_CONNECT_TIMEOUT) { $env:CODEGEIST_WINDOWS_SSH_CONNECT_TIMEOUT } else { "10" }),

    [string]$MsvcCommand = $env:CODEGEIST_WINDOWS_MSVC_CMD,

    [string]$NativeTimeoutSeconds = $env:CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS,

    [string]$AskTimeoutSeconds = $env:CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS,

    [string]$FileEditTimeoutSeconds = $env:CODEGEIST_WINDOWS_FILE_EDIT_TIMEOUT_SECONDS,

    [string]$OllamaBaseUrl = $env:CODEGEIST_WINDOWS_OLLAMA_BASE_URL
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

$allowSkip = Test-SmokeEnvFlag "CODEGEIST_WINDOWS_ALLOW_SKIP"

function Write-WindowsHostStatus {
    param(
        [string]$Status,
        [string]$Reason
    )

    Write-SmokeStatusFile $StatusFile ([ordered]@{
        status = $Status
        platform = 'windows-x64'
        transport = 'ssh'
        reason = $Reason
    })
}

function Complete-WindowsSkip {
    param([string]$Reason)

    Write-WindowsHostStatus 'skipped' $Reason
    Write-Host "Platform smoke status: skipped"
    Write-Host "Platform: windows-x64"
    Write-Host "Reason: $Reason"
    exit 0
}

function Fail-WindowsSmoke {
    param([string]$Reason)

    Write-WindowsHostStatus 'failed' $Reason
    Write-Host "Platform smoke status: failed"
    Write-Host "Platform: windows-x64"
    Write-Host "Reason: $Reason"
    exit 1
}

function Assert-WindowsPrerequisite {
    param([string]$Reason)

    if ($allowSkip) {
        Complete-WindowsSkip $Reason
    }

    Fail-WindowsSmoke $Reason
}

function Assert-IntegerText {
    param(
        [string]$Name,
        [string]$Value
    )

    if ($Value -and $Value -notmatch '^\d+$') {
        Fail-WindowsSmoke "$Name must be an integer"
    }
}

Assert-IntegerText 'CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS' $NativeTimeoutSeconds
Assert-IntegerText 'CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS' $AskTimeoutSeconds
Assert-IntegerText 'CODEGEIST_WINDOWS_FILE_EDIT_TIMEOUT_SECONDS' $FileEditTimeoutSeconds

if (-not $SshTarget) {
    Assert-WindowsPrerequisite 'CODEGEIST_WINDOWS_SSH_TARGET is not set'
}
if (-not $RepoDir) {
    Assert-WindowsPrerequisite 'CODEGEIST_WINDOWS_REPO_DIR is not set'
}
if (-not (Get-Command ssh -ErrorAction SilentlyContinue)) {
    Assert-WindowsPrerequisite 'ssh is not available on PATH'
}

$sshArgs = @('-o', 'BatchMode=yes', '-o', "ConnectTimeout=$SshConnectTimeout")
if ($SshPort) {
    $sshArgs += @('-p', $SshPort)
}
if ($SshKey) {
    $sshArgs += @('-i', $SshKey)
}

$repoArg = ConvertTo-SmokePowerShellSingleQuotedString $RepoDir
$nativeArg = ConvertTo-SmokePowerShellSingleQuotedString $NativeMode
$windowsScriptPath = $RepoDir.TrimEnd('\', '/') + '\scripts\tests\windows-smoke.ps1'
$scriptArg = ConvertTo-SmokePowerShellSingleQuotedString $windowsScriptPath
$remoteCommand = "pwsh -NoProfile -ExecutionPolicy Bypass -Command `"& $scriptArg -RepoDir $repoArg -NativeMode $nativeArg"

if ($MsvcCommand) {
    $msvcArg = ConvertTo-SmokePowerShellSingleQuotedString $MsvcCommand
    $remoteCommand += " -MsvcCommand $msvcArg"
}
if ($NativeTimeoutSeconds) {
    $remoteCommand += " -NativeTimeoutSeconds $NativeTimeoutSeconds"
}
if ($AskTimeoutSeconds) {
    $remoteCommand += " -AskTimeoutSeconds $AskTimeoutSeconds"
}
if ($FileEditTimeoutSeconds) {
    $remoteCommand += " -FileEditTimeoutSeconds $FileEditTimeoutSeconds"
}
if ($OllamaBaseUrl) {
    $ollamaArg = ConvertTo-SmokePowerShellSingleQuotedString $OllamaBaseUrl
    $remoteCommand += " -OllamaBaseUrl $ollamaArg"
}
$remoteCommand += "`""

Write-Host "Platform: windows-x64"
Write-Host "Transport: ssh"
Write-Host "Command: pwsh scripts/tests/windows-smoke.ps1"
$smokeStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

& ssh @sshArgs $SshTarget $remoteCommand
if ($LASTEXITCODE -eq 0) {
    Write-WindowsHostStatus 'passed' 'none'
    Write-Host "Platform smoke status: passed"
    Write-Host "Platform: windows-x64"
    Write-SmokeDuration "windows ssh smoke command" $smokeStopwatch
    exit 0
}

Fail-WindowsSmoke 'Windows VM smoke command failed'

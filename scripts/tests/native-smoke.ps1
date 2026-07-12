# native-smoke.ps1 - Linux native smoke wrapper.
#
# Why this exists:
# - Keeps Taskfile.yml and local Linux smoke commands on PowerShell 7, matching the
#   shared native artifact harness in scripts/tests/artifact-smoke.ps1.
# - Optionally builds the GraalVM native executable before smoking the packaged
#   Linux native archive.
#
# Inputs:
# - Run from anywhere inside the repository checkout.
# - Optional BuildNative compiles app/codegeist/cli/target/codegeist first.
# - Optional timeout parameters override environment defaults.
#
# Side effects:
# - Delegates packaging, unpacking, command checks, file-edit checks, shell-tool
#   checks to scripts/tests/artifact-smoke.ps1.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - scripts/tests/artifact-smoke.ps1
# - scripts/tests/local-linux-smoke.ps1

[CmdletBinding()]
param(
    [string]$CliDir = "",

    [string]$SmokeRoot = "",

    [string]$ReleaseVersion = "",

    [int]$NativeTimeoutSeconds = 0,

    [int]$FileEditTimeoutSeconds = 0,

    [int]$ShellAskTimeoutSeconds = 0,

    [switch]$BuildNative
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-SmokeDuration {
    param(
        [string]$Label,
        [System.Diagnostics.Stopwatch]$Stopwatch
    )

    $Stopwatch.Stop()
    Write-Host ("Duration: {0}: {1:N3}s" -f $Label, $Stopwatch.Elapsed.TotalSeconds)
}

function Get-SmokeSeconds {
    param(
        [int]$ExplicitValue,
        [string]$PrimaryEnvironmentName,
        [string]$LegacyEnvironmentName,
        [int]$DefaultValue
    )

    if ($ExplicitValue -gt 0) {
        return $ExplicitValue
    }

    $primary = [Environment]::GetEnvironmentVariable($PrimaryEnvironmentName)
    if ($primary) {
        return [int]$primary.TrimEnd("s")
    }

    $legacy = [Environment]::GetEnvironmentVariable($LegacyEnvironmentName)
    if ($legacy) {
        return [int]$legacy.TrimEnd("s")
    }

    return $DefaultValue
}

function Invoke-TimedCommand {
    param(
        [string]$CommandLabel,
        [string]$DurationLabel,
        [scriptblock]$Command
    )

    Write-Host "Command: $CommandLabel"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    & $Command
    Write-SmokeDuration $DurationLabel $stopwatch
    if ($LASTEXITCODE -ne 0) {
        throw "$CommandLabel failed with exit code $LASTEXITCODE"
    }
}

if (-not $CliDir) {
    $CliDir = Join-Path $PSScriptRoot "../../app/codegeist/cli"
}
$CliDir = [System.IO.Path]::GetFullPath($CliDir)

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $CliDir "target/smoke-test"
}
$SmokeRoot = [System.IO.Path]::GetFullPath($SmokeRoot)

$nativeTimeout = Get-SmokeSeconds $NativeTimeoutSeconds "CODEGEIST_NATIVE_SMOKE_TIMEOUT_SECONDS" "CODEGEIST_NATIVE_SMOKE_TIMEOUT" 5
$mavenRevisionArgs = @()
if ($ReleaseVersion) {
    $mavenRevisionArgs += "-Drevision=$ReleaseVersion"
}
if ($FileEditTimeoutSeconds -le 0) {
    $fileEditTimeout = [int](${env:CODEGEIST_FILE_EDIT_SMOKE_TIMEOUT_SECONDS} ?? "90")
}
else {
    $fileEditTimeout = $FileEditTimeoutSeconds
}
if ($ShellAskTimeoutSeconds -le 0) {
    $shellAskTimeout = [int](${env:CODEGEIST_SHELL_ASK_SMOKE_TIMEOUT_SECONDS} ?? "90")
}
else {
    $shellAskTimeout = $ShellAskTimeoutSeconds
}

Push-Location -LiteralPath $CliDir
try {
    if ($BuildNative) {
        $nativeCompileCommand = (@("mvn", "--batch-mode", "--no-transfer-progress") + $mavenRevisionArgs + @("-DskipTests", "-Pnative", "clean", "native:compile")) -join " "
        Invoke-TimedCommand `
            $nativeCompileCommand `
            "linux native compile" `
            { & mvn --batch-mode --no-transfer-progress @mavenRevisionArgs -DskipTests -Pnative clean native:compile }
    }

    Write-Host "Command: pwsh scripts/tests/artifact-smoke.ps1 -Platform linux-x64"
    & (Join-Path $PSScriptRoot "artifact-smoke.ps1") `
        -Platform linux-x64 `
        -CliDir $CliDir `
        -ExpectedVersion $ReleaseVersion `
        -SmokeRoot $SmokeRoot `
        -NativeTimeoutSeconds $nativeTimeout `
        -FileEditTimeoutSeconds $fileEditTimeout `
        -ShellAskTimeoutSeconds $shellAskTimeout
}
finally {
    Pop-Location
}

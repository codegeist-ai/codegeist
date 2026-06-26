# windows-smoke.ps1 - Windows-side Codegeist native smoke checks.
#
# Why this exists:
# - Runs inside the pre-provisioned Windows QEMU VM so local Windows native
#   validation uses a real Windows toolchain and operating system.
# - Keeps Windows command details out of the host-side SSH wrapper.
# - Delegates jar and native artifact checks to the shared
#   scripts/tests/artifact-smoke.ps1 harness so Windows, Linux, macOS, and release
#   workflows verify the same artifact contract, then runs the Windows install
#   script smoke against that release-shaped zip inside the VM.
#
# Inputs:
# - RepoDir: absolute path to the repository checkout inside the VM.
# - NativeMode: auto, skip, or required.
# - MsvcCommand: optional command that activates MSVC Build Tools before native
#   Maven compile. If omitted, the script uses the active shell environment when
#   cl.exe is already available, then tries common Visual Studio Build Tools paths.
# - NativeTimeoutSeconds, FileEditTimeoutSeconds, and ShellAskTimeoutSeconds bound
#   native command smoke execution.
#
# Side effects:
# - May rebuild app/codegeist/cli/target/codegeist.exe, write
#   app/codegeist/cli/target/dist/codegeist-windows-x64.zip, and run
#   scripts/install/codegeist-install-windows.ps1 against local release-shaped
#   assets.
# - Writes smoke logs under app/codegeist/cli/target/smoke-test.

param(
    [Parameter(Mandatory = $true)]
    [string]$RepoDir,

    [ValidateSet("auto", "skip", "required")]
    [string]$NativeMode = "auto",

    [string]$MsvcCommand = $env:CODEGEIST_WINDOWS_MSVC_CMD,

    [int]$NativeTimeoutSeconds = 5,

    [int]$FileEditTimeoutSeconds = 15,

    [int]$ShellAskTimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"

$platformStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

function Write-SmokeDuration {
    param(
        [string]$Label,
        [TimeSpan]$Duration
    )

    Write-Host ("Duration: {0}: {1:N3}s" -f $Label, $Duration.TotalSeconds)
}

function Fail-Smoke {
    param([string]$Reason)

    Write-Host "Platform smoke status: failed"
    Write-Host "Platform: windows-x64"
    Write-Host "Reason: $Reason"
    exit 1
}

function Invoke-Step {
    param(
        [string]$Label,
        [string]$DurationLabel,
        [scriptblock]$Command
    )

    Write-Host "Command: $Label"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    & $Command
    $stopwatch.Stop()
    Write-SmokeDuration $DurationLabel $stopwatch.Elapsed
    if ($LASTEXITCODE -ne 0) {
        Fail-Smoke "$Label failed with exit code $LASTEXITCODE"
    }
}

function Get-BuildVersion {
    param([string]$BuildInfoFile)

    $line = Get-Content -LiteralPath $BuildInfoFile |
        Where-Object { $_ -like "build.version=*" } |
        Select-Object -First 1

    if (-not $line) {
        Fail-Smoke "build.version not found in $BuildInfoFile"
    }

    return $line.Substring("build.version=".Length)
}

function Find-MsvcCommand {
    if ($MsvcCommand) {
        return $MsvcCommand
    }

    $candidates = @("C:\BuildTools\Common7\Tools\VsDevCmd.bat")

    if ($env:ProgramFiles) {
        $candidates += Join-Path $env:ProgramFiles "Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat"
        $candidates += Join-Path $env:ProgramFiles "Microsoft Visual Studio\2022\Community\Common7\Tools\VsDevCmd.bat"
    }

    $programFilesX86 = [Environment]::GetEnvironmentVariable("ProgramFiles(x86)")
    if ($programFilesX86) {
        $candidates += Join-Path $programFilesX86 "Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat"
        $candidates += Join-Path $programFilesX86 "Microsoft Visual Studio\2022\Community\Common7\Tools\VsDevCmd.bat"
    }

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return "`"$candidate`" -arch=x64"
        }
    }

    return ""
}

$cliDir = Join-Path $RepoDir "app/codegeist/cli"
$script:artifactSmokeScript = Join-Path $RepoDir "scripts/tests/artifact-smoke.ps1"
$script:installSmokeScript = Join-Path $RepoDir "scripts/tests/install-script-smoke.ps1"
$script:installScriptDir = Join-Path $RepoDir "scripts/install"

if (-not (Test-Path -LiteralPath $cliDir)) {
    Fail-Smoke "CLI module directory not found: $cliDir"
}

Set-Location -LiteralPath $cliDir

Write-Host "Platform: windows-x64"
Write-Host "Artifact: native"

$smokeDir = Join-Path $cliDir "target/smoke-test"
Remove-Item -Recurse -Force -LiteralPath $smokeDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $smokeDir | Out-Null

$nativeStatus = "skipped"
$nativeReason = "NativeMode is skip"
$installStatus = "skipped"
$installReason = "NativeMode is skip"

if ($NativeMode -ne "skip") {
    if (-not (Get-Command native-image -ErrorAction SilentlyContinue)) {
        $nativeReason = "native-image is not available on PATH"
        $installReason = $nativeReason
        if ($NativeMode -eq "required") {
            Fail-Smoke $nativeReason
        }
    }
    else {
        $msvc = Find-MsvcCommand
        $clAvailable = [bool](Get-Command cl.exe -ErrorAction SilentlyContinue)

        if (-not $clAvailable -and -not $msvc) {
            $nativeReason = "MSVC Build Tools environment is not active and no VsDevCmd.bat was found"
            $installReason = $nativeReason
            if ($NativeMode -eq "required") {
                Fail-Smoke $nativeReason
            }
        }
        else {
            Write-Host "Artifact: native"
            if ($clAvailable -and -not $msvc) {
                Invoke-Step "mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile" "windows native compile" {
                    & mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile
                }
            }
            else {
                $nativeCommand = "$msvc && mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile"
                Invoke-Step "MSVC environment plus Maven native compile" "windows native compile" {
                    & cmd /d /s /c $nativeCommand
                }
            }

            $expected = Get-BuildVersion "target/classes/META-INF/build-info.properties"
            New-Item -ItemType Directory -Force -Path $smokeDir | Out-Null

            Write-Host "Command: pwsh scripts/tests/artifact-smoke.ps1 -Platform windows-x64"
            & $script:artifactSmokeScript `
                -Platform windows-x64 `
                -CliDir $cliDir `
                -ExpectedVersion $expected `
                -SmokeRoot $smokeDir `
                -NativeTimeoutSeconds $NativeTimeoutSeconds `
                -FileEditTimeoutSeconds $FileEditTimeoutSeconds `
                -ShellAskTimeoutSeconds $ShellAskTimeoutSeconds

            $nativeStatus = "passed"
            $nativeReason = "none"

            Write-Host "Command: pwsh scripts/tests/install-script-smoke.ps1 -Platform windows-x64"
            try {
                & $script:installSmokeScript `
                    -Platform windows-x64 `
                    -CliDir $cliDir `
                    -ExpectedVersion $expected `
                    -SmokeRoot (Join-Path $smokeDir "install-script/windows-x64") `
                    -InstallScriptDir $script:installScriptDir
            }
            catch {
                Fail-Smoke "Windows install script smoke failed: $($_.Exception.Message)"
            }

            $installStatus = "passed"
            $installReason = "none"
        }
    }
}

Write-Host "Platform smoke status: passed"
Write-Host "Platform: windows-x64"
Write-Host "Jar status: skipped"
Write-Host "Native status: $nativeStatus"
Write-Host "Install status: $installStatus"
Write-Host "Native reason: $nativeReason"
Write-Host "Install reason: $installReason"
$platformStopwatch.Stop()
Write-SmokeDuration "windows platform smoke total" $platformStopwatch.Elapsed

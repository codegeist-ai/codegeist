# windows-smoke.ps1 - Windows-side Codegeist jar and native smoke checks.
#
# Why this exists:
# - Runs inside the pre-provisioned Windows QEMU VM so local Windows validation
#   uses a real Windows toolchain and operating system.
# - Keeps Windows command details out of the host-side SSH wrapper.
# - Packages the Windows native executable with its DLL sidecars, unpacks the zip
#   into a fresh temp directory, and smokes the packaged executable.
#
# Inputs:
# - RepoDir: absolute path to the repository checkout inside the VM.
# - NativeMode: auto, skip, or required.
# - MsvcCommand: optional command that activates MSVC Build Tools before native
#   Maven compile. If omitted, the script uses the active shell environment when
#   cl.exe is already available, then tries common Visual Studio Build Tools paths.
# - JarTimeoutSeconds and NativeTimeoutSeconds bound command smoke execution.
# - AskTimeoutSeconds bounds the real Ollama-backed `ask` smoke.
# - OllamaBaseUrl points to the host Ollama service. With QEMU user networking,
#   the Windows guest reaches the Linux host through `http://10.0.2.2:11434`.
#
# Side effects:
# - Rebuilds app/codegeist/cli/target/codegeist.jar.
# - May rebuild app/codegeist/cli/target/codegeist.exe and write
#   app/codegeist/cli/target/dist/codegeist-windows-x64.zip.
# - Writes smoke logs under app/codegeist/cli/target/smoke-test.

param(
    [Parameter(Mandatory = $true)]
    [string]$RepoDir,

    [ValidateSet("auto", "skip", "required")]
    [string]$NativeMode = "auto",

    [string]$MsvcCommand = $env:CODEGEIST_WINDOWS_MSVC_CMD,

    [int]$JarTimeoutSeconds = 15,

    [int]$NativeTimeoutSeconds = 5,

    [int]$AskTimeoutSeconds = 60,

    [string]$OllamaBaseUrl = $env:CODEGEIST_WINDOWS_OLLAMA_BASE_URL
)

$ErrorActionPreference = "Stop"

if (-not $OllamaBaseUrl) {
    $OllamaBaseUrl = "http://10.0.2.2:11434"
}

$askPrompt = "codegeist"

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

function Join-ProcessArguments {
    param([string[]]$Arguments)

    $escaped = foreach ($argument in $Arguments) {
        if ($argument -notmatch '[\s"]') {
            $argument
        }
        else {
            '"' + $argument.Replace('"', '\"') + '"'
        }
    }

    return ($escaped -join " ")
}

function Invoke-CommandSmoke {
    param(
        [string]$Label,
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$Expected,
        [string]$LogFile,
        [int]$TimeoutSeconds,
        [string]$OutputPrefix,
        [string]$WorkingDirectory = (Get-Location).Path,
        [string]$DurationLabel = $Label
    )

    $stdoutFile = Join-Path $smokeDir "$OutputPrefix.out"
    $stderrFile = Join-Path $smokeDir "$OutputPrefix.err"
    Remove-Item -LiteralPath $stdoutFile, $stderrFile, $LogFile -ErrorAction SilentlyContinue

    $env:LOG_FILE = $LogFile
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $FilePath
    $startInfo.Arguments = Join-ProcessArguments $ArgumentList
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.EnvironmentVariables["LOG_FILE"] = $LogFile

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = [System.Diagnostics.Process]::Start($startInfo)

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        $stopwatch.Stop()
        $process.Kill()
        $process.WaitForExit()
        Fail-Smoke "$Label timed out after $TimeoutSeconds seconds"
    }
    $stopwatch.Stop()

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $exitCode = $process.ExitCode
    Set-Content -LiteralPath $stdoutFile -Value $stdout -NoNewline
    Set-Content -LiteralPath $stderrFile -Value $stderr -NoNewline

    if ($null -eq $exitCode) {
        Fail-Smoke "$Label did not report an exit code"
    }

    if ($exitCode -ne 0) {
        Fail-Smoke "$Label failed with exit code $exitCode"
    }

    $actual = ($stdout + $stderr).TrimEnd("`r", "`n")
    if ($actual -ne $Expected) {
        Fail-Smoke "$Label expected $Expected but got $actual"
    }

    if (-not (Test-Path -LiteralPath $LogFile) -or (Get-Item -LiteralPath $LogFile).Length -eq 0) {
        Fail-Smoke "$Label log was not written: $LogFile"
    }

    Write-SmokeDuration $DurationLabel $stopwatch.Elapsed
}

function Invoke-CommandContainsSmoke {
    param(
        [string]$Label,
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$ExpectedSubstring,
        [string]$LogFile,
        [int]$TimeoutSeconds,
        [string]$OutputPrefix,
        [string]$WorkingDirectory = (Get-Location).Path,
        [string]$DurationLabel = $Label
    )

    $stdoutFile = Join-Path $smokeDir "$OutputPrefix.out"
    $stderrFile = Join-Path $smokeDir "$OutputPrefix.err"
    Remove-Item -LiteralPath $stdoutFile, $stderrFile, $LogFile -ErrorAction SilentlyContinue

    $env:LOG_FILE = $LogFile
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $FilePath
    $startInfo.Arguments = Join-ProcessArguments $ArgumentList
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.EnvironmentVariables["LOG_FILE"] = $LogFile

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = [System.Diagnostics.Process]::Start($startInfo)

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        $stopwatch.Stop()
        $process.Kill()
        $process.WaitForExit()
        Fail-Smoke "$Label timed out after $TimeoutSeconds seconds"
    }
    $stopwatch.Stop()

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $exitCode = $process.ExitCode
    Set-Content -LiteralPath $stdoutFile -Value $stdout -NoNewline
    Set-Content -LiteralPath $stderrFile -Value $stderr -NoNewline

    if ($null -eq $exitCode) {
        Fail-Smoke "$Label did not report an exit code"
    }

    if ($exitCode -ne 0) {
        Fail-Smoke "$Label failed with exit code $exitCode"
    }

    $actual = ($stdout + $stderr).TrimEnd("`r", "`n")
    if (-not $actual.ToLowerInvariant().Contains($ExpectedSubstring.ToLowerInvariant())) {
        Fail-Smoke "$Label expected output to contain $ExpectedSubstring but got $actual"
    }

    if (-not (Test-Path -LiteralPath $LogFile) -or (Get-Item -LiteralPath $LogFile).Length -eq 0) {
        Fail-Smoke "$Label log was not written: $LogFile"
    }

    Write-SmokeDuration $DurationLabel $stopwatch.Elapsed
}

function Assert-OllamaReady {
    param([string]$BaseUrl)

    $versionUrl = $BaseUrl.TrimEnd("/") + "/api/version"
    Write-Host "Command: Invoke-WebRequest $versionUrl"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        Invoke-WebRequest -Uri $versionUrl -UseBasicParsing -TimeoutSec 30 | Out-Null
    }
    catch {
        Fail-Smoke "Host Ollama is not reachable from Windows guest at ${BaseUrl}: $($_.Exception.Message)"
    }
    finally {
        $stopwatch.Stop()
    }
    Write-SmokeDuration "windows ollama reachability" $stopwatch.Elapsed
}

function Write-AskConfig {
    param(
        [string]$ConfigFile,
        [string]$BaseUrl
    )

    Set-Content -LiteralPath $ConfigFile -Encoding ASCII -Value @"
provider:
  ollama:
    type: ollama
    base-url: $BaseUrl
"@
}

function New-WindowsNativeArchive {
    param(
        [string]$CliDir
    )

    $distDir = Join-Path $CliDir "target/dist"
    $packageName = "codegeist-windows-x64"
    $packageDir = Join-Path $distDir $packageName
    $archive = Join-Path $distDir "$packageName.zip"
    $nativeExe = Join-Path $CliDir "target/codegeist.exe"

    if (-not (Test-Path -LiteralPath $nativeExe)) {
        Fail-Smoke "Native executable was not written: $nativeExe"
    }

    Remove-Item -Recurse -Force -LiteralPath $packageDir, $archive -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $packageDir | Out-Null
    Copy-Item -LiteralPath $nativeExe -Destination (Join-Path $packageDir "codegeist.exe") -Force

    $dlls = Get-ChildItem -LiteralPath (Join-Path $CliDir "target") -Filter "*.dll" -File -ErrorAction SilentlyContinue
    foreach ($dll in $dlls) {
        Copy-Item -LiteralPath $dll.FullName -Destination $packageDir -Force
    }

    Compress-Archive -Path $packageDir -DestinationPath $archive -Force
    return $archive
}

function Invoke-PackagedNativeSmoke {
    param(
        [string]$Archive,
        [string]$PackageName,
        [string]$Expected,
        [int]$TimeoutSeconds,
        [string]$AskConfig,
        [string]$AskPrompt,
        [int]$AskTimeoutSeconds
    )

    $archiveStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("codegeist-smoke-" + [guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null

    try {
        Expand-Archive -LiteralPath $Archive -DestinationPath $tempRoot -Force
        $packageDir = Join-Path $tempRoot $PackageName
        $packageExe = Join-Path $packageDir "codegeist.exe"

        if (-not (Test-Path -LiteralPath $packageExe)) {
            Fail-Smoke "Packaged native executable was not found after unzip: $packageExe"
        }

        Invoke-CommandSmoke "Native archive version smoke" `
            $packageExe `
            @("--version") `
            $Expected `
            (Join-Path $smokeDir "codegeist-windows-native.log") `
            $TimeoutSeconds `
            "codegeist-windows-native" `
            $packageDir `
            "windows native version smoke"

        # Keep aligned with CodegeistConfigService and docs/developer/architecture/provider-configuration.md.
        Invoke-CommandSmoke "Native archive show-config smoke" `
            $packageExe `
            @("--show-config") `
            "provider: {}" `
            (Join-Path $smokeDir "codegeist-windows-native-show-config.log") `
            $TimeoutSeconds `
            "codegeist-windows-native-show-config" `
            $packageDir `
            "windows native show-config smoke"

        Invoke-CommandContainsSmoke "Native archive ask smoke" `
            $packageExe `
            @("-Dcodegeist.config=$AskConfig", "ask", $AskPrompt) `
            "codegeist" `
            (Join-Path $smokeDir "codegeist-windows-native-ask.log") `
            $AskTimeoutSeconds `
            "codegeist-windows-native-ask" `
            $packageDir `
            "windows native ask smoke"

        $archiveStopwatch.Stop()
        Write-SmokeDuration "windows native archive smoke" $archiveStopwatch.Elapsed
    }
    finally {
        Remove-Item -Recurse -Force -LiteralPath $tempRoot -ErrorAction SilentlyContinue
    }
}

$cliDir = Join-Path $RepoDir "app/codegeist/cli"

if (-not (Test-Path -LiteralPath $cliDir)) {
    Fail-Smoke "CLI module directory not found: $cliDir"
}

Set-Location -LiteralPath $cliDir

Write-Host "Platform: windows-x64"
Write-Host "Artifact: jar"

Invoke-Step "mvn --batch-mode --no-transfer-progress test" "windows maven tests" {
    & mvn --batch-mode --no-transfer-progress test
}

Invoke-Step "mvn --batch-mode --no-transfer-progress -DskipTests clean package" "windows jar package" {
    & mvn --batch-mode --no-transfer-progress -DskipTests clean package
}

$expected = Get-BuildVersion "target/classes/META-INF/build-info.properties"
$smokeDir = Join-Path $cliDir "target/smoke-test"
New-Item -ItemType Directory -Force -Path $smokeDir | Out-Null
$askConfig = Join-Path $smokeDir "codegeist-ask.yml"

Assert-OllamaReady $OllamaBaseUrl
Write-AskConfig $askConfig $OllamaBaseUrl

Write-Host "Command: java -jar target/codegeist.jar --version"
Invoke-CommandSmoke "Jar version smoke" `
    "java" `
    @("-jar", "target/codegeist.jar", "--version") `
    $expected `
    (Join-Path $smokeDir "codegeist-windows-jar.log") `
    $JarTimeoutSeconds `
    "codegeist-windows-jar" `
    (Get-Location).Path `
    "windows jar version smoke"

Write-Host "Command: java -Dcodegeist.config=$askConfig -jar target/codegeist.jar ask <prompt>"
Invoke-CommandContainsSmoke "Jar ask smoke" `
    "java" `
    @("-Dcodegeist.config=$askConfig", "-jar", "target/codegeist.jar", "ask", $askPrompt) `
    "codegeist" `
    (Join-Path $smokeDir "codegeist-windows-jar-ask.log") `
    $AskTimeoutSeconds `
    "codegeist-windows-jar-ask" `
    (Get-Location).Path `
    "windows jar ask smoke"

$nativeStatus = "skipped"
$nativeReason = "NativeMode is skip"

if ($NativeMode -ne "skip") {
    if (-not (Get-Command native-image -ErrorAction SilentlyContinue)) {
        $nativeReason = "native-image is not available on PATH"
        if ($NativeMode -eq "required") {
            Fail-Smoke $nativeReason
        }
    }
    else {
        $msvc = Find-MsvcCommand
        $clAvailable = [bool](Get-Command cl.exe -ErrorAction SilentlyContinue)

        if (-not $clAvailable -and -not $msvc) {
            $nativeReason = "MSVC Build Tools environment is not active and no VsDevCmd.bat was found"
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

            $packageName = "codegeist-windows-x64"
            $nativeArchive = New-WindowsNativeArchive $cliDir
            New-Item -ItemType Directory -Force -Path $smokeDir | Out-Null
            Write-AskConfig $askConfig $OllamaBaseUrl

            Write-Host "Command: package target/dist/$packageName.zip and run extracted codegeist.exe --version, --show-config, and ask"
            Invoke-PackagedNativeSmoke $nativeArchive $packageName $expected $NativeTimeoutSeconds $askConfig $askPrompt $AskTimeoutSeconds

            $nativeStatus = "passed"
            $nativeReason = "none"
        }
    }
}

Write-Host "Platform smoke status: passed"
Write-Host "Platform: windows-x64"
Write-Host "Jar status: passed"
Write-Host "Native status: $nativeStatus"
Write-Host "Native reason: $nativeReason"
$platformStopwatch.Stop()
Write-SmokeDuration "windows platform smoke total" $platformStopwatch.Elapsed

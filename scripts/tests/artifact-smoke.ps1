# artifact-smoke.ps1 - shared Codegeist native artifact smoke harness.
#
# Why this exists:
# - Keeps native artifact smoke assertions in one PowerShell 7 script used by
#   Linux, Windows, macOS, local Taskfile wrappers, and the GitHub release workflow.
# - Packages native artifacts, unpacks the release-shaped archive, verifies command
#   output and log side effects, then delegates file-edit and shell side effects to
#   ask-driven harnesses under scripts/tests/.
#
# Inputs:
# - Platform: `linux-x64`, `windows-x64`, or `macos-x64`.
# - CliDir: app/codegeist/cli directory containing target/ build outputs.
# - Optional RunProviderAskSmoke also verifies a real Ollama-backed `ask` turn.
#
# Related files:
# - scripts/tests/file-edit-ask-smoke.ps1
# - scripts/tests/shell-ask-smoke.ps1
# - scripts/tests/local-linux-smoke.ps1
# - scripts/tests/native-smoke.ps1
# - scripts/tests/windows-smoke.ps1
# - .github/workflows/release.yml

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("linux-x64", "windows-x64", "macos-x64")]
    [string]$Platform,

    [Parameter(Mandatory = $true)]
    [string]$CliDir,

    [string]$ExpectedVersion = "",

    [string]$NativeExecutable = "",

    [string]$DistDir = "",

    [string]$SmokeRoot = "",

    [string]$FileEditSmokeScript = "",

    [string]$ShellAskSmokeScript = "",

    [int]$NativeTimeoutSeconds = 5,

    [int]$AskTimeoutSeconds = 60,

    [int]$FileEditTimeoutSeconds = 90,

    [int]$ShellAskTimeoutSeconds = 90,

    [switch]$RunProviderAskSmoke,

    [string]$OllamaBaseUrl = "http://localhost:11434",

    [string]$AskPrompt = "Reply with exactly the lowercase word codegeist."
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ExpectedDefaultConfig = "{}"

function Fail-Smoke {
    param([string]$Message)

    throw $Message
}

function Write-SmokeLog {
    param([string]$Message)

    Write-Host "[artifact-smoke] $Message"
}

function Write-SmokeDuration {
    param(
        [string]$Label,
        [System.Diagnostics.Stopwatch]$Stopwatch
    )

    $Stopwatch.Stop()
    Write-Host ("Duration: {0}: {1:N3}s" -f $Label, $Stopwatch.Elapsed.TotalSeconds)
}

function Resolve-SmokePath {
    param([string]$Path)

    return [System.IO.Path]::GetFullPath($Path)
}

function New-Directory {
    param([string]$Path)

    New-Item -ItemType Directory -Force -Path $Path | Out-Null
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

function Invoke-SmokeProcess {
    param(
        [string]$Label,
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$Expected,
        [string]$ExpectedSubstring,
        [switch]$RequireNonEmptyOutput,
        [string]$LogFile,
        [int]$TimeoutSeconds,
        [string]$OutputPrefix,
        [string]$WorkingDirectory,
        [string]$DurationLabel
    )

    $stdoutFile = Join-Path $SmokeRoot "$OutputPrefix.out"
    $stderrFile = Join-Path $SmokeRoot "$OutputPrefix.err"
    Remove-Item -Force -LiteralPath $stdoutFile, $stderrFile, $LogFile -ErrorAction SilentlyContinue

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $FilePath
    foreach ($argument in $ArgumentList) {
        $startInfo.ArgumentList.Add($argument)
    }
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.Environment["LOG_FILE"] = $LogFile

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = [System.Diagnostics.Process]::Start($startInfo)
    if ($null -eq $process) {
        Fail-Smoke "$Label did not start"
    }

    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        $stopwatch.Stop()
        $process.Kill()
        $process.WaitForExit()
        Fail-Smoke "$Label timed out after $TimeoutSeconds seconds"
    }
    $process.WaitForExit()
    Write-SmokeDuration $DurationLabel $stopwatch

    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    Set-Content -LiteralPath $stdoutFile -Value $stdout -NoNewline
    Set-Content -LiteralPath $stderrFile -Value $stderr -NoNewline

    $actual = ($stdout + $stderr).TrimEnd("`r", "`n")
    if ($process.ExitCode -ne 0) {
        Fail-Smoke "$Label failed with exit code $($process.ExitCode): $actual"
    }

    if ($RequireNonEmptyOutput) {
        if (-not $actual) {
            Fail-Smoke "$Label expected non-empty output"
        }
    }
    elseif ($ExpectedSubstring) {
        if (-not $actual.ToLowerInvariant().Contains($ExpectedSubstring.ToLowerInvariant())) {
            Fail-Smoke "$Label expected output to contain $ExpectedSubstring but got $actual"
        }
    }
    elseif ($actual -ne $Expected) {
        Fail-Smoke "$Label expected $Expected but got $actual"
    }

    if (-not (Test-Path -LiteralPath $LogFile) -or (Get-Item -LiteralPath $LogFile).Length -eq 0) {
        Fail-Smoke "$Label log was not written: $LogFile"
    }
}

function Assert-OllamaReady {
    param([string]$BaseUrl)

    $versionUrl = $BaseUrl.TrimEnd("/") + "/api/version"
    Write-SmokeLog "Command: Invoke-WebRequest $versionUrl"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        Invoke-WebRequest -Uri $versionUrl -UseBasicParsing -TimeoutSec 30 | Out-Null
    }
    catch {
        Fail-Smoke "Ollama is not reachable at ${BaseUrl}: $($_.Exception.Message)"
    }
    finally {
        Write-SmokeDuration "$Platform ollama reachability" $stopwatch
    }
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

function Invoke-FileEditAskSmoke {
    param(
        [string]$Path,
        [string]$Root,
        [string]$LabelPrefix,
        [string]$WorkingDirectory
    )

    if (-not (Test-Path -LiteralPath $FileEditSmokeScript)) {
        Fail-Smoke "File-edit ask smoke script not found: $FileEditSmokeScript"
    }

    Write-SmokeLog "Command: pwsh scripts/tests/file-edit-ask-smoke.ps1 -ArtifactPath $Path"
    & $FileEditSmokeScript `
        -ArtifactPath $Path `
        -SmokeRoot $Root `
        -OllamaBaseUrl $OllamaBaseUrl `
        -TimeoutSeconds $FileEditTimeoutSeconds `
        -LabelPrefix $LabelPrefix `
        -WorkingDirectory $WorkingDirectory
}

function Invoke-ShellAskSmoke {
    param(
        [string]$Path,
        [string]$Root,
        [string]$LabelPrefix,
        [string]$WorkingDirectory
    )

    if (-not (Test-Path -LiteralPath $ShellAskSmokeScript)) {
        Fail-Smoke "Shell ask smoke script not found: $ShellAskSmokeScript"
    }

    Write-SmokeLog "Command: pwsh scripts/tests/shell-ask-smoke.ps1 -ArtifactPath $Path"
    & $ShellAskSmokeScript `
        -ArtifactPath $Path `
        -SmokeRoot $Root `
        -TimeoutSeconds $ShellAskTimeoutSeconds `
        -LabelPrefix $LabelPrefix `
        -WorkingDirectory $WorkingDirectory
}

function Get-NativeBinaryName {
    if ($Platform -eq "windows-x64") {
        return "codegeist.exe"
    }

    return "codegeist"
}

function Get-NativeArchiveExtension {
    if ($Platform -eq "windows-x64") {
        return "zip"
    }

    return "tar.gz"
}

function Copy-NativeSidecars {
    param([string]$PackageDir)

    $targetDir = Join-Path $CliDir "target"
    $filter = switch ($Platform) {
        "linux-x64" { "lib*.so" }
        "macos-x64" { "*.dylib" }
        "windows-x64" { "*.dll" }
        default { "" }
    }

    if (-not $filter) {
        return
    }

    $sidecars = Get-ChildItem -LiteralPath $targetDir -Filter $filter -File -ErrorAction SilentlyContinue
    foreach ($sidecar in $sidecars) {
        Copy-Item -LiteralPath $sidecar.FullName -Destination $PackageDir -Force
    }
}

function New-NativeArchive {
    $binaryName = Get-NativeBinaryName
    $native = if ($NativeExecutable) { Resolve-SmokePath $NativeExecutable } else { Join-Path $CliDir "target/$binaryName" }

    if (-not (Test-Path -LiteralPath $native)) {
        Fail-Smoke "Native executable was not found: $native"
    }

    $packageName = "codegeist-$Platform"
    $extension = Get-NativeArchiveExtension
    $packageDir = Join-Path $DistDir $packageName
    $archive = Join-Path $DistDir "$packageName.$extension"

    Remove-Item -Recurse -Force -LiteralPath $packageDir -ErrorAction SilentlyContinue
    Remove-Item -Force -LiteralPath $archive -ErrorAction SilentlyContinue
    New-Directory $packageDir

    $packagedBinary = Join-Path $packageDir $binaryName
    Copy-Item -LiteralPath $native -Destination $packagedBinary -Force
    if ($Platform -ne "windows-x64") {
        & chmod +x $packagedBinary
        if ($LASTEXITCODE -ne 0) {
            Fail-Smoke "Failed to mark native executable as executable: $packagedBinary"
        }
    }

    Copy-NativeSidecars $packageDir

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    if ($extension -eq "zip") {
        Compress-Archive -LiteralPath $packageDir -DestinationPath $archive -Force
    }
    else {
        & tar -C $DistDir -czf $archive $packageName
        if ($LASTEXITCODE -ne 0) {
            Fail-Smoke "Failed to create native archive: $archive"
        }
    }
    Write-SmokeDuration "$Platform native archive package" $stopwatch

    return [pscustomobject]@{
        Archive = $archive
        PackageName = $packageName
        BinaryName = $binaryName
        Extension = $extension
    }
}

function Expand-NativeArchive {
    param(
        [string]$Archive,
        [string]$Extension,
        [string]$Destination
    )

    if ($Extension -eq "zip") {
        Expand-Archive -LiteralPath $Archive -DestinationPath $Destination -Force
    }
    else {
        & tar -xzf $Archive -C $Destination
        if ($LASTEXITCODE -ne 0) {
            Fail-Smoke "Failed to unpack native archive: $Archive"
        }
    }
}

function Invoke-NativeArtifactSmoke {
    $archiveSmoke = [System.Diagnostics.Stopwatch]::StartNew()
    $package = New-NativeArchive
    $tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("codegeist-smoke-" + [guid]::NewGuid().ToString("N"))
    New-Directory $tempRoot

    try {
        Expand-NativeArchive $package.Archive $package.Extension $tempRoot
        $runDir = Join-Path $tempRoot $package.PackageName
        $native = Join-Path $runDir $package.BinaryName

        if (-not (Test-Path -LiteralPath $native)) {
            Fail-Smoke "Packaged native executable was not found after unpack: $native"
        }

        Write-SmokeLog "Command: $native --version"
        Invoke-SmokeProcess `
            -Label "Native version smoke" `
            -FilePath $native `
            -ArgumentList @("--version") `
            -Expected $ExpectedVersion `
            -ExpectedSubstring "" `
            -LogFile (Join-Path $SmokeRoot "codegeist-$Platform-native.log") `
            -TimeoutSeconds $NativeTimeoutSeconds `
            -OutputPrefix "codegeist-$Platform-native" `
            -WorkingDirectory $runDir `
            -DurationLabel "$Platform native version smoke"

        Write-SmokeLog "Command: $native --show-config"
        Invoke-SmokeProcess `
            -Label "Native show-config smoke" `
            -FilePath $native `
            -ArgumentList @("--show-config") `
            -Expected $ExpectedDefaultConfig `
            -ExpectedSubstring "" `
            -LogFile (Join-Path $SmokeRoot "codegeist-$Platform-native-show-config.log") `
            -TimeoutSeconds $NativeTimeoutSeconds `
            -OutputPrefix "codegeist-$Platform-native-show-config" `
            -WorkingDirectory $runDir `
            -DurationLabel "$Platform native show-config smoke"

        Invoke-FileEditAskSmoke `
            -Path $native `
            -Root (Join-Path $SmokeRoot "native-file-edit-ask") `
            -LabelPrefix "$Platform native" `
            -WorkingDirectory $runDir

        Invoke-ShellAskSmoke `
            -Path $native `
            -Root (Join-Path $SmokeRoot "native-shell-ask") `
            -LabelPrefix "$Platform native" `
            -WorkingDirectory $runDir

        if ($RunProviderAskSmoke) {
            Assert-OllamaReady $OllamaBaseUrl
            $askConfig = Join-Path $SmokeRoot "codegeist-$Platform-native-ask.yml"
            Write-AskConfig $askConfig $OllamaBaseUrl

            Write-SmokeLog "Command: $native -Dcodegeist.config=$askConfig ask <prompt>"
            Invoke-SmokeProcess `
                -Label "Native ask smoke" `
                -FilePath $native `
                -ArgumentList @("-Dcodegeist.config=$askConfig", "ask", $AskPrompt) `
                -Expected "" `
                -ExpectedSubstring "" `
                -RequireNonEmptyOutput `
                -LogFile (Join-Path $SmokeRoot "codegeist-$Platform-native-ask.log") `
                -TimeoutSeconds $AskTimeoutSeconds `
                -OutputPrefix "codegeist-$Platform-native-ask" `
                -WorkingDirectory $runDir `
                -DurationLabel "$Platform native ask smoke"
        }
    }
    finally {
        Remove-Item -Recurse -Force -LiteralPath $tempRoot -ErrorAction SilentlyContinue
    }

    Write-SmokeDuration "$Platform native archive smoke" $archiveSmoke
    Write-SmokeLog "Native archive smoke passed: $($package.Archive)"
}

$CliDir = Resolve-SmokePath $CliDir
if (-not (Test-Path -LiteralPath $CliDir)) {
    Fail-Smoke "CLI module directory not found: $CliDir"
}

if (-not $DistDir) {
    $DistDir = Join-Path $CliDir "target/dist"
}
$DistDir = Resolve-SmokePath $DistDir
New-Directory $DistDir

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $CliDir "target/smoke-test"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot
New-Directory $SmokeRoot

if (-not $FileEditSmokeScript) {
    $FileEditSmokeScript = Join-Path $PSScriptRoot "file-edit-ask-smoke.ps1"
}
$FileEditSmokeScript = Resolve-SmokePath $FileEditSmokeScript

if (-not $ShellAskSmokeScript) {
    $ShellAskSmokeScript = Join-Path $PSScriptRoot "shell-ask-smoke.ps1"
}
$ShellAskSmokeScript = Resolve-SmokePath $ShellAskSmokeScript

if (-not $ExpectedVersion) {
    $ExpectedVersion = Get-BuildVersion (Join-Path $CliDir "target/classes/META-INF/build-info.properties")
}

Write-SmokeLog "platform: $Platform"
Write-SmokeLog "artifact: native"
Write-SmokeLog "expected version: $ExpectedVersion"

Invoke-NativeArtifactSmoke

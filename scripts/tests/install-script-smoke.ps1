# install-script-smoke.ps1 - run a platform install script against local assets.
#
# Why this exists:
# - Gives the GitHub release matrix a same-runner installability check for the
#   Linux, Windows, and macOS curl-downloadable install scripts.
# - Uses the native archive already produced by scripts/tests/artifact-smoke.ps1,
#   serves release-shaped assets over localhost, runs the platform installer in an
#   isolated install root, then verifies the installed command wrapper.
#
# Inputs:
# - Platform: `linux-x64`, `windows-x64`, or `macos-x64`.
# - CliDir: app/codegeist/cli directory containing target/dist archives.
# - ExpectedVersion: optional exact `codegeist --version` output.
#
# Related files:
# - scripts/install/codegeist-install-linux.sh
# - scripts/install/codegeist-install-macos.sh
# - scripts/install/codegeist-install-windows.ps1
# - .github/workflows/release.yml

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("linux-x64", "windows-x64", "macos-x64")]
    [string]$Platform,

    [Parameter(Mandatory = $true)]
    [string]$CliDir,

    [string]$ExpectedVersion = "",

    [string]$DistDir = "",

    [string]$SmokeRoot = "",

    [string]$InstallScriptDir = "",

    [int]$CommandTimeoutSeconds = 15
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ChecksumName = "SHA256SUMS.txt"
$RequiredWindowsVcRuntimeFiles = @("VCRUNTIME140.dll", "VCRUNTIME140_1.dll", "MSVCP140.dll")

function Fail-Smoke {
    param([string]$Message)

    throw $Message
}

function Write-SmokeLog {
    param([string]$Message)

    Write-Host "[install-script-smoke] $Message"
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

function Get-PlatformSpec {
    switch ($Platform) {
        "linux-x64" {
            return [pscustomobject]@{
                ArchiveName = "codegeist-linux-x64.tar.gz"
                ScriptName = "codegeist-install-linux.sh"
                InstalledCommand = "codegeist"
                ScriptKind = "bash"
            }
        }
        "macos-x64" {
            return [pscustomobject]@{
                ArchiveName = "codegeist-macos-x64.tar.gz"
                ScriptName = "codegeist-install-macos.sh"
                InstalledCommand = "codegeist"
                ScriptKind = "bash"
            }
        }
        "windows-x64" {
            return [pscustomobject]@{
                ArchiveName = "codegeist-windows-x64.zip"
                ScriptName = "codegeist-install-windows.ps1"
                InstalledCommand = "codegeist.cmd"
                ScriptKind = "pwsh"
            }
        }
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

function Write-LocalChecksums {
    param([object]$Spec)

    $entries = @($Spec.ArchiveName, $Spec.ScriptName)
    $lines = foreach ($entry in $entries) {
        $path = Join-Path $DistDir $entry
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            Fail-Smoke "Expected local release asset not found: $path"
        }

        $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $path).Hash.ToLowerInvariant()
        "$hash  $entry"
    }

    Set-Content -LiteralPath (Join-Path $DistDir $ChecksumName) -Encoding ASCII -Value $lines
}

function Get-FreeTcpPort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    try {
        $listener.Start()
        return $listener.LocalEndpoint.Port
    }
    finally {
        $listener.Stop()
    }
}

function Start-AssetServer {
    $port = Get-FreeTcpPort
    $baseUrl = "http://127.0.0.1:$port"
    $serverScript = Join-Path $SmokeRoot "install-script-asset-server.ps1"
    $pwsh = (Get-Command pwsh -ErrorAction Stop).Source
    $stdoutFile = Join-Path $SmokeRoot "install-script-asset-server.out"
    $stderrFile = Join-Path $SmokeRoot "install-script-asset-server.err"

    Remove-Item -Force -LiteralPath $stdoutFile, $stderrFile -ErrorAction SilentlyContinue
    Set-Content -LiteralPath $serverScript -Encoding UTF8 -Value @'
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Root,

    [Parameter(Mandatory = $true)]
    [int]$Port
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-HttpResponse {
    param(
        [System.IO.Stream]$Stream,
        [int]$Status,
        [string]$Reason,
        [byte[]]$Body
    )

    $header = "HTTP/1.1 $Status $Reason`r`nContent-Length: $($Body.Length)`r`nContent-Type: application/octet-stream`r`nConnection: close`r`n`r`n"
    $headerBytes = [System.Text.Encoding]::ASCII.GetBytes($header)
    $Stream.Write($headerBytes, 0, $headerBytes.Length)
    if ($Body.Length -gt 0) {
        $Stream.Write($Body, 0, $Body.Length)
    }
}

$rootFull = [System.IO.Path]::GetFullPath($Root)
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()
try {
    while ($true) {
        $client = $listener.AcceptTcpClient()
        $reader = $null
        $stream = $null
        try {
            $stream = $client.GetStream()
            $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::ASCII, $false, 1024, $true)
            $requestLine = $reader.ReadLine()
            do {
                $line = $reader.ReadLine()
            } while ($null -ne $line -and $line.Length -gt 0)

            $requestPath = ""
            if ($requestLine -and $requestLine -match '^GET\s+([^\s]+)\s+HTTP/') {
                $requestPath = [System.Uri]::UnescapeDataString($Matches[1].TrimStart('/'))
            }

            $assetName = [System.IO.Path]::GetFileName($requestPath)
            $assetPath = [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($rootFull, $assetName))
            if (-not $assetName -or -not $assetPath.StartsWith($rootFull, [System.StringComparison]::OrdinalIgnoreCase) -or -not (Test-Path -LiteralPath $assetPath -PathType Leaf)) {
                Write-HttpResponse $stream 404 "Not Found" ([System.Text.Encoding]::UTF8.GetBytes("not found"))
                continue
            }

            Write-HttpResponse $stream 200 "OK" ([System.IO.File]::ReadAllBytes($assetPath))
        }
        catch {
            try {
                Write-HttpResponse $stream 500 "Internal Server Error" ([System.Text.Encoding]::UTF8.GetBytes("internal server error"))
            }
            catch {
            }
        }
        finally {
            if ($reader) {
                $reader.Dispose()
            }
            $client.Dispose()
        }
    }
}
finally {
    $listener.Stop()
}
'@

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $pwsh
    foreach ($argument in @("-NoProfile", "-File", $serverScript, "-Root", $DistDir, "-Port", [string]$port)) {
        $startInfo.ArgumentList.Add($argument)
    }
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.RedirectStandardInput = $true

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    if (-not $process.Start()) {
        Fail-Smoke "Failed to start local asset server"
    }

    $readyUrl = "$baseUrl/$ChecksumName"
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if ($process.HasExited) {
            $process.Dispose()
            Fail-Smoke "Local asset server exited before becoming ready"
        }

        try {
            Invoke-WebRequest -Uri $readyUrl -UseBasicParsing -TimeoutSec 2 | Out-Null
            Write-SmokeLog "Serving install assets at $baseUrl/"
            return [pscustomobject]@{
                Process = $process
                BaseUrl = $baseUrl
            }
        }
        catch {
            Start-Sleep -Seconds 1
        }
    }

    if (-not $process.HasExited) {
        $process.Kill()
        $process.WaitForExit()
    }
    $process.Dispose()
    Fail-Smoke "Local asset server did not become ready at $readyUrl"
}

function Stop-AssetServer {
    param([object]$Server)

    if ($null -eq $Server) {
        return
    }

    if (-not $Server.Process.HasExited) {
        $Server.Process.Kill()
        $Server.Process.WaitForExit()
    }
    $Server.Process.Dispose()
}

function Restore-EnvValue {
    param(
        [string]$Name,
        [bool]$WasSet,
        [string]$Value
    )

    if ($WasSet) {
        Set-Item -Path "Env:$Name" -Value $Value
    }
    else {
        Remove-Item -Path "Env:$Name" -ErrorAction SilentlyContinue
    }
}

function Invoke-Installer {
    param(
        [object]$Spec,
        [object]$Server
    )

    $scriptPath = Join-Path $DistDir $Spec.ScriptName
    $installRoot = Join-Path $SmokeRoot "install-root"
    $binDir = Join-Path $SmokeRoot "bin"
    $workDir = Join-Path $SmokeRoot "installer-work"
    $installedCommand = Join-Path $binDir $Spec.InstalledCommand

    Remove-Item -Recurse -Force -LiteralPath $installRoot, $binDir, $workDir -ErrorAction SilentlyContinue
    New-Directory $installRoot
    New-Directory $binDir
    New-Directory $workDir

    $baseUrlWasSet = Test-Path Env:CODEGEIST_INSTALL_BASE_URL
    $installDirWasSet = Test-Path Env:CODEGEIST_INSTALL_DIR
    $binDirWasSet = Test-Path Env:CODEGEIST_BIN_DIR
    $oldBaseUrl = $env:CODEGEIST_INSTALL_BASE_URL
    $oldInstallDir = $env:CODEGEIST_INSTALL_DIR
    $oldBinDir = $env:CODEGEIST_BIN_DIR

    try {
        $env:CODEGEIST_INSTALL_BASE_URL = $Server.BaseUrl
        $env:CODEGEIST_INSTALL_DIR = $installRoot
        $env:CODEGEIST_BIN_DIR = $binDir

        Push-Location $workDir
        try {
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            Write-SmokeLog "Command: $($Spec.ScriptName)"
            if ($Spec.ScriptKind -eq "pwsh") {
                $pwsh = (Get-Command pwsh -ErrorAction Stop).Source
                & $pwsh -NoProfile -ExecutionPolicy Bypass -File $scriptPath 2>&1 |
                    ForEach-Object { Write-Host $_ }
            }
            else {
                & bash $scriptPath 2>&1 |
                    ForEach-Object { Write-Host $_ }
            }
            $exitCode = $LASTEXITCODE
            Write-SmokeDuration "$Platform install script run" $stopwatch
            if ($exitCode -ne 0) {
                Fail-Smoke "$($Spec.ScriptName) failed with exit code $exitCode"
            }
        }
        finally {
            Pop-Location
        }
    }
    finally {
        Restore-EnvValue "CODEGEIST_INSTALL_BASE_URL" $baseUrlWasSet $oldBaseUrl
        Restore-EnvValue "CODEGEIST_INSTALL_DIR" $installDirWasSet $oldInstallDir
        Restore-EnvValue "CODEGEIST_BIN_DIR" $binDirWasSet $oldBinDir
    }

    if (-not (Test-Path -LiteralPath $installedCommand -PathType Leaf)) {
        Fail-Smoke "Installed command wrapper not found: $installedCommand"
    }

    return $installedCommand
}

function Invoke-InstalledCommand {
    param(
        [string]$CommandPath,
        [string[]]$ArgumentList,
        [string]$Expected,
        [switch]$RequireNonEmptyOutput,
        [string]$Label,
        [string]$OutputPrefix
    )

    $stdoutFile = Join-Path $SmokeRoot "$OutputPrefix.out"
    $stderrFile = Join-Path $SmokeRoot "$OutputPrefix.err"
    $logFile = Join-Path $SmokeRoot "$OutputPrefix.log"
    $workDir = Join-Path $SmokeRoot "command-work"
    New-Directory $workDir
    Remove-Item -Force -LiteralPath $stdoutFile, $stderrFile, $logFile -ErrorAction SilentlyContinue

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    if ($Platform -eq "windows-x64") {
        $startInfo.FileName = if ($env:ComSpec) { $env:ComSpec } else { "cmd.exe" }
        $startInfo.Arguments = '/d /s /c ""{0}" {1}"' -f $CommandPath, ($ArgumentList -join " ")
    }
    else {
        $startInfo.FileName = $CommandPath
        foreach ($argument in $ArgumentList) {
            $startInfo.ArgumentList.Add($argument)
        }
    }
    $startInfo.WorkingDirectory = $workDir
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.Environment["LOG_FILE"] = $logFile

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $process = [System.Diagnostics.Process]::Start($startInfo)
    if ($null -eq $process) {
        Fail-Smoke "$Label did not start"
    }

    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()

    if (-not $process.WaitForExit($CommandTimeoutSeconds * 1000)) {
        $stopwatch.Stop()
        $process.Kill()
        $process.WaitForExit()
        Fail-Smoke "$Label timed out after $CommandTimeoutSeconds seconds"
    }
    $process.WaitForExit()
    Write-SmokeDuration "$Platform install command $OutputPrefix smoke" $stopwatch

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
    elseif ($actual -ne $Expected) {
        Fail-Smoke "$Label expected $Expected but got $actual"
    }
}

$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$CliDir = Resolve-SmokePath $CliDir
if (-not (Test-Path -LiteralPath $CliDir -PathType Container)) {
    Fail-Smoke "CLI module directory not found: $CliDir"
}

if (-not $DistDir) {
    $DistDir = Join-Path $CliDir "target/dist"
}
$DistDir = Resolve-SmokePath $DistDir
New-Directory $DistDir

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $CliDir "target/smoke-test/install-script/$Platform"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot
New-Directory $SmokeRoot

if (-not $InstallScriptDir) {
    $InstallScriptDir = Join-Path $PSScriptRoot "../install"
}
$InstallScriptDir = Resolve-SmokePath $InstallScriptDir

if (-not $ExpectedVersion) {
    $ExpectedVersion = Get-BuildVersion (Join-Path $CliDir "target/classes/META-INF/build-info.properties")
}

$spec = Get-PlatformSpec
$archive = Join-Path $DistDir $spec.ArchiveName
$sourceScript = Join-Path $InstallScriptDir $spec.ScriptName
$stagedScript = Join-Path $DistDir $spec.ScriptName
if (-not (Test-Path -LiteralPath $archive -PathType Leaf)) {
    Fail-Smoke "Native archive not found: $archive"
}
if (-not (Test-Path -LiteralPath $sourceScript -PathType Leaf)) {
    Fail-Smoke "Install script not found: $sourceScript"
}
Copy-Item -LiteralPath $sourceScript -Destination $stagedScript -Force
Write-LocalChecksums $spec

Write-SmokeLog "platform: $Platform"
Write-SmokeLog "archive: $($spec.ArchiveName)"
Write-SmokeLog "installer: $($spec.ScriptName)"
Write-SmokeLog "expected version: $ExpectedVersion"

$server = $null
try {
    $server = Start-AssetServer
    $installedCommand = Invoke-Installer $spec $server
    if ($Platform -eq "windows-x64") {
        $installedCurrentDir = Join-Path $SmokeRoot "install-root/current"
        foreach ($runtimeFile in $RequiredWindowsVcRuntimeFiles) {
            $runtimePath = Join-Path $installedCurrentDir $runtimeFile
            if (-not (Test-Path -LiteralPath $runtimePath -PathType Leaf)) {
                Fail-Smoke "Installed app-local MSVC runtime library not found: $runtimePath"
            }
        }
    }
    Write-SmokeLog "Command: $installedCommand --version"
    Invoke-InstalledCommand `
        -CommandPath $installedCommand `
        -ArgumentList @("--version") `
        -Expected $ExpectedVersion `
        -Label "Installed codegeist version smoke" `
        -OutputPrefix "version"

    Write-SmokeLog "Command: $installedCommand --show-config"
    Invoke-InstalledCommand `
        -CommandPath $installedCommand `
        -ArgumentList @("--show-config") `
        -Expected "{}" `
        -Label "Installed codegeist show-config smoke" `
        -OutputPrefix "show-config"
}
finally {
    Stop-AssetServer $server
}

Write-Host "Platform smoke status: passed"
Write-Host "Platform: $Platform"
Write-Host "Install status: passed"
Write-SmokeDuration "$Platform install script smoke" $totalStopwatch
Write-SmokeLog "Install script smoke passed: $($spec.ScriptName)"

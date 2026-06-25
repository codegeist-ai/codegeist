# server-native-smoke.ps1 - native startup smoke for the Codegeist Cloud server.
#
# Why this exists:
# - Builds or reuses app/codegeist/server/target/codegeist-server as a GraalVM
#   native executable, starts it on a temporary localhost port, and verifies that
#   GET /health returns the bootstrap server contract.
# - Prints stable duration lines so startup time stays visible in task output.
#
# Inputs:
# - Run from anywhere inside the repository checkout.
# - Optional BuildNative compiles the server native executable before the smoke.
# - Optional timeout parameters override environment defaults.
#
# Side effects:
# - Writes smoke stdout/stderr files under app/codegeist/server/target/smoke-test.
# - Starts and then terminates a temporary local server process.
#
# Related files:
# - app/codegeist/server/Taskfile.yml
# - scripts/tests/smoke-common.ps1

[CmdletBinding()]
param(
    [string]$ServerDir = "",

    [string]$SmokeRoot = "",

    [string]$NativeExecutable = "",

    [int]$StartupTimeoutSeconds = 0,

    [switch]$BuildNative
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

$ExpectedHealthBody = '{"status":"ok"}'

function Get-FreeSmokePort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Parse("127.0.0.1"), 0)
    try {
        $listener.Start()
        return $listener.LocalEndpoint.Port
    }
    finally {
        $listener.Stop()
    }
}

function Resolve-NativeServerExecutable {
    param(
        [string]$ConfiguredExecutable,
        [string]$ResolvedServerDir
    )

    if ($ConfiguredExecutable) {
        return Resolve-SmokePath $ConfiguredExecutable
    }

    $extension = if ($IsWindows) { ".exe" } else { "" }
    return Join-Path $ResolvedServerDir "target/codegeist-server$extension"
}

function Invoke-ServerStartupSmoke {
    param(
        [string]$Executable,
        [string]$ResolvedServerDir,
        [string]$ResolvedSmokeRoot,
        [int]$TimeoutSeconds
    )

    if (-not (Test-Path -LiteralPath $Executable)) {
        throw "Native server executable does not exist: $Executable"
    }

    New-SmokeDirectory $ResolvedSmokeRoot
    $port = Get-FreeSmokePort
    $url = "http://127.0.0.1:$port/health"
    $stdoutFile = Join-Path $ResolvedSmokeRoot "server-native.out"
    $stderrFile = Join-Path $ResolvedSmokeRoot "server-native.err"
    Remove-Item -Force -LiteralPath $stdoutFile, $stderrFile -ErrorAction SilentlyContinue

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $Executable
    $startInfo.ArgumentList.Add("--server.port=$port")
    $startInfo.WorkingDirectory = $ResolvedServerDir
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true

    Write-Host "Command: $Executable --server.port=$port"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $durationWritten = $false
    $failure = $null
    $process = [System.Diagnostics.Process]::Start($startInfo)
    if ($null -eq $process) {
        throw "Native server process did not start"
    }

    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()

    try {
        $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
        while ([DateTime]::UtcNow -lt $deadline) {
            if ($process.HasExited) {
                throw "Native server exited before health readiness with exit code $($process.ExitCode)"
            }

            try {
                $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 1
                if ($response.StatusCode -eq 200 -and $response.Content.Trim() -eq $ExpectedHealthBody) {
                    Write-SmokeDuration "server native startup" $stopwatch
                    $durationWritten = $true
                    return $stopwatch.Elapsed.TotalSeconds
                }
            }
            catch {
                Start-Sleep -Milliseconds 100
            }
        }

        throw "Native server did not become healthy at $url within $TimeoutSeconds seconds"
    }
    catch {
        $failure = $_
    }
    finally {
        if (-not $durationWritten) {
            Write-SmokeDuration "server native startup" $stopwatch
        }

        if (-not $process.HasExited) {
            $process.Kill()
            $process.WaitForExit()
        }

        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        Set-Content -LiteralPath $stdoutFile -Value $stdout -NoNewline
        Set-Content -LiteralPath $stderrFile -Value $stderr -NoNewline
    }

    if ($failure) {
        throw $failure
    }
}

if (-not $ServerDir) {
    $ServerDir = Join-Path $PSScriptRoot "../../app/codegeist/server"
}
$ServerDir = Resolve-SmokePath $ServerDir

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $ServerDir "target/smoke-test"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot
New-SmokeDirectory $SmokeRoot

$startupTimeout = Get-SmokeSeconds `
    -ExplicitValue $StartupTimeoutSeconds `
    -EnvironmentNames @( "CODEGEIST_SERVER_NATIVE_STARTUP_TIMEOUT_SECONDS", "CODEGEIST_SERVER_STARTUP_TIMEOUT_SECONDS" ) `
    -DefaultValue 30

$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$status = "failed"

try {
    Push-Location -LiteralPath $ServerDir
    try {
        if ($BuildNative) {
            Invoke-SmokeStep `
                "mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile" `
                "server native compile" `
                { & mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile }
        }

        $resolvedExecutable = Resolve-NativeServerExecutable $NativeExecutable $ServerDir
        $startupSeconds = Invoke-ServerStartupSmoke `
            -Executable $resolvedExecutable `
            -ResolvedServerDir $ServerDir `
            -ResolvedSmokeRoot $SmokeRoot `
            -TimeoutSeconds $startupTimeout
        Write-Host ("Server native startup seconds: {0:N3}" -f $startupSeconds)
        $status = "passed"
    }
    finally {
        Pop-Location
    }
}
finally {
    Write-Host "Server native smoke status: $status"
    Write-SmokeDuration "server native smoke total" $totalStopwatch
}

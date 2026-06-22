# smoke-common.ps1 - shared helpers for Codegeist smoke scripts.
#
# Why this exists:
# - Keeps platform smoke orchestration on one PowerShell helper surface instead of
#   duplicating status files, duration output, environment overrides, and command
#   checks across Linux, Windows, MCP, and final-suite entrypoints.
#
# Inputs:
# - Dot-source from scripts under scripts/tests/.
#
# Related files:
# - scripts/tests/local-linux-smoke.ps1
# - scripts/tests/qemu-windows-smoke.ps1
# - scripts/tests/mcp-remote-smoke.ps1
# - scripts/tests/final-smoke-suite.ps1

function Resolve-SmokePath {
    param([string]$Path)

    return [System.IO.Path]::GetFullPath($Path)
}

function New-SmokeDirectory {
    param([string]$Path)

    New-Item -ItemType Directory -Force -Path $Path | Out-Null
}

function Get-SmokeSeconds {
    param(
        [int]$ExplicitValue,
        [string[]]$EnvironmentNames,
        [int]$DefaultValue
    )

    if ($ExplicitValue -gt 0) {
        return $ExplicitValue
    }

    foreach ($environmentName in $EnvironmentNames) {
        if (-not $environmentName) {
            continue
        }

        $value = [Environment]::GetEnvironmentVariable($environmentName)
        if (-not $value) {
            continue
        }

        $trimmed = $value.Trim()
        if ($trimmed -match '^\d+s?$') {
            return [int]($trimmed.TrimEnd('s'))
        }

        throw "$environmentName must be an integer number of seconds"
    }

    return $DefaultValue
}

function Test-SmokeEnvFlag {
    param(
        [string]$Name,
        [bool]$DefaultValue = $false
    )

    $value = [Environment]::GetEnvironmentVariable($Name)
    if (-not $value) {
        return $DefaultValue
    }

    return $value -in @('1', 'true', 'TRUE', 'yes', 'YES', 'on', 'ON')
}

function Write-SmokeDuration {
    param(
        [string]$Label,
        [System.Diagnostics.Stopwatch]$Stopwatch
    )

    $Stopwatch.Stop()
    Write-Host ("Duration: {0}: {1:N3}s" -f $Label, $Stopwatch.Elapsed.TotalSeconds)
}

function Invoke-SmokeStep {
    param(
        [string]$CommandLabel,
        [string]$DurationLabel,
        [scriptblock]$Command
    )

    Write-Host "Command: $CommandLabel"
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $failure = $null
    try {
        & $Command
    }
    catch {
        $failure = $_
    }
    finally {
        Write-SmokeDuration $DurationLabel $stopwatch
    }

    if ($failure) {
        throw $failure
    }

    if ($LASTEXITCODE -ne 0) {
        throw "$CommandLabel failed with exit code $LASTEXITCODE"
    }
}

function Invoke-SmokeWithEnvironment {
    param(
        [hashtable]$Environment,
        [scriptblock]$Command
    )

    $previous = @{}
    foreach ($key in $Environment.Keys) {
        $previous[$key] = [Environment]::GetEnvironmentVariable($key)
        [Environment]::SetEnvironmentVariable($key, [string]$Environment[$key])
    }

    try {
        & $Command
    }
    finally {
        foreach ($key in $Environment.Keys) {
            [Environment]::SetEnvironmentVariable($key, $previous[$key])
        }
    }
}

function Write-SmokeStatusFile {
    param(
        [string]$Path,
        [System.Collections.IDictionary]$Values
    )

    if (-not $Path) {
        return
    }

    New-SmokeDirectory ([System.IO.Path]::GetDirectoryName($Path))
    $lines = foreach ($key in $Values.Keys) {
        "${key}=$($Values[$key])"
    }
    [System.IO.File]::WriteAllText($Path, ($lines -join "`n") + "`n", [System.Text.Encoding]::UTF8)
}

function Read-SmokeStatusFile {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return 'failed'
    }

    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line.StartsWith('status=')) {
            return $line.Substring('status='.Length)
        }
    }

    return 'failed'
}

function Test-SmokeTcpPort {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutMilliseconds = 1000
    )

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connectTask = $client.ConnectAsync($HostName, $Port)
        if (-not $connectTask.Wait($TimeoutMilliseconds)) {
            return $false
        }
        return $client.Connected
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

function ConvertTo-SmokePowerShellSingleQuotedString {
    param([string]$Value)

    return "'" + $Value.Replace("'", "''") + "'"
}

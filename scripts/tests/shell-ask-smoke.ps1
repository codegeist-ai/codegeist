# shell-ask-smoke.ps1 - cross-platform ask-driven shell tool smoke harness.
#
# Why this exists:
# - Exercises the real `ask` command and Spring AI tool-calling path with a
#   deterministic Ollama-compatible fixture provider that calls `codegeist_shell`.
# - Uses PowerShell 7 as the configured shell wrapper so the same command runs on
#   Linux, Windows, and macOS native artifacts.
# - Verifies the filesystem side effect and persisted `ToolSessionPart` instead of
#   trusting model response text.
#
# Inputs:
# - ArtifactPath: path to the packaged native `codegeist` binary.
# - SmokeRoot: disposable directory for fixtures, config, logs, and sessions.
# - WorkingDirectory: host cwd for the artifact process; defaults to caller cwd.
#
# Related files:
# - scripts/tests/artifact-smoke.ps1
# - scripts/tests/file-edit-ask-smoke.ps1

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ArtifactPath,

    [Parameter(Mandatory = $true)]
    [string]$SmokeRoot,

    [int]$TimeoutSeconds = 90,

    [string]$LabelPrefix = "shell ask",

    [string]$WorkingDirectory = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ShellToolName = "codegeist_shell"
$CompletionMessage = "SHELL_ASK_SMOKE_DONE"
$FixtureModel = "codegeist-fixture"
$ShellOutputMarker = "shell-output"
$ShellFileName = "shell-smoke.txt"
$ShellFileContent = "shell-smoke"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Fail-Smoke {
    param([string]$Message)

    throw $Message
}

function Write-SmokeDuration {
    param(
        [string]$Label,
        [System.Diagnostics.Stopwatch]$Stopwatch
    )

    $Stopwatch.Stop()
    Write-Host ("Duration: {0}: {1:N3}s" -f $Label, $Stopwatch.Elapsed.TotalSeconds)
}

function Write-SmokeLog {
    param([string]$Message)

    Write-Host "[shell-ask-smoke] $Message"
}

function Resolve-SmokePath {
    param([string]$Path)

    return [System.IO.Path]::GetFullPath($Path)
}

function ConvertTo-YamlSingleQuotedString {
    param([string]$Value)

    return "'" + $Value.Replace("'", "''") + "'"
}

function New-Directory {
    param([string]$Path)

    New-Item -ItemType Directory -Force -Path $Path | Out-Null
}

function Get-FreeTcpPort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    $listener.Start()
    try {
        return $listener.LocalEndpoint.Port
    }
    finally {
        $listener.Stop()
    }
}

function Start-FixtureOllamaServer {
    param(
        [string]$ToolInputJson,
        [string]$LogFile
    )

    $port = Get-FreeTcpPort
    $prefix = "http://127.0.0.1:$port/"
    New-Directory ([System.IO.Path]::GetDirectoryName($LogFile))
    Set-Content -LiteralPath $LogFile -Encoding UTF8 -Value "fixture starting at $prefix"

    $job = Start-Job -ScriptBlock {
        param(
            [string]$Prefix,
            [string]$ToolName,
            [string]$ToolInputJson,
            [string]$CompletionMessage,
            [string]$Model,
            [string]$LogFile
        )

        $ErrorActionPreference = "Stop"

        function Write-FixtureLog {
            param([string]$Message)

            Add-Content -LiteralPath $LogFile -Encoding UTF8 -Value ("{0:o} {1}" -f [DateTimeOffset]::UtcNow, $Message)
        }

        function Read-RequestBody {
            param([System.Net.HttpListenerRequest]$Request)

            $reader = [System.IO.StreamReader]::new($Request.InputStream, $Request.ContentEncoding)
            try {
                return $reader.ReadToEnd()
            }
            finally {
                $reader.Dispose()
            }
        }

        function Send-Json {
            param(
                [System.Net.HttpListenerResponse]$Response,
                [object]$Payload
            )

            $json = $Payload | ConvertTo-Json -Depth 20 -Compress
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
            $Response.StatusCode = 200
            $Response.ContentType = "application/json"
            $Response.ContentLength64 = $bytes.Length
            $Response.OutputStream.Write($bytes, 0, $bytes.Length)
            $Response.OutputStream.Close()
            Write-FixtureLog "response $json"
        }

        $toolArguments = $ToolInputJson | ConvertFrom-Json
        $listener = [System.Net.HttpListener]::new()
        $listener.Prefixes.Add($Prefix)
        $listener.Start()
        Write-FixtureLog "started $Prefix"

        $chatRequests = 0
        try {
            while ($chatRequests -lt 4) {
                $context = $listener.GetContext()
                $request = $context.Request
                $body = Read-RequestBody $request
                Write-FixtureLog "$($request.HttpMethod) $($request.RawUrl) $body"

                if ($request.HttpMethod -eq "GET" -and $request.RawUrl -eq "/api/version") {
                    Send-Json $context.Response ([ordered]@{ version = "fixture" })
                    continue
                }

                if ($request.HttpMethod -ne "POST" -or $request.RawUrl -ne "/api/chat") {
                    $context.Response.StatusCode = 404
                    $context.Response.OutputStream.Close()
                    continue
                }

                $chatRequests++
                if ($chatRequests -eq 1) {
                    Send-Json $context.Response ([ordered]@{
                        model = $Model
                        created_at = [DateTimeOffset]::UtcNow.ToString("o")
                        message = [ordered]@{
                            role = "assistant"
                            content = ""
                            tool_calls = @(
                                [ordered]@{
                                    id = "call-1"
                                    function = [ordered]@{
                                        name = $ToolName
                                        arguments = $toolArguments
                                    }
                                }
                            )
                        }
                        done_reason = "stop"
                        done = $true
                        total_duration = 1
                        load_duration = 1
                        prompt_eval_count = 1
                        prompt_eval_duration = 1
                        eval_count = 1
                        eval_duration = 1
                    })
                }
                else {
                    Send-Json $context.Response ([ordered]@{
                        model = $Model
                        created_at = [DateTimeOffset]::UtcNow.ToString("o")
                        message = [ordered]@{
                            role = "assistant"
                            content = $CompletionMessage
                        }
                        done_reason = "stop"
                        done = $true
                        total_duration = 1
                        load_duration = 1
                        prompt_eval_count = 1
                        prompt_eval_duration = 1
                        eval_count = 1
                        eval_duration = 1
                    })
                    break
                }
            }
        }
        finally {
            $listener.Stop()
            $listener.Close()
            Write-FixtureLog "stopped after $chatRequests chat requests"
        }
    } -ArgumentList $prefix, $ShellToolName, $ToolInputJson, $CompletionMessage, $FixtureModel, $LogFile

    for ($attempt = 0; $attempt -lt 50; $attempt++) {
        try {
            Invoke-WebRequest -Uri ($prefix.TrimEnd("/") + "/api/version") -UseBasicParsing -TimeoutSec 1 | Out-Null
            Write-SmokeLog "fixture provider ready at $prefix"
            return [pscustomobject]@{
                BaseUrl = $prefix.TrimEnd("/")
                Job = $job
                LogFile = $LogFile
            }
        }
        catch {
            if ($job.State -ne "Running") {
                $output = Receive-Job -Job $job -Keep -ErrorAction SilentlyContinue | Out-String
                throw "Fixture provider exited before readiness. Output: $output"
            }
            Start-Sleep -Milliseconds 100
        }
    }

    Stop-Job -Job $job -ErrorAction SilentlyContinue
    Remove-Job -Job $job -Force -ErrorAction SilentlyContinue
    throw "Fixture provider did not become ready at $prefix"
}

function Stop-FixtureOllamaServer {
    param([object]$Server)

    if ($null -eq $Server) {
        return
    }

    if ($Server.Job.State -eq "Running") {
        Wait-Job -Job $Server.Job -Timeout 5 | Out-Null
    }
    if ($Server.Job.State -eq "Running") {
        Stop-Job -Job $Server.Job -ErrorAction SilentlyContinue
    }

    $jobOutput = Receive-Job -Job $Server.Job -ErrorAction SilentlyContinue | Out-String
    if ($jobOutput.Trim()) {
        Write-SmokeLog "fixture job output: $($jobOutput.Trim())"
    }
    Remove-Job -Job $Server.Job -Force -ErrorAction SilentlyContinue
    Write-SmokeLog "fixture provider log: $($Server.LogFile)"
}

function Write-CodegeistConfig {
    param(
        [string]$ConfigPath,
        [string]$WorkspaceDirectory,
        [string]$ProviderBaseUrl
    )

    New-Directory ([System.IO.Path]::GetDirectoryName($ConfigPath))
    $lines = @(
        "provider:",
        "  ollama:",
        "    type: ollama",
        "    base-url: $ProviderBaseUrl",
        "workspace:",
        "  directory: $(ConvertTo-YamlSingleQuotedString $WorkspaceDirectory)",
        "tools:",
        "  codegeist-shell:",
        "    command-prefix:",
        "      - 'pwsh'",
        "      - '-NoProfile'",
        "      - '-NonInteractive'",
        "      - '-Command'",
        "    default-timeout-seconds: 10"
    )

    [System.IO.File]::WriteAllText($ConfigPath, ($lines -join "`n") + "`n", [System.Text.Encoding]::ASCII)
}

function ConvertTo-ToolInputJson {
    $command = "Set-Content -LiteralPath '$ShellFileName' -Value '$ShellFileContent' -NoNewline; Write-Output '$ShellOutputMarker'"
    $input = [ordered]@{
        command = $command
        cwd = "."
        timeoutSeconds = 10
    }
    return $input | ConvertTo-Json -Depth 8 -Compress
}

function New-AskPrompt {
    param([string]$ToolInputJson)

    return @"
You are running a Codegeist smoke test. Use the $ShellToolName tool exactly once with this exact JSON input:
$ToolInputJson

Do not call any other tool. Do not answer before the tool call completes. After the tool call succeeds, reply exactly $CompletionMessage.
"@
}

function Invoke-CodegeistAsk {
    param(
        [string]$ConfigPath,
        [string]$SessionDirectory,
        [string]$LogFile,
        [string]$Prompt,
        [string]$OutputPrefix,
        [string]$CaseWorkingDirectory
    )

    $stdoutFile = Join-Path $SessionDirectory "$OutputPrefix.out"
    $stderrFile = Join-Path $SessionDirectory "$OutputPrefix.err"
    New-Directory $SessionDirectory
    Remove-Item -LiteralPath $stdoutFile, $stderrFile, $LogFile -Force -ErrorAction SilentlyContinue

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $script:ResolvedArtifactPath
    foreach ($argument in @(
        "-Dcodegeist.config=$ConfigPath",
        "-Dcodegeist.session.directory=$SessionDirectory",
        "ask",
        $Prompt
    )) {
        [void]$startInfo.ArgumentList.Add($argument)
    }

    $startInfo.WorkingDirectory = $CaseWorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.EnvironmentVariables["LOG_FILE"] = $LogFile

    Write-SmokeLog "starting Codegeist ask for $OutputPrefix"
    Write-SmokeLog "stdout: $stdoutFile"
    Write-SmokeLog "stderr: $stderrFile"
    Write-SmokeLog "log: $LogFile"
    $process = [System.Diagnostics.Process]::Start($startInfo)
    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        $process.Kill($true)
        $process.WaitForExit()
        $stdout = $process.StandardOutput.ReadToEnd()
        $stderr = $process.StandardError.ReadToEnd()
        [System.IO.File]::WriteAllText($stdoutFile, $stdout, $Utf8NoBom)
        [System.IO.File]::WriteAllText($stderrFile, $stderr, $Utf8NoBom)
        Write-SmokeLog "timeout stdout saved: $stdoutFile"
        Write-SmokeLog "timeout stderr saved: $stderrFile"
        Fail-Smoke "Codegeist ask timed out after $TimeoutSeconds seconds for $OutputPrefix"
    }

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    [System.IO.File]::WriteAllText($stdoutFile, $stdout, $Utf8NoBom)
    [System.IO.File]::WriteAllText($stderrFile, $stderr, $Utf8NoBom)

    $combined = ($stdout + $stderr).TrimEnd("`r", "`n")
    if ($process.ExitCode -ne 0) {
        Fail-Smoke "Codegeist ask failed with exit code $($process.ExitCode) for ${OutputPrefix}: $combined"
    }

    Write-SmokeLog "Codegeist ask exit 0 for $OutputPrefix with output: $combined"

    return $combined
}

function Assert-CompletedShellToolPart {
    param(
        [string]$SessionDirectory,
        [string]$CaseName
    )

    $storePath = Join-Path $SessionDirectory "session.json"
    Write-SmokeLog "$CaseName checking session store $storePath"
    if (-not (Test-Path -LiteralPath $storePath)) {
        Fail-Smoke "$CaseName did not write session store: $storePath"
    }

    $store = Get-Content -LiteralPath $storePath -Raw | ConvertFrom-Json
    $toolParts = @($store.sessions) |
        ForEach-Object { @($_.messages) } |
        Where-Object { $_.role -eq "ASSISTANT" } |
        ForEach-Object { @($_.parts) } |
        Where-Object { $_.type -eq "tool" -and $_.tool -eq $ShellToolName }

    if (-not $toolParts -or @($toolParts).Count -eq 0) {
        Fail-Smoke "$CaseName did not persist a $ShellToolName ToolSessionPart"
    }

    $completedParts = @($toolParts) | Where-Object { $_.status -eq "completed" }
    if (-not $completedParts -or @($completedParts).Count -eq 0) {
        Fail-Smoke "$CaseName did not persist a completed $ShellToolName ToolSessionPart"
    }

    $preview = [string]$completedParts[0].outputPreview
    if (-not $preview.Contains("Exit code: 0") -or -not $preview.Contains($ShellOutputMarker)) {
        Fail-Smoke "$CaseName persisted an unexpected shell preview: $preview"
    }
    Write-SmokeLog "$CaseName found completed $ShellToolName ToolSessionPart"
}

function Invoke-SmokeCase {
    $caseName = "shell-tool"
    $caseRoot = Join-Path $script:ResolvedSmokeRoot $caseName
    $workspace = Join-Path $caseRoot "workspace"
    $sessionDirectory = Join-Path $caseRoot "session"
    $configPath = Join-Path $caseRoot "codegeist.yml"
    $logFile = Join-Path $caseRoot "codegeist.log"
    $shellOutputFile = Join-Path $workspace $ShellFileName

    New-Directory $workspace
    $toolInputJson = ConvertTo-ToolInputJson
    $fixtureServer = Start-FixtureOllamaServer $toolInputJson (Join-Path $caseRoot "fixture-provider.log")

    try {
        Write-CodegeistConfig $configPath $workspace $fixtureServer.BaseUrl
        Write-SmokeLog "$caseName config: $configPath"
        Write-SmokeLog "$caseName workspace: $workspace"
        Write-SmokeLog "$caseName provider: $($fixtureServer.BaseUrl) (fixture)"

        $prompt = New-AskPrompt $toolInputJson
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        [void](Invoke-CodegeistAsk $configPath $sessionDirectory $logFile $prompt $caseName $script:ResolvedWorkingDirectory)
        Write-SmokeDuration "$LabelPrefix shell ask smoke" $stopwatch

        if (-not (Test-Path -LiteralPath $shellOutputFile)) {
            Fail-Smoke "$caseName did not create shell output file: $shellOutputFile"
        }
        $actual = [System.IO.File]::ReadAllText($shellOutputFile, $Utf8NoBom)
        if ($actual -ne $ShellFileContent) {
            Fail-Smoke "$caseName expected shell output file content $ShellFileContent but got $actual"
        }
        Write-SmokeLog "$caseName file assertion passed"
        Assert-CompletedShellToolPart $sessionDirectory $caseName
    }
    finally {
        Stop-FixtureOllamaServer $fixtureServer
    }
}

$script:ResolvedArtifactPath = Resolve-SmokePath $ArtifactPath
if (-not (Test-Path -LiteralPath $script:ResolvedArtifactPath)) {
    Fail-Smoke "Artifact does not exist: $script:ResolvedArtifactPath"
}

$script:ResolvedSmokeRoot = Resolve-SmokePath $SmokeRoot
if ($WorkingDirectory) {
    $script:ResolvedWorkingDirectory = Resolve-SmokePath $WorkingDirectory
}
else {
    $script:ResolvedWorkingDirectory = (Get-Location).Path
}

Remove-Item -Recurse -Force -LiteralPath $script:ResolvedSmokeRoot -ErrorAction SilentlyContinue
New-Directory $script:ResolvedSmokeRoot
Write-SmokeLog "using deterministic fixture provider"

$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
Invoke-SmokeCase
Write-SmokeDuration "$LabelPrefix shell ask total" $totalStopwatch
Write-Host "Shell ask smoke passed: $LabelPrefix"

# file-edit-ask-smoke.ps1 - cross-platform ask-driven file-edit smoke harness.
#
# Why this exists:
# - Runs the same file-edit smoke contract on Linux, Windows, and macOS without
#   adding smoke-only Codegeist commands.
# - Exercises the real `ask` command and provider tool-calling path, then verifies
#   file bytes and persisted `ToolSessionPart` output instead of trusting the model
#   response text.
#
# Inputs:
# - ArtifactPath: path to the packaged native `codegeist` binary.
# - SmokeRoot: disposable directory for fixtures, config, logs, and sessions.
# - ProviderMode: `fixture` starts a deterministic local Ollama-compatible server;
#   `ollama` points at an existing Ollama endpoint for exploratory checks.
#
# Related files:
# - scripts/tests/artifact-smoke.ps1
# - scripts/tests/local-linux-smoke.ps1
# - scripts/tests/native-smoke.ps1
# - scripts/tests/windows-smoke.ps1

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ArtifactPath,

    [Parameter(Mandatory = $true)]
    [string]$SmokeRoot,

    [ValidateSet("fixture", "ollama")]
    [string]$ProviderMode = "fixture",

    [string]$OllamaBaseUrl = "http://localhost:11434",

    [int]$TimeoutSeconds = 90,

    [string]$LabelPrefix = "file-edit ask",

    [string]$WorkingDirectory = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$EditToolName = "codegeist_edit"
$CompletionMessage = "FILE_EDIT_ASK_SMOKE_DONE"
$FixtureModel = "codegeist-fixture"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Utf8Bom = [byte[]](0xef, 0xbb, 0xbf)
$Latin1 = [System.Text.Encoding]::GetEncoding("ISO-8859-1")

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

    Write-Host "[file-edit-ask-smoke] $Message"
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
    } -ArgumentList $prefix, $EditToolName, $ToolInputJson, $CompletionMessage, $FixtureModel, $LogFile

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
        [string]$ProviderBaseUrl,
        [string]$EncodingName = ""
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
        "  codegeist-edit:",
        "    diff-preview-lines: 1",
        "    diff-preview-chars: 120"
    )
    if ($EncodingName) {
        $workspaceIndex = [Array]::IndexOf($lines, "workspace:")
        $before = if ($workspaceIndex -ge 0) { $lines[0..($workspaceIndex + 1)] } else { @() }
        $after = if ($workspaceIndex + 2 -lt $lines.Count) { $lines[($workspaceIndex + 2)..($lines.Count - 1)] } else { @() }
        $lines = @($before + "  encoding: $EncodingName" + $after)
    }

    [System.IO.File]::WriteAllText($ConfigPath, ($lines -join "`n") + "`n", [System.Text.Encoding]::ASCII)
}

function New-Utf8Bytes {
    param([string]$Text)

    return $Utf8NoBom.GetBytes($Text)
}

function New-Latin1Bytes {
    param([string]$Text)

    return $Latin1.GetBytes($Text)
}

function Add-Utf8Bom {
    param([byte[]]$Bytes)

    $result = [byte[]]::new($Utf8Bom.Length + $Bytes.Length)
    [System.Array]::Copy($Utf8Bom, 0, $result, 0, $Utf8Bom.Length)
    [System.Array]::Copy($Bytes, 0, $result, $Utf8Bom.Length, $Bytes.Length)
    return $result
}

function Assert-BytesEqual {
    param(
        [string]$Path,
        [byte[]]$Expected,
        [string]$Label
    )

    $actual = [System.IO.File]::ReadAllBytes($Path)
    if ($actual.Length -ne $Expected.Length) {
        Fail-Smoke "$Label expected $($Expected.Length) bytes, got $($actual.Length) bytes in $Path"
    }

    for ($index = 0; $index -lt $Expected.Length; $index++) {
        if ($actual[$index] -ne $Expected[$index]) {
            Fail-Smoke "$Label byte mismatch at offset $index in $Path"
        }
    }
}

function ConvertTo-ToolInputJson {
    param(
        [string]$Path,
        [string]$OldText,
        [string]$NewText
    )

    $input = [ordered]@{
        path = $Path
        edits = @(
            [ordered]@{
                oldText = $OldText
                newText = $NewText
            }
        )
    }
    return $input | ConvertTo-Json -Depth 8 -Compress
}

function New-AskPrompt {
    param(
        [string]$ToolInputJson
    )

    return @"
You are running a Codegeist smoke test. Use the $EditToolName tool exactly once with this exact JSON input:
$ToolInputJson

The target file already exists. Do not call codegeist_write. Do not answer before the tool call completes. After the tool call succeeds, reply exactly $CompletionMessage.
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

function Assert-CompletedEditToolPart {
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
        Where-Object { $_.type -eq "tool" -and $_.tool -eq $EditToolName }

    if (-not $toolParts -or @($toolParts).Count -eq 0) {
        Fail-Smoke "$CaseName did not persist a $EditToolName ToolSessionPart"
    }

    $completedParts = @($toolParts) | Where-Object { $_.status -eq "completed" }
    if (-not $completedParts -or @($completedParts).Count -eq 0) {
        Fail-Smoke "$CaseName did not persist a completed $EditToolName ToolSessionPart"
    }

    $preview = [string]$completedParts[0].outputPreview
    if (-not $preview.Contains("Operation: edit") -or -not $preview.Contains("Replacements: 1")) {
        Fail-Smoke "$CaseName persisted an unexpected edit preview: $preview"
    }
    Write-SmokeLog "$CaseName found completed $EditToolName ToolSessionPart"
}

function Invoke-SmokeCase {
    param(
        [hashtable]$Case
    )

    $caseName = [string]$Case.Name
    $caseRoot = Join-Path $script:ResolvedSmokeRoot $caseName
    $workspace = Join-Path $caseRoot "workspace"
    $sessionDirectory = Join-Path $caseRoot "session"
    $configPath = Join-Path $caseRoot "codegeist.yml"
    $logFile = Join-Path $caseRoot "codegeist.log"
    $targetPath = Join-Path $workspace ([string]$Case.Path)

    New-Directory $workspace
    [System.IO.File]::WriteAllBytes($targetPath, [byte[]]$Case.InitialBytes)
    $toolInputJson = ConvertTo-ToolInputJson ([string]$Case.Path) ([string]$Case.OldText) ([string]$Case.NewText)
    $fixtureServer = $null
    $providerBaseUrl = $OllamaBaseUrl
    if ($ProviderMode -eq "fixture") {
        $fixtureServer = Start-FixtureOllamaServer $toolInputJson (Join-Path $caseRoot "fixture-provider.log")
        $providerBaseUrl = $fixtureServer.BaseUrl
    }

    try {
        Write-CodegeistConfig $configPath $workspace $providerBaseUrl ([string]$Case.EncodingName)
        Write-SmokeLog "$caseName config: $configPath"
        Write-SmokeLog "$caseName workspace: $workspace"
        Write-SmokeLog "$caseName target: $targetPath"
        Write-SmokeLog "$caseName provider: $providerBaseUrl ($ProviderMode)"

        $prompt = New-AskPrompt $toolInputJson
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        [void](Invoke-CodegeistAsk $configPath $sessionDirectory $logFile $prompt $caseName $script:ResolvedWorkingDirectory)
        Write-SmokeDuration "$LabelPrefix file-edit ask $caseName smoke" $stopwatch

        Assert-BytesEqual $targetPath ([byte[]]$Case.ExpectedBytes) "$caseName file edit"
        Write-SmokeLog "$caseName byte assertion passed"
        Assert-CompletedEditToolPart $sessionDirectory $caseName
    }
    finally {
        Stop-FixtureOllamaServer $fixtureServer
    }
}

function Assert-OllamaReachable {
    $versionUrl = $OllamaBaseUrl.TrimEnd("/") + "/api/version"
    try {
        Invoke-WebRequest -Uri $versionUrl -UseBasicParsing -TimeoutSec 30 | Out-Null
    }
    catch {
        Fail-Smoke "Ollama is not reachable at ${OllamaBaseUrl}: $($_.Exception.Message)"
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
if ($ProviderMode -eq "ollama") {
    Write-SmokeLog "checking external Ollama provider at $OllamaBaseUrl"
    Assert-OllamaReachable
}
else {
    Write-SmokeLog "using deterministic fixture provider"
}

$uUmlaut = [string][char]0x00fc
$aUmlaut = [string][char]0x00e4
$eszett = [string][char]0x00df
$checkMark = [string][char]0x2713
$emoji = [char]::ConvertFromUtf32(0x1f600)

$gruesse = "Gr${uUmlaut}${eszett}e"
$strasse = "Stra${eszett}e"
$fussgaenger = "Fu${eszett}g${aUmlaut}nger"

$cases = @(
    @{
        Name = "utf8-lf"
        Path = "utf8-lf.txt"
        EncodingName = ""
        InitialBytes = New-Utf8Bytes "alpha`n$strasse`nomega`n"
        ExpectedBytes = New-Utf8Bytes "alpha`n$fussgaenger`nomega`n"
        OldText = "$strasse`n"
        NewText = "$fussgaenger`n"
    },
    @{
        Name = "utf8-bom-crlf"
        Path = "utf8-bom-crlf.txt"
        EncodingName = ""
        InitialBytes = Add-Utf8Bom (New-Utf8Bytes "start`r`nemoji $emoji`r`nend`r`n")
        ExpectedBytes = Add-Utf8Bom (New-Utf8Bytes "start`r`ncheck $checkMark`r`nend`r`n")
        OldText = "emoji $emoji`n"
        NewText = "check $checkMark`n"
    },
    @{
        Name = "no-final-newline"
        Path = "no-final-newline.txt"
        EncodingName = ""
        InitialBytes = New-Utf8Bytes "first`nlast"
        ExpectedBytes = New-Utf8Bytes "first`nLAST"
        OldText = "last"
        NewText = "LAST"
    },
    @{
        Name = "latin1-crlf"
        Path = "latin1-crlf.txt"
        EncodingName = "ISO-8859-1"
        InitialBytes = New-Latin1Bytes "alpha`r`n$gruesse`r`nomega`r`n"
        ExpectedBytes = New-Latin1Bytes "alpha`r`n$strasse`r`nomega`r`n"
        OldText = "$gruesse`n"
        NewText = "$strasse`n"
    }
)

$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
foreach ($case in $cases) {
    Write-SmokeLog "case start: $($case.Name)"
    Invoke-SmokeCase $case
    Write-SmokeLog "case passed: $($case.Name)"
}
Write-SmokeDuration "$LabelPrefix file-edit ask smoke" $totalStopwatch
Write-Host "File edit ask smoke passed: $LabelPrefix"

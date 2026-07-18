# tui-hello-world-smoke.ps1 - native TUI hello-world tool smoke with video capture.
#
# Why this exists:
# - Drives the real native `codegeist tui` command through Charmbracelet VHS so the
#   recorded terminal video and the behavioral assertions come from the same run.
# - Uses a deterministic localhost Ollama-compatible fixture provider so the native
#   TUI, agent loop, local tools, video capture, and assertions are reproducible.
# - Verifies the concrete side effects after recording: `hello-world.sh` exists,
#   `sh hello-world.sh` prints `Hello World`, and the configured session store
#   records completed `codegeist_write` and `codegeist_shell` tool parts.
#
# Inputs:
# - Run from anywhere in the repository checkout.
# - Optional BuildNative compiles app/codegeist/cli/target/codegeist first.
# - Optional NativeExecutable points at an existing native binary.
# - Optional ReadmeGifPath controls where the README GIF preview is written.
#
# Side effects:
# - Writes smoke artifacts under app/codegeist/cli/target/smoke-test/tui-hello-world.
# - Refreshes docs/user/assets/tui/tui-hello-world.gif by default.
# - Starts a temporary localhost-only Ollama-compatible fixture provider.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - scripts/tests/smoke-common.ps1
# - scripts/tests/tui-capture-smoke.ps1

[CmdletBinding()]
param(
    [string]$CliDir = "",

    [string]$SmokeRoot = "",

    [string]$NativeExecutable = "",

    [string]$ReadmeGifPath = "",

    [int]$TimeoutSeconds = 0,

    [switch]$BuildNative
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

$PromptText = "Create a shell script named hello-world.sh that prints Hello World using echo, then run it with sh hello-world.sh."
$CompletionMessage = "Hello World script created and executed."
$ExpectedShellOutput = "Hello World"
$ScriptFileName = "hello-world.sh"
$WriteToolName = "codegeist_write"
$ShellToolName = "codegeist_shell"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Fail-Smoke {
    param([string]$Message)

    throw $Message
}

function Write-SmokeLog {
    param([string]$Message)

    Write-Host "[tui-hello-world-smoke] $Message"
}

function Assert-VhsAvailable {
    if (-not (Get-Command vhs -ErrorAction SilentlyContinue)) {
        Fail-Smoke "vhs is required for native TUI video capture. Install Charmbracelet VHS and rerun this smoke."
    }
    if (-not (Get-Command ffmpeg -ErrorAction SilentlyContinue)) {
        Fail-Smoke "ffmpeg is required by VHS for MP4/WebM output. Install ffmpeg and rerun this smoke."
    }
    if (-not (Get-Command ttyd -ErrorAction SilentlyContinue)) {
        Fail-Smoke "ttyd is required by VHS for terminal capture. Install ttyd and rerun this smoke."
    }
}

function ConvertTo-YamlSingleQuotedString {
    param([string]$Value)

    return "'" + $Value.Replace("'", "''") + "'"
}

function ConvertTo-ShellSingleQuotedString {
    param([string]$Value)

    $singleQuote = "'"
    $escapedSingleQuote = "'`"'`"'"
    return $singleQuote + $Value.Replace($singleQuote, $escapedSingleQuote) + $singleQuote
}

function ConvertTo-VhsDoubleQuotedString {
    param([string]$Value)

    return '"' + $Value.Replace('`', '``').Replace('"', '`"') + '"'
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
    param([string]$LogFile)

    $port = Get-FreeTcpPort
    $prefix = "http://127.0.0.1:$port/"
    New-SmokeDirectory ([System.IO.Path]::GetDirectoryName($LogFile))
    Set-Content -LiteralPath $LogFile -Encoding UTF8 -Value "fixture starting at $prefix"

    $job = Start-Job -ScriptBlock {
        param(
            [string]$Prefix,
            [string]$WriteToolName,
            [string]$ShellToolName,
            [string]$CompletionMessage,
            [string]$ScriptFileName,
            [string]$ExpectedShellOutput,
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
                        model = "codegeist-fixture"
                        created_at = [DateTimeOffset]::UtcNow.ToString("o")
                        message = [ordered]@{
                            role = "assistant"
                            content = ""
                            tool_calls = @(
                                [ordered]@{
                                    id = "call-write"
                                    function = [ordered]@{
                                        name = $WriteToolName
                                        arguments = [ordered]@{
                                            path = $ScriptFileName
                                            content = "echo `"$ExpectedShellOutput`"`n"
                                        }
                                    }
                                },
                                [ordered]@{
                                    id = "call-shell"
                                    function = [ordered]@{
                                        name = $ShellToolName
                                        arguments = [ordered]@{
                                            command = "sh $ScriptFileName"
                                            cwd = "."
                                            timeoutSeconds = 15
                                        }
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
                        model = "codegeist-fixture"
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
    } -ArgumentList $prefix, $WriteToolName, $ShellToolName, $CompletionMessage, $ScriptFileName, $ExpectedShellOutput, $LogFile

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

    New-SmokeDirectory ([System.IO.Path]::GetDirectoryName($ConfigPath))
    $lines = @(
        "provider:",
        "  ollama:",
        "    type: ollama",
        "    base-url: $ProviderBaseUrl",
        "workspace:",
        "  directory: $(ConvertTo-YamlSingleQuotedString $WorkspaceDirectory)",
        "tools:",
        "  codegeist-shell:",
        "    default-timeout-seconds: 15"
    )
    [System.IO.File]::WriteAllText($ConfigPath, ($lines -join "`n") + "`n", $Utf8NoBom)
}

function Write-VhsTape {
    param(
        [string]$Path,
        [string]$ArtifactPath,
        [string]$ConfigPath,
        [string]$SessionDirectory,
        [string]$LogFile,
        [string]$Mp4Path,
        [string]$WebmPath
    )

    $command = @(
        "LOG_FILE=$(ConvertTo-ShellSingleQuotedString $LogFile)",
        "TERM=xterm-256color",
        "COLUMNS=100",
        "LINES=30",
        $(ConvertTo-ShellSingleQuotedString $ArtifactPath),
        $(ConvertTo-ShellSingleQuotedString "-Dcodegeist.config=$ConfigPath"),
        $(ConvertTo-ShellSingleQuotedString "-Dcodegeist.session.directory=$SessionDirectory"),
        "tui"
    ) -join " "

    $lines = @(
        "Output $(ConvertTo-VhsDoubleQuotedString $Mp4Path)",
        "Output $(ConvertTo-VhsDoubleQuotedString $WebmPath)",
        "Set Shell bash",
        "Set FontSize 18",
        'Set FontFamily "DejaVu Sans Mono"',
        "Set Width 1100",
        "Set Height 720",
        "Set TypingSpeed 12ms",
        "Set WaitTimeout $($script:ResolvedTimeoutSeconds)s",
        "Hide",
        "Type $(ConvertTo-VhsDoubleQuotedString $command)",
        "Enter",
        "Wait+Screen@30s /Enter a prompt below/",
        "Show",
        "Sleep 500ms",
        "Type $(ConvertTo-VhsDoubleQuotedString $PromptText)",
        "Sleep 250ms",
        "Enter",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Tool: codegeist_write completed/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Created file: hello-world\.sh/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Tool: codegeist_shell completed/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Command: sh hello-world\.sh/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Exit code: 0/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /Hello World +[^A-Za-z0-9 ]/",
        "Wait+Screen@$($script:ResolvedTimeoutSeconds)s /$CompletionMessage/",
        "Sleep 1500ms",
        "Ctrl+Q",
        "Sleep 500ms"
    )
    [System.IO.File]::WriteAllText($Path, ($lines -join "`n") + "`n", $Utf8NoBom)
}

function Invoke-NativeTuiRecording {
    param(
        [string]$WorkspaceDirectory,
        [string]$TapePath,
        [string]$VhsOutput
    )

    Push-Location -LiteralPath $WorkspaceDirectory
    try {
        & vhs $TapePath *> $VhsOutput
        if ($LASTEXITCODE -ne 0) {
            Fail-Smoke "VHS-driven native TUI hello-world smoke failed with exit code $LASTEXITCODE; see $VhsOutput"
        }
    }
    finally {
        Pop-Location
    }
}

function Assert-VideoArtifacts {
    param(
        [string]$TapePath,
        [string]$Mp4Path,
        [string]$WebmPath,
        [string]$VhsOutput,
        [string]$GifPath
    )

    foreach ($artifactPath in @($TapePath, $Mp4Path, $WebmPath, $VhsOutput, $GifPath)) {
        if (-not (Test-Path -LiteralPath $artifactPath) -or (Get-Item -LiteralPath $artifactPath).Length -eq 0) {
            Fail-Smoke "Expected TUI hello-world artifact was not written: $artifactPath"
        }
    }

}

function Invoke-GifGeneration {
    param(
        [string]$SourceMp4Path,
        [string]$GifPath,
        [string]$PalettePath,
        [string]$LogPath
    )

    New-SmokeDirectory ([System.IO.Path]::GetDirectoryName($GifPath))
    $paletteFilter = "fps=10,scale=900:-1:flags=lanczos,palettegen=max_colors=96"
    $gifFilter = "fps=10,scale=900:-1:flags=lanczos[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=5"

    & ffmpeg -y -i $SourceMp4Path -vf $paletteFilter $PalettePath *> $LogPath
    if ($LASTEXITCODE -ne 0) {
        Fail-Smoke "Failed to generate README GIF palette with ffmpeg; see $LogPath"
    }

    & ffmpeg -y -i $SourceMp4Path -i $PalettePath -lavfi $gifFilter $GifPath *>> $LogPath
    if ($LASTEXITCODE -ne 0) {
        Fail-Smoke "Failed to generate README GIF with ffmpeg; see $LogPath"
    }
}

function Assert-HelloWorldWorkspace {
    param([string]$WorkspaceDirectory)

    $scriptPath = Join-Path $WorkspaceDirectory $ScriptFileName
    if (-not (Test-Path -LiteralPath $scriptPath)) {
        Fail-Smoke "The TUI run did not create $ScriptFileName in $WorkspaceDirectory"
    }

    $content = [System.IO.File]::ReadAllText($scriptPath, $Utf8NoBom)
    if (-not $content.Contains("echo") -or -not $content.Contains("Hello") -or -not $content.Contains("World")) {
        Fail-Smoke "$ScriptFileName should use echo to print $ExpectedShellOutput. Actual content: $content"
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo.FileName = "sh"
    $process.StartInfo.ArgumentList.Add($ScriptFileName)
    $process.StartInfo.WorkingDirectory = $WorkspaceDirectory
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.RedirectStandardError = $true
    [void]$process.Start()
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    if (-not $process.WaitForExit(10000)) {
        $process.Kill($true)
        $process.WaitForExit()
        $process.Dispose()
        Fail-Smoke "Timed out while verifying sh $ScriptFileName"
    }
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $exitCode = $process.ExitCode
    $process.Dispose()

    $expectedStdout = $ExpectedShellOutput + [Environment]::NewLine
    if ($exitCode -ne 0 -or $stdout -ne $expectedStdout -or $stderr) {
        Fail-Smoke "Expected sh $ScriptFileName to print exactly $ExpectedShellOutput on stdout with exit 0 and empty stderr, got exit ${exitCode}, stdout: $stdout, stderr: $stderr"
    }
}

function Assert-SessionStoreToolParts {
    param([string]$SessionDirectory)

    $storePath = Join-Path $SessionDirectory "session.json"
    if (-not (Test-Path -LiteralPath $storePath)) {
        Fail-Smoke "The TUI run did not write session store: $storePath"
    }

    $store = Get-Content -LiteralPath $storePath -Raw | ConvertFrom-Json
    $sessions = @($store.sessions)
    if ($sessions.Count -ne 1) {
        Fail-Smoke "Session store should contain exactly one TUI smoke session"
    }
    Assert-JsonObjectProperties `
        -Object $store `
        -ExpectedProperties @("schemaVersion", "workingDir", "createdAt", "updatedAt", "sessions") `
        -Description "session store"
    Assert-JsonObjectProperties `
        -Object $sessions[0] `
        -ExpectedProperties @("id", "title", "createdAt", "updatedAt", "messages") `
        -Description "session"

    $messages = @(@($store.sessions) | ForEach-Object { @($_.messages) })
    if ($messages.Count -ne 2) {
        Fail-Smoke "Session store should contain exactly one user and one assistant message"
    }
    $userMessages = @($messages | Where-Object { $_.role -eq "user" })
    $assistantMessages = @($messages | Where-Object { $_.role -eq "assistant" })
    if ($userMessages.Count -ne 1 -or $assistantMessages.Count -ne 1) {
        Fail-Smoke "Session store should contain exactly one user and one assistant role"
    }
    Assert-JsonObjectProperties `
        -Object $userMessages[0] `
        -ExpectedProperties @("id", "role", "createdAt", "parts") `
        -Description "user message"
    Assert-JsonObjectProperties `
        -Object $assistantMessages[0] `
        -ExpectedProperties @("id", "role", "createdAt", "completedAt", "parentMessageId", "parts") `
        -Description "assistant message"

    $userParts = @($userMessages[0].parts)
    if ($userParts.Count -ne 1 -or $userParts[0].type -ne "text") {
        Fail-Smoke "User message should contain exactly one text session part"
    }
    $userTextParts = @($userParts |
        Where-Object { $_.type -eq "text" -and $_.text -eq $PromptText })
    if ($userTextParts.Count -ne 1) {
        Fail-Smoke "Session store should contain exactly one user text part with the submitted TUI prompt"
    }

    $assistantParts = @($assistantMessages[0].parts)
    if ($assistantParts.Count -ne 3 -or
        $assistantParts[0].type -ne "tool" -or
        $assistantParts[1].type -ne "tool" -or
        $assistantParts[2].type -ne "text") {
        Fail-Smoke "Assistant message should contain exactly two tool parts followed by one text part"
    }
    $toolParts = @($assistantParts | Where-Object { $_.type -eq "tool" })

    if ($toolParts.Count -ne 2 -or
        $toolParts[0].tool -ne $WriteToolName -or
        $toolParts[1].tool -ne $ShellToolName) {
        Fail-Smoke "Session store should contain ordered $WriteToolName then $ShellToolName tool parts"
    }

    Assert-CompletedToolPart $toolParts $WriteToolName @($ScriptFileName)
    Assert-CompletedToolPart $toolParts $ShellToolName @(
        "Command: sh $ScriptFileName",
        "Exit code: 0",
        $ExpectedShellOutput)

    $assistantTextParts = @($assistantParts |
        Where-Object { $_.type -eq "text" -and $_.text -eq $CompletionMessage })
    if ($assistantTextParts.Count -ne 1) {
        Fail-Smoke "Session store should contain exactly one final assistant text part"
    }

    foreach ($textPart in @($userTextParts) + @($assistantTextParts)) {
        Assert-JsonObjectProperties `
            -Object $textPart `
            -ExpectedProperties @("type", "id", "text") `
            -Description "text session part"
    }
    foreach ($toolPart in $toolParts) {
        Assert-JsonObjectProperties `
            -Object $toolPart `
            -ExpectedProperties @("type", "id", "tool", "status", "outputPreview") `
            -Description "tool session part"
    }
}

function Assert-JsonObjectProperties {
    param(
        [object]$Object,
        [string[]]$ExpectedProperties,
        [string]$Description
    )

    $actualProperties = @($Object.PSObject.Properties.Name | Sort-Object)
    $sortedExpectedProperties = @($ExpectedProperties | Sort-Object)
    $differences = @(Compare-Object $sortedExpectedProperties $actualProperties)
    if ($differences.Count -gt 0) {
        $actualText = $actualProperties -join ", "
        $expectedText = $sortedExpectedProperties -join ", "
        Fail-Smoke "Unexpected $Description properties. Expected: $expectedText. Actual: $actualText"
    }
}

function Assert-CompletedToolPart {
    param(
        [object[]]$ToolParts,
        [string]$ToolName,
        [string[]]$ExpectedPreviewText
    )

    $matchingParts = @($ToolParts) | Where-Object { $_.tool -eq $ToolName -and $_.status -eq "completed" }
    if (-not $matchingParts -or @($matchingParts).Count -eq 0) {
        Fail-Smoke "Session store does not contain a completed $ToolName ToolSessionPart"
    }

    $preview = [string]$matchingParts[0].outputPreview
    foreach ($expectedText in $ExpectedPreviewText) {
        if (-not $preview.Contains($expectedText)) {
            Fail-Smoke "$ToolName ToolSessionPart preview did not contain $expectedText. Preview: $preview"
        }
    }
}

function Write-RunSummary {
    param(
        [string]$CaptureRoot,
        [string]$WorkspaceDirectory,
        [string]$SessionDirectory,
        [string]$Mp4Path,
        [string]$WebmPath,
        [string]$GifPath
    )

    $summary = @"
# Native TUI Hello World Smoke

Generated by scripts/tests/tui-hello-world-smoke.ps1 while driving the native
codegeist tui command through Charmbracelet VHS.

## Prompt

$PromptText

## Verified Behavior

- The native TUI accepted the prompt through the real TerminalUI surface.
- The fixture Ollama provider selected $WriteToolName to create $ScriptFileName.
- The fixture Ollama provider selected $ShellToolName to run sh $ScriptFileName.
- The recorded TUI transcript displayed the completed shell command and exit code.
- $ScriptFileName prints exactly $ExpectedShellOutput when run with sh.
- The configured session/session.json store contains completed tool parts for $WriteToolName and $ShellToolName.
- The README GIF preview was regenerated from the recorded MP4.

## Artifacts

- MP4: $Mp4Path
- WebM: $WebmPath
- GIF: $GifPath
- Workspace: $WorkspaceDirectory
- Session directory: $SessionDirectory

These artifacts are build output intended as reproducible evidence for later demo or
video-generation work. This smoke does not create a storyboard, voiceover, or video
script.
"@
    [System.IO.File]::WriteAllText((Join-Path $CaptureRoot "run-summary.md"), $summary, $Utf8NoBom)
}

if (-not $CliDir) {
    $CliDir = Join-Path $PSScriptRoot "../../app/codegeist/cli"
}
$script:ResolvedCliDir = Resolve-SmokePath $CliDir
if (-not (Test-Path -LiteralPath $script:ResolvedCliDir)) {
    Fail-Smoke "CLI module directory not found: $script:ResolvedCliDir"
}

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $script:ResolvedCliDir "target/smoke-test"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot
$RepoRoot = Resolve-SmokePath (Join-Path $PSScriptRoot "../..")
$CaptureRoot = Join-Path $SmokeRoot "tui-hello-world"
$WorkspaceDirectory = Join-Path $CaptureRoot "workspace"
$SessionDirectory = Join-Path $CaptureRoot "session"
$ConfigPath = Join-Path $CaptureRoot "codegeist.yml"
$LogFile = Join-Path $CaptureRoot "codegeist.log"
$FixtureLogFile = Join-Path $CaptureRoot "fixture-provider.log"
$TapePath = Join-Path $CaptureRoot "drive-tui-hello-world.tape"
$Mp4Path = Join-Path $CaptureRoot "tui-hello-world.mp4"
$WebmPath = Join-Path $CaptureRoot "tui-hello-world.webm"
$VhsOutput = Join-Path $CaptureRoot "vhs-output.log"
$GifPalettePath = Join-Path $CaptureRoot "tui-hello-world-palette.png"
$GeneratedGifPath = Join-Path $CaptureRoot "tui-hello-world.gif"
$GifOutput = Join-Path $CaptureRoot "gif-output.log"
if (-not $ReadmeGifPath) {
    $ReadmeGifPath = Join-Path $RepoRoot "docs/user/assets/tui/tui-hello-world.gif"
}
$ReadmeGifPath = Resolve-SmokePath $ReadmeGifPath

$script:ResolvedTimeoutSeconds = Get-SmokeSeconds $TimeoutSeconds @("CODEGEIST_TUI_HELLO_WORLD_TIMEOUT_SECONDS") 180
Assert-VhsAvailable

if (-not $NativeExecutable) {
    $binaryName = if ($IsWindows) { "codegeist.exe" } else { "codegeist" }
    $NativeExecutable = Join-Path $script:ResolvedCliDir "target/$binaryName"
}
$NativeExecutable = Resolve-SmokePath $NativeExecutable

Push-Location -LiteralPath $script:ResolvedCliDir
try {
    if ($BuildNative) {
        Invoke-SmokeStep `
            "mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile" `
            "tui hello-world native compile" `
            { & mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile }
    }
}
finally {
    Pop-Location
}

if (-not (Test-Path -LiteralPath $NativeExecutable)) {
    Fail-Smoke "Native executable was not found: $NativeExecutable"
}

Remove-Item -Recurse -Force -LiteralPath $CaptureRoot -ErrorAction SilentlyContinue
New-SmokeDirectory $CaptureRoot
New-SmokeDirectory $WorkspaceDirectory
New-SmokeDirectory $SessionDirectory

$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

$fixtureServer = $null
try {
    $fixtureStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $fixtureServer = Start-FixtureOllamaServer $FixtureLogFile
    Write-SmokeDuration "tui hello-world fixture start" $fixtureStopwatch

    Write-CodegeistConfig $ConfigPath $WorkspaceDirectory $fixtureServer.BaseUrl
    Write-SmokeLog "native executable: $NativeExecutable"
    Write-SmokeLog "capture root: $CaptureRoot"
    Write-SmokeLog "config: $ConfigPath"
    Write-SmokeLog "workspace: $WorkspaceDirectory"
    Write-SmokeLog "session: $SessionDirectory"
    Write-SmokeLog "readme gif: $ReadmeGifPath"
    Write-SmokeLog "provider: $($fixtureServer.BaseUrl) (fixture)"

    Write-VhsTape `
        -Path $TapePath `
        -ArtifactPath $NativeExecutable `
        -ConfigPath $ConfigPath `
        -SessionDirectory $SessionDirectory `
        -LogFile $LogFile `
        -Mp4Path $Mp4Path `
        -WebmPath $WebmPath

    $recordingStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    Invoke-NativeTuiRecording `
        -WorkspaceDirectory $WorkspaceDirectory `
        -TapePath $TapePath `
        -VhsOutput $VhsOutput
    Write-SmokeDuration "tui hello-world native recording" $recordingStopwatch

    $gifStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    Invoke-GifGeneration `
        -SourceMp4Path $Mp4Path `
        -GifPath $GeneratedGifPath `
        -PalettePath $GifPalettePath `
        -LogPath $GifOutput
    Write-SmokeDuration "tui hello-world gif generation" $gifStopwatch

    $assertionStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    Assert-VideoArtifacts $TapePath $Mp4Path $WebmPath $VhsOutput $GeneratedGifPath
    Assert-HelloWorldWorkspace $WorkspaceDirectory
    Assert-SessionStoreToolParts $SessionDirectory
    New-SmokeDirectory ([System.IO.Path]::GetDirectoryName($ReadmeGifPath))
    Copy-Item -LiteralPath $GeneratedGifPath -Destination $ReadmeGifPath -Force
    Write-RunSummary $CaptureRoot $WorkspaceDirectory $SessionDirectory $Mp4Path $WebmPath $ReadmeGifPath
    Write-SmokeDuration "tui hello-world assertions" $assertionStopwatch
}
finally {
    Stop-FixtureOllamaServer $fixtureServer
}

Write-SmokeDuration "tui hello-world smoke total" $totalStopwatch
Write-Host "TUI hello-world smoke status: passed"
Write-Host "TUI hello-world artifact root: $CaptureRoot"
Write-Host "TUI hello-world smoke passed: $CaptureRoot"

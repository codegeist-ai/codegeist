# tui-capture-smoke.ps1 - native TUI documentation capture smoke.
#
# Why this exists:
# - Proves the native Codegeist `tui` command can render a prompt, submit a turn,
#   receive a deterministic provider response, persist the session, and exit through
#   Ctrl-Q from a real terminal recording.
# - Generates local PNG documentation-preview artifacts through Charmbracelet VHS so
#   the images come from a terminal renderer instead of a hand-written ANSI parser,
#   HTML frame, or committed binary asset.
#
# Inputs:
# - Run from anywhere in the repository checkout.
# - Optional BuildNative compiles app/codegeist/cli/target/codegeist first.
# - Optional NativeExecutable points at an existing native binary.
#
# Side effects:
# - Writes only under app/codegeist/cli/target/smoke-test/tui-capture by default.
# - Starts a temporary localhost-only Ollama-compatible fixture provider.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - scripts/tests/smoke-common.ps1
# - docs/user/codegeist-tui.md

[CmdletBinding()]
param(
    [string]$CliDir = "",

    [string]$SmokeRoot = "",

    [string]$NativeExecutable = "",

    [int]$TimeoutSeconds = 0,

    [switch]$BuildNative
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot "smoke-common.ps1")

$PromptText = "SummarizeCodegeistTUI"
$ResponseText = "TUI fixture response"
$FixtureModel = "codegeist-fixture"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Fail-Smoke {
    param([string]$Message)

    throw $Message
}

function Write-SmokeLog {
    param([string]$Message)

    Write-Host "[tui-capture-smoke] $Message"
}

function Assert-VhsAvailable {
    if (-not (Get-Command vhs -ErrorAction SilentlyContinue)) {
        Fail-Smoke "vhs is required for TUI documentation captures. Install Charmbracelet VHS and rerun this smoke."
    }
    if (-not (Get-Command ffmpeg -ErrorAction SilentlyContinue)) {
        Fail-Smoke "ffmpeg is required by VHS for TUI documentation captures. Install ffmpeg and rerun this smoke."
    }
    if (-not (Get-Command ttyd -ErrorAction SilentlyContinue)) {
        Fail-Smoke "ttyd is required by VHS for TUI documentation captures. Install ttyd and rerun this smoke."
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

    return '"' + $Value.Replace('\', '\\').Replace('"', '\"') + '"'
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
            [string]$ResponseText,
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

        $listener = [System.Net.HttpListener]::new()
        $listener.Prefixes.Add($Prefix)
        $listener.Start()
        Write-FixtureLog "started $Prefix"

        try {
            while ($true) {
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

                Send-Json $context.Response ([ordered]@{
                    model = $Model
                    created_at = [DateTimeOffset]::UtcNow.ToString("o")
                    message = [ordered]@{
                        role = "assistant"
                        content = $ResponseText
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
        finally {
            $listener.Stop()
            $listener.Close()
            Write-FixtureLog "stopped"
        }
    } -ArgumentList $prefix, $ResponseText, $FixtureModel, $LogFile

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
        "  directory: $(ConvertTo-YamlSingleQuotedString $WorkspaceDirectory)"
    )
    [System.IO.File]::WriteAllText($ConfigPath, ($lines -join "`n") + "`n", [System.Text.Encoding]::ASCII)
}

function Assert-SessionStoreContainsTurn {
    param(
        [string]$SessionDirectory,
        [string]$Prompt,
        [string]$Response
    )

    $storePath = Join-Path $SessionDirectory "session.json"
    if (-not (Test-Path -LiteralPath $storePath)) {
        Fail-Smoke "TUI did not write session store: $storePath"
    }

    $store = Get-Content -LiteralPath $storePath -Raw | ConvertFrom-Json
    $texts = @($store.sessions) |
        ForEach-Object { @($_.messages) } |
        ForEach-Object { @($_.parts) } |
        Where-Object { $_.type -eq "text" } |
        ForEach-Object { [string]$_.text }

    if (-not ($texts -contains $Prompt)) {
        Fail-Smoke "TUI session store did not contain submitted prompt: $storePath"
    }
    if (-not ($texts -contains $Response)) {
        Fail-Smoke "TUI session store did not contain fixture response: $storePath"
    }
}

function Write-VhsTape {
    param(
        [string]$Path,
        [string]$ArtifactPath,
        [string]$ConfigPath,
        [string]$SessionDirectory,
        [string]$LogFile,
        [string]$InitialPngPath,
        [string]$PromptPngPath,
        [string]$ResponsePngPath,
        [string]$Prompt,
        [string]$Response
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

    $responsePattern = [System.Text.RegularExpressions.Regex]::Escape($Response).Replace('\ ', ' ')
    $lines = @(
        "Set Shell bash",
        "Set FontSize 18",
        'Set FontFamily "DejaVu Sans Mono"',
        "Set Width 1100",
        "Set Height 720",
        "Set TypingSpeed 1ms",
        "Set WaitTimeout $($script:ResolvedTimeoutSeconds)s",
        "Hide",
        "Type $(ConvertTo-VhsDoubleQuotedString $command)",
        "Enter",
        "Wait+Screen@30s /Enter a prompt below/",
        "Show",
        "Sleep 500ms",
        "Screenshot $(ConvertTo-VhsDoubleQuotedString $InitialPngPath)",
        "Sleep 500ms",
        "Type $(ConvertTo-VhsDoubleQuotedString $Prompt)",
        "Sleep 250ms",
        "Screenshot $(ConvertTo-VhsDoubleQuotedString $PromptPngPath)",
        "Sleep 500ms",
        "Enter",
        "Wait+Screen@30s /Codegeist: $responsePattern/",
        # TerminalUI processes the submitted turn asynchronously. A blank-space key
        # on the reset prompt is invisible, but advances the read/display loop so
        # the transcript frame is painted before VHS captures the response image.
        'Type " "',
        "Sleep 500ms",
        "Screenshot $(ConvertTo-VhsDoubleQuotedString $ResponsePngPath)",
        "Sleep 500ms",
        "Ctrl+Q",
        "Sleep 500ms"
    )
    [System.IO.File]::WriteAllText($Path, ($lines -join "`n") + "`n", [System.Text.Encoding]::ASCII)
}

function New-TuiCaptureArtifacts {
    param([string]$CaptureRoot)

    $artifactNames = @("drive-tui.tape", "01-initial.png", "02-prompt.png", "03-response.png", "vhs-output.log")
    foreach ($artifactName in $artifactNames) {
        $artifactPath = Join-Path $CaptureRoot $artifactName
        if (-not (Test-Path -LiteralPath $artifactPath) -or (Get-Item -LiteralPath $artifactPath).Length -eq 0) {
            Fail-Smoke "VHS did not write expected capture artifact: $artifactPath"
        }
    }

    $manifest = @'
# TUI Capture Smoke Artifacts

Generated by `scripts/tests/tui-capture-smoke.ps1` with Charmbracelet VHS while
driving the native `codegeist tui` command against a deterministic fixture provider.

## Files

- `drive-tui.tape` - generated VHS script used for this capture run.
- `01-initial.png` - initial TUI screen rendered by VHS.
- `02-prompt.png` - prompt-entered TUI screen rendered by VHS.
- `03-response.png` - assistant-response TUI screen rendered by VHS.
- `vhs-output.log` - stdout and stderr from the VHS run.

## Markdown Snippets

```markdown
![Codegeist TUI initial prompt](01-initial.png)
![Codegeist TUI submitted prompt](02-prompt.png)
![Codegeist TUI assistant response](03-response.png)
```

These artifacts live under ignored build output. Selected screenshots for the
committed TUI user guide live under `docs/user/assets/tui/`; refresh those copies
only as part of an explicit documentation task.
'@
    [System.IO.File]::WriteAllText((Join-Path $CaptureRoot "manifest.md"), $manifest, $Utf8NoBom)
}

function Invoke-NativeTuiCapture {
    param(
        [string]$ArtifactPath,
        [string]$CaptureRoot,
        [string]$ConfigPath,
        [string]$SessionDirectory,
        [string]$WorkspaceDirectory,
        [string]$LogFile,
        [string]$TapePath
    )

    $initialPngPath = Join-Path $CaptureRoot "01-initial.png"
    $promptPngPath = Join-Path $CaptureRoot "02-prompt.png"
    $responsePngPath = Join-Path $CaptureRoot "03-response.png"
    $vhsOutput = Join-Path $CaptureRoot "vhs-output.log"
    Write-VhsTape `
        -Path $TapePath `
        -ArtifactPath $ArtifactPath `
        -ConfigPath $ConfigPath `
        -SessionDirectory $SessionDirectory `
        -LogFile $LogFile `
        -InitialPngPath $initialPngPath `
        -PromptPngPath $promptPngPath `
        -ResponsePngPath $responsePngPath `
        -Prompt $PromptText `
        -Response $ResponseText

    Push-Location -LiteralPath $WorkspaceDirectory
    try {
        & vhs $TapePath *> $vhsOutput
        if ($LASTEXITCODE -ne 0) {
            Fail-Smoke "VHS-driven native TUI capture failed with exit code $LASTEXITCODE; see $vhsOutput"
        }
    }
    finally {
        Pop-Location
    }

    Assert-SessionStoreContainsTurn $SessionDirectory $PromptText $ResponseText
}

if (-not $CliDir) {
    $CliDir = Join-Path $PSScriptRoot "../../app/codegeist/cli"
}
$CliDir = Resolve-SmokePath $CliDir
if (-not (Test-Path -LiteralPath $CliDir)) {
    Fail-Smoke "CLI module directory not found: $CliDir"
}

if (-not $SmokeRoot) {
    $SmokeRoot = Join-Path $CliDir "target/smoke-test"
}
$SmokeRoot = Resolve-SmokePath $SmokeRoot
$CaptureRoot = Join-Path $SmokeRoot "tui-capture"
$WorkspaceDirectory = Join-Path $CaptureRoot "workspace"
$SessionDirectory = Join-Path $CaptureRoot "session"
$ConfigPath = Join-Path $CaptureRoot "codegeist.yml"
$LogFile = Join-Path $CaptureRoot "codegeist.log"
$FixtureLogFile = Join-Path $CaptureRoot "fixture-provider.log"
$TapePath = Join-Path $CaptureRoot "drive-tui.tape"

$script:ResolvedTimeoutSeconds = Get-SmokeSeconds $TimeoutSeconds @("CODEGEIST_TUI_CAPTURE_TIMEOUT_SECONDS") 90
Assert-VhsAvailable

if (-not $NativeExecutable) {
    $binaryName = if ($IsWindows) { "codegeist.exe" } else { "codegeist" }
    $NativeExecutable = Join-Path $CliDir "target/$binaryName"
}
$NativeExecutable = Resolve-SmokePath $NativeExecutable

Push-Location -LiteralPath $CliDir
try {
    if ($BuildNative) {
        Invoke-SmokeStep `
            "mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile" `
            "tui capture native compile" `
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
    $fixtureServer = Start-FixtureOllamaServer $FixtureLogFile
    Write-CodegeistConfig $ConfigPath $WorkspaceDirectory $fixtureServer.BaseUrl
    Write-SmokeLog "native executable: $NativeExecutable"
    Write-SmokeLog "capture root: $CaptureRoot"
    Write-SmokeLog "config: $ConfigPath"
    Write-SmokeLog "session: $SessionDirectory"

    $captureStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    Invoke-NativeTuiCapture `
        -ArtifactPath $NativeExecutable `
        -CaptureRoot $CaptureRoot `
        -ConfigPath $ConfigPath `
        -SessionDirectory $SessionDirectory `
        -WorkspaceDirectory $WorkspaceDirectory `
        -LogFile $LogFile `
        -TapePath $TapePath
    Write-SmokeDuration "tui capture native run" $captureStopwatch

    $artifactStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    New-TuiCaptureArtifacts $CaptureRoot
    Write-SmokeDuration "tui capture artifact generation" $artifactStopwatch
}
finally {
    Stop-FixtureOllamaServer $fixtureServer
}

Write-SmokeDuration "tui capture smoke total" $totalStopwatch
Write-Host "TUI capture smoke status: passed"
Write-Host "TUI capture artifact root: $CaptureRoot"
Write-Host "TUI capture smoke passed: $CaptureRoot"

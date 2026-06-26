# codegeist-install-windows.ps1 - install the Windows x64 Codegeist release archive.
#
# Why this exists:
# - Gives Windows users a small curl-downloadable bootstrap script for GitHub
#   Release assets without adding package-manager publishing or an MSI installer.
# - Installs the complete native archive directory so GraalVM sidecar DLLs remain
#   beside codegeist.exe while a stable codegeist.cmd shim is exposed.
#
# Inputs:
# - CODEGEIST_INSTALL_BASE_URL overrides the release asset base URL. Defaults to
#   the public GitHub Releases latest/download URL.
# - CODEGEIST_INSTALL_DIR overrides the user-local install root.
# - CODEGEIST_BIN_DIR overrides the directory where codegeist.cmd is written.
#
# Side effects:
# - Downloads codegeist-windows-x64.zip and SHA256SUMS.txt into a temporary
#   directory, verifies the archive checksum, installs under CODEGEIST_INSTALL_DIR,
#   and writes CODEGEIST_BIN_DIR\codegeist.cmd.
#
# Related files:
# - scripts/install/codegeist-install-linux.sh
# - .github/workflows/release.yml

[CmdletBinding()]
param(
    [string]$BaseUrl = $(if ($env:CODEGEIST_INSTALL_BASE_URL) { $env:CODEGEIST_INSTALL_BASE_URL } else { 'https://github.com/codegeist-ai/codegeist/releases/latest/download' }),

    [string]$InstallDir = $(if ($env:CODEGEIST_INSTALL_DIR) { $env:CODEGEIST_INSTALL_DIR } else { Join-Path $env:LOCALAPPDATA 'Codegeist' }),

    [string]$BinDir = $(if ($env:CODEGEIST_BIN_DIR) { $env:CODEGEIST_BIN_DIR } else { Join-Path $InstallDir 'bin' })
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ArchiveName = 'codegeist-windows-x64.zip'
$PackageName = 'codegeist-windows-x64'
$ChecksumName = 'SHA256SUMS.txt'

function Join-AssetUrl {
    param(
        [string]$Root,
        [string]$Name
    )

    return $Root.TrimEnd('/') + '/' + $Name
}

function Invoke-CodegeistDownload {
    param(
        [string]$Name,
        [string]$OutputPath
    )

    $url = Join-AssetUrl $BaseUrl $Name
    Write-Host "Downloading $url"
    Invoke-WebRequest -Uri $url -OutFile $OutputPath -UseBasicParsing
}

function Get-ExpectedArchiveHash {
    param([string]$ChecksumFile)

    foreach ($line in Get-Content -LiteralPath $ChecksumFile) {
        $trimmed = $line.Trim()
        if (-not $trimmed) {
            continue
        }

        $parts = $trimmed -split '\s+', 2
        if ($parts.Count -ne 2) {
            continue
        }

        $name = $parts[1].TrimStart('*')
        if ($name -eq $ArchiveName) {
            return $parts[0].ToLowerInvariant()
        }
    }

    throw "$ChecksumName does not contain an entry for $ArchiveName"
}

function Assert-ArchiveHash {
    param(
        [string]$ArchivePath,
        [string]$ChecksumFile
    )

    $expected = Get-ExpectedArchiveHash $ChecksumFile
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $ArchivePath).Hash.ToLowerInvariant()
    if ($actual -ne $expected) {
        throw "Checksum mismatch for ${ArchiveName}: expected $expected but got $actual"
    }
}

function Write-CodegeistShim {
    param(
        [string]$ShimPath,
        [string]$CodegeistHome
    )

    $escapedHome = $CodegeistHome.Replace('%', '%%')
    $content = @"
@echo off
set "CODEGEIST_HOME=$escapedHome"
"%CODEGEIST_HOME%\codegeist.exe" %*
"@
    Set-Content -LiteralPath $ShimPath -Encoding ASCII -Value $content
}

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("codegeist-install-" + [guid]::NewGuid().ToString('N'))
try {
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
    $archivePath = Join-Path $tempRoot $ArchiveName
    $checksumPath = Join-Path $tempRoot $ChecksumName
    $extractDir = Join-Path $tempRoot 'extract'
    $releaseDir = Join-Path (Join-Path $InstallDir 'releases') $PackageName
    $nextReleaseDir = "$releaseDir.next.$PID"
    $currentDir = Join-Path $InstallDir 'current'
    $nextCurrentDir = "$currentDir.next.$PID"
    $shimPath = Join-Path $BinDir 'codegeist.cmd'

    Invoke-CodegeistDownload $ChecksumName $checksumPath
    Invoke-CodegeistDownload $ArchiveName $archivePath
    Assert-ArchiveHash $archivePath $checksumPath

    Expand-Archive -LiteralPath $archivePath -DestinationPath $extractDir -Force
    $extractedPackage = Join-Path $extractDir $PackageName
    $executable = Join-Path $extractedPackage 'codegeist.exe'
    if (-not (Test-Path -LiteralPath $extractedPackage -PathType Container)) {
        throw "Archive did not contain $PackageName"
    }
    if (-not (Test-Path -LiteralPath $executable -PathType Leaf)) {
        throw 'Archive did not contain codegeist.exe'
    }

    New-Item -ItemType Directory -Force -Path (Join-Path $InstallDir 'releases'), $BinDir | Out-Null
    Remove-Item -Recurse -Force -LiteralPath $nextReleaseDir, $nextCurrentDir -ErrorAction SilentlyContinue
    Copy-Item -LiteralPath $extractedPackage -Destination $nextReleaseDir -Recurse
    Remove-Item -Recurse -Force -LiteralPath $releaseDir -ErrorAction SilentlyContinue
    Rename-Item -LiteralPath $nextReleaseDir -NewName (Split-Path -Leaf $releaseDir)
    Copy-Item -LiteralPath $releaseDir -Destination $nextCurrentDir -Recurse
    Remove-Item -Recurse -Force -LiteralPath $currentDir -ErrorAction SilentlyContinue
    Rename-Item -LiteralPath $nextCurrentDir -NewName (Split-Path -Leaf $currentDir)

    Write-CodegeistShim $shimPath $currentDir

    Write-Host "Installed Codegeist to $releaseDir"
    Write-Host "Installed command shim to $shimPath"
    & $shimPath --version | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'Installed codegeist --version self-check failed'
    }
    Write-Host "Codegeist install smoke passed: $shimPath --version"
    Write-Host "Add this directory to PATH if needed: $BinDir"
}
finally {
    Remove-Item -Recurse -Force -LiteralPath $tempRoot -ErrorAction SilentlyContinue
}

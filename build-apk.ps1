param(
    [Parameter(Mandatory = $true)]
    [string]$Keystore,
    [Parameter(Mandatory = $true)]
    [string]$KeyAlias
)

$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME -or -not $env:ANDROID_HOME) {
    throw 'Set JAVA_HOME to JDK 8 and ANDROID_HOME to the Android SDK directory.'
}

$keystorePath = (Resolve-Path -LiteralPath $Keystore).ProviderPath
$tools = Join-Path $env:ANDROID_HOME 'build-tools/27.0.3'

function Invoke-BuildTool {
    param([string]$Tool, [string[]]$Arguments)
    & $Tool @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Failed: $Tool ($LASTEXITCODE)" }
}

Push-Location $PSScriptRoot
try {
    Invoke-BuildTool '.\gradlew.bat' @(':app:assembleRelease')

    $unsigned = 'app/build/outputs/apk/release/app-release-unsigned.apk'
    $output = 'app/build/outputs/apk/uplink'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    $apk = Join-Path $output 'app-release.apk'

    # The Android Gradle plugin aligns the unsigned release APK.
    Invoke-BuildTool "$tools/zipalign.exe" @('-c', '4', $unsigned)
    Invoke-BuildTool "$tools/apksigner.bat" @(
        'sign', '--ks', $keystorePath, '--ks-key-alias', $KeyAlias, '--out', $apk, $unsigned
    )
    Invoke-BuildTool "$tools/apksigner.bat" @('verify', '--min-sdk-version', '16', '--print-certs', $apk)

    $manifest = Invoke-BuildTool "$tools/aapt.exe" @('dump', 'xmltree', $apk, 'AndroidManifest.xml')
    if ($manifest | Where-Object { $_ -match '^\s*A: android:(testOnly|debuggable)\(' -and $_ -notmatch '=\(type 0x12\)0x0\s*$' }) {
        throw 'Expected a release APK with testOnly and debuggable disabled.'
    }

    Write-Output (Join-Path $PSScriptRoot $apk)
} finally {
    Pop-Location
}

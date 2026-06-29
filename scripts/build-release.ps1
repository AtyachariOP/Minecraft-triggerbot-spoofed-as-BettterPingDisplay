$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

Write-Host 'Cleaning previous build outputs...'
./gradlew clean build --stacktrace | Out-Host

$jar = Join-Path $repo 'build/libs/better-ping-display-fabric-1.21.11-1.2.0.jar'
if (-not (Test-Path $jar)) {
    throw "Expected build artifact was not produced: $jar"
}

$hash = (Get-FileHash $jar -Algorithm SHA256).Hash.ToLowerInvariant()
Write-Host "SHA256=$hash"
Write-Host "Artifact=$jar"

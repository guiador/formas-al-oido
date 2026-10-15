param([string]$Jdk='')
$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path (Split-Path -Parent $workspace) 'tools/Build-Helpers.ps1')
$Jdk=Resolve-ProjectJdk $Jdk
$out=Join-Path $workspace 'tests/out'
Reset-GeneratedDirectory $out (Join-Path $workspace 'tests')
$sources=@(Get-ChildItem -LiteralPath "$workspace/src","$workspace/tests" -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$Jdk/bin/javac.exe" --release 8 -encoding UTF-8 -d $out @sources
if($LASTEXITCODE -ne 0){throw 'Compilación de pruebas del núcleo fallida'}
& "$Jdk/bin/java.exe" -cp $out CoreChecks "$workspace/assets/kemar.bin"
if($LASTEXITCODE -ne 0){throw 'Pruebas de geometría/audio fallidas'}
& "$Jdk/bin/java.exe" -cp $out ImageChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de extracción de imágenes fallidas'}

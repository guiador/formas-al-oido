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

& "$Jdk/bin/java.exe" -cp $out TraceChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de trazado y lectura fallidas'}
& "$Jdk/bin/java.exe" -cp $out OrientationChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de orientación, pausa y autoría fallidas'}
& "$Jdk/bin/java.exe" -cp $out VisionChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de regiones y proveedores de IA fallidas'}
& "$Jdk/bin/java.exe" -cp $out SceneMapChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de nodos y contacto unificado fallidas'}
& "$Jdk/bin/java.exe" -cp $out VoiceChecks "$workspace/assets/voice/genny-es.bin" "$out/genny-matematicas.wav"
if($LASTEXITCODE -ne 0){throw 'Pruebas de voz local Genny fallidas'}
& "$Jdk/bin/java.exe" -cp $out MathChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas matemáticas fallidas'}

& "$Jdk/bin/java.exe" -cp $out TeachingChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de lecciones y anotaciones fallidas'}
& "$Jdk/bin/java.exe" -cp $out MathExplorationChecks
if($LASTEXITCODE -ne 0){throw 'Pruebas de exploración matemática fallidas'}

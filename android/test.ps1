param([string]$Jdk='')
$ErrorActionPreference='Stop'
if(-not $Jdk){$Jdk=Join-Path $env:USERPROFILE '.codex\build-tools\evvdroid\jdk21\jdk-21.0.12.1+1'}
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
$out=Join-Path $workspace 'tests\out'
New-Item -ItemType Directory -Force -Path $out | Out-Null
& "$Jdk\bin\javac.exe" -encoding UTF-8 -d $out "$workspace\src\org\formasaloido\Geometry.java" "$workspace\src\org\formasaloido\Sound.java" "$workspace\src\org\formasaloido\Curriculum.java" "$workspace\src\org\formasaloido\TouchMap.java" "$workspace\src\org\formasaloido\ImageTrace.java" "$workspace\tests\CoreChecks.java" "$workspace\tests\ImageChecks.java"
if($LASTEXITCODE -ne 0){throw 'Compilación de pruebas fallida'}
& "$Jdk\bin\java.exe" -cp $out CoreChecks "$workspace\assets\kemar.bin"
if($LASTEXITCODE -ne 0){throw 'Pruebas fallidas'}

& "$Jdk\bin\java.exe" -cp $out ImageChecks
if($LASTEXITCODE -ne 0){throw "Pruebas de imagen fallidas"}

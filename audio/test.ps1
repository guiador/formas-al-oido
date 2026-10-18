$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
. "$root/tools/Build-Helpers.ps1"
$jdk=Resolve-ProjectJdk
$out=Join-Path $PSScriptRoot 'tests/out'
Reset-GeneratedDirectory $out (Join-Path $PSScriptRoot 'tests')
& "$jdk/bin/javac.exe" --release 8 -encoding UTF-8 -cp "$root/windows/build/formas-engine.jar;$root/tools/audio-cache/jna.jar" -d $out "$PSScriptRoot/tests/OpenALChecks.java"
if($LASTEXITCODE -ne 0){throw 'No se pudieron compilar las pruebas OpenAL'}
foreach($arch in @('x86','x64')){
 $audioHome=Join-Path $root "windows/build/Formas-al-oido-Windows-$arch"
 & "$audioHome/runtime/bin/java.exe" -cp "$out;$audioHome/formas-engine.jar;$audioHome/jna.jar" OpenALChecks "$audioHome/soft_oal.dll"
 if($LASTEXITCODE -ne 0){throw "Prueba OpenAL fallida: $arch"}
 Write-Output "PASS arquitectura $arch"
}

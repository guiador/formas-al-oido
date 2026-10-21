$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
$root=Split-Path -Parent $workspace
. (Join-Path $root 'tools/Build-Helpers.ps1')
$final=Join-Path $root 'Formas al oído'
New-Item -ItemType Directory -Force -Path (Join-Path $final 'Windows') | Out-Null
foreach($arch in @('x86','x64')){
 $output=Join-Path $workspace "build/Formas-al-oido-Windows-$arch"
 $stage=Join-Path $workspace "build/package-$arch"
 Reset-GeneratedDirectory $stage (Join-Path $workspace 'build')
 Copy-Item -LiteralPath "$output/Formas-al-oido.exe","$output/formas-engine.jar","$output/jna.jar","$output/kemar.bin","$output/KEMAR-NOTICE.txt","$output/build-info.json","$workspace/LEEME.txt","$root/LICENSE","$root/THIRD-PARTY-NOTICES.md" -Destination $stage -Force
 Copy-Item -LiteralPath "$output/teaching" -Destination "$stage/teaching" -Recurse -Force
 Copy-Item -LiteralPath "$output/voice" -Destination "$stage/voice" -Recurse -Force
 Copy-Item -LiteralPath "$output/openal" -Destination "$stage/openal" -Recurse -Force
 Copy-Item -Path "$output/*.dll" -Destination $stage -Force; Copy-Item -LiteralPath "$output/vision" -Destination "$stage/vision" -Recurse -Force; Copy-Item -LiteralPath "$output/runtime" -Destination "$stage/runtime" -Recurse -Force
 $zip=Join-Path $final "Windows/Formas-al-oido-Windows-$arch-0.8.0.zip"
 Compress-Archive -Path "$stage/*" -DestinationPath $zip -Force
 Write-Output "ZIP: $zip"
}
$teacher=Join-Path $workspace 'build/package-docente'
Reset-GeneratedDirectory $teacher (Join-Path $workspace 'build')
$teacherSources=@("$root/core/assets/teaching/ecuacion-con-anotaciones.fao","$root/core/assets/teaching/fracciones-con-anotaciones.fao","$root/docs/PRUEBA-DOCENTE.txt","$root/docs/FORMATO-LECCION-FAO.md")
foreach($source in $teacherSources){Copy-Item -LiteralPath $source -Destination $teacher -Force}
$teacherFiles=@($teacherSources | ForEach-Object {Join-Path $teacher ([IO.Path]::GetFileName($_))})
$teacherZip=Join-Path $final 'Formas-al-oido-Prueba-docente-0.8.0.zip'
Compress-Archive -LiteralPath $teacherFiles -DestinationPath $teacherZip -Force
$files=@("$final/Android/Formas-al-oido-0.13.0.apk","$final/Windows/Formas-al-oido-Windows-x64-0.8.0.zip","$final/Windows/Formas-al-oido-Windows-x86-0.8.0.zip",$teacherZip)
$voiceSample=Join-Path $root 'core/tests/out/genny-matematicas.wav'
$finalSample=Join-Path $final 'Muestras/genny-matematicas.wav'
if(Test-Path -LiteralPath $voiceSample){New-Item -ItemType Directory -Force -Path (Split-Path -Parent $finalSample) | Out-Null;Copy-Item -LiteralPath $voiceSample -Destination $finalSample -Force}
if(Test-Path -LiteralPath $finalSample){$files+=$finalSample}
@'
FORMAS AL OÍDO — ENTREGA ACTUAL

Android/: APK universal, Android 8 o posterior.
Windows/: ZIP x64 y x86. Extrae el que corresponda y abre Formas-al-oido.exe.
Formas-al-oido-Prueba-docente-0.8.0.zip: lecciones y guía para el profesor.
Muestras/: ejemplo de pronunciación matemática con la voz local Genny.

Incluye mapa de hasta seis objetos reconocidos, contacto compartido con
matemáticas y voz local Genny. No requiere instalar Java, Python ni SAPI.
Las palabras fuera del banco de voz se deletrean.

SHA256SUMS.txt permite comprobar los archivos. El código, pruebas y manuales
se conservan en las carpetas del proyecto; aquí sólo está la entrega actual.
'@ | Set-Content -LiteralPath (Join-Path $final 'LEEME.txt') -Encoding UTF8
$lines=foreach($file in $files){$hash=Get-FileHash -LiteralPath $file -Algorithm SHA256;"$($hash.Hash.ToLowerInvariant())  $(($file.Substring($final.Length+1)).Replace([char]92,[char]47))"}
$lines | Set-Content -LiteralPath "$final/SHA256SUMS.txt" -Encoding ascii

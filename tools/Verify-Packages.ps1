$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$final=Join-Path $root 'Formas al oído'
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Check-Voice($archive,[string]$prefix){
 $entry=$archive.GetEntry($prefix+'genny-es.bin')
 if(-not $entry -or -not $archive.GetEntry($prefix+'genny-es.json')){throw 'Falta la voz Genny incluida'}
 $stream=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create()
 try{$actual=([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-','').ToLowerInvariant()}finally{$stream.Dispose();$sha.Dispose()}
 $metadata=Get-Content -LiteralPath (Join-Path $root 'core/assets/voice/genny-es.json') -Raw | ConvertFrom-Json
 if($actual -ne $metadata.bank_sha256){throw 'El banco Genny no coincide con el probado'}
}
$teacherZip=[IO.Compression.ZipFile]::OpenRead((Join-Path $final 'Formas-al-oido-Prueba-docente-0.8.0.zip'))
try{if($teacherZip.Entries.Count -ne 4){throw 'El paquete docente debe contener cuatro archivos'};foreach($name in @('ecuacion-con-anotaciones.fao','fracciones-con-anotaciones.fao','PRUEBA-DOCENTE.txt','FORMATO-LECCION-FAO.md')){if(-not $teacherZip.GetEntry($name)){throw "Falta material docente: $name"}}}finally{$teacherZip.Dispose()}
function Check-Model($zip,$prefix,$name){
 $entry=$zip.GetEntry($prefix+$name);if(-not $entry){throw "Falta modelo: $prefix$name"}
 $inputStream=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create()
 try{$actual=([BitConverter]::ToString($sha.ComputeHash($inputStream))).Replace('-','').ToLowerInvariant()}finally{$inputStream.Dispose();$sha.Dispose()}
 $modelManifest=Get-Content -LiteralPath (Join-Path $root 'tools/vision-dependencies.json') -Raw | ConvertFrom-Json
 $pinned=@($modelManifest.downloads | Where-Object {$_.cache -eq "core/assets/vision/$name"})
 if($pinned.Count -ne 1){throw "Falta el hash fijado del modelo: $name"}
 $expected=$pinned[0].sha256
 if($actual -ne $expected){throw "Peso de modelo diferente: $name"}
}
foreach($arch in @('x86','x64')){
 $zip=[IO.Compression.ZipFile]::OpenRead((Join-Path $final "Windows/Formas-al-oido-Windows-$arch-0.8.0.zip"))
 try{Check-Voice $zip 'voice/';if($zip.Entries.FullName -match '^vision/vision/'){throw 'Directorio de modelos duplicado'};foreach($model in @('model.onnx','model-standard.onnx')){Check-Model $zip 'vision/' $model};foreach($audioFile in @('jna.jar','soft_oal.dll','openal/openal-source.zip','openal/JNA-LICENSE','openal/COPYING')){if(-not $zip.GetEntry($audioFile)){throw "Falta dependencia de audio: $audioFile"}};if(-not $zip.GetEntry('vision/licenses/ONNX-MODELS-LICENSE.txt')){throw 'Falta licencia de modelos'}}finally{$zip.Dispose()}
 Write-Output "PASS Windows ${arch}: modelos y licencias presentes."
}
$apkPath=Join-Path $final 'Android/Formas-al-oido-0.13.0.apk'
# Read the ZIP central directory: Android 11+ rejects compressed resources.arsc.
$reader=[IO.BinaryReader]::new([IO.File]::OpenRead($apkPath))
try{
 $stream=$reader.BaseStream
 $stream.Seek(-22,[IO.SeekOrigin]::End) | Out-Null
 if($reader.ReadUInt32() -ne 0x06054b50){throw 'EOCD ZIP no reconocido'}
 $stream.Position+=6;$count=$reader.ReadUInt16();$stream.Position+=4;$central=$reader.ReadUInt32()
 $stream.Position=$central;$foundResources=$false
 for($i=0;$i -lt $count;$i++){
  if($reader.ReadUInt32() -ne 0x02014b50){throw 'Directorio ZIP no válido'}
  $stream.Position+=6;$method=$reader.ReadUInt16();$stream.Position+=16
  $nameLength=$reader.ReadUInt16();$extraLength=$reader.ReadUInt16();$commentLength=$reader.ReadUInt16();$stream.Position+=12
  $name=[Text.Encoding]::UTF8.GetString($reader.ReadBytes($nameLength))
  if($name -eq 'resources.arsc'){if($method -ne 0){throw 'resources.arsc debe estar sin compresión'};$foundResources=$true}
  $stream.Position+=$extraLength+$commentLength
 }
 if(-not $foundResources){throw 'Falta resources.arsc'}
}finally{$reader.Dispose()}
& (Join-Path $env:USERPROFILE '.codex/build-tools/evvdroid/sdk/build-tools/35.0.0/zipalign.exe') -c 4 $apkPath
if($LASTEXITCODE -ne 0){throw 'El APK no cumple la alineación de 4 bytes'}
Write-Output 'PASS Android: resources.arsc sin compresión y APK alineado.'
$apk=[IO.Compression.ZipFile]::OpenRead($apkPath)
try{Check-Voice $apk 'assets/voice/';if($apk.Entries | Where-Object {$_.FullName.Contains([char]92)}){throw 'El APK contiene rutas con separadores Windows'};foreach($abi in @('armeabi-v7a','arm64-v8a','x86','x86_64')){foreach($lib in @('libonnxruntime.so','libonnxruntime4j_jni.so','libopenal.so','libjnidispatch.so','libc++_shared.so')){if(-not $apk.GetEntry("lib/$abi/$lib")){throw "Falta biblioteca: $abi/$lib"}}};foreach($model in @('model.onnx','model-standard.onnx')){Check-Model $apk 'assets/vision/' $model};foreach($audioFile in @('openal-source.zip','COPYING','JNA-LICENSE')){if(-not $apk.GetEntry('assets/openal/'+$audioFile)){throw "Falta licencia o fuente de audio: $audioFile"}};if(-not $apk.GetEntry('assets/vision/labels.tsv')){throw 'Faltan etiquetas accesibles'}}finally{$apk.Dispose()}
Write-Output 'PASS Android: rutas de assets portables, dos modelos íntegros y cuatro ABI.'
foreach($line in Get-Content -LiteralPath (Join-Path $final 'SHA256SUMS.txt')){$parts=$line -split '  ',2;if($parts.Length -ne 2 -or (Get-FileHash -LiteralPath (Join-Path $final $parts[1]) -Algorithm SHA256).Hash.ToLowerInvariant() -ne $parts[0]){throw 'Manifiesto SHA256 no coincide'}}
Write-Output 'PASS manifiesto SHA256 de la entrega.'

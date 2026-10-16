$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$final=Join-Path $root 'Formas al oído'
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Check-Model($zip,$prefix,$name){
 $entry=$zip.GetEntry($prefix+$name);if(-not $entry){throw "Falta modelo: $prefix$name"}
 $inputStream=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create()
 try{$actual=([BitConverter]::ToString($sha.ComputeHash($inputStream))).Replace('-','').ToLowerInvariant()}finally{$inputStream.Dispose();$sha.Dispose()}
 $expected=(Get-FileHash -LiteralPath (Join-Path $root "core/assets/vision/$name") -Algorithm SHA256).Hash.ToLowerInvariant()
 if($actual -ne $expected){throw "Peso de modelo diferente: $name"}
}
foreach($arch in @('x86','x64')){
 $zip=[IO.Compression.ZipFile]::OpenRead((Join-Path $final "Windows/Formas-al-oido-Windows-$arch-0.4.0.zip"))
 try{if($zip.Entries.FullName -match '^vision/vision/'){throw 'Directorio de modelos duplicado'};foreach($model in @('model.onnx','model-standard.onnx')){Check-Model $zip 'vision/' $model};if(-not $zip.GetEntry('vision/licenses/ONNX-MODELS-LICENSE.txt')){throw 'Falta licencia de modelos'}}finally{$zip.Dispose()}
 Write-Output "PASS Windows ${arch}: modelos y licencias presentes."
}
$apk=[IO.Compression.ZipFile]::OpenRead((Join-Path $final 'Android/Formas-al-oido-0.9.0.apk'))
try{if($apk.Entries | Where-Object {$_.FullName.Contains([char]92)}){throw 'El APK contiene rutas con separadores Windows'};foreach($abi in @('armeabi-v7a','arm64-v8a','x86','x86_64')){foreach($lib in @('libonnxruntime.so','libonnxruntime4j_jni.so')){if(-not $apk.GetEntry("lib/$abi/$lib")){throw "Falta biblioteca: $abi/$lib"}}};foreach($model in @('model.onnx','model-standard.onnx')){Check-Model $apk 'assets/vision/' $model};if(-not $apk.GetEntry('assets/vision/labels.tsv')){throw 'Faltan etiquetas accesibles'}}finally{$apk.Dispose()}
Write-Output 'PASS Android: rutas de assets portables, dos modelos íntegros y cuatro ABI.'
foreach($line in Get-Content -LiteralPath (Join-Path $final 'SHA256SUMS.txt')){$parts=$line -split '  ',2;if($parts.Length -ne 2 -or (Get-FileHash -LiteralPath (Join-Path $final $parts[1]) -Algorithm SHA256).Hash.ToLowerInvariant() -ne $parts[0]){throw 'Manifiesto SHA256 no coincide'}}
Write-Output 'PASS manifiesto SHA256 de la entrega.'

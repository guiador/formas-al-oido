param([string]$Jdk='')
$ErrorActionPreference='Stop'
& (Join-Path (Split-Path -Parent $PSScriptRoot) 'tools/Prepare-OpenAL.ps1')
& (Join-Path (Split-Path -Parent $PSScriptRoot) 'tools/Prepare-Vision.ps1')
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
$root=Split-Path -Parent $workspace
. (Join-Path $root 'tools/Build-Helpers.ps1')
$Jdk=Resolve-ProjectJdk $Jdk
$csc=Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
$speech=Get-ChildItem -LiteralPath (Join-Path $env:WINDIR 'Microsoft.NET/assembly/GAC_MSIL/System.Speech') -Filter System.Speech.dll -Recurse | Select-Object -First 1 -ExpandProperty FullName
$classes=Join-Path $workspace 'build/classes'
Reset-GeneratedDirectory $classes (Join-Path $workspace 'build')
$sources=@(Get-ChildItem -LiteralPath (Join-Path $root 'core/src'),(Join-Path $root 'audio/src') -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$Jdk/bin/javac.exe" --release 8 -encoding UTF-8 -cp "$root/tools/audio-cache/jna.jar" -d $classes @sources "$workspace/src/DesktopEngine.java"
if($LASTEXITCODE -ne 0){throw 'No se pudo compilar el motor'}
& "$Jdk/bin/jar.exe" --create --file "$workspace/build/formas-engine.jar" -C $classes .
if($LASTEXITCODE -ne 0){throw 'No se pudo empaquetar el motor'}
foreach($architecture in @('x32','x64')){
 $platform=if($architecture -eq 'x32'){'x86'}else{'x64'}
 $output=Join-Path $workspace "build/Formas-al-oido-Windows-$platform"
 New-Item -ItemType Directory -Force -Path $output | Out-Null
 $response=Join-Path $workspace "build/csc-$platform.rsp"
 $exePath=Join-Path $output 'Formas-al-oido.exe'
 $sourcePath=Join-Path $workspace 'src/FormasWindows.cs'
 @('/nologo','/target:winexe',"/platform:$platform",'/optimize+','/codepage:65001',('/out:"'+$exePath+'"'),'/reference:System.Windows.Forms.dll','/reference:System.Drawing.dll','/reference:System.Web.Extensions.dll',('/reference:"'+(Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/netstandard.dll')+'"'),('/reference:"'+(Join-Path $root 'tools/ai-cache/managed/Microsoft.ML.OnnxRuntime.dll')+'"'),('/reference:"'+(Join-Path $root 'tools/ai-cache/managed/System.Memory.dll')+'"'),('/reference:"'+(Join-Path $root 'tools/ai-cache/managed/System.Buffers.dll')+'"'),('/reference:"'+$speech+'"'),('"'+$sourcePath+'"'),('"'+(Join-Path $workspace 'src/WindowsStudio.cs')+'"'),('"'+(Join-Path $workspace 'src/LocalVision.cs')+'"'),('"'+(Join-Path $workspace 'src/WindowsVision.cs')+'"')) | Set-Content -LiteralPath $response -Encoding UTF8
 & $csc "@$response"
 if($LASTEXITCODE -ne 0){throw "No se pudo compilar Windows $platform"}
 Copy-Item -LiteralPath "$workspace/build/formas-engine.jar","$root/core/assets/kemar.bin","$root/core/assets/KEMAR-NOTICE.txt","$workspace/LEEME.txt" -Destination $output -Force
 $asset=Get-Content -LiteralPath "$workspace/tools/temurin-$architecture.json" -Raw | ConvertFrom-Json
 $runtimeArch=if($architecture -eq 'x32'){'i586'}else{'amd64'}
 $runtimes=@(Get-ChildItem -LiteralPath "$workspace/tools/$architecture" -Directory | Where-Object {Test-JavaRuntime $_.FullName $asset.version.semver $runtimeArch})
 if($runtimes.Count -ne 1){throw "Se requiere un único entorno Java $architecture versión $($asset.version.semver); consulta README.md"}
 $runtime=$runtimes[0]
 $previous=$null
 if(Test-Path -LiteralPath "$output/build-info.json"){try{$previous=Get-Content -LiteralPath "$output/build-info.json" -Raw | ConvertFrom-Json}catch{}}
 if(-not(Test-JavaRuntime "$output/runtime" $asset.version.semver $runtimeArch) -or $previous.runtime_sha256 -ne $asset.binary.package.checksum){
  Remove-GeneratedPath "$output/runtime" (Join-Path $workspace 'build')
  Copy-Item -LiteralPath $runtime.FullName -Destination "$output/runtime" -Recurse -Force
 }
 Copy-Item -LiteralPath "$root/tools/audio-cache/jna.jar","$root/tools/audio-cache/$platform/soft_oal.dll" -Destination $output -Force
 Reset-GeneratedDirectory "$output/openal" $output
 Copy-Item -Path "$root/core/assets/openal/*" -Destination "$output/openal" -Force
 $provenance=[ordered]@{architecture=$platform;runtime_version=$asset.version.semver;runtime_url=$asset.binary.package.link;runtime_sha256=$asset.binary.package.checksum;app_version='0.5.0';engine_audio_version='0.10.0'}
 $provenance | ConvertTo-Json | Set-Content -LiteralPath "$output/build-info.json" -Encoding UTF8
 Copy-Item -Path "$root/tools/ai-cache/managed/*.dll","$root/tools/ai-cache/$platform/*.dll" -Destination $output -Force; Reset-GeneratedDirectory "$output/vision" $output; Copy-Item -Path "$root/core/assets/vision/*" -Destination "$output/vision" -Recurse -Force; Write-Output "EXE: $output/Formas-al-oido.exe"
}

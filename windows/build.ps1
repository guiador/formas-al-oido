param([string]$Jdk='')
$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
$root=Split-Path -Parent $workspace
if(-not $Jdk){$Jdk=Join-Path $env:USERPROFILE '.codex/build-tools/evvdroid/jdk21/jdk-21.0.12.1+1'}
$csc=Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
$speech=Get-ChildItem -LiteralPath (Join-Path $env:WINDIR 'Microsoft.NET/assembly/GAC_MSIL/System.Speech') -Filter System.Speech.dll -Recurse | Select-Object -First 1 -ExpandProperty FullName
$classes=Join-Path $workspace 'build/classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources=@('Geometry','Sound','TouchMap','Curriculum','ImageTrace') | ForEach-Object {Join-Path $root "android/src/org/formasaloido/$_.java"}
& "$Jdk/bin/javac.exe" --release 8 -encoding UTF-8 -d $classes @sources "$workspace/src/DesktopEngine.java"
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
 @('/nologo','/target:winexe',"/platform:$platform",'/optimize+','/codepage:65001',('/out:"'+$exePath+'"'),'/reference:System.Windows.Forms.dll','/reference:System.Drawing.dll','/reference:System.Web.Extensions.dll',('/reference:"'+$speech+'"'),('"'+$sourcePath+'"')) | Set-Content -LiteralPath $response -Encoding UTF8
 & $csc "@$response"
 if($LASTEXITCODE -ne 0){throw "No se pudo compilar Windows $platform"}
 Copy-Item -LiteralPath "$workspace/build/formas-engine.jar","$root/android/assets/kemar.bin","$root/android/assets/KEMAR-NOTICE.txt","$workspace/LEEME.txt" -Destination $output -Force
 $runtime=Get-ChildItem -LiteralPath "$workspace/tools/$architecture" -Directory | Select-Object -First 1
 if(-not $runtime){throw "Falta entorno Java $architecture; consulta README.md"}
 if(-not(Test-Path -LiteralPath "$output/runtime/bin/java.exe")){Copy-Item -LiteralPath $runtime.FullName -Destination "$output/runtime" -Recurse -Force}
 $asset=Get-Content -LiteralPath "$workspace/tools/temurin-$architecture.json" -Raw | ConvertFrom-Json
 $provenance=[ordered]@{architecture=$platform;runtime_version=$asset.version.semver;runtime_url=$asset.binary.package.link;runtime_sha256=$asset.binary.package.checksum;app_version='0.1.0';engine_audio_version='0.6.0'}
 $provenance | ConvertTo-Json | Set-Content -LiteralPath "$output/build-info.json" -Encoding UTF8
 Write-Output "EXE: $output/Formas-al-oido.exe"
}

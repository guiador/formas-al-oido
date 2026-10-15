$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
$root=Split-Path -Parent $workspace
foreach($arch in @('x86','x64')){
 $output=Join-Path $workspace "build/Formas-al-oido-Windows-$arch"
 $stage=Join-Path $workspace "build/package-$arch"
 New-Item -ItemType Directory -Force -Path $stage | Out-Null
 Copy-Item -LiteralPath "$output/Formas-al-oido.exe","$output/formas-engine.jar","$output/kemar.bin","$output/KEMAR-NOTICE.txt","$output/build-info.json","$workspace/LEEME.txt","$root/LICENSE","$root/THIRD-PARTY-NOTICES.md" -Destination $stage -Force
 if(-not(Test-Path -LiteralPath "$stage/runtime/bin/java.exe")){Copy-Item -LiteralPath "$output/runtime" -Destination "$stage/runtime" -Recurse -Force}
 $zip=Join-Path $workspace "build/Formas-al-oido-Windows-$arch-0.1.0.zip"
 Compress-Archive -Path "$stage/*" -DestinationPath $zip -Force
 Write-Output "ZIP: $zip"
}
$files=@("$root/android/build/Formas-al-oido-0.6.0.apk","$workspace/build/Formas-al-oido-Windows-x64-0.1.0.zip","$workspace/build/Formas-al-oido-Windows-x86-0.1.0.zip")
$lines=foreach($file in $files){$hash=Get-FileHash -LiteralPath $file -Algorithm SHA256;"$($hash.Hash.ToLowerInvariant())  $(Split-Path -Leaf $file)"}
$lines | Set-Content -LiteralPath "$workspace/build/SHA256SUMS.txt" -Encoding ascii

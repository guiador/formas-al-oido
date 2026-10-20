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
 Copy-Item -LiteralPath "$output/openal" -Destination "$stage/openal" -Recurse -Force
 Copy-Item -Path "$output/*.dll" -Destination $stage -Force; Copy-Item -LiteralPath "$output/vision" -Destination "$stage/vision" -Recurse -Force; Copy-Item -LiteralPath "$output/runtime" -Destination "$stage/runtime" -Recurse -Force
 $zip=Join-Path $final "Windows/Formas-al-oido-Windows-$arch-0.6.0.zip"
 Compress-Archive -Path "$stage/*" -DestinationPath $zip -Force
 Write-Output "ZIP: $zip"
}
$files=@("$final/Android/Formas-al-oido-0.11.0.apk","$final/Windows/Formas-al-oido-Windows-x64-0.6.0.zip","$final/Windows/Formas-al-oido-Windows-x86-0.6.0.zip")
$lines=foreach($file in $files){$hash=Get-FileHash -LiteralPath $file -Algorithm SHA256;"$($hash.Hash.ToLowerInvariant())  $(($file.Substring($final.Length+1)).Replace([char]92,[char]47))"}
$lines | Set-Content -LiteralPath "$final/SHA256SUMS.txt" -Encoding ascii

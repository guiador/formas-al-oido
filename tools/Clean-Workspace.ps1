[CmdletBinding(SupportsShouldProcess)]
param([switch]$KeepDependencyCache)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$relativeTargets=@('android/build','windows/build','android/tests/out','core/tests/out','audio/tests/out','tools/tests/out','android/tools/kemar','android/tools/compact.zip','prototipo.tar.gz')
if(-not $KeepDependencyCache){$relativeTargets+=@('tools/ai-cache','tools/audio-cache','tools/tts-cache','windows/tools/x32','windows/tools/x64','windows/tools/temurin-x32.zip','windows/tools/temurin-x64.zip','core/assets/vision/model.onnx','core/assets/vision/model-standard.onnx','core/assets/openal')}
$boundary=[IO.Path]::GetFullPath($root).TrimEnd([char]92,[char]47)+[IO.Path]::DirectorySeparatorChar
$plan=@()
# Validate every target before sending anything to the Recycle Bin.
foreach($relative in $relativeTargets){
 $target=[IO.Path]::GetFullPath((Join-Path $root $relative))
 if(-not $target.StartsWith($boundary,[StringComparison]::OrdinalIgnoreCase)){throw "Ruta fuera del proyecto: $target"}
 if(-not(Test-Path -LiteralPath $target)){continue}
 $tracked=@(& git -C $root ls-files -- $relative)
 if($LASTEXITCODE -ne 0 -or $tracked.Count -gt 0){throw "No se reciclan archivos de código versionados: $target"}
 $item=Get-Item -LiteralPath $target -Force
 if(($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0){throw "No se recicla un enlace: $target"}
 $children=if($item.PSIsContainer){@(Get-ChildItem -LiteralPath $target -Recurse -Force)}else{@()}
 if($children | Where-Object {($_.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0 -or $_.Name -eq '.git'}){throw "Hay enlaces o repositorios dentro de $target"}
 $size=if($item.PSIsContainer){($children | Where-Object {-not $_.PSIsContainer} | Measure-Object -Property Length -Sum).Sum}else{$item.Length}
 $plan+= [PSCustomObject]@{Path=$target;Directory=$item.PSIsContainer;Bytes=[long]$size}
}
Add-Type -AssemblyName Microsoft.VisualBasic
$bytes=0L;$removed=0
foreach($entry in $plan){
 if($PSCmdlet.ShouldProcess($entry.Path,'Enviar residuo generado a la Papelera')){
  if($entry.Directory){[Microsoft.VisualBasic.FileIO.FileSystem]::DeleteDirectory($entry.Path,[Microsoft.VisualBasic.FileIO.UIOption]::OnlyErrorDialogs,[Microsoft.VisualBasic.FileIO.RecycleOption]::SendToRecycleBin,[Microsoft.VisualBasic.FileIO.UICancelOption]::ThrowException)}
  else{[Microsoft.VisualBasic.FileIO.FileSystem]::DeleteFile($entry.Path,[Microsoft.VisualBasic.FileIO.UIOption]::OnlyErrorDialogs,[Microsoft.VisualBasic.FileIO.RecycleOption]::SendToRecycleBin,[Microsoft.VisualBasic.FileIO.UICancelOption]::ThrowException)}
  $bytes+=$entry.Bytes;$removed++
 }
}
Write-Output ("Papelera: {0} destinos, {1:N1} MiB retirados del proyecto. El disco recupera ese espacio al vaciar la Papelera." -f $removed,($bytes/1MB))

[CmdletBinding(SupportsShouldProcess)]
param()
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'Build-Helpers.ps1')
$targets=@(
 'android/build/source-bundle','android/tests/out',
 'android/tools/kemar','android/tools/compact.zip',
 'windows/build/package-x86','windows/build/package-x64',
 'windows/build/Formas-al-oido-Windows-x86/smoke-data',
 'windows/build/Formas-al-oido-Windows-x64/smoke-data',
 'windows/build/Formas-al-oido-Windows-x86/smoke-result.txt',
 'windows/build/Formas-al-oido-Windows-x64/smoke-result.txt',
 'windows/tools/temurin-x32.zip','windows/tools/temurin-x64.zip',
 'prototipo.tar.gz'
) | ForEach-Object {Join-Path $root $_}
$oldBuild=Join-Path $root 'android/build'
if(Test-Path -LiteralPath $oldBuild){$targets+=@(Get-ChildItem -LiteralPath $oldBuild -File | Where-Object {$_.Name -match '^Formas-al-oido-(?:fuentes-)?0\.[345]\.0\.(?:apk(?:\.idsig)?|zip)$'} | ForEach-Object {$_.FullName})}
$bytes=0L;$removed=0
foreach($target in $targets){
 if(-not(Test-Path -LiteralPath $target)){continue}
 $item=Get-Item -LiteralPath $target -Force
 $size=if($item.PSIsContainer){(Get-ChildItem -LiteralPath $target -Recurse -File -Force | Measure-Object -Property Length -Sum).Sum}else{$item.Length}
 if($PSCmdlet.ShouldProcess($target,'Eliminar residuo generado')){Remove-GeneratedPath $target $root;$bytes+=[long]$size;$removed++}
}
Write-Output ("Eliminados: {0} destinos; {1:N1} MiB liberados." -f $removed,($bytes/1MB))

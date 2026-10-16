param()
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$manifest=Get-Content -LiteralPath (Join-Path $PSScriptRoot 'vision-dependencies.json') -Raw | ConvertFrom-Json
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Matches-Hash($path,$expected){return (Test-Path -LiteralPath $path) -and (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() -eq $expected}
foreach($item in $manifest.downloads){
 $cache=[IO.Path]::GetFullPath((Join-Path $root $item.cache))
 $ready=Matches-Hash $cache $item.sha256
 if(-not $ready){New-Item -ItemType Directory -Force -Path (Split-Path -Parent $cache) | Out-Null;Invoke-WebRequest -Uri $item.url -OutFile ($cache+'.download');if(-not(Matches-Hash ($cache+'.download') $item.sha256)){throw "SHA256 no coincide: $($item.cache)"};Move-Item -LiteralPath ($cache+'.download') -Destination $cache -Force}
 if(@($item.files).Count -eq 0){continue}
 $zip=$null
 try{foreach($file in $item.files){$target=Join-Path $root $file.target;if(Matches-Hash $target $file.sha256){continue};if(-not $zip){$zip=[IO.Compression.ZipFile]::OpenRead($cache)};$entry=$zip.GetEntry($file.entry);if(-not $entry){throw "No existe $($file.entry)"};New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null;[IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$target,$true);if(-not(Matches-Hash $target $file.sha256)){throw "Extracción no verificada: $($file.target)"}}}finally{if($zip){$zip.Dispose()}}
}
Write-Output 'IA: dependencias, modelos y arquitecturas verificados con SHA256.'

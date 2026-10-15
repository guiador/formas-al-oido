# Shared build helpers; dot-source from platform scripts.
function Resolve-ProjectJdk([string]$Requested='') {
 $candidates=@()
 if($Requested){$candidates+= $Requested}
 else {
  if($env:JAVA_HOME){$candidates+= $env:JAVA_HOME}
  $javac=Get-Command javac.exe -ErrorAction SilentlyContinue
  if($javac){$candidates+=Split-Path -Parent (Split-Path -Parent $javac.Source)}
  $candidates+=Join-Path $env:USERPROFILE '.codex/build-tools/evvdroid/jdk21/jdk-21.0.12.1+1'
 }
 foreach($candidate in $candidates){if(Test-Path -LiteralPath (Join-Path $candidate 'bin/javac.exe')){return [IO.Path]::GetFullPath($candidate)}}
 throw 'No se encuentra un JDK. Indica -Jdk o configura JAVA_HOME con JDK 21 o posterior.'
}
function Remove-GeneratedPath([string]$Path,[string]$AllowedRoot) {
 $target=[IO.Path]::GetFullPath($Path)
 $boundary=[IO.Path]::GetFullPath($AllowedRoot).TrimEnd([IO.Path]::DirectorySeparatorChar,[IO.Path]::AltDirectorySeparatorChar)+[IO.Path]::DirectorySeparatorChar
 if(-not $target.StartsWith($boundary,[StringComparison]::OrdinalIgnoreCase)){throw "Ruta generada fuera del directorio permitido: $target"}
 if(Test-Path -LiteralPath $target){
  $item=Get-Item -LiteralPath $target -Force
  if(($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0){throw "No se elimina un enlace: $target"}
  Remove-Item -LiteralPath $target -Recurse -Force
 }
}
function Reset-GeneratedDirectory([string]$Path,[string]$AllowedRoot) {
 Remove-GeneratedPath -Path $Path -AllowedRoot $AllowedRoot
 New-Item -ItemType Directory -Force -Path $Path | Out-Null
}
function Test-JavaRuntime([string]$Path,[string]$Version,[string]$Architecture) {
 $release=Join-Path $Path 'release'
 if(-not(Test-Path -LiteralPath $release) -or -not(Test-Path -LiteralPath (Join-Path $Path 'bin/java.exe'))){return $false}
 $content=Get-Content -LiteralPath $release -Raw
 return $content.Contains('SEMANTIC_VERSION="'+$Version+'"') -and $content.Contains('OS_ARCH="'+$Architecture+'"')
}

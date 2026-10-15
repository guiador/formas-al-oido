$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'Build-Helpers.ps1')
$allowed=Join-Path $PSScriptRoot 'tests'
$fixture=Join-Path $allowed 'out'
Reset-GeneratedDirectory $fixture $allowed
Set-Content -LiteralPath (Join-Path $fixture 'removed-class.class') -Value 'stale'
Reset-GeneratedDirectory $fixture $allowed
if(Test-Path -LiteralPath (Join-Path $fixture 'removed-class.class')){throw 'La clase antigua sigue presente'}
foreach($bad in @($allowed,(Join-Path $PSScriptRoot 'tests-other/out'),(Join-Path $allowed '../outside'))){
 $rejected=$false
 try{Remove-GeneratedPath $bad $allowed}catch{$rejected=$true}
 if(-not $rejected){throw "Se aceptó una ruta fuera del límite: $bad"}
}
New-Item -ItemType Directory -Force -Path (Join-Path $fixture 'bin') | Out-Null
Set-Content -LiteralPath (Join-Path $fixture 'bin/java.exe') -Value 'fixture only'
Set-Content -LiteralPath (Join-Path $fixture 'release') -Value "SEMANTIC_VERSION=`"8.0.504+1`"`nOS_ARCH=`"amd64`""
if(-not(Test-JavaRuntime $fixture '8.0.504+1' 'amd64')){throw 'Se rechazó el entorno correcto'}
if((Test-JavaRuntime $fixture '8.0.472+8' 'amd64') -or (Test-JavaRuntime $fixture '8.0.504+1' 'i586')){throw 'Se aceptó un entorno incorrecto'}
Remove-GeneratedPath $fixture $allowed
Write-Output 'PASS: limpieza de clases, límites de eliminación y detección de versión/arquitectura Java.'

$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
node "$workspace/tests/engine-checks.cjs"
if($LASTEXITCODE -ne 0){throw 'Pruebas del motor Windows fallidas'}
foreach($arch in @('x86','x64')){
 $folder=Join-Path $workspace "build/Formas-al-oido-Windows-$arch"
 if(Test-Path -LiteralPath "$folder/smoke-result.txt"){Remove-Item -LiteralPath "$folder/smoke-result.txt"}
 $p=Start-Process -FilePath "$folder/Formas-al-oido.exe" -ArgumentList '--smoke-test' -WindowStyle Hidden -PassThru
 if(-not $p.WaitForExit(30000)){$p.Kill();throw "Prueba de apertura agotada: $arch"}
 $p.Refresh()
 $result=Get-Content -LiteralPath "$folder/smoke-result.txt" -Raw
 if($p.ExitCode -ne 0 -or -not $result.StartsWith('PASS')){throw "Prueba de apertura fallida: $arch $result"}
 Write-Output "$arch $result"
}

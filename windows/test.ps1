$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
node "$workspace/tests/engine-checks.cjs"
if($LASTEXITCODE -ne 0){throw 'Pruebas del motor Windows fallidas'}
foreach($arch in @('x86','x64')){
 $folder=Join-Path $workspace "build/Formas-al-oido-Windows-$arch"
 $history=Join-Path $folder 'smoke-data/sessions'
 New-Item -ItemType Directory -Force -Path $history | Out-Null
 $preserved=Join-Path $history 'session-preserved.json'
 Set-Content -LiteralPath $preserved -Value '{"test":"previous-session"}' -Encoding UTF8
 $before=@(Get-ChildItem -LiteralPath $history -Filter 'session-*.json').Count
 if(Test-Path -LiteralPath "$folder/smoke-result.txt"){Remove-Item -LiteralPath "$folder/smoke-result.txt"}
 $p=Start-Process -FilePath "$folder/Formas-al-oido.exe" -ArgumentList '--smoke-test' -WindowStyle Hidden -PassThru
 if(-not $p.WaitForExit(30000)){$p.Kill();throw "Prueba de apertura agotada: $arch"}
 $p.Refresh()
 $result=Get-Content -LiteralPath "$folder/smoke-result.txt" -Raw
 if($p.ExitCode -ne 0 -or -not $result.StartsWith('PASS')){throw "Prueba de apertura fallida: $arch $result"}
 if((Get-Content -LiteralPath $preserved -Raw).Trim() -ne '{"test":"previous-session"}'){throw "Se alteró una sesión anterior: $arch"}
 if(@(Get-ChildItem -LiteralPath $history -Filter 'session-*.json').Count -ne ($before+1)){throw "No se conservó una sesión nueva separada: $arch"}
 $project=Get-Content -LiteralPath (Join-Path $folder 'smoke-data/studio-current.json') -Raw | ConvertFrom-Json
 $payload=[Convert]::FromBase64String($project.payload)
 if($project.schema -ne 'formas-trace-v1' -or $payload[11] -ne 6){throw "No se conservó el último trazo al cerrar: $arch"}
 Write-Output "$arch $result"
}

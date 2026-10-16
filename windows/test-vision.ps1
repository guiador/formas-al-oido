param()
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$sample=Join-Path $root 'tools/ai-cache/bus.jpg'
if(-not(Test-Path -LiteralPath $sample)){Invoke-WebRequest 'https://ultralytics.com/images/bus.jpg' -OutFile $sample}
foreach($arch in @('x86','x64')){
 $folder=Join-Path $PSScriptRoot "build/Formas-al-oido-Windows-$arch"
 $result=Join-Path $folder 'vision-smoke.txt'
 if(Test-Path -LiteralPath $result){Remove-Item -LiteralPath $result}
 $process=Start-Process -FilePath (Join-Path $folder 'Formas-al-oido.exe') -ArgumentList ('--vision-test "'+$sample+'"') -WindowStyle Hidden -PassThru
 if(-not $process.WaitForExit(90000)){$process.Kill();throw "Tiempo agotado: $arch"}
 $process.Refresh();$text=Get-Content -LiteralPath $result -Raw
 if($process.ExitCode -ne 0 -or $text.StartsWith('FAIL')){throw $text}
 foreach($line in ($text.Trim() -split "`n")){$scene=$line | ConvertFrom-Json;if(@($scene.regions).Count -eq 0 -or -not(($scene.regions.label -join ' ') -match 'autobús|persona')){throw 'No se reconocieron categorías esperadas en la fotografía de prueba'};Write-Output ("PASS {0} {1}: {2} regiones. Esta comprobación no mide exactitud general." -f $arch,$scene.source,@($scene.regions).Count)}
}

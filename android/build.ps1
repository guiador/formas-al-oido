param([string]$AndroidSdk='', [string]$Jdk='')
$ErrorActionPreference='Stop'
$workspace=Split-Path -Parent $MyInvocation.MyCommand.Path
if(-not $AndroidSdk){$AndroidSdk=Join-Path $env:USERPROFILE '.codex\build-tools\evvdroid\sdk'}
. (Join-Path (Split-Path -Parent $workspace) 'tools/Build-Helpers.ps1')
$Jdk=Resolve-ProjectJdk $Jdk
$core=Join-Path (Split-Path -Parent $workspace) 'core'
$tools=Join-Path $AndroidSdk 'build-tools\35.0.0'
$androidJar=Join-Path $AndroidSdk 'platforms\android-36\android.jar'
foreach($required in @("$Jdk\bin\javac.exe","$tools\aapt2.exe",$androidJar)){if(-not(Test-Path -LiteralPath $required)){throw "No se encuentra: $required"}}
function Run([string]$exe,[string[]]$arguments){& $exe @arguments;if($LASTEXITCODE -ne 0){throw "Falló $exe, código $LASTEXITCODE"}}
$output=Join-Path $workspace 'build'
$classes=Join-Path $output 'classes'
$dex=Join-Path $output 'dex'
New-Item -ItemType Directory -Force -Path $output,$classes,$dex,(Join-Path $workspace 'private') | Out-Null
Reset-GeneratedDirectory $classes $output
Reset-GeneratedDirectory $dex $output
$sources=@(Get-ChildItem -LiteralPath (Join-Path $workspace 'src'),(Join-Path $core 'src') -Recurse -Filter '*.java' | ForEach-Object FullName)
Run "$Jdk\bin\javac.exe" (@('--release','8','-encoding','UTF-8','-classpath',$androidJar,'-d',$classes)+$sources)
Run "$Jdk\bin\jar.exe" @('--create','--file',"$output\classes.jar",'-C',$classes,'.')
Run "$Jdk\bin\java.exe" @('-cp',"$tools\lib\d8.jar",'com.android.tools.r8.D8','--lib',$androidJar,'--min-api','26','--output',$dex,"$output\classes.jar")
Run "$tools\aapt2.exe" @('link','-I',$androidJar,'--manifest',"$workspace\AndroidManifest.xml",'-A',"$core/assets",'-o',"$output\unsigned.apk")
Run "$Jdk\bin\jar.exe" @('--update','--file',"$output\unsigned.apk",'-C',$dex,'classes.dex')
Run "$tools\zipalign.exe" @('-f','4',"$output\unsigned.apk","$output\aligned.apk")
$keystore=Join-Path $workspace 'private\formas-pruebas.jks'
if(-not(Test-Path -LiteralPath $keystore)){Run "$Jdk\bin\keytool.exe" @('-genkeypair','-keystore',$keystore,'-storepass','android','-keypass','android','-alias','formas-pruebas','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=Formas al oido Prototype, O=Local development, C=PE')}
$apk=Join-Path $output 'Formas-al-oido-0.7.0.apk'
Run "$Jdk\bin\java.exe" @('-jar',"$tools\lib\apksigner.jar",'sign','--ks',$keystore,'--ks-key-alias','formas-pruebas','--ks-pass','pass:android','--key-pass','pass:android','--out',$apk,"$output\aligned.apk")
Run "$Jdk\bin\java.exe" @('-jar',"$tools\lib\apksigner.jar",'verify','--verbose',$apk)
Run "$tools\aapt.exe" @('dump','badging',$apk)
Get-FileHash -Algorithm SHA256 -LiteralPath $apk | Format-List
Write-Output "APK: $apk"

param([switch]$Android,[string]$Ndk='', [string]$Cmake='')
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$cache=Join-Path $PSScriptRoot 'audio-cache'
New-Item -ItemType Directory -Force $cache | Out-Null
function Fetch($name,$url,$hash){
 $path=Join-Path $cache $name
 if(-not(Test-Path -LiteralPath $path)){Invoke-WebRequest $url -OutFile $path}
 if((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $hash){throw "SHA256 no válido: $name"}
}
Fetch 'openal-source.zip' 'https://github.com/kcat/openal-soft/archive/refs/tags/1.25.2.zip' '47e22c066dffa2f65a1747272978344db845dcef9c7ee118449ec560a44adc8f'
Fetch 'openal-bin.zip' 'https://github.com/kcat/openal-soft/releases/download/1.25.2/openal-soft-1.25.2-bin.zip' '67a0c4b800bd860c93c04f38caf8cbe4875f9c84700ac430efc451f70e265434'
Fetch 'jna.jar' 'https://repo.maven.apache.org/maven2/net/java/dev/jna/jna/5.17.0/jna-5.17.0.jar' 'b3a9408e7c51e08ef0e3bfcc08f443f6ec0f6191ba8cd7c18d53d2b22e5bdbc0'
Fetch 'jna.aar' 'https://repo.maven.apache.org/maven2/net/java/dev/jna/jna/5.17.0/jna-5.17.0.aar' '4dbeffffa665d97ad5aa7eee297531d3c841a86716ab7f774fd6956422b3cf38'
Fetch 'libffi-LICENSE' 'https://raw.githubusercontent.com/java-native-access/jna/5.17.0/native/libffi/LICENSE' '2c9c2acb9743e6b007b91350475308aee44691d96aa20eacef8e199988c8c388'
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Extract-One($zip,$entry,$destination){
 New-Item -ItemType Directory -Force (Split-Path -Parent $destination) | Out-Null
 $archive=[IO.Compression.ZipFile]::OpenRead($zip)
 try{$item=$archive.GetEntry($entry);if(-not $item){throw "Falta $entry"};[IO.Compression.ZipFileExtensions]::ExtractToFile($item,$destination,$true)}finally{$archive.Dispose()}
}
$source=Join-Path $cache 'openal-soft-1.25.2'
if(-not(Test-Path "$source/CMakeLists.txt")){[IO.Compression.ZipFile]::ExtractToDirectory("$cache/openal-source.zip",$cache)}
$assets=Join-Path $root 'core/assets/openal'
New-Item -ItemType Directory -Force $assets | Out-Null
Copy-Item -LiteralPath "$cache/openal-source.zip","$source/COPYING","$source/LICENSE-pffft","$source/BSD-3Clause" -Destination $assets -Force
Copy-Item -LiteralPath "$source/fmt-11.2.0/LICENSE" -Destination "$assets/FMT-LICENSE" -Force
Copy-Item -LiteralPath "$source/gsl/LICENSE" -Destination "$assets/GSL-LICENSE" -Force
Copy-Item -LiteralPath "$cache/libffi-LICENSE" -Destination $assets -Force
Extract-One "$cache/jna.jar" 'META-INF/LICENSE' "$assets/JNA-LICENSE"
Extract-One "$cache/jna.jar" 'META-INF/AL2.0' "$assets/JNA-APACHE-2.0"
Extract-One "$cache/jna.jar" 'META-INF/LGPL2.1' "$assets/JNA-LGPL-2.1"
Extract-One "$cache/jna.aar" 'classes.jar' "$cache/jna-android.jar"
foreach($arch in @('x86','x64')){$upstream=if($arch -eq 'x86'){'Win32'}else{'Win64'};Extract-One "$cache/openal-bin.zip" "openal-soft-1.25.2-bin/bin/$upstream/soft_oal.dll" "$cache/$arch/soft_oal.dll"}
if($Android){
 if(-not $Ndk){$Ndk=$env:ANDROID_NDK_HOME}
 if(-not $Ndk){$Ndk=Join-Path (Split-Path -Parent $root) 'wicked quest/.toolchain/android-sdk/ndk/28.2.13676358'}
 if(-not $Cmake){$Cmake=Join-Path (Split-Path -Parent $root) 'wicked quest/.toolchain/android-sdk/cmake/3.22.1/bin/cmake.exe'}
 if(-not(Test-Path "$Ndk/build/cmake/android.toolchain.cmake") -or -not(Test-Path $Cmake)){throw 'Indica -Ndk (NDK r28+) y -Cmake (cmake.exe con Ninja) para compilar OpenAL Android.'}
 Copy-Item -LiteralPath "$Ndk/NOTICE" -Destination "$assets/NDK-NOTICE" -Force
 Copy-Item -LiteralPath "$Ndk/NOTICE.toolchain" -Destination "$assets/NDK-TOOLCHAIN-NOTICE" -Force
 $ninja=Join-Path (Split-Path -Parent $Cmake) 'ninja.exe'
 foreach($abi in @('armeabi-v7a','arm64-v8a','x86','x86_64')){
  $build=Join-Path $cache "build/$abi";$lib=Join-Path $cache "android/lib/$abi"
  New-Item -ItemType Directory -Force $lib | Out-Null
  Extract-One "$cache/jna.aar" "jni/$abi/libjnidispatch.so" "$lib/libjnidispatch.so"
  if(-not(Test-Path "$lib/libopenal.so")){
   & $Cmake -S $source -B $build -G Ninja "-DCMAKE_MAKE_PROGRAM=$ninja" "-DCMAKE_TOOLCHAIN_FILE=$Ndk/build/cmake/android.toolchain.cmake" "-DANDROID_ABI=$abi" -DANDROID_PLATFORM=android-26 -DANDROID_STL=c++_shared -DCMAKE_BUILD_TYPE=Release -DALSOFT_UTILS=OFF -DALSOFT_EXAMPLES=OFF -DALSOFT_INSTALL=OFF -DALSOFT_BACKEND_OPENSL=ON -DALSOFT_REQUIRE_OPENSL=ON -DALSOFT_BACKEND_OBOE=OFF -DALSOFT_BACKEND_WAVE=OFF -DALSOFT_ENABLE_MODULES=OFF -DALSOFT_UPDATE_BUILD_VERSION=OFF -DALSOFT_EMBED_HRTF_DATA=ON -DLIBTYPE=SHARED
   if($LASTEXITCODE -ne 0){throw "CMake: $abi"}
   & $Cmake --build $build --target OpenAL -j 2
   if($LASTEXITCODE -ne 0){throw "Compilación OpenAL: $abi"}
   Copy-Item -LiteralPath "$build/libopenal.so" -Destination $lib -Force
  }
  $triple=switch($abi){'armeabi-v7a'{'arm-linux-androideabi'};'arm64-v8a'{'aarch64-linux-android'};'x86'{'i686-linux-android'};'x86_64'{'x86_64-linux-android'}}
  Copy-Item -LiteralPath "$Ndk/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/$triple/libc++_shared.so" -Destination $lib -Force
  & "$Ndk/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-strip.exe" --strip-unneeded "$lib/libopenal.so" "$lib/libc++_shared.so"
  if($LASTEXITCODE -ne 0){throw "No se pudieron quitar símbolos de depuración: $abi"}
 }
}
Write-Output 'OpenAL Soft 1.25.2 y JNA 5.17.0 preparados; archivos originales verificados con SHA256.'

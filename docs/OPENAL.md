# OpenAL Soft en Android y Windows

Android 0.10.0 y Windows 0.5.0 añaden **OpenAL Soft HRTF · audífonos** en Sonido > Motor espacial. Los otros tres modos siguen disponibles. La selección se guarda y se aplica también al taller y a los componentes de imágenes. No necesita conexión a Internet ni modelos de IA.

Para comparar: comienza con volumen bajo, elige una actividad conocida y escucha su recorrido primero con KEMAR y después con OpenAL. Conserva los mismos valores de anchura, elevación, velocidad y perfil. Ambos emplean HRTF genéricas; una puede resultarte más comprensible que otra. OpenAL no proporciona una medición personalizada de tus oídos. Con un solo altavoz sigue siendo preferible el modo de código lateral; no se promete estéreo real.

El sintetizador compartido conserva timbres, altura tonal, intensidad, marcas de dirección y profundidad declarada. En el modo OpenAL produce una fuente mono **sin** panorama previo ni convolución KEMAR. OpenAL recibe su azimut y elevación, con actualización por bloques de 256 muestras a 44,1 kHz. La anchura y elevación de Sonido cambian esos ángulos; intercambiar oídos refleja el azimut. La profundidad conserva su atenuación existente, sin aplicarla de nuevo en OpenAL. No hay reverberación añadida que pueda ocultar los bordes.

Se utiliza una sola fuente en movimiento y una cola acotada de cuatro bloques (unos 23 ms de muestras, aparte de las latencias del dispositivo y del mezclador). Pausar, detener, cambiar de escena o cambiar de motor vacía la cola. La posición de los bloques pendientes sigue al emisor; esto no equivale a una simulación independiente por píxel. Bluetooth puede añadir demora considerable y necesita una prueba perceptiva propia.

Si OpenAL no puede inicializarse o no activa HRTF, el audio vuelve a KEMAR. Android ofrece el botón **Estado del motor de audio**; Windows anuncia la apertura de OpenAL o el uso del respaldo. No se cambia la preferencia guardada. Un error posterior de salida se informa; para volver a abrir un dispositivo desconectado puede ser necesario reiniciar la aplicación.

## Construcción y dependencias

La idea se contrastó con la integración de Wicked Quest. No se copia su código GPL: este proyecto mantiene su integración Java propia bajo MIT y enlaza dinámicamente OpenAL Soft 1.25.2 mediante JNA 5.17.0. OpenAL Soft tiene su licencia LGPL; JNA se utiliza bajo su alternativa Apache-2.0. Las licencias de las dependencias no se sustituyen por la licencia de la aplicación.

`tools/Prepare-OpenAL.ps1` descarga archivos originales con versiones y SHA256 fijos. Windows usa los DLL oficiales Win32 y Win64. Android compila el mismo código fuente con NDK r28 y CMake/Ninja para armeabi-v7a, arm64-v8a, x86 y x86_64, con OpenSL y datos HRTF embebidos. Las bibliotecas se mantienen compartidas y reemplazables mediante reconstrucción; el código fuente completo original de OpenAL y sus avisos se incluyen en cada paquete (`openal/openal-source.zip` en Windows; `assets/openal/openal-source.zip` dentro del APK). No se modifica OpenAL. JNA y las bibliotecas nativas deben conservarse junto al programa.

Para preparar Android en otra máquina, ejecuta `tools/Prepare-OpenAL.ps1 -Android -Ndk <ruta> -Cmake <cmake.exe>`. También reconoce ANDROID_NDK_HOME. En este equipo puede aprovechar las herramientas existentes de la carpeta hermana wicked quest. Después ejecuta `android/build.ps1`; para Windows, `windows/build.ps1` y `windows/package.ps1`. También existe el flujo manual OpenAL Android en GitHub Actions, con compilaciones independientes para las cuatro arquitecturas. Las fuentes y artefactos descargados están en tools/audio-cache, excluidos del repositorio. Los instaladores finales están en la subcarpeta Formas al oído.

## Validación y límites

`audio/test.ps1` prueba el mezclador OpenAL real mediante loopback sin altavoces, en los entornos Windows x86 y x64: lateralización, cambio por elevación, fuente mono, inversión, vaciado, cierre y dos contextos simultáneos en hilos distintos. También se prueba la cola en tiempo real con el dispositivo null: inicio, pausa, continuación, detener, contacto y cierre del hilo. La carga de la biblioteca se verifica desde nombres con espacios y tildes. Las pruebas del núcleo verifican los otros modos de audio y los recorridos. Las pruebas de protocolo y apertura Windows también deben pasar. Estas comprobaciones no miden localización humana, accesibilidad con lectores de pantalla ni latencia real.

La compilación Android se verifica con las herramientas del SDK, firma, alineación y presencia de dependencias en las cuatro arquitecturas. Por petición del usuario, esta entrega no se instala en un teléfono durante su preparación. Queda pendiente comprobar reproducción OpenAL, interrupciones, TalkBack, audífonos y cambios de salida en Android físico.

Fuentes originales: https://openal-soft.org/ y https://github.com/java-native-access/jna/tree/5.17.0

Las cuatro compilaciones nativas Android de esta entrega terminaron correctamente en https://github.com/guiador/formas-al-oido/actions/runs/39090500034 . Se verificaron los SHA256 de los artefactos, los de las bibliotecas y su arquitectura ELF antes de incorporarlas al APK. El APK contiene android-native-build.json con su procedencia.

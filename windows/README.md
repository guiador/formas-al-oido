# Formas al oído para Windows 0.1

Interfaz nativa Windows Forms (.NET Framework 4.8), con un proceso Java local incluido para reutilizar Geometry, Curriculum, TouchMap, ImageTrace y Sound del proyecto Android. No usa navegador, WebView ni servidor. El protocolo entre procesos sólo usa entrada y salida estándar. La síntesis PCM de 44.1 kHz sale por Java Sound al dispositivo predeterminado de Windows.

Incluye 29 actividades guiadas, figuras libres, recorrido por elementos, ratón con botón izquierdo, flechas con tres pasos, tacto mediante WM_TOUCH, perfiles Suave/Cuerda/Retro, estéreo/HRTF/mono experimental, contorno/relleno/negativo, importación local de imágenes, ejemplos conocidos y exportación JSON sin imágenes ni sus contornos. No es una copia completa de la interfaz Android: cámara en directo, cuestionarios y filtros por componente quedan pendientes.

Los botones/listas/campos de texto son controles nativos con nombres accesibles. La navegación por lienzo no atrapa Tab. La voz SAPI es opcional y usa voces instaladas, con preferencia por español. NVDA/Narrador y la percepción espacial requieren validación humana.

## Compilar

Se necesita Windows, compilador C# de .NET Framework, System.Speech de la instalación de .NET y un JDK para compilar con `--release 8`. build.ps1 acepta `-Jdk`. Los binarios publicados incluyen un JRE Temurin de Eclipse Adoptium, con las licencias originales intactas. No se exige instalar Java al usuario.

Para obtener los dos JRE de forma reproducible, consulta los JSON tools/temurin-x32.json y tools/temurin-x64.json. Sus enlaces son públicos oficiales; descarga cada ZIP, verifica su SHA256 y extráelo en tools/x32 o tools/x64. El script de compilación toma el primer directorio de cada carpeta. Se usan versiones diferentes para x86 y x64 porque son las que el proveedor devolvió para cada arquitectura. build-info.json conserva versión, origen y hash.

Ejecuta desde la raíz del proyecto:

```powershell
.\windows\build.ps1
.\windows\test.ps1
.\android\test.ps1
```

Se generan build/Formas-al-oido-Windows-x86 y build/Formas-al-oido-Windows-x64. Conserva toda la carpeta. La aplicación no tiene firma Authenticode ni instalador. Windows 10/11 con .NET 4.8; no se ha probado ARM64. La edición x86 también se verifica aquí en Windows x64 mediante WOW64; no equivale a una prueba en un sistema operativo de 32 bits.

## Háptica

XInput 1.4: detectar capacidades, probar pulso breve y activar vibración en cambios de contacto. La opción comienza desactivada en cada apertura. El control por cruceta/palanca es independiente y sólo actúa con foco en el lienzo. Se detienen los motores al perder foco, soltar/desactivar o cerrar. No se presupone control genérico del panel táctil, ratón o chasis de una portátil. Una prueba sin mando sólo confirma que su ausencia se maneja correctamente.

## Datos

Preferencias y sesión actual en `%LOCALAPPDATA%\FormasAlOido`. No se almacena una biblioteca de fotos. La importación crea una copia temporal PNG sin EXIF, limitada a 768 píxeles, que se elimina al terminar el análisis o al cerrar. El límite de entrada de 80 megapíxeles se comprueba tras abrir la cabecera con GDI+. No deben usarse imágenes de fuentes no confiables para esta prueba inicial.

La sesión registra ajustes, impresiones, actividad/variante o semilla de figura generada e identificadores de imagen aleatorios. No incluye rutas ni geometría de las fotos. La sesión previa permanece en session-current.json hasta el primer registro de la siguiente apertura; exporta antes de cerrar para conservar varias. No hay transmisión por red ni lectura automática desde este chat.

## Verificación

El núcleo conserva sus pruebas Android/JVM. engine-checks.cjs verifica las dos arquitecturas PE, ejecución de los dos entornos, 29 escenas, coordenadas, protocolo táctil/sonoro, huecos y cierre. --smoke-test construye la interfaz real, conecta el proceso local y verifica cambio de actividad, ejemplo con hueco y exportación; utiliza una carpeta de datos separada y salida de audio silenciosa. No verifica la calidad perceptiva ni dispositivos táctiles, mandos o lectores de pantalla físicos.

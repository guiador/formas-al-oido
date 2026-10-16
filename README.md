# Formas al oído

Exploración de figuras mediante sonido, tacto y entrenamiento guiado, creada con la participación de una persona ciega. El objetivo es investigar qué reglas sonoras ayudan a distinguir puntos, continuidad, bordes, curvas y regiones, y recoger experiencias para mejorarlas.

Prototipo abierto en desarrollo. Las interpretaciones de imágenes son aproximadas y no sustituyen una ayuda de movilidad. No hay evidencia todavía de eficacia pedagógica o transferencia de lo aprendido a otros contextos.

## Descargar y empezar

Android 0.9 / Windows 0.4: nueva exploración inteligente de capturas con componentes ampliables, dos variantes locales de SSD-MobileNet, Gemini y API compatible configurables. Las interpretaciones y analogías están separadas de la geometría. [Manual, privacidad, licencias y límites](docs/IMAGEN-INTELIGENTE.md).

[Descargas para Android y Windows](https://github.com/guiador/formas-al-oido/releases/latest).

- **Android 0.9:** un APK universal de 32/64 bits, Android 8 o posterior. Instala el APK y comienza en Guía. Firma local de pruebas, sin credenciales incluidas en el repositorio.
- **Windows 0.4:** paquetes portátiles x86 y x64, Windows 10/11 con .NET Framework 4.8. Descomprime toda la carpeta y abre Formas-al-oido.exe. Incluye el motor y su entorno Java; no debes instalar Java aparte. No tiene firma digital de editor. ARM64 no se ha probado.
- **Web:** prototipo inicial estático en `web/`, con alcance menor que las versiones nativas. Sirve esa carpeta desde localhost para probarla.

No necesitas pantalla táctil en Windows. En el lienzo puedes usar flechas, ratón manteniendo el botón izquierdo o un dedo en un monitor táctil. Mayúsculas permite pasos finos, Control pasos amplios, Inicio vuelve al centro, Espacio repite y Tab sale del lienzo. La vibración es opcional y, en Windows, se implementa para mandos XInput con motores compatibles.

## Taller de trazos y lectura

Android 0.8 y Windows 0.3 incorporan orientación, pausa y continuación, retroceso y repetición de cinco segundos, marcador temporal y dibujo por coordenadas. Consulta [el manual de orientación y las dos prácticas para clase](docs/ORIENTACION-Y-PRACTICA.md). La vista general describe geometría disponible; no reconoce objetos. Estas funciones aún necesitan pruebas con lectores de pantalla y equipos reales.

Nuevo: dibujo acompañado con sonido en directo, repetición por trazo o fragmentos, etiquetas, cierre de regiones y selección de capas. Lectura por filas, columnas, cuadrantes, silueta o contorno desde una esquina o punto elegido. Proyectos de trazos compatibles entre Android y Windows, sin incluir la foto. Ejemplos de cara esquemática, ruta y gesto cursivo. Mapas de alturas aportados con interpretación explícita.

Consulta [cómo usar el taller y sus límites](docs/TALLER-DE-TRAZOS.md) y [el formato de intercambio](docs/FORMATO-DE-TRAZOS.md). No incluye detección facial, profundidad inferida, interpretación clínica, DICOM ni colaboración remota en directo.

## Qué incluye

Las aplicaciones nativas comparten 29 actividades y un núcleo de geometría/audio: puntos, direcciones, huecos, intersecciones, curvas, polígonos, regiones y profundidad declarada. Hay tres perfiles de timbre, estéreo, HRTF KEMAR para audífonos y un código lateral experimental para altavoz único. Este último usa timbre y pulsación; no produce estéreo real con un solo altavoz.

Importación local de imágenes por contraste, con contorno/relleno/negativo y ejemplos conocidos. La extracción básica no identifica objetos; el reconocimiento opcional está en Imagen inteligente. No hay OCR ni profundidad inferida. Android incluye cámara con captura estática y análisis periódico. La cámara en directo de Windows queda pendiente, junto con los cuestionarios y algunas opciones avanzadas de Android.

Windows usa controles nativos accesibles por teclado. Android conserva TalkBack. Los lectores de pantalla, la respuesta táctil, las cámaras, los mandos y la utilidad perceptiva necesitan pruebas con usuarios y dispositivos reales.

## Código y compilación

| Carpeta | Contenido |
| --- | --- |
| `core/` | Geometría, actividades, audio, extracción de imágenes, datos HRTF y pruebas compartidas |
| `android/` | Interfaz Java nativa, cámara y adaptación de audio, compilación sin Gradle |
| `windows/` | Interfaz Windows Forms, puente local al núcleo y paquetes x86/x64 |
| `web/` | Prototipo HTML/CSS/JavaScript independiente |

Los archivos finales locales se reúnen en **Formas al oído/Android** (APK) y **Formas al oído/Windows** (ZIP x86/x64). Las carpetas build sólo contienen temporales de compilación.

Consulta [Android](android/README.md), [Windows](windows/README.md) y [la pauta para una sesión de clase](PRUEBA-EN-CLASE.md). Las instrucciones admiten rutas de SDK/JDK proporcionadas por quien compila. Las carpetas de compilación, claves privadas y resultados personales están excluidos de Git.

## Ayuda que necesitamos

Consulta [la evaluación técnica y de uso](docs/EVALUACION.md), [CONTRIBUTING.md](CONTRIBUTING.md) y [ROADMAP.md](ROADMAP.md). Son especialmente valiosas las pruebas con personas ciegas, NVDA/Narrador/TalkBack, monitores táctiles, mandos y distintos altavoces. También hacen falta mejoras pedagógicas, calibración sonora y evaluaciones que distingan impresiones de aciertos medidos.

No necesitas programar para colaborar: describe qué esperabas escuchar, qué escuchaste, con qué equipo y qué parte resultó confusa. Evita adjuntar fotos, voces o archivos con datos de otras personas sin su consentimiento. Los archivos de sesión no se envían automáticamente a ningún servicio.

Licencia MIT para el código del proyecto. Las mediciones KEMAR y los entornos Java conservan sus condiciones y atribución en [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).

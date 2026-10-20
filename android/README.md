# Formas al oído · Android nativo 0.13.0

Aplicación Java para Android 8.0 o posterior. No usa WebView. APK de pruebas para todas las arquitecturas, firmado con la misma clave que 0.3 para permitir la actualización.

## Nuevo: matemáticas

Abre Matemáticas para pizarra progresiva, operaciones, ecuaciones lineales, ejemplos algebraicos/trigonométricos y plano cartesiano sonoro. El cálculo está en Java y usa un TTS instalado; no necesita Python. [Manual y límites](../docs/MATEMATICAS.md).

## Imágenes y cámara

La pestaña Imagen permite importar archivos con el selector de Android, abrir cámara, congelar una captura o activar análisis periódico. Los ajustes de extracción están plegados al inicio. Incluye ejemplos locales de círculo, cuadrado con hueco y casa.

Procesamiento local acotado: luminancia, umbral automático ajustable para dibujos o bordes Sobel para fotos, trazado de fronteras de píxel y simplificación RDP. Resolución máxima de análisis 256 píxeles, presupuesto de contornos y 160 puntos por contorno. Los recortes interiores conservan su signo para relleno y negativo. No identifica objetos, texto ni profundidad. Los trazos gruesos pueden producir doble borde.

La cámara usa Camera2 y un único ImageReader YUV; se procesa solo luminancia. Entrega aproximadamente un fotograma por segundo, reducido y corregido por orientación del sensor y pantalla. El análisis periódico intenta una actualización cada dos segundos si la anterior terminó. Al explorar, se congela la geometría y se cierra la cámara. Al salir de la pantalla o pausar la app, también se cierra. La tasa real y compatibilidad deben comprobarse en el dispositivo.

La importación corrige orientación EXIF y compone transparencia sobre blanco, con decodificación reducida. La copia estática queda en privado; Guardar copia genera PNG sin EXIF y Eliminar copia borra la copia privada. Exportar sesión no incluye fotos ni sus geometrías, nombres o rutas: registra identificadores aleatorios de fuente/análisis, ajustes, cantidades e impresiones. No se envían imágenes por red.

Consultar PRUEBA-EN-CLASE.md para una comparación breve de figuras conocidas, imágenes y cámara.

## Empezar

1. Instala `../Formas al oído/Android/Formas-al-oido-0.13.0.apk` sobre la versión anterior. Exporta la sesión si quieres una copia adicional.
2. La app abre en **Guía**, con una actividad cada vez. **Empezar o repetir** reproduce la explicación y el recorrido. La voz es opcional.
3. Explora, pide una pista o usa **Me perdí** para escuchar centro y esquinas. **Lo encontré** guarda tu impresión; no exige acertar ni avanzar.
4. **Continuar** cambia de actividad. **Elegir una actividad** permite saltar libremente. La actividad y variante se guardan.
5. **Sonido** contiene calibración y ayudas. **Explorar** conserva el laboratorio. **Sesión** exporta datos y analiza JSON de Android o web.

## Guía y ayudas

29 actividades en cinco módulos: lienzo y punto; puntos en ocho direcciones y continuidad; relaciones entre líneas; curvas y polígonos; irregularidad y profundidad explícita. Las variantes cambian una propiedad anunciada. [el diseño histórico de la versión 0.4](../docs/archive/DISENO-ANDROID-0.4.md) documenta las decisiones y lo que queda pendiente.

- Marco acústico, aire espacial tenue opcional y proximidad diferenciada del contacto.
- Vibración opcional: un pulso de entrada y dos de salida, limitada en frecuencia. Puede mezclarse con las señales de TalkBack; requiere probar el dispositivo.
- Contorno, relleno y negativo. Con aire activo, el fondo conserva una señal tenue. Sin aire puede quedar en silencio.
- Histéresis de contacto. La tolerancia no prolonga los extremos abiertos para tapar huecos.
- Filtros por tipo o un componente concreto, conservando posición y geometría. Recuperar el conjunto quita el filtro.
- Orden de componentes desde centro, arriba izquierda, base, último toque u orden original. Conserva la dirección interna de los trazos. Los ejercicios de dirección entre puntos mantienen su orden pedagógico.
- Tras dos pedidos de referencia, una propuesta opcional ofrece aire, mayor tolerancia y menor velocidad. Se registra aceptación o rechazo. No hay adaptación oculta.

El lienzo lógico es de 600 × 450 unidades y conserva proporciones. Puede haber márgenes silenciosos. Las unidades no son milímetros ni un umbral personal medido.

## Sonido y controles

Tres perfiles procedurales: Suave, Cuerda y Retro. Cuerda es un timbre armónico, no modelado físico. Trayectorias continuas, ataques de punto, clic de esquina, referencias de giro y señales distintas de cierre y final abierto. Los movimientos y silencios entre componentes dependen de la distancia, con mínimos temporales pequeños.

HRTF KEMAR: convolución FIR de 128 muestras por oído y 157 posiciones a 44.100 Hz, con interpolación y espejo para la izquierda. Se ajustan panorama, elevación, refuerzo tonal, extremos grave/agudo, brillo, volumen, duración y pausas. Las señales breves conservan sus tonos propios. Es HRTF genérica, sin seguimiento de cabeza.

La profundidad demostrada tiene una z escrita explícitamente y se codifica por intensidad. Una elipse no se interpreta automáticamente como profundidad. La guía usa escenas construidas, sin IA conectada ni reconocimiento semántico de objetos.

Con TalkBack, usa los controles nativos. La app recibe eventos hover del lienzo, pendientes de validar en teléfono. No desactiva el lector. Sin lector, en **Lienzo a pantalla completa**, un dedo explora; dos dedos simultáneos o Atrás salen.

## Datos

Sin permisos de internet, micrófono, contactos ni almacenamiento general. Se solicita VIBRATE y CAMERA; el permiso de cámara se pide al abrirla. Importación y exportación mediante el selector de documentos de Android. La voz usa el motor TTS del teléfono.

La sesión contiene un UUID aleatorio, tiempos relativos, geometría, z declarada, versión sonora, actividad/variante, separación, ajustes, respuestas y esfuerzo. Se registran cambios de componente, no un trazado continuo del dedo. Los autoinformes y pedidos de ayuda se separan de los aciertos. Cada evento mantiene su versión sonora.

El archivo importado se resume localmente sin mezclarse con la sesión actual. No hay sincronización con Codex ni entrenamiento automático de modelos. Las comparaciones por perfil son descriptivas, sin controlar diferencias entre usuarios o ejercicios.

## Compilar y verificar

JDK 21, SDK platform android-36, Build Tools 35.0.0. Sin Gradle ni dependencias externas.

```powershell
.\test.ps1 -Jdk 'C:\ruta\al\jdk'
.\build.ps1 -AndroidSdk 'C:\ruta\al\sdk' -Jdk 'C:\ruta\al\jdk'
```

Se usan javac, D8, AAPT2, zipalign y apksigner. Conservar `private/formas-pruebas.jks` para actualizaciones; no incluirla en fuentes ni usarla como clave de publicación de producción.

Pruebas JVM de extracción: dibujos, fotos sintéticas, transparencia, inversión, huecos, componentes diagonales, detalle y presupuestos. Pruebas de regresión: 2.500 geometrías anteriores; 29 actividades × 16 variantes × 3 separaciones; límites, filtros, huecos, contacto, relleno/negativo, HRTF y PCM. Compilación y firma verificadas. No hay teléfono conectado ni emulador ejecutable en el entorno: TalkBack, TTS, vibración, latencia y utilidad perceptiva requieren prueba real.

El núcleo y las mediciones viven en `../core/`. Para regenerar `core/assets/kemar.bin`, descomprime los datos originales en `core/tools/kemar/` y, desde la raíz, ejecuta `node core/tools/pack-hrtf.cjs`. Los datos preparados ya están incluidos.

## Atribución

Bill Gardner y Keith Martin, MIT Media Laboratory, *HRTF Measurements of a KEMAR Dummy-Head Microphone*, 1994. Fuente y condiciones: https://sound.media.mit.edu/resources/KEMAR.html. Atribución completa en `assets/KEMAR-NOTICE.txt` y en la ayuda de la app.

## Altavoz único experimental (0.6)

En Sonido, Motor espacial, elige Altavoz único. Activa o desactiva Código lateral y ajusta su intensidad. Izquierda: timbre más apagado y pulsación más lenta; derecha: más brillante y rápida; centro neutro. El tono fundamental sigue representando altura. Comparar izquierda, centro y derecha reproduce tres posiciones a igual altura durante dos segundos cada una. La modulación no se aplica a los marcadores cortos.

Es un código aprendido, no estéreo virtual ni HRTF para un solo altavoz. Ambos canales reciben muestras idénticas; desactivarlo elimina la información lateral mono. Los tres perfiles permanecen disponibles y los ajustes quedan en preferencias y exportación de sesión. Su utilidad perceptiva aún debe probarse con usuarios.

El APK contiene bytecode DEX sin bibliotecas .so ni restricciones ABI: es universal para Android de 32 y 64 bits, incluidos ARM y x86, desde Android 8. No se ha ejecutado en hardware de ambas arquitecturas.

## Taller (0.7)

Nueva actividad nativa StudioActivity con grabación de trazos, fondo, capas y selección, lectura por cuadrícula, alturas aportadas y proyectos compartibles. Ver [manual del taller](../docs/TALLER-DE-TRAZOS.md). Conserva proyecto local y resultados de taller por separado. El JSON de resultados no contiene etiquetas, fotografías ni puntos de dibujo; el proyecto sí contiene trazos y debe revisarse antes de compartir.

## Exploración inteligente

Android 0.9 añade dos variantes locales de SSD-MobileNet y conexiones explícitas a Gemini y API compatible, con regiones ampliables e interpretaciones separadas de los contornos. Consulta docs/IMAGEN-INTELIGENTE.md. El permiso de Internet sólo se usa en este modo opcional al pulsar Enviar; el reconocimiento local no envía fotos.

## Corrección de instalación 0.9.1

El normalizador de rutas del APK comprimía resources.arsc. Android con destino API 30 o superior exige almacenarlo sin compresión y alineado a 4 bytes; esto provocaba el error de instalación -124. La compilación conserva ese recurso sin compresión y comprueba la alineación. Verify-Packages comprueba también el método ZIP del recurso.

Instalación por ADB y apertura correctas en Samsung SM-A065M con Android 16 (16 de octubre de 2026). Esta prueba no valida todavía la cámara, reconocimiento local ni interacción con TalkBack. En el otro teléfono, Xiaomi con Android 13, la política de instalación USB impidió llegar a comprobar el APK; INSTALL_FAILED_USER_RESTRICTED requiere permitir la instalación en el teléfono.

## OpenAL Soft 0.13.0

En Sonido > Motor espacial está OpenAL Soft HRTF, junto a KEMAR, estéreo y altavoz único. Consulta [la integración y las pruebas](../docs/OPENAL.md).

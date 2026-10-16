# Imagen inteligente: componentes, hipótesis y primer plano

Android 0.9.0 y Windows 0.4.0 añaden reconocimiento local y conexiones opcionales a APIs. El prototipo web conserva su alcance anterior. Estas funciones son experimentales y no demuestran eficacia pedagógica ni seguridad para movilidad o uso clínico.

## Empezar en Android

1. En Imagen importa una fotografía o congela una captura de cámara.
2. Pulsa **Explorar captura con IA y componentes ampliados**. Se abre una captura fija: la cámara no continúa cambiando la imagen mientras exploras.
3. El detector local compacto se ejecuta automáticamente. Puedes escuchar la vista general, seleccionar un componente y abrirlo en primer plano.
4. Con TalkBack, selecciona el componente en la lista y activa **Abrir componente seleccionado** con su gesto habitual de doble toque. Sin lector, también puedes hacer doble toque sobre una región del lienzo. La lista sirve de alternativa cuando los gestos del lector interfieren.
5. **Volver al conjunto** restaura la imagen original. Atrás vuelve primero al conjunto si hay una región ampliada.

Los cuatro ABI del APK son armeabi-v7a, arm64-v8a, x86 y x86_64. No se han probado aún los motores en un teléfono real; su disponibilidad, memoria, latencia, bibliotecas nativas y accesibilidad deben comprobarse en el dispositivo. La aplicación conserva la exploración geométrica cuando falla un motor.

## Empezar en Windows

Importa una foto en Imagen. El detector compacto se ejecuta automáticamente. Abre la pestaña **Imagen inteligente** para elegir región, escuchar la descripción, corregirla y abrirla en primer plano. También puedes abrir una región con doble clic, Intro sobre el lienzo o doble toque táctil. La navegación táctil y los lectores de pantalla requieren validación humana.

La versión Windows de esta entrega sigue importando capturas guardadas; no añade cámara en directo. Los paquetes x86 y x64 incluyen los dos modelos y sus runtimes. ONNX Runtime puede requerir que el equipo tenga instalado el runtime Microsoft Visual C++ 2015–2022 de su arquitectura; si no carga, la geometría sigue disponible. Información oficial: https://onnxruntime.ai/docs/install/.

## Motores disponibles

| Motor | Ejecución | Qué aporta |
| --- | --- | --- |
| SSD-MobileNetV1 compacto, int8 | Local, sin conexión | Hipótesis de objetos y cajas aproximadas |
| SSD-MobileNetV1 estándar, float32 | Local, sin conexión | Variante del mismo detector, útil para comparar resultados |
| Gemini Generate Content | API, cuenta del usuario | Vista general, hipótesis, analogías y regiones propuestas |
| API compatible con Chat Completions | API o servidor local compatible | Descripciones de un modelo de visión elegido por el usuario |

Las variantes locales usan los pesos COCO correspondientes. No detectan cualquier categoría y no reconocen caras, texto, profundidad ni hallazgos médicos. No garantizamos que una sea más rápida o exacta en todos los equipos. El umbral interno de detección no es un porcentaje de certeza comunicado al usuario. Los modelos locales marcan las identificaciones como «Podría ser…». El modo geométrico puede dar analogías de forma mediante reglas; eso no es un segundo modelo semántico.

## Configurar Gemini

Selecciona **Gemini · API**. Dirección base: `https://generativelanguage.googleapis.com/v1beta`. Introduce el nombre exacto de un modelo de visión habilitado en tu cuenta y su clave. Pulsa **Enviar esta imagen a la API elegida**.

El cliente usa `POST /models/{modelo}:generateContent`, imagen PNG en `inline_data`, cabecera `x-goog-api-key` y salida JSON. Referencia oficial: https://ai.google.dev/gemini-api/docs/generate-content/image-understanding.

## Configurar otra API

Selecciona **API compatible · Chat Completions**. Indica la URL completa del endpoint, por ejemplo `https://api.openai.com/v1/chat/completions`, el modelo con capacidad de visión y la clave correspondiente. Para un servidor instalado en el mismo dispositivo se permite HTTP en localhost/127.0.0.1; otros destinos requieren HTTPS. En Android localhost es el teléfono, no la computadora.

El cliente envía `messages` con texto e `image_url` como PNG Base64; espera JSON dentro de `choices[0].message.content`. El formato corresponde al protocolo documentado en https://developers.openai.com/api/docs/guides/images-vision. Un servicio compatible debe admitir imágenes y responder según el esquema solicitado; no basta con que tenga una API de texto.

No hay credenciales incluidas. No se hicieron consultas pagadas durante la implementación. Los protocolos se probaron con servidores simulados locales; la cuota, el modelo habilitado, las restricciones de cuenta y la calidad de respuestas reales quedan por verificar con el proveedor.

## Qué significa cada capa

- **Geometría extraída:** contornos obtenidos de contraste. Su extracción puede tener errores. Las regiones no identificadas siguen siendo explorables.
- **Hipótesis:** etiqueta propuesta por el detector local o la API; se muestra su procedencia.
- **Analogía:** un parecido que puede ayudar a imaginar la forma. No confirma la identidad del objeto.
- **Anotación del usuario:** una corrección para esta captura. No modifica los contornos ni entrena automáticamente al modelo.

Puedes ocultar hipótesis y analogías y escuchar sólo la geometría. Al ampliar se anuncia la posición de origen y el cambio de escala: el panorama y la altura sonora pasan a representar posiciones dentro de la región, no su posición en la imagen completa. Las cajas de detección son zonas de selección; no se convierten en falsos contornos del objeto. Se recortan y amplían los trazos extraídos; los bordes que cruzan el recorte permanecen abiertos, sin inventar cierres. No se ha añadido segmentación neuronal de siluetas.

## Privacidad y límites

Los motores locales no envían fotografías. Las APIs sólo reciben una imagen completa al pulsar el botón de envío. El usuario debe elegir el proveedor y tener en cuenta su tratamiento de datos y costes. No hay envío periódico de la cámara a APIs. La clave permanece durante la ejecución/pantalla, no se guarda en preferencias ni resultados. No se siguen redirecciones HTTP con credenciales.

Las interpretaciones y correcciones son temporales y no se exportan a las sesiones de resultados ni a proyectos de trazos. Los registros nuevos sólo indican el uso de un motor, sin incluir etiquetas, claves, endpoints ni fotos. Una captura Android queda en almacenamiento privado durante la pantalla y se elimina al salir normalmente; la copia previa de Imagen mantiene sus controles de eliminación. Las interrupciones o cierres forzados pueden dejar archivos temporales privados; no son un borrado garantizado.

La cámara sólo puede captar lo que queda dentro de su campo de visión. Esta versión no reconstruye una escena de 360 grados, no sigue la cabeza ni deduce lo que hay detrás de la cámara. Ampliar una región ofrece más tiempo y espacio para explorar, no recupera detalles ausentes de la captura.

## Distribución y pruebas

Licencias, avisos de terceros y ficha original están incluidos en `core/assets/vision/`, también dentro de los paquetes. `tools/Prepare-Vision.ps1` descarga los pesos y runtimes oficiales y verifica los SHA256 fijados en `tools/vision-dependencies.json`. El uso gratuito del proyecto no elimina las condiciones de sus dependencias ni de los servicios externos.

Se verificaron inferencias reales de ambos modelos locales en Windows x86 y x64 sobre una fotografía de prueba; es una comprobación técnica, no una medición general de exactitud. Las pruebas del núcleo verifican recorte, conservación de huecos, continuidad, validación de regiones, separación de analogías y los dos protocolos API con datos simulados. Falta evaluar latencia percibida, TalkBack/NVDA, doble toque real y comprensión con participantes.

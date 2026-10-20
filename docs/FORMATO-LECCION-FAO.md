# Lección Formas al oído v1 (.fao)

Prototipo de intercambio de una lección matemática entre Android 0.12 y Windows 0.7. Archivo JSON UTF-8 de hasta 262144 bytes, extensión `.fao`. No es ZIP ni contiene código ejecutable. Un profesor puede enviarlo como documento por cualquier servicio externo; la aplicación no crea cuentas ni envía mensajes.

Campos del documento:

| Campo | Contenido |
| --- | --- |
| `schema` | `formas-lesson-v1`; versiones desconocidas se rechazan |
| `title` | Título de hasta 160 unidades UTF-16 |
| `expression` | Expresión admitida por MathLesson, hasta 160 caracteres; el cálculo se vuelve a validar |
| `readings` | Seis listas: `tokens`, `digits`, `rows`, `columns`, `steps`, `development`; se conservan tal como se recibieron |
| `notes` | Mapa de hasta 128 anotaciones vinculadas a destinos explícitos |
| `feedback` | Comentario general, hasta 6000 unidades UTF-16 |
| `cursor` | `mode` 0–5, `position` válido en esa lista, `plane` 0–7, `x` e `y` finitos entre -5 y 5 |

Cada lista tiene entre 1 y 128 textos, cada uno de hasta 8192 unidades UTF-16, sujeto al límite global de archivo. El orden es parte del contenido docente. La expresión se recalcula para resultados y estructura; eso no valida las explicaciones aportadas por el autor. La interfaz conserva las listas cargadas incluso al preparar otra vez la misma expresión. Cambiar de expresión abre otra lección; hasta veinte anteriores se conservan en memoria durante la sesión. El archivo no contiene ese historial.

Destinos de anotación:

- `fragment:M:I`: modo de lectura M y posición I, ambos desde cero. Modos: símbolos, cifras, filas, columnas, procedimiento y pizarra progresiva. El índice debe existir en la lista guardada.
- `plane:K`: vista general del plano K.
- `point:K:X:Y`: punto del plano K; X e Y son enteros en décimas de unidad entre -50 y 50. Por ejemplo, `point:4:0:-20` es la coordenada (0,-2) de la parábola. Los puntos se redondean a décimas con la regla `floor(valor*10+0.5)`.

Una nota contiene cuatro textos: `name` (160), `description`, `hint` y `comment` (3000 cada uno). Los destinos inválidos y los caracteres de control, salvo tabulación y saltos de línea, se rechazan. Son textos planos: no se interpreta HTML, enlaces, rutas, comandos, SSML ni instrucciones de IA. La lectura usa el TTS elegido por la persona; el archivo no contiene preferencias de voz, volumen, sonido o modo de escucha y nunca inicia una reproducción al abrirse.

En este piloto, el editor permite escribir notas, pistas y comentarios; no editar manualmente la lista de transformaciones matemáticas. Los archivos preparados por otras herramientas pueden aportar sus propias listas dentro de estos límites, que la interfaz conserva y presenta como contenido del autor. No existe aún importador MathML/SVG, firma de autor, control de cambios ni resolución automática de respuestas simultáneas. Para varios alumnos se distribuye el mismo archivo y cada uno devuelve su propia copia. Los comentarios nuevos no se fusionan solos.

El formato cubre un ejercicio y uno de los planos matemáticos incorporados. No sustituye el proyecto de trazos ni conserva toda la app. Los viejos esquemas `formas-math-v1` y `formas-math-plane-v1` continúan funcionando con sus propios botones. Se necesita guardar FAO para conservar las nuevas notas y la secuencia congelada.

Los ejemplos están en [core/assets/teaching](../core/assets/teaching). Consulta [la prueba para el profesor](PRUEBA-DOCENTE.txt). Las pruebas del núcleo y el protocolo Windows cubren validación, conservación de etapas, Unicode, comentarios, anclajes, límites y reapertura; la prueba nativa comprueba los tres modos y el archivo escrito en disco. La utilidad pedagógica y la experiencia con lectores requieren evaluación de usuarios.

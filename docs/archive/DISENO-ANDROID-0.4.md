# Aplicación de las notas de exploración · versión 0.4

Documento histórico. La estructura y el estado actual se describen en [la evaluación](../EVALUACION.md) y [el README](../../README.md).

Se revisó el texto «Conocimiento del lienzo: límites tanto táctiles como acústicos», aportado el 15 de octubre de 2026. La implementación principal está en Android, que es el medio de prueba elegido en esta conversación. El sitio conserva el laboratorio web y distribuye el APK actualizado y sus fuentes.

## Decisiones pedagógicas

La aplicación abre en **Guía**, con una actividad cada vez. El recorrido es libre: se puede repetir, pedir una pista, recuperar referencias, saltar a cualquier actividad o continuar. No se desbloquean niveles por puntuación. La actividad y variante se conservan entre aperturas.

«Me perdí» reproduce centro y esquinas sin desplazar la figura. Tras dos solicitudes, se ofrece activar aire, ampliar ligeramente el radio de contacto y ralentizar el recorrido. La propuesta requiere pulsar «Aplicar esta ayuda»; se registra su aceptación o rechazo. No se cambian automáticamente los significados del sonido, ni se atribuye un umbral al participante.

«Lo encontré» guarda un autoinforme. No se registra como una respuesta correcta ni se mezcla con los cuestionarios existentes. Los resultados de sesión separan ambos tipos de evidencia.

## Correspondencia con las notas

| Propuesta | Aplicación concreta |
| --- | --- |
| Conocer el lienzo | Primera actividad con marco, sonido al acercarse a sus límites y vibración opcional. Las coordenadas de trabajo conservan proporción y pueden ocupar una parte de la pantalla. El marco lógico no se confunde con el marco físico del teléfono. |
| Calibración propia | Límites grave/agudo de altura tonal, anchura, elevación HRTF, refuerzo tonal, volumen, velocidad, brillo y tolerancia. Pruebas de los extremos y referencias espaciales. |
| Punto y vacío | Punto central estable, aire sonoro opcional de bajo nivel y una señal de proximidad distinguible del contacto. El contacto con el punto produce un ataque que decae. |
| Dos puntos en ocho direcciones | Variantes explícitas y separación ajustable. El cambio de variante conserva tamaño y centro. El recorrido de esta actividad mantiene su orden para que la dirección anunciada sea cierta. |
| Varios puntos y continuidad | Cadena de cinco puntos. Siguen representados individualmente al reducir la separación. No hay transformación automática que invente una línea. |
| Rectas y discontinuidades | Horizontal, vertical, diagonales, segmentos separados y un hueco central de tamaño controlado. La tolerancia no prolonga los extremos hasta tapar el hueco. El escaneo conserva silencio entre componentes, cuya duración depende de su separación. |
| Relaciones | Paralelas rotadas conjuntamente, convergentes, cruce y zigzag con longitudes distintas. No se añade una «voz fantasma» que pudiera mezclarse con la línea explorada. |
| Curvas y figuras | Círculo verdadero, trazo grueso, círculo abierto, semicírculo, media luna, elipse inclinada, triángulo, cuadrado, rectángulo, rombo, trapecio, romboide y figuras anidadas. |
| Relleno, contorno y negativo | Modos de contacto separados. Aire opcional: con aire activo el fondo es tenue; sin aire puede quedar en silencio. La vibración distingue entrada y salida y se limita en frecuencia. |
| Atención selectiva | Filtros de puntos aislados, trazos rectos, curvas o un componente concreto. Recuperar el conjunto no cambia posiciones ni tamaños. |
| Inicio del escaneo | Orden original o componentes próximos al centro, arriba izquierda, base o último toque. Cambia el orden de componentes, conservando la conectividad y sentido interno de los trazos. No se dibujan conexiones ficticias desde el centro. |
| Forma irregular | Contorno con abombamientos controlados. Cambia la trayectoria; no se asigna ruido como supuesto significado universal de «deformidad». |
| Profundidad | Demostración con coordenada z declarada: altura constante y cercanía codificada por intensidad. Se explica que es una escena construida; una elipse plana no se interpreta automáticamente como un círculo 3D. |

Son **29 actividades**, agrupadas en cinco módulos. Las distintas orientaciones se obtienen con «Otra variante controlada», sin presentar una gran lista inicial de posibilidades.

## Lo que no debe tomarse como un hecho demostrado

Las referencias del texto a discriminación «exacta», detección de 10 milisegundos, comprensión instantánea o universalidad de timbres son hipótesis o simplificaciones, no resultados de este proyecto. Ninguna cifra se usa para evaluar capacidades personales.

La guía enseña convenciones. Los datos permiten describir una sesión y comparar configuraciones; todavía no establecen eficacia pedagógica, transferencia a figuras nuevas ni umbrales sensoriales. Se necesita comprobar el comportamiento en teléfonos y hacer pruebas con participantes.

Dos puntos próximos pueden compartir zonas de tolerancia. Eso debe analizarse como interacción entre geometría y ayuda de contacto, no como prueba de que el oído los ha fusionado. Las distancias se expresan en unidades del lienzo, no milímetros: aún no hay medición física de pantalla validada.

El marco produce una señal y una vibración de todo el teléfono; no crea una superficie de relieve ni fuerza que detenga el dedo. La HRTF sigue siendo KEMAR genérica.

## Continuación de IA y escenas complejas

La guía de esta versión es determinista y funciona sin red. No se ha conectado un modelo local o una API ni se envían datos. Una futura IA podrá proponer explicaciones, orden y simplificación sobre una escena estructurada. La geometría comprobable seguirá siendo la referencia.

Para admitir imágenes externas se necesitará un contrato de escena con identificadores de componentes, puntos, curvas y aristas compartidas; pertenencia y adyacencia; procedencia y confianza; profundidad opcional marcada como conocida o desconocida. La salida del detector se validará antes de sonificar: continuidad, cierre, intersecciones y coherencia entre detalle y conjunto. Una jerarquía simple no basta para las aristas que pertenecen a dos regiones.

Quedan como trabajo posterior el reconocimiento de fotografías, el grafo general de intersecciones, el relieve físico, la medición de HRTF individual, la deformación 3D y las superficies con textura semántica. La escena de cruce enseña dos líneas intersectadas, pero aún no ofrece una navegación general por nodos de intersección compartidos.

## Verificación

- Compilación y firma del APK con la misma clave de pruebas que 0.3, para actualizar sin cambiar de aplicación.
- Pruebas JVM de 2.500 geometrías anteriores y de las 29 actividades, 16 variantes y tres separaciones por actividad.
- Comprobación de límites, filtros y recuperación de componentes, cierre y finitud, ausencia de puentes ficticios sobre huecos, contacto con histéresis, relleno/negativo, orientación de HRTF y PCM acotado.
- El entorno no tiene un teléfono conectado. TalkBack, TTS, vibración, latencia y utilidad perceptiva siguen pendientes de una prueba real.

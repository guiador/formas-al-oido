# Marco, vacío y barridos en Matemáticas

Android 0.13.0 y Windows 0.8.0 recuperan en el plano matemático las referencias sonoras de figuras e imágenes. Se reutilizan `TouchMap`, `ImageScan`, los programas de `Sound` y los mismos ajustes de salida, perfiles y HRTF. No es un motor de contacto separado.

## Qué se corrigió

El plano matemático rechazaba las posiciones fuera del intervalo graduado de -5 a 5. Como el marco del lienzo de 600 por 450 unidades queda más allá de ese intervalo, sus bordes resultaban inaccesibles al dedo y al teclado. Además, la exploración usaba tolerancia fija y aire desactivado, sin conservar la histéresis del contacto entre muestras. El lienzo Android consumía los eventos hover sin devolverlos al comportamiento de accesibilidad de View.

Ahora todo el rectángulo responde: marco, zona de referencia exterior al plano graduado, vacío, proximidad y trazos. El aire, la tolerancia, contorno/relleno/negativo y la vibración se ajustan como en el resto de la app. El marco siempre es una referencia del área de trabajo: no representa los límites de una función ni el borde físico de la pantalla.

## Android

Abre **Matemáticas → Abrir lienzo matemático a pantalla completa**, disponible cerca del principio. También puedes abrirlo desde el apartado del plano. En pantalla completa hay botones fijos para marco, centro y esquinas, barrido completo, siguiente sección, detener y volver. Un selector permite cambiar filas, columnas, cuadrantes, relleno o contorno. Atrás regresa a la lección.

En el apartado del plano puedes elegir:

- Respuesta al dedo: contorno, relleno o negativo.
- Todo, sólo figura o sólo ejes.
- Aire espacial tenue y proximidad; puede desactivarse.
- Vibración si el teléfono la admite y tolerancia de contacto.
- Granularidad y elemento del plano mediante controles nativos, con nombre y coordenadas.
- Inicio del barrido desde una esquina o el último punto explorado, separación de 20/40/60 unidades e inversión del contraste sonoro.

El lienzo tiene nombre de accesibilidad y conserva los eventos del lector. Al deslizar directamente dentro del lienzo se evita que el ScrollView se lleve el gesto; fuera de él puedes seguir desplazando los controles. Los selectores incluyen su valor en la descripción y la operación textual incluye números y signos verbalizados.

Los gestos efectivos con Jieshuo o TalkBack deben comprobarse manualmente en cada dispositivo. No se usan inspecciones `uiautomator dump` en el teléfono del usuario: esa herramienta puede suspender temporalmente otros servicios de accesibilidad. No se debe desactivar el lector para dar por válida una prueba de accesibilidad.

## Windows

El puntero y las flechas pueden llegar a todo el marco; antes se limitaban al plano graduado. Matemáticas incorpora botones de marco, referencia, filtro y barridos. **Ajustar vacío, contacto y vibración** abre los controles compartidos de Sonido y mando; al volver a Matemáticas, esos mismos ajustes se aplican al lienzo. La vibración depende de un mando XInput compatible y de su activación.

## Barridos y transporte

Filas, columnas y cuadrantes son barridos geométricos del plano, como los de imágenes; no son la lectura TTS por filas o símbolos de una expresión. El relleno exige una figura cerrada, como cuadrado o circunferencia: si no existe una región cerrada con el filtro elegido, se explica y se ofrecen otros modos. El contorno conserva los extremos y las señales de cierre. El contraste del barrido distingue tonos tenues del vacío y tonos más intensos de la figura; es aproximado a la separación elegida.

Puedes escuchar todas las secciones o avanzar una a una. Los programas se limitan a 180 segundos. Hay pausa, continuación, retroceso de cinco segundos, marcador y repetición, utilizando el transporte del motor común. Detener o comenzar una exploración táctil cancela el recorrido anterior. En Android esos controles de transporte están en el apartado de exploración; salir del diálogo de pantalla completa detiene su audio.

Se conserva la distinción entre plano graduado y marco. Moverse por la zona exterior no introduce coordenadas fuera de -5 a 5 en el archivo FAO: el cursor matemático guardado conserva la última coordenada válida. El inicio de un barrido puede usar la posición física del lienzo. Los ajustes de escucha siguen siendo locales y no cambian el formato FAO.

## Verificación

27 709 comprobaciones nuevas validan acceso a los cuatro bordes, aire activable, contacto, relleno, negativo, filtros y coincidencia de cada tramo sonoro con el barrido de ImageScan, para las ocho escenas, diferentes orígenes y modos. Se mantienen las pruebas matemáticas, de FAO y del núcleo. Windows x86/x64 verifica protocolo, marco alcanzable y barrido por cuadrantes en la interfaz nativa.

Android está compilado y firmado para cuatro arquitecturas. La instalación y la prueba manual con el lector se informan aparte; la compilación por sí sola no confirma la experiencia táctil o auditiva.

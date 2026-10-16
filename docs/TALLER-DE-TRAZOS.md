# Taller de trazos y lectura — Android 0.7 / Windows 0.2

## Qué se puede probar

Una persona dibuja o remarca y otra escucha el movimiento, en el mismo equipo. El trazo se conserva como puntos ordenados y tiempos, con separación al levantar el puntero. Después puede repetirse completo, por trazo o por fragmentos de hasta veinte segmentos. El ajuste de duración de Sonido ralentiza o acelera su reproducción.

También se puede recorrer una imagen por filas, columnas, cuadrantes, interior de silueta o contorno desde una referencia. Se usan muestras de una cuadrícula, no una reproducción de cada píxel original. La separación controla el detalle. X se representa con el motor espacial o código mono, Y con altura tonal y luminosidad con intensidad. Una señal breve separa filas, columnas o cuadrantes.

## Primera prueba en Windows

1. En Imagen importa un dibujo o foto. El original reducido aparece debajo de los contornos y trazos.
2. En Trazar y leer escribe una etiqueta y activa Dibujar. Con ratón mantén el botón izquierdo; en pantalla táctil desliza un dedo. Levantar termina el trazo. Escape detiene y desactiva el dibujo.
3. Desactiva Dibujar para explorar. Elige Sólo trazos, Sólo fondo, Fondo y trazos o Sólo trazo seleccionado. Los controles de teclado del lienzo siguen disponibles.
4. Selecciona un trazo y repítelo. Los fragmentos son bloques de hasta veinte segmentos, no letras ni partes semánticas reconocidas.
5. Para delimitar una zona, dibuja su borde y pulsa Cerrar último trazo como región. Esto añade el cierre entre final e inicio. Selecciona ese trazo como capa y usa Relleno de la silueta para escuchar muestras del fondo dentro de esa región.
6. Guarda el proyecto antes de cargar un ejemplo u otro proyecto. El taller conserva también una copia local del último proyecto. Las sesiones de resultados se guardan separadas.

## Primera prueba en Android

Abre Taller desde la navegación principal. Puedes importar un fondo allí o usar la última imagen guardada en Imagen. Los controles equivalentes permiten grabar, cerrar, deshacer, repetir, elegir capa y leer por secciones. El proyecto actual se conserva en privado al terminar un trazo.

Con TalkBack, las listas y botones permiten seleccionar y repetir trazos. La exploración por eventos de tacto y los gestos para dibujar requieren verificación en el teléfono; un acompañante puede dibujar usando los gestos de arrastre disponibles en su lector. La aplicación no desactiva TalkBack. Se incluye control por flechas con teclado externo; no se ha validado aún su comodidad con hardware real.

## Elegir el inicio y la lectura

- Esquinas: superior izquierda/derecha e inferior derecha/izquierda.
- Posición actual: mueve el puntero o dedo, termina el contacto y pulsa Usar posición actual como inicio.
- Próximo toque: pulsa Marcar inicio y toca el lugar deseado.

La figura conserva su posición y escala. En filas/columnas se empieza en la celda de muestreo próxima a la referencia y se completa el orden restante, incluyendo la vuelta al principio de la secuencia. En cuadrantes, la referencia elige el primer cuadrante. En contornos, se empieza en el vértice muestreado más cercano; las líneas abiertas pueden invertirse. No se fuerza una conexión entre componentes separados.

Leer siguiente permite escuchar una fila, columna o cuadrante cada vez. Si llegas al final, vuelve a primera sección. La lectura completa y las reproducciones están limitadas a 180 segundos. Los movimientos grabados conservan sus duraciones relativas al reproducir, con pausas individuales limitadas a dos segundos y señales opcionales de inicio, giro y final; esas señales agregan tiempo. No se registra presión del lápiz.

## Caras, cursiva, mapas y relieves

Hay tres ejemplos sintéticos: cara esquemática con rasgos etiquetados, ruta con giros y gesto cursivo. El tercero no representa una letra identificada. Los ejemplos reemplazan el proyecto de trazos actual después de confirmarlo.

En una fotografía de cara, una persona puede remarcar el óvalo o un rasgo y etiquetarlo. No hay detección automática de rasgos, reconocimiento de identidad ni estimación 3D. Las coordenadas izquierda/derecha corresponden al lienzo, no a la anatomía de la persona fotografiada.

Para relieves se admite una imagen que el usuario declara como mapa de alturas. Blanco representa mayor altura relativa mediante más intensidad; invertir cambia el sentido. No hay unidades físicas ni calibración en milímetros. Activarlo sobre una foto corriente no convierte sus sombras en profundidad. El recorrido sigue en el lienzo 2D y no reconstruye un objeto 3D.

## Imágenes radiográficas de estudio

Se pueden importar PNG/JPEG exportados para enseñanza y superponer marcas del docente. Los ajustes de luminosidad y contraste son de la copia de ocho bits y no equivalen a una implementación médica de ventana DICOM.

No se abren estudios DICOM ni se preservan sus parámetros clínicos, orientación, escala física o profundidad de bits original. La extracción automática es aproximada y no está validada para detectar hallazgos ni medir anatomía. El blanco de una radiografía no representa altura física. Para una práctica de fisioterapia, comenzar con una estructura previamente identificada por el docente y contrastar lo explorado con el material y su interpretación profesional.

## Compartir y recoger resultados

Guardar proyecto produce un `.formas.json` compatible entre Android y Windows. Incluye trazos, etiquetas, tiempos y cierres; no incluye el fondo. Comparte el fondo por separado sólo si corresponde y vuelve a importarlo con la misma orientación y proporción. Se mantienen las coordenadas del lienzo de 600 × 450; no existe calibración física de tamaños.

El proyecto puede revelar escritura, rasgos o ubicaciones aunque no incluya la fotografía. Revísalo antes de compartirlo. El JSON de resultados se exporta aparte y no contiene etiquetas, puntos del trazo ni fotos. El programa no envía archivos a este chat ni publica proyectos en GitHub.

La colaboración en directo funciona en un mismo dispositivo. Entre dos equipos se comparten proyectos mediante archivos; todavía no hay transmisión remota sincronizada del puntero. Los ejemplos automáticos y las pruebas de software no sustituyen una evaluación de escucha, TalkBack/NVDA/Narrador, tacto y mandos con usuarios reales.

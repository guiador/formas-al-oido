# Orientación, pausa y creación autónoma

Disponible en las aplicaciones nativas Android 0.8 y Windows 0.3. El prototipo web mantiene su alcance anterior.

## Escuchar con control

1. Inicia una actividad, contorno, trazo o barrido.
2. En Android abre **Orientación**; entrar en esta sección pausa sin borrar el recorrido. En Windows abre **Orientación y pausa**.
3. **Pausar** conserva la posición. **Continuar** prosigue desde ella.
4. **Retroceder cinco segundos** retrocede como máximo cinco segundos y queda en pausa. Pulsa Continuar para escuchar.
5. **Guardar marcador** conserva un punto del recorrido y pausa. **Volver al marcador** vuelve a ese punto y pausa.
6. **Repetir últimos cinco segundos** reproduce hasta cinco segundos anteriores al punto actual y pausa al llegar a él. Desde el inicio reproduce los primeros cinco segundos. Los silencios y las señales forman parte del intervalo.
7. **Dónde estoy en el recorrido** pausa y describe el tiempo transcurrido, el segmento sonoro y su posición aproximada. Un segmento sonoro puede ser una pequeña muestra de curva o una señal; no equivale necesariamente a un lado completo.

Detener, tocar el lienzo o cambiar de escena cancela el recorrido y su marcador. Hay un marcador temporal por reproducción; no se exporta al proyecto. Los controles también están disponibles dentro del Taller de Android. Los controles no recuperan un recorrido que se canceló.

## Obtener contexto

**Vista general** cuenta puntos, trazos abiertos, contornos cerrados y huecos marcados, e indica la extensión aproximada en el lienzo. Describe los datos disponibles, sin identificar objetos, inferir profundidad ni comprobar la calidad de una fotografía. El resumen global incluye toda la geometría de la capa aunque el recorrido use un filtro.

**Dónde dejé el puntero** describe la última posición de exploración y el componente más cercano. Su distancia se mide al trazo, no al interior de una región. Es diferente de la posición de reproducción: escuchar automáticamente no mueve el puntero.

El resumen del fondo importado se refiere a contornos extraídos por contraste; las etiquetas de los trazos son aportadas por su autor. Los perfiles conservan la gramática espacial existente. No se ha añadido IA semántica.

## Dibujar sin arrastrar

En **Taller** de Android o **Trazar y leer** de Windows:

1. Escribe una etiqueta y elige **Crear trazo por coordenadas** / **Crear trazo con este punto inicial**.
2. Introduce horizontal entre 0 y 600, y vertical entre 0 y 450. El origen está arriba a la izquierda.
3. Usa **Añadir punto al último trazo**. Cada punto se une al anterior mediante una recta. Por ejemplo, (100,100), (400,100), (400,300) produce un recorrido hacia la derecha y después hacia abajo.
4. Escucha el trazo. Puedes cerrarlo como región con al menos tres puntos, guardarlo y abrirlo en la otra plataforma.

La entrada por coordenadas asigna pasos de un segundo: son tiempos sintéticos, no mediciones del gesto de una persona. No permite prolongar un trazo ya cerrado. Deshacer elimina el último trazo completo; la edición individual de vértices y el rehacer siguen pendientes.

## Dos prácticas para la clase

### Reconocer y explicar

El docente prepara una figura sencilla sin anunciar su nombre. El participante explora primero sin pistas; luego puede pedir una descripción o referencia. Registrar la respuesta inicial, las ayudas solicitadas, el tiempo aproximado y el esfuerzo de 1 a 5 por separado. La descripción puede revelar la respuesta: no debe contarse como reconocimiento independiente.

### Reconstruir y transferir

El participante escucha un recorrido de dos o tres segmentos y lo reconstruye por coordenadas. Comparar orden, direcciones, continuidad y cierre, aceptando diferencias de escala según el objetivo. Al día siguiente repetir con un recorrido nuevo de estructura semejante para observar retención y transferencia. Esta propuesta es un protocolo manual; la app no calcula todavía una puntuación de reconstrucción.

Los eventos de uso de los controles se guardan en la sesión sin añadir coordenadas, etiquetas ni audio. Los proyectos de dibujo sí contienen puntos y etiquetas. Revisar y acordar por separado qué archivos se compartirán.

## Validación pendiente en equipos reales

Comprobar con TalkBack/NVDA: acceso a todos los controles, ausencia de superposición molesta de voces, pausa y retorno precisos al oído, latencia tras Continuar, tolerancia al cansancio y comprensión de las instrucciones. Probar altavoz mono, audífonos y los dispositivos disponibles. Las pruebas automáticas verifican posiciones y señales PCM, pero no demuestran aprendizaje ni sustituyen estas sesiones.

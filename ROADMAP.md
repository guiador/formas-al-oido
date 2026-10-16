# Próximos pasos

Orden recomendado: accesibilidad completa, comprensión de figuras simples, resultados comparables y estabilidad. Ver [la evaluación y sus prioridades](docs/EVALUACION.md).

## Accesibilidad y uso real

- Probar y corregir recorridos completos con NVDA, Narrador y TalkBack.
- Evaluar teclado, ratón, pantallas táctiles y mandos; documentar modelos y errores.
- Verificar orientación de cámara Android y estabilidad al congelar una escena.
- Mejorar el anuncio del estado del lienzo sin saturar la voz.

## Sonido y aprendizaje

- Comparar estéreo, HRTF y código mono mediante tareas de identificación controladas.
- Evaluar si timbre/pulsación lateral interfieren con altura, perfil o marcadores de giro.
- Diseñar progresión, pausas y vocabulario con docentes y participantes ciegos.
- Añadir cuestionarios equivalentes en Windows y distinguir rendimiento de autoinforme.
- Evaluar calibración HRTF y niveles de salida cómodos en dispositivos reales.

## Imágenes

- Investigar doble borde, filtrado de textura y simplificación de contornos sin inventar conexiones.
- Incorporar cámara estática/directa en Windows.
- Explorar descripción semántica opcional mediante IA, indicando incertidumbre y coste, sin tratarla como geometría comprobada.

## Distribución y colaboración

- Automatizar compilaciones reproducibles y comprobaciones en CI.
- Evaluar instaladores, firma de binarios y soporte ARM64.
- Definir formatos de estímulos y sesiones compartibles con consentimiento.
- Documentar resultados negativos y límites, además de los casos prometedores.

## Implementado en Android 0.7 / Windows 0.2

Trazado acompañado local, repetición temporal, capas y selección, proyectos comunes, lectura por filas/columnas/cuadrantes/silueta y contorno con inicio elegido. Ejemplos sintéticos y altura relativa aportada. Pendiente: validación humana, colaboración remota, extracción semántica, DICOM y medición clínica. Ver docs/TALLER-DE-TRAZOS.md.

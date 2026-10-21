# Revisión local de Genny y su integración en Formas al oído

20 de octubre de 2026. Fuentes revisadas: `../genny-tts/app/src/main/python/`, `app/src/main/c/genny_dsp.c`, servicio Android y motor `nvda-addon/synthDrivers/genny_engine/speech.py`. No se consultó una versión remota del proyecto. La carpeta de Genny no tiene repositorio Git local; sus cambios se hicieron directamente en esos archivos.

## Resultado e integración realizada

La articulación española R/RR del adaptador se puede incorporar al visualizador regenerando su banco de palabras. Ya se regeneraron las 2.228 entradas con `genny_bridge.render`, conservando voz femenina, 16 kHz, velocidad 1,12 y semilla 0. La R/RR usa los cierres y duraciones protegidas de `spanish_r.py`. No basta con importar `genny.speech.render_speech`: esa función original no incorpora la adaptación española del laboratorio.

El visualizador reproduce PCM precalculado mediante Java/AudioTrack o Java Sound. Su respuesta durante el uso no necesita el DSP C ni Python. Incorporar el motor completo cambiaría el tamaño, dependencias, cancelación y construcción de ambas plataformas; no es necesario para trasladar la mejora de pronunciación. Las palabras nuevas siguen deletreándose. La prosodia de frases completas sigue siendo una limitación del banco.

El generador ahora acepta tanto el checkout original como `genny-tts`. Separa la caché por hashes del adaptador, articulación, fuentes originales, biblioteca nativa cargada, generador, parámetros y versiones NumPy/SciPy. Antes, una nueva revisión podía reutilizar PCM antiguo por nombre de palabra. Los hashes quedan en `core/assets/voice/genny-es.json`. La escritura del banco usa un archivo temporal antes de sustituir el anterior.

## Cambios aplicados a la implementación C y su puente

- `genny_bridge.py`: validación de dimensiones, cobertura de coeficientes, tamaño de bloque, límites de enteros y normalización `a0 = 1` antes de pasar punteros a C. Una matriz A más corta que B podía provocar una lectura fuera de sus límites en C.
- `genny_bridge.py`: se recuerda el intento fallido de cargar la biblioteca. Antes se repetían todas las búsquedas por cada filtro al usar el respaldo SciPy.
- `genny_dsp.c`: índices de coeficientes con `size_t` y cálculo del final del bloque sin sumar enteros que pudieran desbordarse. No cambia la fórmula acústica.
- `tests/check_engine.py`: comparación C/SciPy con coeficientes variables, bloques de una muestra, bloque final parcial y entradas no contiguas; rechazo de matrices incompatibles; comprobación de que una biblioteca ausente se busca una sola vez.

Se recompilaron las bibliotecas DSP de Genny para ARM32, ARM64 y Windows x64. No se empaquetó ni instaló una nueva versión del laboratorio TTS o del complemento NVDA. La DLL que ya estaba copiada en el árbol NVDA no se sustituye con estos cambios.

## Medición y comprobaciones

Frase: «Pero, perro. Caro, carro. Tres, comer.», 16 kHz, voz femenina, velocidad 1,12. Mediana de cinco síntesis después de una preparación previa en este PC:

| Motor de filtrado | Síntesis completa |
| --- | ---: |
| Python/SciPy original | 839,129 ms |
| C mediante ctypes | 44,015 ms |

La relación fue 19,06 veces. La diferencia máxima entre muestras fue 6,72 × 10⁻¹⁴ aproximadamente. Es una medición de síntesis en este equipo, con ambas rutas usando la misma articulación R/RR; no mide primer arranque, reproducción, lector de pantalla ni latencia Android. Los tiempos varían con la carga y el equipo.

La repetición final dio 1.582,296 ms en Python/SciPy y 69,876 ms en C, una relación de 22,64 veces, con el mismo error numérico. El JSON de comprobación conserva esta última ejecución; la tabla anterior conserva la primera medición.

Pasaron las 16 pruebas del laboratorio, incluida la comparación numérica C/SciPy. Pasó la suite completa del núcleo del visualizador; después de sustituir el banco se repitieron sus 59 comprobaciones específicas de voz. La comprobación `tools/tests/check_genny_fork.py` verificó el hash y procedencia del banco y la igualdad exacta de «tres», «raíz», «cuatro», «sobre», la letra R y la conjunción «y» con el audio regenerado del laboratorio.

Los resultados reproducibles están en `core/tests/out/genny-fork-checks.json`. Se generaron `genny-r-original.wav` y `genny-r-fork.wav` en esa carpeta para comparación auditiva. La prueba no determina inteligibilidad humana. Las pruebas del núcleo recrean esa carpeta; ejecutar después la comprobación de Genny para recuperar las muestras.

El reproductor Windows `GennySpeech.java` también compiló contra el núcleo comprobado. La compilación Windows verificó dependencias y compiló el motor, pero se interrumpió al faltar `windows/tools/x32`, el entorno Java portátil requerido para el paquete. No se produjeron nuevos APK/ZIP del visualizador. Se detuvo la compilación completa de Android al reconstruir OpenAL. La validación de esta revisión corresponde al código, banco y DSP; falta verificar el conjunto empaquetado y escuchar la voz en dispositivos reales.

## Mejoras adicionales recomendadas

1. **Acelerar el núcleo real de NVDA.** El motor NVDA actual usa C para los efectos finales; sus filtros de síntesis principales siguen en los bucles Python generados por `_make_run`. No utiliza `tv_filter`. La mejora medida en el adaptador NumPy no demuestra la misma aceleración en NVDA. El siguiente paso sería trasladar a C la cadena completa con estado persistente y comparar su salida con la implementación actual.
2. **Unificar los efectos con y sin biblioteca C.** En Android los controles nuevos de de-esser, presencia, calidez y radio sólo se aplican cuando la biblioteca está disponible. En NVDA existen respaldos Python, pero cambian las fórmulas: de-esser dinámico en C frente a estante fijo en Python; radio con otras pendientes, frecuencia de corte y saturación. El mismo ajuste puede sonar distinto según la ruta. Conviene elegir una definición y probar su equivalencia antes de cambiar el timbre por defecto.
3. **Revisar la descripción de calidez.** La fórmula C `v + amount * .3 * (v * abs(v) - .15 * v)` es impar: para una señal simétrica no introduce los armónicos pares que promete el comentario de saturación asimétrica. Cambiar la fórmula sería una decisión acústica que necesita muestras y escucha comparativa.
4. **Cancelación dentro del cálculo nativo.** El comprobador Python no se ejecuta durante una llamada C. Los fragmentos pequeños ayudan, pero para lectura larga o fonemas explícitos conviene una API de bloques con estado y comprobaciones de cancelación entre bloques. No se deben reiniciar los estados del filtro en cada corte.
5. **Construcción explícita de dependencias nativas.** En la preparación de continuidad del 20 de octubre se corrigió `Compile-Dsp.ps1`: ahora exige los compiladores del destino solicitado antes de tocar las salidas. `-Target Android` construye ambas ABI y `-Target Windows` sólo la DLL; cada empaquetador solicita su destino. Sigue pendiente verificar los símbolos de una biblioteca cargada: detectar sólo `tv_filter` no garantiza que una DLL antigua exporte todos los efectos nuevos. Una entrega debe registrar también los hashes de sus bibliotecas.

La integración aplicada mantiene el alcance del visualizador y permite evaluar la R mejorada con sus reproductores actuales. La nueva síntesis completa, los cambios acústicos de los efectos y la portabilidad del núcleo NVDA quedan identificados como trabajo posterior, no como mejoras verificadas en esta revisión.

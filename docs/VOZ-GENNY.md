# Voz local Genny y lectura matemática

La aplicación incorpora un banco de palabras españolas sintetizadas con [Genny de ogomez92](https://github.com/ogomez92/genny), reproducido con código Java compartido. Android usa AudioTrack y Windows Java Sound. No se necesita SAPI, un TTS instalado, Python ni Internet para reproducirlo.

Es una voz sintética de respaldo, con concatenación de palabras y pausas. No conserva la prosodia de una frase sintetizada completa. Las palabras que no estén en el banco se anuncian mediante deletreo; esto incluye nombres propios o anotaciones nuevas. No se presenta como un port completo del sintetizador Python ni como una voz neuronal. Su inteligibilidad requiere una prueba auditiva con usuarios.

En Windows, **Usar voz local Genny incluida, sin SAPI** está en Sonido y mando. Se usa también como respaldo si no hay voz SAPI disponible. En Android, la opción está en Sonido y en Imagen inteligente; en Matemáticas aparece como el primer motor de la lista, incluso si Android no tiene ningún TTS instalado. Las opciones de voz instalada siguen disponibles.

La capa `MathSpeech` convierte los símbolos a texto antes de enviarlos a cualquier voz: `x^2 = 1/2` se pronuncia «equis elevado a dos igual a uno sobre dos». Los decimales mantienen sus cifras después de la coma. También se convierten operadores Unicode, comparaciones, porcentajes, paréntesis, raíces y nombres de funciones. El texto visual, los cálculos y las anotaciones guardadas conservan su contenido original.

El reproductor permite cancelar una frase al detener o cambiar de pantalla. La exploración matemática espera a que termine la voz para volver a producir tonos de contacto. En el mapa de objetos, mantener el contacto dentro de la misma zona no vuelve a iniciar la etiqueta.

## Banco y reproducción

`core/assets/voice/genny-es.bin` contiene audio PCM mono de 16 kHz con claves de vocabulario. Incluye el vocabulario de los controles, las categorías del detector, los términos matemáticos y las letras españolas para deletreo. `genny-es.json` registra revisión de Genny, parámetros y SHA256 del banco. Ambos se incluyen en el APK y en los paquetes Windows.

`core/test.ps1` comprueba la lectura del banco, los términos matemáticos, el deletreo, los límites del PCM y la salida sin dependencias de voz del sistema. Produce `core/tests/out/genny-matematicas.wav` con el mismo banco que usan las aplicaciones. Las pruebas automáticas no verifican comprensión humana ni los altavoces de un teléfono real.

## Regenerar

La generación requiere Python, numpy y scipy. Acepta un checkout original o la bifurcación local `genny-tts`, con su adaptador `genny_bridge.py` y articulación española `spanish_r.py`. El banco actual usa esta bifurcación y contiene la R/RR mejorada. El DSP C acelera la construcción; las aplicaciones siguen reproduciendo PCM sin cargar Python ni la biblioteca C de Genny. Ejecuta:

```
python tools/Build-Genny-Voice.py RUTA_AL_CHECKOUT_DE_GENNY
# O bien, desde este proyecto:
python tools/Build-Genny-Voice.py ../genny-tts
```

La caché se guarda en `tools/tts-cache/`, excluida de Git. Se separa automáticamente por hashes del código Python, biblioteca nativa cargada, generador, versiones numpy/scipy y parámetros de voz. Cambiar la articulación de R o el DSP ya no reutiliza audio antiguo. `genny-es.json` registra esos datos además de la revisión original. El código Python de Genny y sus dependencias no se incorporan a las aplicaciones.

Con el Python y dependencias del laboratorio, `python tools/tests/check_genny_fork.py ../genny-tts` verifica procedencia, hash y coincidencia exacta del banco en palabras con R, letras y la conjunción «y». También compara el filtro C con SciPy, mide síntesis en el equipo y genera dos WAV de R/RR en `core/tests/out/`. Estos tiempos no miden latencia Android ni comprensión auditiva. Véase [la revisión técnica](REVISION-GENNY-C.md).

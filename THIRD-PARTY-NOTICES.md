# Componentes de terceros

## Voz generada con Genny

El banco `core/assets/voice/genny-es.bin` se genera con el sintetizador de formantes Genny de ogomez92: https://github.com/ogomez92/genny. La revisión, parámetros y hashes están en `core/assets/voice/genny-es.json`, incluido en las aplicaciones. Se distribuye audio sintetizado, junto con un reproductor propio; no se incorpora el código Python de Genny, numpy ni scipy. Véase `docs/VOZ-GENNY.md` para alcance, deletreo de palabras nuevas y regeneración.

## Mediciones HRTF KEMAR

Bill Gardner y Keith Martin, MIT Media Laboratory, 1994. Copyright MIT Media Laboratory. El conjunto se ofrece sin restricciones de uso, con atribución a sus autores al usarlo en investigación o aplicaciones comerciales.

Fuente y condiciones: https://sound.media.mit.edu/resources/KEMAR.html

El archivo `core/assets/kemar.bin` contiene una selección reempaquetada de las respuestas compactas. Las posiciones izquierdas reflejan los oídos. Véase `core/assets/KEMAR-NOTICE.txt` para el detalle. La licencia MIT de nuestro código no sustituye las condiciones del conjunto de datos.

## Eclipse Temurin / OpenJDK en los paquetes Windows

Los paquetes portátiles incluyen un JRE Eclipse Temurin para evitar una instalación adicional. Conservan LICENSE, ASSEMBLY_EXCEPTION, THIRD_PARTY_README y los demás avisos originales dentro de runtime. OpenJDK se distribuye bajo GPL versión 2 con Classpath Exception y los avisos adicionales incluidos. Nuestro código se licencia por separado bajo MIT.

Los JSON en `windows/tools/` y build-info.json de cada paquete identifican binario, versión y SHA256. Código fuente correspondiente de los entornos redistribuidos:

- x86, 8u472-b08: https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u472-b08/OpenJDK8U-jdk-sources_8u472b08.tar.gz
- x64, 8u504-b01: https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u504-b01/OpenJDK8U-jdk-sources_8u504b01.tar.gz

Proyecto y compilaciones: https://adoptium.net/ y https://github.com/adoptium/temurin8-binaries
## OpenAL Soft y JNA

OpenAL Soft 1.25.2, copyright de sus autores, bajo LGPL (véanse sus archivos COPYING y avisos originales). Se enlaza dinámicamente, sin modificaciones. El código fuente original completo se entrega en openal/openal-source.zip en Windows y assets/openal/openal-source.zip en Android, junto con los avisos de OpenAL, pffft, fmt y GSL. Fuente: https://github.com/kcat/openal-soft/tree/1.25.2

JNA 5.17.0 se utiliza bajo la alternativa Apache-2.0 de su licencia dual Apache-2.0/LGPL-2.1-or-later; incluye los avisos JNA-LICENSE, JNA-APACHE-2.0, JNA-LGPL-2.1 y libffi-LICENSE. Fuente: https://github.com/java-native-access/jna/tree/5.17.0 . La integración propia de Formas al oído no copia el código GPL de Wicked Quest.

## Reconocimiento de imágenes

La entrega Android 0.9 / Windows 0.4 incorpora ONNX Runtime 1.16.3 (MIT), paquetes auxiliares Microsoft/.NET (MIT) y SSD-MobileNetV1 opset 12 de ONNX Model Zoo en variantes int8/float32. Se incluyen las licencias, avisos de terceros, ficha del modelo y nota de procedencia en `core/assets/vision/`. La ficha específica declara MIT y se conserva también la licencia Apache-2.0 del repositorio de modelos. Los servicios Gemini y APIs compatibles se configuran por el usuario; no se redistribuyen sus modelos remotos.

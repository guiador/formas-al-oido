# Componentes de terceros

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
# Reconocimiento de imágenes

La entrega Android 0.9 / Windows 0.4 incorpora ONNX Runtime 1.16.3 (MIT), paquetes auxiliares Microsoft/.NET (MIT) y SSD-MobileNetV1 opset 12 de ONNX Model Zoo en variantes int8/float32. Se incluyen las licencias, avisos de terceros, ficha del modelo y nota de procedencia en `core/assets/vision/`. La ficha específica declara MIT y se conserva también la licencia Apache-2.0 del repositorio de modelos. Los servicios Gemini y APIs compatibles se configuran por el usuario; no se redistribuyen sus modelos remotos.

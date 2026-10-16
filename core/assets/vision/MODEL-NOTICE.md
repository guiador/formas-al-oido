# Reconocimiento local y licencias

Los binarios incluyen SSD-MobileNetV1 opset 12 del ONNX Model Zoo, en sus variantes float32 y cuantizada int8. Ambos detectan las categorías COCO admitidas por sus pesos. No son clasificadores universales de escenas ni modelos de segmentación. La cuantización y el modelo completo pueden producir predicciones diferentes; no se promete que una variante sea mejor para todos los equipos o imágenes.

- Ficha original: https://github.com/onnx/models/tree/main/validated/vision/object_detection_segmentation/ssd-mobilenetv1
- La ficha del modelo declara SPDX MIT / MIT License. Se incluye también la licencia Apache-2.0 del repositorio ONNX Model Zoo, sin sustituir las condiciones específicas indicadas por sus autores.
- ONNX Runtime 1.16.3: Microsoft, MIT. Se conservan su licencia y sus avisos de terceros en licenses/.
- System.Memory, System.Buffers, System.Numerics.Vectors y System.Runtime.CompilerServices.Unsafe: paquetes de Microsoft/.NET, MIT; licencias y avisos adjuntos.
- Modelos y runtimes se descargan de sus repositorios oficiales durante la preparación. tools/vision-dependencies.json fija sus versiones, URL, SHA256 y archivos extraídos.

Las etiquetas españolas corresponden a IDs COCO de TensorFlow; son traducciones del proyecto. Los archivos de resultados o capturas de usuarios no se incluyen en estos modelos. Una corrección manual sólo afecta a la interpretación de esa captura, no reentrena el modelo.

Gemini y las API compatibles son servicios externos opcionales, no pesos distribuidos por este proyecto. Su disponibilidad, costes y condiciones corresponden al proveedor y a la cuenta configurada. Las solicitudes sólo se realizan al pulsar Enviar esta imagen a la API elegida.

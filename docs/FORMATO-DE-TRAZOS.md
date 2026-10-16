# Formato de proyecto de trazos v1

Contenedor JSON UTF-8, máximo 2 MB en los importadores:

```json
{"schema":"formas-trace-v1","payload":"BASE64"}
```

El payload se interpreta exclusivamente como datos binarios, sin ejecutar código ni abrir rutas. No contiene imagen ni nombre de archivo original. Se decodifica en el núcleo compartido TraceDrawing.

Estructura big-endian (DataOutputStream de Java): magic int32 `0x46415452` (FATR), versión int32 `1`, cantidad de trazos int32. Por trazo: etiqueta en UTF modificado de Java (writeUTF), boolean de cierre, cantidad de puntos int32. Cada punto contiene X float64, Y float64 y tiempo int64 en milisegundos. No hay campos restantes; datos adicionales se rechazan.

Límites: 128 trazos, 20.000 puntos totales, etiqueta de 120 unidades UTF-16, X entre 0 y 600, Y entre 0 y 450, números finitos, tiempo entre 0 y 86.400.000 y no decreciente dentro de cada trazo. El orden de la lista determina el orden de reproducción. Los tiempos son relativos a la captura, no fechas; al incorporar un proyecto y seguir dibujando puede empezar un nuevo origen temporal. Las pausas entre trazos se acotan al reproducir.

El cierre es una decisión explícita del usuario: la geometría y reproducción completan el último segmento hacia el inicio. No se intenta adivinarlo por cercanía. Las trayectorias rasterizadas a partir de fotografías no se convierten en trazos de escritura con supuesto orden original.

Las etiquetas son aportadas por quien dibuja. No deben interpretarse como detecciones del modelo. La geometría puede contener información personal; el archivo de proyecto es distinto de la exportación de resultados sin geometría.

# Matemáticas y pizarra progresiva

Android 0.11.0 y Windows 0.6.0 incorporan un taller matemático propio. En Android abre **Matemáticas**; en Windows elige la pestaña del mismo nombre. El cálculo y la geometría se resuelven localmente en Java: no necesitan Python, una cuenta ni una API. La voz es la del motor TTS instalado que elijas; su conexión, descargas y privacidad dependen de ese motor.

## Aprender el desarrollo, no sólo el resultado

Introduce una expresión y pulsa **Preparar expresión**. La lectura inicial es **Pizarra progresiva: cambios y motivos**. Usa Fragmento siguiente, anterior y Leer o repetir: no hay avance automático. La lista permite saltar a cualquier etapa. **Leer expresión completa** recuerda el punto de partida y **Explicar estructura** distingue agrupaciones, fracciones y potencias.

En expresiones aritméticas agrupadas, cada transformación conserva el antes y el después de la expresión completa, verbaliza la parte que se sustituye y explica el orden de resolución. Por ejemplo, `(3+2)^2+7` conserva el siete mientras el grupo se convierte en cinco y la potencia en veinticinco; sólo después suma treinta y dos. Las fracciones explican el denominador común cuando corresponde.

Para dos enteros, las etapas recorren las columnas y explican llevadas, préstamos encadenados, productos parciales o cifras bajadas en una división. La lectura de columnas va de izquierda a derecha; el procedimiento de suma y resta comienza por unidades, a la derecha. Son dos recorridos distintos, anunciados por el nombre de la columna.

En ecuaciones lineales se explica qué operación se hace en ambos miembros y por qué conserva la igualdad. Por ejemplo, en `2x+3=11` se reducen los miembros a `2x-8=0`, se resta menos ocho en ambos lados para obtener `2x=8` y se divide entre dos para llegar a `x=4`; se comprueba sustituyendo. Es un procedimiento válido, aunque un docente podría elegir otros pasos equivalentes. La expansión algebraica y la trigonometría tienen explicaciones por etapas, con menos detalle que estas transformaciones aritméticas y lineales.

También puedes elegir símbolos, cifras, filas, columnas o procedimiento sin contexto adicional. **Leer resultado** es una acción explícita. El resultado sigue visible en el cuadro y en los modos de lectura; no es un examen con respuesta oculta. Si editas la entrada, debes prepararla de nuevo antes de leer o exportar sus resultados.

Este taller no observa la secuencia de una pizarra real ni reconoce fórmulas en una foto. Un futuro modo docente podría registrar cada renglón, identificar qué se añadió o borró y permitir volver al estado anterior; todavía no se implementa esa captura.

## Ejemplos y límites

- `28+17`, `1000-1`, `23*14`, `125/4`: procedimientos de dos enteros no negativos de hasta seis cifras.
- `1/2+1/3`, `(3+2)^2`, `-3^2`: fracciones exactas, agrupación y precedencia.
- `2x+3=11`, `(x+2)*(x-3)`: ecuaciones lineales y desarrollo de polinomios de una incógnita hasta grado cuatro.
- `sin(30)`, `cos(60)`, `tan(45)`, `sqrt(9)`: valores numéricos; grados por defecto.
- `rad: sin(pi/2)`: radianes explícitos. `sin(30)^2+cos(30)^2` muestra una coincidencia numérica, no una demostración simbólica de una identidad.

Aritmética racional exacta con +, -, *, /, ^ y paréntesis. Se aceptan coma decimal, signos ×, ÷, − y superíndices ²/³. Máximo 160 caracteres, 64 símbolos, doce cifras por número, 16 niveles de anidación y resultados racionales de hasta 256 bits. Los exponentes aritméticos son enteros entre -12 y 12. La división entre cero y cero elevado a cero se rechazan.

Álgebra admite coeficientes racionales, multiplicación implícita como `2x` y división por una constante. No resuelve ecuaciones cuadráticas, sistemas, matrices ni expresiones con otras incógnitas. Trigonometría calcula en coma flotante y anuncia siempre aproximación, mostrada con hasta diez decimales; no promete diez decimales de exactitud. Rechaza dominios no reales, tangentes próximas a sus polos y magnitudes fuera del límite del taller. No es un sistema de álgebra computacional general.

## Plano y alternativas inspiradas en calculadoras parlantes

Ocho dibujos: ejes con dos puntos, segmento diagonal, cuadrado, circunferencia, parábola, seno, coseno y tangente en un intervalo sin polos. Origen central, equis aumenta a la derecha, ye hacia arriba, coordenadas de -5 a 5. Las funciones dibujadas usan equis en **radianes**; sus descripciones lo indican.

Puedes mover el punto con controles nativos, introducir coordenadas, explorar el lienzo, describir la posición o escucharla. Hay recorrido ordenado con primer punto, anterior y siguiente; las figuras cerradas anuncian el retorno al inicio y las abiertas su final. Las coordenadas del recorrido se anuncian redondeadas a una décima. **Punto de la función en la equis indicada** calcula la ye de los ejemplos funcionales dentro de su dominio; se rechaza en cuadrado, circunferencia o conjunto de puntos porque no hay una única ye.

Las descripciones geométricas distinguen extremos, esquinas, líneas, curvas, mínimo de la parábola, ejes e interior frente a espacio vacío. Se basan en dibujos matemáticos conocidos, no en inferencias sobre fotografías. Puedes activar marcas al cruzar los ejes (880 Hz en vertical, 440 Hz en horizontal) y anuncios de cambios; la voz pausa el sonido para evitar superposición.

Hay modo **Conocer botones** para escuchar el nombre de un botón sin ejecutar su acción, salvo Detener y Volver. El historial conserva hasta veinte ejercicios durante la sesión. Son implementaciones propias inspiradas en necesidades que también cubren calculadoras parlantes como Orion: no contienen software TI, no emulan una TI-84 ni ofrecen todas sus funciones. El modo de conocer botones no explica todavía una secuencia completa de teclas de calculadora.

## Voces, intercambio y Genny

Selecciona una voz o motor instalado y ajusta su velocidad. Si se superpone con TalkBack, NVDA o Narrador, desactiva la voz propia del módulo; las explicaciones siguen disponibles como texto. La navegación usa controles nativos, pero su experiencia con lectores de pantalla y la percepción espacial necesitan pruebas de usuarios en equipos reales.

Guarda ejercicios con esquema `formas-math-v1` (expresión; el resultado y los pasos se regeneran al abrir). Los planos usan `formas-math-plane-v1` (tipo de dibujo, x e y validados). No almacenan un historial de pasos ya leídos. Los selectores de importación limitan el archivo a 16 KB. También se exporta la explicación completa como texto.

Genny, de Oriol Gómez, es una biblioteca Python de audio y voz, no un solucionador matemático. No se encontró una licencia explícita de redistribución en el repositorio examinado. Por ello no se incorpora ni se porta su código. **Exportar guion para Genny externo** genera JSON con una capa de voz y todos los signos expandidos a palabras para quien disponga de ese programa por separado. No lo ejecuta, no instala Python y no garantiza pronunciación o compatibilidad con futuras versiones. El TTS nativo es la alternativa incluida.

Referencias: [Genny](https://github.com/ogomez92/genny) y [Orion TI-84, información del fabricante](https://www.orbitresearch.com/products/blindness-products/talking-calculators/orion-ti-84-plus-talking-graphing-calculator/).

## Verificación y pendientes

Pruebas automáticas del núcleo: resultados exactos, signos, precedencia, llevadas/préstamos, dominios rechazados, ecuaciones, trigonometría, intercambio, geometría y conservación del contexto en las transformaciones. Windows: protocolo Java y apertura de las interfaces x86/x64, controles de lectura, préstamos, historial, ecuación y esquina del cuadrado. Android: compilación universal, firma y empaquetado; esta entrega no se instala en un teléfono por petición del usuario.

Pendiente de evaluar en clase: claridad de las explicaciones, número adecuado de etapas, fatiga por repetición, navegación con lectores y aprendizaje. No hay todavía registro de respuestas del alumno, comparación con un paso propio, seguimiento del progreso entre sesiones ni evidencia de eficacia pedagógica.

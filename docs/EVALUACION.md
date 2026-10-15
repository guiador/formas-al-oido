# Evaluación del proyecto — 15 de octubre de 2026

## Dictamen

El prototipo es viable para experimentar con figuras conocidas y recoger impresiones. Todavía necesita validar que una persona pueda aprender las reglas, reconocer figuras sin pistas y repetir ese resultado en su dispositivo. La cantidad de opciones y de pruebas automáticas no demuestra utilidad pedagógica.

La prioridad que propongo es estabilizar una experiencia breve: calibrar, escuchar una figura simple, explorarla, responder y guardar el resultado. La incorporación de imágenes reales y cámara amplía las posibilidades, pero también incorpora errores de extracción que pueden confundirse con dificultades de aprendizaje.

Esta evaluación corresponde al código de main después de la reorganización. Los binarios publicados en windows-v0.1.0 siguen siendo los originales de esa entrega; no se reemplazaron con archivos diferentes bajo el mismo nombre. Las compilaciones locales realizadas aquí verifican la nueva estructura.

## Qué hay y qué significa

| Área | Estado comprobable en el código | Límite actual |
| --- | --- | --- |
| Geometría y guía | 29 actividades con variantes controladas, huecos y referencias | Falta validar progresión, comprensión y fatiga con participantes |
| Audio | Tres perfiles; estéreo, HRTF genérica y código mono | Falta medir discriminación, interferencias entre señales y latencia real |
| Exploración | Contorno/relleno/negativo; teclado, ratón, tacto y mando según plataforma | Lectores de pantalla, contacto táctil y vibración requieren pruebas de hardware |
| Imágenes | Extracción local acotada por contraste, huecos y simplificación | Textura, sombras y trazos gruesos pueden producir contornos difíciles o dobles |
| Cámara | Capturas y análisis periódico en Android | Falta validación de orientación/dispositivos; Windows no tiene cámara directa |
| Resultados | Autoinformes y eventos locales; exportación sin fotografías ni sus contornos | Formatos y tareas no son equivalentes entre plataformas; falta análisis común |
| Distribución | APK universal y paquetes Windows x86/x64, repositorio público | Firma de editor, instalador, automatización y ARM64 pendientes |

## Qué se limpió y consolidó

- El núcleo Java, sus pruebas y KEMAR pasan a core/. Android y Windows lo compilan desde allí. Windows ya no depende de una ubicación interna de Android.
- El diseño de Android 0.4 se archiva como documento histórico. Sigue disponible porque explica decisiones que no conviene perder.
- Se retira la pantalla antigua Entrenar, sin acceso desde la navegación actual, junto con su función auxiliar. La guía de 29 actividades y el entrenamiento de Exploración se conservan.
- Se eliminan variables sin uso y se corrige el rótulo Android que aún decía 0.5.
- Las compilaciones vacían clases/DEX temporales antes de compilar, para no empaquetar clases eliminadas. Los paquetes Windows se reconstruyen desde una carpeta limpia y actualizan el entorno cuando cambia su procedencia declarada.
- Se centralizan búsqueda de JDK y eliminación acotada de rutas generadas. Hay un limpiador con vista previa mediante -WhatIf; no incluye claves de firma ni archivos personales.
- Se corrige la conservación de sesiones Windows: cada sesión nueva tiene su propio archivo. Un session-current.json anterior no se elimina. Los errores al guardar muestran un aviso para permitir exportar.

Los APK antiguos, ZIP y carpetas de empaquetado no estaban versionados en GitHub. La limpieza local retira residuos regenerables y mantiene las descargas actuales y los entornos necesarios para compilar. No se reescribe el historial Git ni se borran entregas públicas.

## Qué falta primero

1. **Accesibilidad de principio a fin.** Instalar, calibrar, entrar/salir del lienzo, repetir, responder y exportar usando NVDA/Narrador o TalkBack. Registrar bloqueos concretos, modelo de equipo y versión. Debe poder completarse sin ayuda visual.
2. **Pruebas de comprensión controladas.** Comenzar con punto, recta y tres figuras sencillas. Mantener volumen, perfil y tamaño. Comparar reconocimiento con ayuda y sin ella; evitar que la voz revele la respuesta durante la identificación. Registrar aciertos, confusiones, tiempo aproximado y esfuerzo por separado.
3. **Cuaderno de sesiones utilizable.** Unificar unidades, nombres de motores y esquema de eventos entre plataformas; añadir visor/importador de historial Windows, recuperación después de fallos y mensajes de error de persistencia consistentes. Conservar archivos es sólo la primera parte.
4. **Estabilidad de audio y cámara.** Medir latencia y cortes en equipos reales; probar cambios de salida, auriculares, mono/estéreo, orientación y pausa/reanudación de cámara. Las pruebas actuales de apertura de Windows usan audio silencioso y no validan la salida física.

## Qué sugiero después

- Un inicio básico que muestre una actividad y pocas acciones. La calibración avanzada queda disponible para quien quiera investigar; no hace falta presentarla toda al empezar.
- Dos instrumentos breves de respuesta: una elección cerrada para identificar forma/dirección y una impresión abierta o escala de esfuerzo. Registrar siempre qué estímulo y ajustes se usaron. Las respuestas abiertas no se califican automáticamente como aciertos.
- Una comparación del código mono activado/desactivado y de sus tres perfiles. Hay que comprobar si brillo y pulso ayudan con la posición sin dificultar altura, continuidad o marcadores.
- Una colección pequeña de imágenes sintéticas conocidas, con resultados esperados. Evaluar fotografías sólo después de distinguir fallos de extracción de fallos de interpretación sonora.
- Separar gradualmente responsabilidades de MainActivity.java y FormasWindows.cs: interfaz, sesión, preferencias e importación. Son archivos muy concentrados, aunque el núcleo ya está separado. Hacerlo por partes con comprobaciones, evitando una reescritura total.
- Automatizar pruebas del núcleo y web en CI; después ampliar a compilaciones nativas. Las descargas de entornos deben quedar verificadas y sus licencias conservadas.

## Qué está de más o puede esperar

**Ya sobraba:** la pantalla de entrenamiento inaccesible, variables sin uso, clases temporales acumuladas y copias locales de empaquetado. Se retiran sin perder actividades accesibles.

**Puede esperar:** IA para describir escenas complejas, reconstrucción 3D, personalización HRTF profunda, más perfiles, nuevas arquitecturas e instaladores elaborados. Son líneas posibles; hoy aumentarían las variables antes de saber cuáles reglas ayudan.

**Conviene acotar:** el motor web es una implementación independiente y menos completa. Mantenerlo como demostración ligera evita asumir tres interfaces con la misma cobertura. Una equivalencia completa requeriría pruebas compartidas y una decisión de arquitectura adicional.

**Conservaría:** los tres perfiles actuales, las señales de referencia, el código mono opcional, las variantes controladas, la atribución KEMAR y las pruebas del núcleo. Permiten investigar preferencias y regresiones; su eliminación ahora reduciría comparabilidad.

## Verificación y alcance de esta revisión

Se comprueban el núcleo JVM, el prototipo web, compilación/firma Android y las dos compilaciones Windows con sus pruebas de protocolo y apertura. También se comprueban conservación del historial Windows y límites del limpiador. Resultado: pasaron las pruebas JVM de geometría/audio e imágenes, las 2.500 figuras del prototipo web, la compilación y firma Android, y las pruebas de protocolo/apertura de Windows x86 y x64. Se comprobó que el empaquetado descarta archivos obsoletos de una carpeta anterior y que las sesiones se guardan sin reemplazar las previas. La limpieza local retiró 21 destinos generados y liberó unos 265 MiB.

No se han probado aquí lectores de pantalla con una persona, cámara en teléfono, mandos, pantalla táctil ni discriminación sonora humana. No se atribuyen ganancias de rendimiento al sintetizador: requieren medición antes de optimizarlo.

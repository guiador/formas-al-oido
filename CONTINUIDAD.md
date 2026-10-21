# Continuidad — blind visualizer

Actualizado el 20 de octubre de 2026. Formas al oído: exploración geométrica, matemática, imágenes y voz local en Android y Windows.

## Punto de partida

Repositorio de trabajo: https://github.com/guiador/formas-al-oido (public). Se conserva el historial y la procedencia; esta organización no convierte los componentes de terceros en código propio.

core: geometría, audio, MathSpeech, GennyVoice y SceneMap; audio: OpenAL; android y windows: interfaces; web: prototipo separado.

## Construcción y comprobación

core/test.ps1, android/build.ps1 y windows/build.ps1. Windows requiere los JRE fijados por sus JSON de herramientas; SDK/JDK se pueden indicar explícitamente.

core/test.ps1; windows/test.ps1 cuando los paquetes estén construidos. tools/tests/check_genny_fork.py comprueba el banco frente a Genny.

Comprobaciones ejecutadas durante la preparación de continuidad:

- APROBADA: `pwsh.EXE -NoProfile -File core/test.ps1` (21.14 s).
  Resultado: PASS 15548 math checks: exact arithmetic, precedence, carry/borrow, division, linear equations, polynomials, trig units, rejected domains, serialization, progressive development and Cartesian geometry.; PASS 23 teaching checks: authored stages, coordinate/fragment anchors, feedback, inert text, cursor, bounds and portable FAO roundtrip.; PASS 27709 math exploration checks: frame, air, contact, fill, negative, filters and identical shared image scans.

Las comprobaciones automáticas describen sólo lo ejecutado. No certifican accesibilidad humana, funcionamiento de todos los dispositivos ni equivalencia entre un paquete anterior y el código actual. La evidencia histórica conserva su fecha y versión.

## Entrega y entorno local

En el equipo de trabajo, el índice único es `Codex/Entregas/Formas al oído`. `Codex/Entregas/LEEME.md` y sus manifiestos identifican los paquetes conservados y sus SHA-256. Las entregas anteriores a esta revisión no deben presentarse como compilaciones de este commit. Los paquetes y entornos no forman parte del código fuente.

Las herramientas locales se reúnen en `Codex/Soporte/Herramientas`; los enlaces de compatibilidad conservan las rutas de los scripts. En otro equipo se deben preparar las dependencias indicadas por este proyecto. No copiar ciegamente rutas de Guillermo ni tratar un entorno virtual Windows como un instalador portable.

Las claves de firma y configuraciones privadas se conservan localmente y se excluyen de Git. No se sustituyen para crear una actualización; usar las claves existentes o documentar una nueva identidad de paquete. No incorporar cuentas, bases, fotos ni documentos de usuarios a las pruebas.

## Próximo trabajo útil

Completar la verificación de paquetes con el banco R/RR actual y probar la voz, contacto y lectores en dispositivos. Revisar docs/REVISION-GENNY-C.md antes de portar más DSP. La voz concatenada deletrea vocabulario nuevo.

Antes de editar, leer el alcance vigente y revisar `git status`. Mantener separados cambios de producto, construcción y limpieza. Conservar las APIs, formatos y licencias salvo que la tarea requiera cambiarlos y se pueda verificar la migración. Las instrucciones posteriores del usuario prevalecen sobre esta nota.

## Referencias

- [README.md](README.md)
- [docs/VOZ-GENNY.md](docs/VOZ-GENNY.md)
- [docs/REVISION-GENNY-C.md](docs/REVISION-GENNY-C.md)
- [docs/EVALUACION.md](docs/EVALUACION.md)

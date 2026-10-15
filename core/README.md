# Núcleo compartido

Android y Windows compilan estos mismos archivos Java, sin dependencias de Android ni de la interfaz Windows:

- Geometry: figuras, elementos y descripciones geométricas.
- Curriculum: 29 actividades y variantes controladas.
- TouchMap: contacto, proximidad, contorno, relleno y negativo.
- Sound: programas de recorrido, perfiles, estéreo, HRTF y código mono.
- ImageTrace: extracción local de contornos por contraste.

`assets/` contiene las mediciones KEMAR y su atribución. Cada aplicación las incluye en su paquete. `tests/` verifica geometría, audio, extracción y límites; se ejecuta desde la raíz con `core/test.ps1 -Jdk RUTA`. `android/test.ps1` conserva compatibilidad con las instrucciones anteriores y delega aquí.

Las compilaciones admiten `-Jdk`, JAVA_HOME o javac en PATH, y conservan una ruta local de respaldo. Se necesita JDK 21 para las herramientas de compilación actuales. El bytecode resultante es compatible con Java 8. El navegador mantiene un motor JavaScript separado y de menor alcance; no se presenta como equivalente al núcleo.

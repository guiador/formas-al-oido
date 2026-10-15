# Cómo colaborar

Puedes abrir un issue para contar una experiencia, proponer una actividad o informar de un fallo. Para un cambio de código, abre una rama y un pull request que explique el problema, el comportamiento resultante y cómo lo comprobaste.

## Informar de una prueba

Indica aplicación y versión, sistema operativo, lector de pantalla si lo usas, salida de audio y método de entrada. Describe la actividad concreta, lo que esperabas y lo que ocurrió. En mono experimental compara con el código activado y desactivado, al mismo volumen y sin cambiar simultáneamente el perfil.

Separa «me pareció claro» de «identifiqué correctamente la figura». No deduzcas eficacia pedagógica o clínica de una sesión breve. Prueba primero figuras conocidas y cambia una variable cada vez.

No publiques nombres de participantes ni imágenes de otras personas. Revisa el JSON antes de adjuntarlo. Aunque la exportación omite fotos y sus geometrías, puede vincular respuestas dentro de la sesión mediante identificadores. Una imagen aparentemente anónima puede seguir conteniendo texto o rasgos identificables.

## Cambios de código

- Conserva teclado, nombres accesibles y salida del lienzo con Tab. No desactives lectores de pantalla.
- Mantén separadas geometría, significado sonoro y ayudas opcionales. No añadas ruido que cambie las reglas sin explicarlo.
- Los cambios del núcleo se comparten entre Android y Windows. Ejecuta `android/test.ps1`; para Windows ejecuta además `windows/build.ps1` y `windows/test.ps1`.
- La versión web tiene su propia prueba: `node web/test.cjs` desde la raíz.
- Indica qué dispositivos y lectores se probaron realmente. Las pruebas automáticas no sustituyen la escucha ni las pruebas táctiles.
- No subas claves de firma, credenciales, entornos descargados, resultados personales ni archivos de compilación al código fuente.

Al contribuir, aceptas que tu aportación se distribuya con la licencia MIT del proyecto, respetando las licencias de terceros.

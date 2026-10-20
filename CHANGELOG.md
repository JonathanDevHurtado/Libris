# Changelog

Todos los cambios relevantes de **Libris** se documentan aquí. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [1.0.0] — 2026-10-20

Primera instantánea pública del proyecto original creado en Google AI Studio.

### Añadido

- Biblioteca de demostración con portadas, progreso, favoritos y filtros.
- Lector de PDF con animación Page Curl 3D y modo deslizamiento.
- Temas de lectura Claro, Oscuro, Sepia y OLED.
- Narrador con `TextToSpeech`, control de velocidad, tono y selección de voz.
- Historial de sesiones de lectura.
- Pantalla Explorar con pestañas de Wattpad, clásicos y Google Drive (maqueta).
- Asistente literario con Gemini y respaldo local sin conexión.
- Pantalla de ajustes con cuentas de Google/Wattpad simuladas.
- Layout adaptativo con `NavigationBar` en teléfono y `NavigationRail` en tablet.
- Documentación de arquitectura, funcionamiento, dificultades, privacidad y
  verificación.
- Licencia propietaria source-available PSAL-1.0.
- CI de GitHub Actions, plantillas de issues/PR y Dependabot.

### Cambiado

- El correo personal que aparecía en la cuenta de demostración se sustituyó por
  `lector@example.com`.
- El test de Robolectric usa `sdk = [35]` para poder ejecutarse con la versión de
  Robolectric del proyecto.

### Notas

- No se modificó la arquitectura ni el comportamiento de la app.
- La aplicación sigue siendo una **demo**: no hay persistencia, OAuth real ni
  sincronización en la nube.
- `versionCode` y `versionName` se mantienen en `1` y `1.0`.

[1.0.0]: https://github.com/JonathanDevHurtado/LIBROUS/releases/tag/v1.0.0

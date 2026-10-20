# Privacidad y datos — Libris

Libris es una demo local. No tiene servidor propio, cuentas reales ni analítica.
Este documento describe exactamente qué datos toca la app.

---

## Qué se guarda en el dispositivo

| Dato | Ubicación | ¿Persiste? |
|------|-----------|------------|
| PDFs importados | `filesDir` privado de la app | Sí, hasta borrar datos o desinstalar |
| PDFs de ejemplo | `filesDir` / caché de la app | Se regeneran al iniciar |
| Progreso, favoritos, historial | Memoria (`BookRepository`) | **No** |
| Ajustes de lectura | Memoria (`ReaderSettings`) | **No** |
| Estado de cuentas | Memoria | **No** |

No hay `SharedPreferences`, Room, DataStore ni base de datos. Al cerrar el proceso
se pierden el progreso, los favoritos, el historial y los ajustes.

### Copia de seguridad de Android

El manifest declara `android:allowBackup="true"` y las reglas de backup son las de
ejemplo de Android Studio (sin inclusiones ni exclusiones). Por tanto, si el
usuario tiene activada la copia de seguridad de Android, **los PDFs guardados en
`filesDir` podrían incluirse en la copia en la nube del dispositivo**. Los datos en
memoria no se copian.

Para una versión de producción conviene excluir los PDFs o desactivar el backup.

---

## Qué se envía a Gemini

El asistente solo llama a Gemini si `BuildConfig.GEMINI_API_KEY` contiene una clave
distinta del placeholder. Si no hay clave, o la llamada falla, responde localmente
y **no sale ningún dato del dispositivo**.

Cuando sí hay clave, la petición HTTPS a `generativelanguage.googleapis.com`
incluye:

- el email de la cuenta de demostración (`lector@example.com`);
- los títulos, autores y formatos de **todos** los libros de la biblioteca;
- las **tres** entradas más recientes del historial, con su porcentaje;
- la pregunta escrita por el usuario.

No se envían los archivos PDF ni el texto completo de los libros.

La clave de API viaja en la URL de la petición y queda registrada en
`BuildConfig`, dentro del APK. Cualquiera puede extraerla. Para producción se debe
usar un backend intermedio que no exponga la clave.

Consulta la política de Google para Gemini:
<https://ai.google.dev/gemini-api/terms>.

---

## Permisos

| Permiso | Motivo |
|---------|--------|
| `INTERNET` | Llamadas opcionales a Gemini |
| `<queries>` TTS | Descubrir voces del sistema en Android 11+ |

No se piden permisos de almacenamiento: la importación de PDF usa el selector del
sistema (`OpenDocument`), que concede acceso solo al archivo elegido.

No hay permisos de ubicación, cámara, contactos, teléfono ni notificaciones.

---

## Terceros

| Componente | Uso | Datos |
|------------|-----|-------|
| Gemini API | Asistente literario opcional | Prompt descrito arriba |
| `TextToSpeech` del sistema | Narración | Texto del libro, procesado por el motor instalado |
| Coil | Carga de imágenes locales | Ninguno fuera del dispositivo |
| Firebase (dependencias) | **No se usa en el código actual** | — |
| Wattpad / Google Drive | **Maqueta** | No hay conexión |

El motor TTS puede ser local o de red según el fabricante; Libris no controla su
comportamiento. Revisa la política del motor de voz instalado.

---

## Analítica, publicidad y rastreo

**No hay.** No se incluyen SDK de analytics, anuncios, crash reporting ni
telemetría. No se genera un identificador de publicidad.

---

## Derechos y contacto

Como no hay cuentas reales, no existe un sistema de borrado de cuenta. Para borrar
los datos locales basta con borrar los datos de la app o desinstalarla.

Para preguntas de privacidad: **JonathanHurtadoDev@proton.me**.

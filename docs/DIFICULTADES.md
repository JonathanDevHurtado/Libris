# Dificultades y decisiones — Libris

Libris nació como una app generada en **Google AI Studio**. Mantener el proyecto
original y sacarlo de ese entorno produjo varios problemas. Este documento explica
qué se encontró y qué se decidió, sin adornos.

---

## 1. El build de debug dependía de un keystore que no estaba

**Síntoma.** `./gradlew assembleDebug` fallaba con:

```
Keystore file '/.../debug.keystore' not found
```

**Causa.** AI Studio firma el build de debug con un `debug.keystore` en la raíz y lo
excluye del control de versiones. Sin ese archivo, el proyecto no compila.

**Decisión.** Se documentó el comando `keytool` para generarlo y se añadió el mismo
paso al CI. El keystore sigue sin versionarse.

---

## 2. Robolectric no soportaba el SDK 36

**Síntoma.** `ExampleRobolectricTest` fallaba con
`UnsupportedOperationException` en `DefaultSdkProvider`.

**Causa.** El proyecto declara `compileSdk 36` y el test pedía `@Config(sdk = [36])`,
pero la versión de Robolectric del proyecto no tiene ese SDK.

**Decisión.** El test se ejecuta con `@Config(sdk = [35])`. No cambia la app; solo
hace que la prueba de ejemplo sea ejecutable en JVM.

---

## 3. La app no persiste nada

**Problema.** `BookRepository` guarda libros, historial y ajustes en
`MutableStateFlow`. Al cerrar el proceso se pierde todo: progreso, favoritos,
historial y tema.

**Causa.** El demo de AI Studio estaba pensado para una sesión; no usa Room ni
DataStore.

**Decisión.** No se reescribió la arquitectura porque el usuario pidió conservar la
app original. Se documenta como limitación principal. Una versión de producción
necesitaría persistencia y migraciones.

---

## 4. Las cuentas y la nube son una maqueta

**Problema.** La pantalla de Ajustes muestra “Conectado con Google” y “Wattpad”
con datos y botones, pero no hay OAuth, tokens ni llamadas a esas plataformas. La
pestaña “Google Drive Cloud” tampoco sincroniza.

**Causa.** Eran datos de demostración para mostrar el diseño.

**Decisión.** Se mantuvieron para no alterar la app, pero el README y
`docs/FUNCIONAMIENTO.md` lo dicen explícitamente. También se sustituyó el email
personal que aparecía en la cuenta de demostración por `lector@example.com`.

---

## 5. La clave de Gemini se compila en el APK

**Problema.** `GeminiBookAdvisorService` lee `BuildConfig.GEMINI_API_KEY`. El plugin
de Secrets toma el valor de `.env`. Cualquiera que tenga el APK puede extraer la
clave.

**Causa.** Es la convención de AI Studio para demos.

**Decisión.** `.env` está ignorado y `.env.example` solo contiene un placeholder.
Se documenta que una app real debe usar un backend/proxy y no incluir la clave.

También se observó que el modelo `gemini-3.5-flash` puede no estar disponible para
todas las cuentas. Si falla, la app usa recomendaciones locales; no se rompe.

---

## 6. Textos de ejemplo con derechos de terceros

**Problema.** La biblioteca incluye fragmentos de *El Principito*, *Don Quijote*,
*Viaje al Centro de la Tierra* y *Alicia en el País de las Maravillas*, además de
una historia original de estilo Wattpad.

**Causa.** El demo necesitaba contenido atractivo.

**Decisión.** Se conservaron porque forman parte de la app original, pero la
licencia PSAL **no cubre esos textos**. El README avisa de que deben sustituirse
por contenido propio o de dominio público verificado antes de reutilizar el
proyecto.

---

## 7. Fugas de recursos en el lector PDF (conocidas, no corregidas)

**Problema.** `PdfRendererManager` tiene puntos frágiles:

- `openPdfFromUri` no cierra el `InputStream` de origen.
- `renderPage` no cierra la `Page` si `render` lanza una excepción.
- `createSampleLiteraryPdf` no cierra el `PdfDocument` si falla la escritura.

**Causa.** El código original no usaba `use` en todos los recursos.

**Decisión.** No se modificó al conservar la app original. Está documentado como
deuda técnica; corregirlo es un cambio pequeño y aislado para una futura versión.

---

## 8. Navegación y estado global simples

`MainApp` mantiene el destino y el libro abierto con `remember`. No hay
`NavHost`, deep links ni back stack. Es suficiente para cinco pantallas, pero no
escala a un lector con enlaces, notificaciones o múltiples ventanas.

**Decisión.** Se mantiene por simplicidad y porque el usuario pidió conservar la
configuración original.

---

## 9. `applicationId` heredado

El `namespace` es `com.example` y el `applicationId` es
`com.aistudio.libris.reader`. Funciona, pero `com.example` no es publicable en una
tienda y el id de AI Studio no representa al autor.

**Decisión.** No se renombró para no romper la configuración original. Se documenta
como tarea pendiente antes de una publicación real.

---

## 10. Firma de release de AI Studio

`app/build.gradle.kts` espera `my-upload-key.jks` o `KEYSTORE_PATH`, con
`STORE_PASSWORD` y `KEY_PASSWORD`. No hay claves en el repositorio. El build de
debug es el que se verifica; el release requiere configurar un keystore propio.

---

## Lo que sí se verificó

- `assembleDebug` compila.
- `testDebugUnitTest` pasa (dos pruebas de ejemplo).
- El APK debug se genera y contiene los recursos.
- El logo y las imágenes promocionales están presentes.

Lo que **no** se verificó: lectura real de un PDF enorme, TTS en un dispositivo
físico, rotación, modo oscuro del sistema, accesibilidad con TalkBack y llamadas
reales a Gemini.

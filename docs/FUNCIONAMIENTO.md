# Funcionamiento de Libris

Guía de las pantallas y flujos principales tal como están implementados en la
versión 1.1.0. Recuerda que es una **demo**: las cuentas y la nube son simuladas.

---

## Navegación

`MainApp` decide entre dos layouts:

- **Teléfono (< 650 dp):** `Scaffold` con `NavigationBar` inferior.
- **Tablet (≥ 650 dp):** `NavigationRail` lateral.

Destinos:

| Destino | Icono | Pantalla |
|---------|-------|----------|
| Biblioteca | `LocalLibrary` | `HomeScreen` |
| Historial | `History` | `HistoryScreen` |
| Explorar | `Explore` | `ExploreScreen` |
| Asistente IA | `AutoAwesome` | `GeminiAdvisorScreen` |
| Ajustes | `ManageAccounts` | `AccountsSyncScreen` |

Al abrir un libro, el lector ocupa toda la pantalla y el botón atrás vuelve a la
biblioteca.

---

## Biblioteca

**Archivo:** `ui/screens/HomeScreen.kt`

- Cabecera con título “Mi Biblioteca” y resumen de lectura.
- Filtros por categoría: Todos, Descargados, PDFs, Wattpad y Clásicos.
- Buscador por título o autor.
- Tarjetas de libro con portada, progreso y estado de descarga.
- Acciones: abrir, favorito y descarga simulada.
- Botón **Importar PDF** con `ActivityResultContracts.OpenDocument`.
- Botón de historial.

La lista se obtiene de `repository.books` (`StateFlow<List<BookItem>>`).

---

## Importar un PDF

```
HomeScreen
  → OpenDocument (application/pdf)
  → BookRepository.importPdfFromUri(uri, displayName)
      → PdfRendererManager.openPdfFromUri(uri)
      → copia el contenido a filesDir
      → PdfRenderer.pageCount
      → crea BookItem y lo añade a _books
```

El PDF se copia al almacenamiento privado de la app. No se pide permiso global de
almacenamiento porque el selector del sistema concede acceso al URI elegido.

---

## Lector

**Archivo:** `ui/screens/ReaderScreen.kt`

### PDF

- `PdfRendererManager` abre el archivo y renderiza la página actual a un `Bitmap`.
- Se puede avanzar/retroceder y saltar con la barra de progreso.
- El zoom y el ajuste de página dependen del tamaño del `Bitmap`.

### Texto y capítulos

- Los libros con `chapters` se muestran como texto paginado.
- `PageCurlBookView` ofrece animación de curl 3D o deslizamiento.
- Se puede cambiar tipografía, tamaño, tema y animación desde
  `ReaderCustomizationModal`.

### Progreso

`repository.updateReadingProgress(bookId, page, minutes)`:

1. Actualiza `currentPage` y `lastReadTimestamp`.
2. Añade una entrada a `_history`.
3. Actualiza la UI por `StateFlow`.

Todo ocurre en memoria.

---

## Narrador

**Archivos:** `service/NarratorManager.kt`, `ui/components/NarratorControlSheet.kt`

- Inicializa `TextToSpeech` con el idioma por defecto.
- Consulta las voces disponibles y permite elegir una.
- Aplica velocidad (`setSpeechRate`) y tono (`setSpeechPitch`).
- `speak`, `pause`, `stop` y liberación en `onDestroy` de la actividad.
- No usa red: depende de las voces instaladas en el dispositivo.

---

## Historial

**Archivo:** `ui/screens/HistoryScreen.kt`

- Lista de `ReadingHistoryItem` ordenada por la más reciente.
- Muestra libro, página, porcentaje, duración y hora.
- Permite abrir el libro en la página registrada.
- No hay borrado individual ni exportación.

---

## Explorar

**Archivo:** `ui/screens/ExploreScreen.kt`

Tres pestañas:

1. **Wattpad & Web:** tarjetas de historias de ejemplo. Los botones de descarga
   no conectan con Wattpad.
2. **Clásicos Libres:** libros de ejemplo de dominio público (textos abreviados).
3. **Google Drive Cloud:** maqueta de sincronización. No hay OAuth ni llamadas a
   Drive.

Esta pantalla existe para mostrar el diseño; no implementa descargas ni cuentas.

---

## Asistente literario

**Archivos:** `service/GeminiBookAdvisorService.kt`, `ui/screens/GeminiAdvisorScreen.kt`

1. El usuario escribe una pregunta.
2. Se construye un prompt con la biblioteca y el historial.
3. Si `BuildConfig.GEMINI_API_KEY` existe y no es el placeholder, se hace un POST a
   `generativelanguage.googleapis.com`.
4. Si no hay clave, la llamada falla o la respuesta está vacía, se usa
   `getOfflineLiteraryRecommendation()`.
5. La respuesta se muestra como mensaje del asistente.

El modelo configurado en el código es `gemini-3.5-flash`. Si tu cuenta no tiene
acceso a ese modelo, la llamada fallará y se usará el respaldo local.

> La clave se compila en el APK. No la publiques ni la uses en producción sin un
> proxy.

---

## Ajustes y cuentas

**Archivo:** `ui/screens/AccountsSyncScreen.kt`

- Tarjetas de **Google** y **Wattpad** con datos de demostración.
- Botones para “conectar/desconectar” que solo cambian estado en memoria.
- Ajustes de lectura: tema, tipografía, tamaño y animación.
- Tarjeta de contacto con el email del desarrollador y opción de copiarlo.

No hay autenticación, tokens ni sincronización.

---

## Temas y lectura

`ReaderTheme` define cuatro apariencias:

| Tema | Uso |
|------|-----|
| `LIGHT` | Fondo claro |
| `DARK` | Fondo oscuro (por defecto) |
| `SEPIA` | Papel vintage |
| `OLED` | Negro puro para pantallas OLED |

`ReaderFont` y `ReaderSettings` controlan familia, tamaño y animación de página.
Los cambios se aplican en memoria y se pierden al cerrar el proceso.

---

## Estados vacíos y errores

- Si no hay libros, la biblioteca muestra un mensaje y el botón de importar.
- Si un PDF no se puede abrir, `importPdfFromUri` devuelve `null` y no se añade.
- Si TTS no está disponible, el narrador queda sin voces y no reproduce.
- Si Gemini falla, se muestra la recomendación local.

No hay pantalla de error global ni reintentos automáticos.

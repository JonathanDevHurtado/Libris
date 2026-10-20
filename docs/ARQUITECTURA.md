# Arquitectura de Libris

Libris es una app Android de lectura escrita en Kotlin con Jetpack Compose. Nació
como un proyecto generado en AI Studio y conserva una arquitectura sencilla, sin
base de datos ni inyección de dependencias.

---

## Visión general

```
┌─────────────────────────────────────────────────────────────┐
│                    MainActivity (Android)                    │
│  Crea manualmente los servicios y el repositorio             │
│                                                              │
│  PdfRendererManager · NarratorManager · GeminiService        │
│                     BookRepository                           │
└───────────────────────────┬─────────────────────────────────┘
                            │ StateFlow
┌───────────────────────────▼─────────────────────────────────┐
│                     UI (Jetpack Compose)                     │
│                                                              │
│  MainApp                                                     │
│   ├── HomeScreen          Biblioteca y estadísticas         │
│   ├── HistoryScreen       Sesiones de lectura                │
│   ├── ExploreScreen       Descubrimiento (maqueta)           │
│   ├── GeminiAdvisorScreen Chat literario                     │
│   ├── AccountsSyncScreen  Ajustes y cuentas simuladas        │
│   └── ReaderScreen        Lector PDF/texto + TTS             │
└─────────────────────────────────────────────────────────────┘
```

La navegación es estado local (`AppDestination`) y `Crossfade`; no hay
`NavHost`, deep links ni back stack de Navigation Compose.

---

## Capas

### 1. Presentación (`ui/`)

| Paquete | Responsabilidad |
|---------|-----------------|
| `ui/MainApp.kt` | Navegación principal, layout adaptativo y reparto de dependencias |
| `ui/screens/` | Pantallas de biblioteca, historial, explorar, lector, IA y ajustes |
| `ui/components/` | Portadas, hoja del narrador, modal de lectura y Page Curl |
| `ui/theme/` | Paleta, tipografía, formas y tema Material 3 |

Las pantallas reciben el repositorio y los servicios como parámetros; no crean
dependencias globales. El estado de cada pantalla se deriva de `StateFlow` y de
`remember` locales.

### 2. Dominio/datos (`data/`)

| Archivo | Responsabilidad |
|---------|-----------------|
| `data/model/BookItem.kt` | `BookItem`, `ChapterContent`, `ReadingHistoryItem`, `BookFormat` |
| `data/model/ReaderSettings.kt` | Tema, tipografía, tamaño y animación de página |
| `data/repository/BookRepository.kt` | Biblioteca, historial, cuentas simuladas y ajustes en memoria |

`BookRepository` expone `StateFlow` y usa `CoroutineScope(Dispatchers.IO)` para
generar PDFs de ejemplo. **No hay Room, DataStore ni archivos de preferencias**:
todo se pierde al destruir el proceso.

### 3. Servicios (`service/`)

| Archivo | Responsabilidad |
|---------|-----------------|
| `PdfRendererManager.kt` | Abre PDFs con `PdfRenderer`, renderiza páginas y crea PDFs de ejemplo |
| `NarratorManager.kt` | `TextToSpeech`: voces, velocidad, tono, play/pause/stop |
| `GeminiBookAdvisorService.kt` | Llamada REST a Gemini y respaldo local |

`GeminiBookAdvisorService` usa OkHttp directamente. La clave llega desde
`BuildConfig.GEMINI_API_KEY`, generada por el plugin de Secrets a partir de `.env`.
Si la clave falta o la llamada falla, devuelve una recomendación local.

---

## Flujo de datos

### Abrir un libro

```
HomeScreen → onOpenBook(bookId)
    → MainApp.activeBookId
    → ReaderScreen(bookId)
    → repository.books StateFlow
    → PdfRendererManager.renderPage() o chapters
```

### Importar un PDF

```
HomeScreen → ActivityResultContracts.OpenDocument
    → Uri
    → BookRepository.importPdf(uri)
    → copia a filesDir
    → PdfRendererManager.open()
    → nuevo BookItem en _books
```

### Narración

```
ReaderScreen → NarratorControlSheet
    → NarratorManager.speak(texto, velocidad, tono)
    → TextToSpeech del sistema
```

### Asistente

```
GeminiAdvisorScreen → GeminiBookAdvisorService.askGeminiAdvisor()
    ├── ¿Hay clave? → POST a generativelanguage.googleapis.com
    └── No / error → getOfflineLiteraryRecommendation()
```

---

## Decisiones de diseño

| Decisión | Motivo | Consecuencia |
|----------|--------|--------------|
| Repositorio en memoria | Simplicidad del demo de AI Studio | No hay persistencia real |
| Sin Hilt | Menos configuración para una app pequeña | Dependencias creadas a mano en `MainActivity` |
| Sin Navigation Compose | Cinco destinos y un lector | Navegación simple, sin deep links |
| PDF con `PdfRenderer` | API del sistema, sin librerías pesadas | Solo PDF; EPUB/TXT son maquetas |
| TTS del sistema | Sin servicios en la nube | Depende de las voces instaladas |
| Gemini opcional | El demo funciona sin clave | La clave se compila en el APK |
| NavigationRail en tablet | Aprovechar pantallas anchas | Punto de corte en 650 dp |

---

## Limitaciones estructurales

1. **No hay persistencia.** El progreso, los favoritos, el historial y los ajustes
   viven solo en memoria.
2. **No hay cuentas reales.** Google/Wattpad son tarjetas de demostración.
3. **No hay sincronización.** “Google Drive” es una pestaña informativa.
4. **No hay EPUB/TXT reales.** Los formatos existen como etiquetas; el lector de
   texto usa los capítulos de ejemplo.
5. **No hay tests de UI.** Solo hay pruebas de ejemplo.
6. **Hay dependencias heredadas sin uso.** `build.gradle.kts` declara Room, KSP,
   Firebase AI/AppCheck, Moshi, Retrofit y el interceptor de OkHttp, pero el código
   actual solo usa OkHttp y `org.json`. Se conservan porque forman parte de la
   configuración original de AI Studio; eliminarlas reduziría el APK, pero es un
   cambio separado.

Estas limitaciones son deliberadas para conservar el estado original del proyecto.
Una versión de producción requeriría Room/DataStore, OAuth real, un backend para la
IA y pruebas instrumentadas.

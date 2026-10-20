# Verificación — Libris 1.0

Fecha: **2026-10-20**  
Entorno: Linux, JDK 17 (`17.0.13-tem`), Android SDK Platform 36.

---

## Comandos ejecutados

```bash
export JAVA_HOME=/ruta/al/jdk-17
./gradlew --no-daemon testDebugUnitTest assembleDebug lintDebug
```

Resultado:

```
BUILD SUCCESSFUL in 1m 21s
58 actionable tasks: 17 executed, 41 up-to-date
```

| Tarea | Resultado |
|-------|-----------|
| `testDebugUnitTest` | ✅ 3 tests, 0 fallos, 0 errores |
| `assembleDebug` | ✅ `app-debug.apk` (25 MB) |
| `lintDebug` | ✅ sin errores (81 avisos) |
| `assembleRelease` | ⏸️ No ejecutado: requiere keystore de AI Studio |

Los tests son los de ejemplo del proyecto:

- `ExampleUnitTest`: 1 test (suma).
- `ExampleRobolectricTest`: 2 tests (nombre de la app y recurso de texto).

No cubren el lector, el repositorio, el TTS ni Gemini.

---

## Avisos de lint

Los 81 avisos se reparten principalmente en:

| Categoría | Cantidad | Comentario |
|-----------|----------|------------|
| `UnusedResources` | 33 | Recursos de demo no referenciados |
| `GradleDependency` | 22 | Dependencias con versiones más nuevas |
| `NewerVersionAvailable` | 9 | Actualizaciones disponibles |
| `IconLauncherShape` | 5 | Iconos heredados con forma |
| `IconLocation` | 4 | Ubicación de iconos |
| `UseKtx` | 3 | Sugerencias de Kotlin extensions |
| `DefaultLocale` | 2 | Formato dependiente del locale |
| `AndroidGradlePluginVersion` | 2 | AGP con actualización disponible |
| `RedundantLabel` | 1 | Etiqueta redundante en la actividad |

No se corrigieron para no alterar la app original ni actualizar dependencias sin
probar el lector en un dispositivo.

---

## Qué **no** se verificó

No había un dispositivo físico ni un emulador conectado (`adb devices` vacío). Por
tanto no se probó:

- importar un PDF real y pasar páginas;
- la animación Page Curl en pantalla táctil;
- el TTS con voces instaladas;
- rotación, pantalla pequeña/tablet y modo oscuro del sistema;
- TalkBack y tamaños de fuente grandes;
- una llamada real a Gemini con clave;
- el build de release y su firma.

La verificación cubre compilación, tests JVM, lint, recursos y empaquetado del APK
debug.

---

## Cómo reproducirla

```bash
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA -keysize 2048 -validity 10000

./gradlew --no-daemon testDebugUnitTest assembleDebug lintDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

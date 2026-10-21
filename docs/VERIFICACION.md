# Verificación — Libris 1.1.0

Fecha: **2026-10-21**
Entorno: Linux, JDK 17 (`17.0.13-tem`), Android SDK Platform 36.

---

## Comandos ejecutados

```bash
export JAVA_HOME=/ruta/al/jdk-17
export KEYSTORE_PATH=/ruta/segura/Libris-release.jks
export STORE_PASSWORD='...'
export KEY_PASSWORD='...'

./gradlew --no-daemon clean testDebugUnitTest lintDebug
./gradlew --no-daemon assembleRelease bundleRelease
```

Resultado:

```
BUILD SUCCESSFUL in 3m 57s
102 actionable tasks: 83 executed, 19 from cache
```

| Tarea | Resultado |
|-------|-----------|
| `testDebugUnitTest` | ✅ 3 tests, 0 fallos, 0 errores |
| `lintDebug` | ✅ sin errores (81 avisos) |
| `assembleRelease` | ✅ `app-release.apk` firmado (17,7 MB) |
| `bundleRelease` | ✅ `app-release.aab` firmado (17,2 MB) |

Los tests son los de ejemplo del proyecto:

- `ExampleUnitTest`: 1 test (suma).
- `ExampleRobolectricTest`: 2 tests (nombre de la app y recurso de texto).

No cubren el lector, el repositorio, el TTS ni Gemini.

---

## Release 1.1.0

| Artefacto | Tamaño | SHA-256 |
|---|---:|---|
| `Libris-1.1.0.apk` | 17,7 MB | `a187ed5e04a3154b76f1fde9c1ed32a70312cae8b5ec7718f2f82b6940f6231c` |
| `Libris-1.1.0.aab` | 17,2 MB | `718710c837c2c50abc780329693115421f15d8fce52226dcbe72d02e4d00da12` |

Firma comprobada con `apksigner verify --print-certs`:

```
Signer #1 certificate DN: CN=Jonathan Hurtado, OU=Libris,
  O=JonathanDevHurtado, L=Bogota, C=CO
```

El AAB también pasa `jarsigner -verify`. La clave de release se guarda fuera del
repositorio y se pasa mediante `KEYSTORE_PATH`, `STORE_PASSWORD` y `KEY_PASSWORD`;
no hay claves versionadas.

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
- la instalación del APK de release en un dispositivo físico.

La verificación cubre compilación, tests JVM, lint, recursos, empaquetado y firma
del APK/AAB de release.

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

Para el release firmado:

```bash
export KEYSTORE_PATH=/ruta/segura/Libris-release.jks
export STORE_PASSWORD='...'
export KEY_PASSWORD='...'
./gradlew --no-daemon assembleRelease bundleRelease
```

El APK de debug queda en `app/build/outputs/apk/debug/app-debug.apk`; los
artefactos firmados quedan en `app/build/outputs/apk/release/` y
`app/build/outputs/bundle/release/`.

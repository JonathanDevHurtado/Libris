# 📦 Publicar una release — Libris

Guía del proceso usado para empaquetar y publicar la demo.

---

## 🔢 Versionado

Se usa [SemVer](https://semver.org/lang/es/):

| Cambio | Ejemplo |
|---|---|
| Corrección compatible | `1.1.1` |
| Nueva función compatible | `1.2.0` |
| Cambio incompatible | `2.0.0` |

El tag de Git lleva la `v`: `v1.1.0`. La app mantiene el `applicationId`
heredado de AI Studio (`com.aistudio.libris.reader`).

---

## 🔐 Firma

La configuración de AI Studio espera un keystore `my-upload-key.jks` o la ruta de
`KEYSTORE_PATH`, con alias `upload` y las variables `STORE_PASSWORD` y
`KEY_PASSWORD`.

La clave **nunca se sube al repositorio**. Debe guardarse fuera del proyecto y
respaldarse junto con sus contraseñas.

```bash
export KEYSTORE_PATH=/ruta/segura/Libris-release.jks
export STORE_PASSWORD='...'
export KEY_PASSWORD='...'
```

Sin esas variables, `assembleRelease` falla. El keystore de depuración es otro
archivo (`debug.keystore`) y no sirve para publicar.

---

## 🛠️ Compilar los artefactos

```bash
export JAVA_HOME=/ruta/al/jdk-17
export KEYSTORE_PATH=/ruta/segura/Libris-release.jks
export STORE_PASSWORD='...'
export KEY_PASSWORD='...'

./gradlew --no-daemon clean testDebugUnitTest lintDebug
./gradlew --no-daemon assembleRelease bundleRelease
```

Salidas:

| Artefacto | Ruta |
|---|---|
| APK firmado | `app/build/outputs/apk/release/app-release.apk` |
| AAB firmado | `app/build/outputs/bundle/release/app-release.aab` |

Verificar la firma:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

---

## 🧾 Nombres y checksums

```text
Libris-1.1.0.apk
Libris-1.1.0.aab
Libris-1.1.0-docs.zip
SHA256SUMS.txt
```

```bash
sha256sum Libris-1.1.0.apk \
          Libris-1.1.0.aab \
          Libris-1.1.0-docs.zip > SHA256SUMS.txt
```

---

## 🚀 Publicar en GitHub

1. Crear el tag y subirlo:
   ```bash
   git tag -a v1.1.0 -m "Libris 1.1.0"
   git push origin main --tags
   ```
2. Crear la release con el título `🚀 Libris 1.1.0`.
3. Adjuntar APK, AAB, documentación y checksums.
4. Marcar la release como **Latest**.
5. Recordar en las notas que es una **demo** y que no hay persistencia real.

---

## ✅ Checklist

- [ ] `versionCode` y `versionName` actualizados.
- [ ] `CHANGELOG.md` actualizado.
- [ ] Tests y lint sin errores nuevos.
- [ ] APK y AAB firmados y verificados.
- [ ] Checksums generados y comprobados.
- [ ] Documentación incluida en el ZIP.
- [ ] Release publicada con notas y emojis.
- [ ] Clave de firma respaldada fuera del repositorio.

# Contribuir a Libris

Gracias por el interés. Este es un proyecto **propietario source-available**: el
código se publica para auditoría y las contribuciones se aceptan bajo los términos
de la sección 4 de [`LICENSE`](LICENSE). Si no estás de acuerdo con esos términos,
no envíes cambios.

## Antes de empezar

- Lee [`README.md`](README.md), [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) y
  [`docs/DIFICULTADES.md`](docs/DIFICULTADES.md).
- Ten en cuenta que se conserva la app original de AI Studio: **no se aceptan
  refactorizaciones masivas** ni cambios que eliminen las pantallas de demostración
  sin una justificación muy clara.
- Para cambios grandes, abre un issue con el problema, la propuesta y el impacto.
- No incluyas datos personales, keystores, tokens ni capturas con información
  privada.

## Preparar el entorno

```bash
git clone https://github.com/JonathanDevHurtado/Libris.git
cd Libris

# El build de debug de AI Studio espera este archivo en la raíz
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA -keysize 2048 -validity 10000

# Opcional: clave de Gemini
cp .env.example .env
```

## Flujo

```bash
git checkout -b fix/mi-correccion
# edita
./gradlew testDebugUnitTest assembleDebug
git commit -m "fix: describe el cambio"
git push origin fix/mi-correccion
```

Abre un Pull Request contra `main`.

## Convención de commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

- `fix:` corrección de un bug
- `docs:` documentación
- `test:` pruebas
- `build:` Gradle, dependencias, CI
- `chore:` mantenimiento

No se recomienda `refactor:` amplio mientras se conserve la app original.

## Estándares

- Kotlin oficial, 4 espacios, sin tabs.
- No añadas dependencias sin justificarlo en el PR.
- No incrustes claves ni correos personales.
- Los textos visibles van en `res/values/strings.xml` cuando sea posible.
- Comenta el **por qué**, no el **qué**.
- Si tocas PDF, TTS o Gemini, explica cómo lo probaste.

## Checklist del PR

- [ ] El cambio tiene un issue o una explicación clara.
- [ ] Generé `debug.keystore` localmente y no lo incluí.
- [ ] `./gradlew testDebugUnitTest` pasa.
- [ ] `./gradlew assembleDebug` compila.
- [ ] Actualicé `CHANGELOG.md` si el cambio es visible.
- [ ] Actualicé la documentación afectada.
- [ ] No incluí `.env`, keystores, `local.properties` ni artefactos de build.

## Revisión

Se revisa el diseño, la seguridad, la privacidad y el impacto en la app original.
La decisión final es del mantenedor.

## Código de conducta

Al participar aceptas [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

# Política de seguridad

## Estado del proyecto

Libris es una **demo funcional** publicada como código source-available para
auditoría. No está pensada para producción, no maneja cuentas reales y no debe
usarse con información sensible.

| Versión | Soporte |
|---|---|
| 1.0.x | ✅ Sí, sobre la instantánea publicada |
| Otras | ❌ No |

## Cómo reportar una vulnerabilidad

**No abras un issue público.** Envía un correo a
**JonathanHurtadoDev@proton.me** con:

- versión de la app y de Android;
- modelo de dispositivo (si aplica);
- pasos para reproducir;
- impacto esperado;
- prueba de concepto, si la tienes.

Si quieres cifrar el mensaje, indícalo y se acordará un canal.

## Plazos orientativos

| Etapa | Plazo |
|---|---|
| Acuse de recibo | 72 h |
| Evaluación inicial | 7 días |
| Corrección / mitigación | según gravedad, se acuerda contigo |
| Divulgación | coordinada, después de publicar la corrección |

## Alcance

Entran en alcance:

- el código de la app y su configuración de build;
- el manifest y los permisos;
- el manejo de PDFs importados;
- la integración con Gemini y el almacenamiento de la clave;
- la cadena de suministro del build (dependencias, wrapper, CI).

Quedan fuera de alcance:

- vulnerabilidades del sistema operativo o del fabricante;
- fallos de servicios de terceros (Gemini, Google, Wattpad);
- la ausencia de funciones que la documentación ya declara como maqueta;
- ingeniería social;
- denegación de servicio local que requiera acceso físico al dispositivo.

## Prácticas y limitaciones conocidas

- No hay secretos reales en el repositorio: `.env` está ignorado y
  `.env.example` solo contiene un placeholder.
- La clave de Gemini se compila en `BuildConfig` si se configura. Es extraíble
  del APK; para producción debe usarse un backend intermedio.
- La app declara `INTERNET` únicamente para el asistente.
- No hay analytics, publicidad ni telemetría.
- Las cuentas de Google/Wattpad son simuladas; no hay OAuth ni tokens.
- No hay persistencia de progreso, historial ni ajustes.
- El lector PDF tiene fugas de recursos conocidas, documentadas en
  [`docs/DIFICULTADES.md`](docs/DIFICULTADES.md).

Gracias por ayudar a que el proyecto sea más seguro.

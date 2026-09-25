# NotiBatch

Entrega las notificaciones de WhatsApp en tandas a horas fijas, con excepciones (contactos, llamadas, alarmas, códigos). Uso personal, instalación por APK, sin red.

**Estado:** Fase 0 (prueba de viabilidad) preparada, sin probar en teléfono. Nada de esto está compilado todavía: ver [`docs/03-pendientes.md`](docs/03-pendientes.md) (P1).

## Instalar (desarrollo)
1. Abrí la carpeta en Android Studio (genera el wrapper de Gradle) o instalá JDK 21 + SDK de Android.
2. Teléfono con depuración USB: `./gradlew installDebug`.
3. Activá el acceso a notificaciones (en Android 13+, antes "Permitir ajustes restringidos" en la ficha de la app).

## Permisos y por qué
| Permiso | Para qué |
|---|---|
| Acceso a notificaciones | Leer y posponer notificaciones. Sin él la app no hace nada. |
| `POST_NOTIFICATIONS` (Fase 2) | Mostrar el resumen de cada tanda y el indicador de estado. |
| Exclusión de optimización de batería (Fase 2) | Que el sistema no mate el servicio. |
| **No** pide `INTERNET` ni contactos | A propósito: nada sale del teléfono. |

## Documentación
[`CLAUDE.md`](CLAUDE.md) (spec resumido y comandos) · [`docs/`](docs/) (decisiones, pendientes, protocolo de Fase 0, [checklist de prueba manual](docs/05-checklist-manual.md)).

# Fase 0: protocolo de viabilidad (para correr en tu teléfono)

Objetivo: responder Q1–Q6 con evidencia y decidir **(A) snooze** o **(B) cancelar y guardar** (nuevo ADR). El arnés vive solo en la variante `debug` (ADR-0010) y **no registra contenido**: ni texto, ni títulos, ni nombres, ni la key cruda (ADR-0008); cada notificación se identifica con un id de 6 hex.

## Preguntas

| Id | Pregunta | Qué se mira en el log |
|---|---|---|
| Q1 | ¿Vuelven todas juntas al terminar el snooze? ¿Suenan varias veces? ¿Qué tan puntual es? | eventos `posted` con `relation=regreso_de_snooze` y `delta_ms`; lo que oís en el teléfono |
| Q2 | ¿Qué pasa si WhatsApp actualiza la misma key o publica keys nuevas (resumen de grupo)? | `posted` con `relation=actualizacion_antes_de_tiempo`, `groupSummary`, `snoozed_list`, `snapshot` |
| Q3 | ¿Sobrevive el snooze a un reinicio del teléfono? | tras reiniciar: `snoozed_list` y si vuelven a la hora prevista |
| Q4 | ¿Se puede sacar una notificación del snooze antes de tiempo? | no hay API pública conocida; se prueba `snoozeNotification` con duración corta sobre la misma key y se mira `snapshot`. Si no se puede → ADR-0003 vale tal cual |
| Q5 | ¿Se lee de forma fiable remitente y "es grupo"? | evento `shape` (solo booleanos y conteos) |
| Q6 | ¿El servicio sobrevive con la app cerrada y con el ahorro de batería de tu marca? | `listener_disconnected` / `listener_connected` a lo largo de un día |

## Preparación (una vez)

1. Activá **Opciones de desarrollador → Depuración USB** y conectá el teléfono.
2. Compilá e instalá: `./gradlew installDebug` (o Run en Android Studio).
3. Activá el acceso a notificaciones para "NotiBatch sonda (debug)". Android 13+ exige antes **Ajustes de la app → ⋮ → Permitir ajustes restringidos**. Alternativa por adb:
   `adb shell cmd notification allow_listener app.notibatch/app.notibatch.probe.ProbeListenerService`
4. En una terminal aparte, dejá corriendo el log: `adb logcat -s NotiBatchProbe:I`

## Comandos de la sonda

Todos con `-n app.notibatch/app.notibatch.probe.ProbeControlReceiver`.

```bash
# Posponer las notificaciones de un paquete durante N minutos
adb shell am broadcast -n app.notibatch/app.notibatch.probe.ProbeControlReceiver \
  -a app.notibatch.probe.CONFIG --es pkg com.whatsapp --ei minutes 5

# Qué hay activo ahora
adb shell am broadcast -n app.notibatch/app.notibatch.probe.ProbeControlReceiver -a app.notibatch.probe.SNAPSHOT

# Qué considera pospuesto el sistema (reflexión; puede no estar disponible)
adb shell am broadcast -n app.notibatch/app.notibatch.probe.ProbeControlReceiver -a app.notibatch.probe.SNOOZED
```

`com.whatsapp.w4b` si usás WhatsApp Business. Para pruebas sin pacientes, usá otro teléfono o un contacto tuyo.

## Pruebas

**T1 (Q1, Q2, Q5).** Configurá 5 minutos. Desde otro teléfono mandá: 2 mensajes de un contacto individual, 1 de un grupo, y un tercer mensaje del mismo individual. Esperá el vencimiento. Anotá: ¿volvieron juntas?, ¿sonó una o varias veces?, ¿el botón de responder funciona?, ¿abre el chat correcto? Corré `SNAPSHOT` y `SNOOZED` antes y después.

**T2 (Q4).** Con algo pospuesto, corré `CONFIG` con 1 minuto y mandá otro mensaje del mismo contacto; mirá si la anterior vuelve antes (no debería) y qué dice `snoozed_list`.

**T3 (Q3).** Configurá 10 minutos, mandá 2 mensajes, **reiniciá el teléfono** antes del vencimiento, reactivá el listener si hace falta y anotá si vuelven, cuándo y si el log muestra `regreso_de_snooze`.

**T4 (Q6).** Dejá la sonda instalada un día entero con uso normal, cerrá la app desde recientes y mirá si hay `listener_disconnected`.

## Qué me tenés que pasar

Pegá el log (es seguro compartirlo: no tiene contenido) y respondé las tablas de arriba con lo que viste/oíste. Con eso el Consejo redacta el ADR de A vs. B ([`02-decisiones.md`](02-decisiones.md)).

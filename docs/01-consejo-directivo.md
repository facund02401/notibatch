# Consejo Directivo de NotiBatch

## Composición y aclaración de honestidad

El Consejo tiene cuatro voces. **Son roles que interpreta Claude**, no personas reales que hayan opinado. En particular, las posturas de "Cal Newport" son una inferencia a partir de las ideas que publicó (trabajo profundo, minimalismo digital, "convenciones de comunicación", protocolos de disponibilidad), no una opinión suya sobre esta app.

| Voz | Perspectiva | Qué protege |
|---|---|---|
| **Seguridad Android** | Especialista en seguridad de apps Android (listeners de notificaciones, Keystore, superficie de ataque, cadena de suministro). | Que ningún dato de paciente salga del teléfono ni quede en disco; que la app no sea un vector. |
| **Producto** | Especialista en producto y diseño de interacción. | Que el alcance sea el mínimo que resuelve el problema; que los estados de error sean honestos; que la UI no se convierta en otra fuente de distracción. |
| **Usuaria/o interesada/o** | Una persona que usaría el producto (psicoterapeuta con pacientes por WhatsApp). | Que nunca se pierda un mensaje urgente y que la app no agregue trabajo. |
| **Cal Newport** | Trabajo profundo, comunicación por lotes con expectativas explícitas. | Que el sistema cambie la *convención* con los pacientes, no solo el teléfono; que el escape ("Ver ahora") exista pero cueste. |

## Reglas de funcionamiento

1. **Decisión por acuerdo:** si las cuatro voces convergen (o la disidencia es menor y queda anotada), se decide y se registra en [`02-decisiones.md`](02-decisiones.md) como ADR.
2. **Pendiente del dueño:** si la decisión depende de hechos personales (agenda, teléfono, tolerancia al riesgo, dinero, temas legales propios) o de datos que solo se obtienen probando en el teléfono, se registra en [`03-pendientes.md`](03-pendientes.md) y **no se decide**.
3. **Prioridad en conflicto:** la del prompt original: privacidad y no perder mensajes urgentes por encima de cualquier feature.
4. Toda disidencia se conserva en el ADR.

## Sesión 1 (2026-09-25): revisión del prompt v2

### Hallazgos técnicos que el Consejo discutió

Estos puntos surgen de leer el prompt contra lo que se sabe de la API de Android. **Ninguno está verificado en un teléfono todavía**; por eso la Fase 0 existe.

**H1. "Ver ahora" y `snoozeNotification`.** No hay una API pública conocida para *des-posponer* una notificación antes de tiempo. Además, la duración del snooze se fija cuando se pospone. Consecuencias: (a) "Ver ahora", (b) "pausar 1 hora / hasta mañana" desde el tile, (c) cambiar de perfil durante el día y (d) cambiar un horario, **no pueden acortar** un snooze ya emitido. Es el conflicto central del diseño.

**H2. Estado de "quién está esperando".** La pantalla de inicio pide mostrar remitentes en espera. Con snooze, la notificación deja de estar activa y el servicio solo puede saberlo si lo guardó en memoria. Si el proceso muere, esa lista se pierde aunque los snoozes sigan vivos. Además, el resumen `InboxStyle` de la entrega necesita los remitentes.

**H3. Re-posteo de notificaciones vencidas.** Al terminar el snooze, el listener recibe de nuevo `onNotificationPosted` con la misma key. Si el listener no distingue "vuelta de snooze" de "notificación nueva", entra en bucle (la vuelve a posponer). Distinguirlo sin guardar estado es no trivial (hay que probar qué cambia en la notificación al volver).

**H4. Alarmas.** El snooze entrega por sí mismo a la hora pedida, así que **la entrega no depende de `AlarmManager`**. La alarma queda para la notificación resumen y como "vigilante" de fail-open. Esto rebaja la necesidad de permiso de alarma exacta: `setWindow` sirve. (Desde Android 12 las ventanas de alarmas inexactas tienen un mínimo, a verificar en Fase 0; por eso conviene que la hora exacta la marque el snooze.)

**H5. Alias de contactos.** "Nunca nombres en texto plano" choca con "un alias que pongo yo": el alias *es* un nombre. Debe cifrarse igual que el resto.

**H6. Hash de nombres.** El HMAC con clave en Keystore protege bien contra lectura del archivo, pero identifica por *nombre visible del chat*: dos pacientes con el mismo nombre, o un cambio de nombre en la agenda de WhatsApp, rompen la coincidencia. Dado el fail-open, el fallo es "pasa de más", no "se pierde".

**H7. Ajustes restringidos y Android 15/16.** APK instalado a mano: Android 13+ bloquea el acceso a notificaciones hasta "Permitir ajustes restringidos". Android 15+ oculta contenido sensible (OTP) a los listeners. Ambos ya están contemplados en el prompt.

**H8. Reinicio.** No queda claro si los snoozes sobreviven al reinicio del teléfono (las fuentes públicas son ambiguas). Es la pregunta de Fase 0 más importante para decidir A vs. B.

**H9. Nombre del remitente como dato de salud.** Que una persona escriba a una psicoterapeuta es en sí un dato sensible. Esto justifica FLAG_SECURE, el resumen sin nombres en pantalla de bloqueo y no guardar nada. (No es asesoramiento legal; ver pendiente sobre encuadre normativo.)

### Posturas por voz

**Seguridad.** Favorece (A) snooze: no hay texto en disco. Pide que la lista de espera en memoria no se persista, y que los logs de Fase 0 no contengan ni nombres ni contenido ni siquiera en debug. Pide firmar el APK con una clave propia guardada fuera del repo y desactivar `debuggable` en el APK que se instala en el teléfono real. Rechaza cualquier dependencia de red, incluso "solo en debug".

**Producto.** Señala que H1 contradice tres funciones de UI. Propone **tratar el snooze como compromiso irrevocable** y diseñar la UI honestamente: "Ver ahora" no libera lo ya pospuesto; abre una lista en memoria. Pausar/cambiar perfil afecta solo a lo que llegue *después*. Pide que el estado de la pantalla de inicio diga la verdad cuando la lista en memoria se perdió ("puede haber mensajes en espera").

**Usuaria/o.** Su miedo real es no enterarse de una urgencia. Pide que el peor caso del sistema sea "me llega todo junto" y nunca "no me llega". Acepta que "Ver ahora" no pueda liberar el snooze si la lista alcanza para saber a quién abrir. Pide que la app no agregue una pantalla más para revisar compulsivamente.

**Newport.** Aprueba el modelo por tandas y horas fijas alineadas a la agenda, y el mensaje de encuadre a pacientes como pieza *central*, no accesorio: sin una convención explícita, el paciente que no obtiene respuesta escala. Le preocupa que "Ver ahora" sin tope se vuelva el modo normal; acepta el aviso suave y sugiere que el contador quede visible solo al usuario. Le preocupa el indicador persistente (una notificación fija es otra señal que mirar): acepta que exista por seguridad pero **sin conteo** como ya dice el prompt.

### Resoluciones

Ver ADR-0001 a ADR-0012 en [`02-decisiones.md`](02-decisiones.md). Los puntos que dependen del usuario o de pruebas en el teléfono están en [`03-pendientes.md`](03-pendientes.md).

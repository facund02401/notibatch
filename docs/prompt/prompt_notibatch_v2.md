# NotiBatch v2: prompt para Claude Code

> Versión revisada del prompt original, con las decisiones de un panel de cuatro agentes: seguridad, diseño Android, usuario y Cal Newport.
> Uso: pegalo en Claude Code dentro de una carpeta vacía. Está pensado para trabajar **por fases**: Claude Code tiene que parar al final de cada fase para que pruebes en tu teléfono.

---

Quiero construir una app Android nativa llamada **NotiBatch**: Kotlin, Jetpack Compose y Material 3, minSdk 26, targetSdk el último estable. La uso yo solo, en mi teléfono personal, instalada por APK y no desde Play Store. Soy psicoterapeuta y por WhatsApp me escriben pacientes y padres de pacientes niños. **La privacidad y no perder mensajes urgentes son más importantes que cualquier feature.**

## Objetivo

Entregar las notificaciones **en tandas, a horas fijas alineadas con mi agenda** (por ejemplo, a los :50 de cada hora en días de consultorio), en lugar de recibirlas en tiempo real. Tienen que pasar siempre:

- los contactos que yo marque;
- las llamadas;
- las alarmas;
- los códigos de verificación.

No es una app para leer mensajes: no tiene historial, ni estadísticas, ni resúmenes con IA, ni permite responder desde ella.

## Forma de trabajo

1. Antes de programar, creá un `CLAUDE.md` con este spec resumido, las decisiones de arquitectura y los comandos para compilar e instalar (`./gradlew installDebug`).
2. Trabajá por fases. Al final de cada fase: compilá, contame qué probar en el teléfono y **pará hasta que te confirme**.
3. Si algo del spec no es técnicamente posible o es frágil en el Android actual, decímelo antes de inventar un parche.

## Fase 0: prueba de viabilidad (sin UI)

La decisión central es **posponer las notificaciones con `snoozeNotification(key, duración)` en lugar de cancelarlas y guardarlas en una base de datos**. Así la notificación original de WhatsApp vuelve con su botón de responder y abre el chat correcto, y la app no guarda texto de nadie.

Construí un `NotificationListenerService` mínimo que posponga las notificaciones de una app de prueba hasta una hora dada. Tiene que registrar en logs, **sin contenido**, lo necesario para responder estas preguntas:

- ¿Vuelven todas juntas al terminar el snooze? ¿Suenan varias veces?
- ¿Qué pasa cuando WhatsApp actualiza la misma key o publica keys nuevas, como el resumen de grupo?
- ¿Sobrevive a un reinicio del teléfono?
- ¿Hay forma de sacarlas del snooze antes de tiempo (para "Ver ahora")? Si no la hay, proponé una alternativa. Por ejemplo, que "Ver ahora" abra una lista en memoria con app, remitente y hora, y que tocar abra la app de origen.
- ¿Se puede leer de forma confiable el remitente y si es un grupo en WhatsApp con `MessagingStyle` / `isGroupConversation`?

Con los resultados, recomendame:

- **(A)** snooze como mecanismo principal, o
- **(B)** plan B: cancelar y guardar, con la base cifrada (SQLCipher + clave en Android Keystore), **solo metadatos** y borrado al entregar.

## Fase 1: núcleo

**Listener y reglas de retención.** Una notificación NUNCA se retiene si cumple alguna de estas condiciones (se detectan por categoría o flags, no solo por paquete):

- categoría `CALL`, `ALARM`, `REMINDER`, `NAVIGATION`, `TRANSPORT`, `SYSTEM` o `STOPWATCH`;
- tiene `FLAG_ONGOING_EVENT` o `FLAG_FOREGROUND_SERVICE`;
- es de media (tiene MediaSession);
- es de SystemUI, de alertas de emergencia o del marcador por defecto;
- es una notificación de la propia NotiBatch;
- tiene el contenido oculto por el sistema (códigos OTP en Android 15+);
- parece un código de verificación en los SMS.

**"Siempre pueden avisarme"** (reemplaza a la "lista blanca"):

- **Por contacto:** se eligen de una lista de *remitentes recientes* detectados por el listener, sin pedir permiso de contactos. Se guardan como **HMAC-SHA256 del nombre normalizado**, con la clave en Keystore, y un alias que pongo yo. Nunca se guardan nombres en texto plano.
- **Por app:** para apps enteras que quiero dejar pasar.
- **Grupos:** quedan retenidos por defecto.
- **Si no se puede determinar el remitente**, la notificación pasa (fail-open).

**Regla de insistencia**, opcional y apagada por defecto: si un mismo contacto de chat individual manda 3 mensajes en 10 minutos, pasa. Los contadores viven solo en memoria y con hash. Nunca aplica a grupos.

**Entregas a horas fijas.** Nada de intervalos:

- Perfiles por tipo de día: "Consultorio" (entregas a los :50, de 9 a 19), "Escritura/tesis" (2 o 3 entregas al día) y "Libre" (la app no actúa).
- Se asigna un perfil a cada día de la semana, y se puede cambiar el del día de hoy desde la app.
- Implementalo con `AlarmManager` (`setWindow` con una ventana chica, o exacta si hace falta y está justificado), no con `PeriodicWorkRequest`, que es inexacto.
- Fuera del horario del perfil, todo pasa normal.

**Entrega.** Cuando se libera la tanda, las notificaciones originales reaparecen. Además hay una notificación resumen `InboxStyle`, con una línea por remitente (por ejemplo, "WhatsApp · Ana, Mamá (3)"), silenciosa, con `VISIBILITY_PRIVATE` y una versión pública sin nombres para la pantalla de bloqueo.

**Fail-open.** Ante cualquiera de estas fallas se entrega todo y se me avisa:

- `onListenerDisconnected` (después de llamar a `requestRebind()`);
- un snooze que falla;
- una entrega atrasada más del doble de lo previsto.

## Fase 2: UI (3 pantallas + tile)

1. **Onboarding**, un permiso por pantalla y con su porqué:
   - acceso a notificaciones, incluido el paso de "Permitir ajustes restringidos" que Android 13+ exige para APKs instalados a mano;
   - `POST_NOTIFICATIONS`;
   - excepción de optimización de batería.

   Después: configuración rápida con valores ya cargados, elección de contactos que siempre pasan, y una **plantilla de mensaje de encuadre** para copiar y mandar a pacientes: "Respondo mensajes a las 13 y a las 20. Si es urgente, llamame."
2. **Inicio**:
   - estado ("Activa · próxima entrega 13:50" / "En pausa hasta las 15:00" / "Fuera de horario");
   - quién está esperando, solo para leer, sin texto de los mensajes;
   - botón **"Ver ahora"**, que se mantiene apretado 1-2 segundos. No tiene tope; después del tercer uso del día aparece un aviso suave, sin bloquear.
3. **Ajustes**: perfiles y horarios, quién pasa siempre, regla de insistencia, bloqueo con huella (opcional) y una sección "Privacidad" que explique qué se guarda y qué no.

Además:

- **Tile de Ajustes rápidos**: activar, pausar 1 hora o pausar hasta mañana. Cambia de aspecto si está en pausa. Nunca pide huella.
- **Indicador persistente**: encendido por defecto, silencioso, **sin conteo**. Muestra solo el estado y la próxima entrega.
- Estados vacíos y de error: permiso revocado, servicio desconectado, entrega atrasada.
- Accesibilidad: TalkBack, áreas táctiles de 48dp y Dynamic Color. UI en español.

## Privacidad y seguridad (no negociable)

- **Sin permiso `INTERNET`**: la app no tiene red. Sin analytics ni reporte remoto de crashes.
- `android:allowBackup="false"`, más `dataExtractionRules` y `fullBackupContent` que excluyan todo.
- **Ningún contenido de mensajes en disco.** Lo que se guarde (configuración, hashes) va cifrado. Todo lo relativo a una tanda se borra al entregarla, como máximo a las 24 h.
- `FLAG_SECURE` en las pantallas que muestran remitentes.
- Logs sin contenido ni nombres.
- No hace falta `RECEIVE_BOOT_COMPLETED` para el listener. Usalo solo si hace falta para reprogramar alarmas después de un reinicio, y explicámelo.

## Fuera del MVP (no construir)

Historial de tandas, estadísticas o "tiempo ahorrado", gamificación, resúmenes con IA, responder desde la app, widget, reglas por palabra clave (quizás en v2) y sincronización.

## Entregables

- Proyecto Gradle (version catalog) que compile en Android Studio.
- Paquetes separados: `listener`, `rules`, `scheduling`, `data`, `ui`.
- Tests unitarios de las reglas de retención y del cálculo de la próxima entrega.
- README corto con cómo instalar el APK, qué permisos pide y por qué, y un **checklist de prueba manual**. El checklist tiene que incluir: forzar el cierre de la app y verificar que no se pierdan notificaciones, recibir una llamada, recibir una alarma y recibir un código 2FA.

# NotiBatch (CLAUDE.md)

App Android nativa (Kotlin, Compose/M3 desde la Fase 2, minSdk 26) de **uso personal** de una psicoterapeuta: entrega las notificaciones de WhatsApp **en tandas a horas fijas** alineadas con su agenda. Se instala por APK. **Privacidad y no perder mensajes urgentes mandan sobre cualquier feature.** Prompt original: `docs/prompt/prompt_notibatch_v2.md`.

## Reglas duras
- Sin permiso `INTERNET`, sin analytics ni crash reporting. `allowBackup=false` + reglas que excluyen todo.
- Ningún contenido de mensajes en disco; logs sin texto, nombres ni keys (ADR-0008).
- Fail-open: ante cualquier duda, la notificación pasa y se avisa (ADR-0005).
- Mecanismo central provisional: `snoozeNotification` (plan A). **No se construye el listener definitivo ni la UI hasta correr la Fase 0** en el teléfono (ADR-0002, `docs/04-fase0-viabilidad.md`).
- Trabajo por fases; al terminar una, parar y pedir prueba en el teléfono.

## Estructura
- `app/src/main/kotlin/app/notibatch/{rules,scheduling}`: lógica pura (JVM, sin `android.*`), con tests en `src/test`.
- `app/src/debug/.../probe`: arnés de Fase 0, solo debug (ADR-0010).
- Paquetes futuros: `listener`, `data`, `ui`.
- `docs/`: `00-proceso` bitácora · `01` consejo · `02` decisiones (ADR) · `03` pendientes · `04` protocolo Fase 0 · `05` checklist manual. Un dato vive en un solo lugar.

## Comandos
- Compilar e instalar debug: `./gradlew installDebug`
- Tests unitarios: `./gradlew testDebugUnitTest`
- Log de la sonda: `adb logcat -s NotiBatchProbe:I`

## Estado
Ver `docs/00-proceso.md` y `docs/03-pendientes.md`. El wrapper de Gradle aún no existe (lo genera Android Studio o `gradle wrapper`); la toolchain no está instalada en esta PC (pendiente P1).

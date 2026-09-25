# Registro de decisiones (ADR)

Decididas por acuerdo del Consejo Directivo ([`01-consejo-directivo.md`](01-consejo-directivo.md)). Estado: **Aceptada**, **Provisional** (vale hasta que la Fase 0 diga otra cosa) o **Reemplazada**.
Lo que necesita al dueño está en [`03-pendientes.md`](03-pendientes.md).

Fecha de todas: 2026-09-25 (sesión 1).

---

## ADR-0001: Repositorio privado, nombre y paquete
- **Estado:** Aceptada.
- **Decisión:** repo privado `notibatch` en la cuenta de GitHub autenticada. Paquete Kotlin/`applicationId`: `app.notibatch` (neutro, sin dominio propio). Sin licencia de código abierto (uso personal, repo privado).
- **Por qué:** el prompt dice uso personal por APK. Un repo privado evita exponer nada por accidente.
- **Disidencia:** ninguna.

## ADR-0002: Fase 0 manda; A/B queda abierta hasta medir
- **Estado:** Aceptada.
- **Decisión:** no se construye el listener definitivo (Fase 1) ni ninguna UI hasta que se ejecute el protocolo de [`04-fase0-viabilidad.md`](04-fase0-viabilidad.md) en el teléfono real. Lo que **sí** se adelanta: la lógica pura (reglas de retención, cálculo de entrega, contador de insistencia) porque no depende de A vs B.
- **Por qué:** el prompt lo pide ("decímelo antes de inventar un parche"). Construir sobre un mecanismo no verificado es el riesgo principal (H1, H3, H8).

## ADR-0003: El snooze es un compromiso irrevocable (semántica provisional)
- **Estado:** Provisional (depende de Fase 0, pregunta Q4).
- **Decisión:** bajo el plan A, lo que ya fue pospuesto **no se puede liberar antes**. Por lo tanto:
  - "Pausar 1 h / hasta mañana", cambio de perfil del día y "Ver ahora" **solo afectan a notificaciones que lleguen después**.
  - "Ver ahora" abre una **lista en memoria** (app, remitente, hora; nunca texto) y tocar un ítem abre la app de origen. La lista no se persiste.
  - La UI dice esto explícitamente; no promete lo que el sistema no puede cumplir.
- **Por qué:** honestidad de la interfaz (Producto) y peor caso aceptable = "llega todo junto a la hora prevista" (Usuaria/o).
- **Disidencia:** Newport prefiere que "Ver ahora" cueste más que un toque; se resuelve con el mantener apretado 1-2 s del prompt.

## ADR-0004: La hora de entrega la fija el snooze; `AlarmManager` es vigilante y resumen
- **Estado:** Aceptada (verificar en Fase 0 la precisión real del snooze, Q1).
- **Decisión:** al posponer, la duración = `próxima_entrega − ahora`. `AlarmManager.setWindow` (inexacta, sin permiso especial) dispara la notificación resumen y el chequeo de "entrega atrasada". No se pide `SCHEDULE_EXACT_ALARM` ni `USE_EXACT_ALARM`.
- **Por qué:** menos permisos = menos superficie. Nada de `PeriodicWorkRequest`, como pide el prompt.
- **Nota:** `RECEIVE_BOOT_COMPLETED` solo se agrega si Fase 0 muestra que las alarmas se pierden tras reiniciar y los snoozes no (ver ADR-0007).

## ADR-0005: Fail-open ante toda duda
- **Estado:** Aceptada.
- **Decisión:** pasa (no se retiene) toda notificación cuando: no se puede determinar el remitente; hay excepción en la lógica; el servicio se reconectó y no sabe el estado; el reloj/hora no está disponible; la categoría es ambigua. Se registra el evento (sin contenido) y se avisa al usuario.
- **Por qué:** prioridad del prompt. Un falso "pasa" cuesta una interrupción; un falso "retiene" puede costar una urgencia.

## ADR-0006: Cifrado propio con Keystore, sin dependencias extra
- **Estado:** Aceptada.
- **Decisión:** configuración y alias se guardan cifrados con AES-256-GCM cuya clave vive en Android Keystore (no exportable). Los contactos "siempre pasan" se guardan como HMAC-SHA256 (clave también en Keystore). Se usa `javax.crypto` directamente; no se usan `EncryptedSharedPreferences` (deprecado) ni SQLCipher salvo que Fase 0 obligue al plan B. El alias cifrado resuelve H5.
- **Disidencia:** Seguridad preferiría Tink por auditabilidad; se acepta `javax.crypto` porque el uso es mínimo y evita una dependencia de terceros (cadena de suministro). Reabrir si el código crece.

## ADR-0007: Sin persistencia de la lista de espera (plan A)
- **Estado:** Provisional (P5 en pendientes).
- **Decisión:** la lista de "quién espera" vive solo en RAM. Si el proceso muere, la pantalla de inicio dice "puede haber mensajes en espera desde HH:MM" (estado honesto) y la entrega ocurre igual (la hace el sistema vía snooze).
- **Por qué:** cero datos de pacientes en disco. Costo: se pierde detalle tras una muerte del proceso. Si el dueño prefiere persistir metadatos cifrados hasta la entrega (máx. 24 h), es el plan B ligero: ver P5.

## ADR-0008: Política de logs
- **Estado:** Aceptada.
- **Decisión:** ningún log contiene texto, título, nombre de remitente, ni la `key` cruda (la key de WhatsApp puede incluir número de teléfono en el `tag`). Para correlacionar eventos se usa un **id efímero**: los primeros 6 hex de SHA-256(sal aleatoria por proceso + key). Se registra: evento, paquete, categoría, flags, tiempos. Fase 0 lo implementa así.
- **Por qué:** los logs de Android los pueden leer `adb` y apps con `READ_LOGS` en algunos contextos; el requisito "logs sin contenido ni nombres" del prompt incluye la key.

## ADR-0009: Toolchain y versiones
- **Estado:** Aceptada para AGP/Gradle/SDK 37 (compilan y pasan tests el 2026-09-25 con Gradle 9.8.0 y JDK 21); el resto Provisional (Compose, P12).
- **Decisión:** AGP 9.4.0, Gradle según lo que pida AGP (el wrapper lo genera Android Studio), `compileSdk = 37`, `targetSdk = 37` ("último estable" según el prompt; fallback 36 si 37 da problemas), `minSdk = 26`, Kotlin incluido en AGP 9 + plugin de Compose recién en Fase 2, Compose BOM 2026.08.00 (Fase 2). Version catalog en `gradle/libs.versions.toml`.
- **Por qué:** búsqueda web del 2026-09-25 (fuentes al final de [`00-proceso.md`](00-proceso.md)). La compilación se verificó después de instalar la toolchain (ver `00-proceso.md`).

## ADR-0010: Arnés de Fase 0 solo en la variante `debug`
- **Estado:** Aceptada.
- **Decisión:** el servicio sonda y su receptor de control (comandos por `adb shell am broadcast`) viven en `src/debug`, con el receiver protegido por el permiso `android.permission.DUMP` (lo tiene `adb shell`, no las apps de terceros; un receiver `exported=false` rechazaría también a `adb`, que corre como uid 2000). El APK `release` que se instale definitivamente no contiene código de sonda.
- **Por qué:** una superficie de control por broadcast en el APK final sería un vector (Seguridad).

## ADR-0011: Pruebas automáticas solo de lógica pura
- **Estado:** Aceptada.
- **Decisión:** JUnit sobre `rules` y `scheduling` (JVM puro, `java.time`, sin `android.*`). El código con `android.*` (listener, Keystore, alarmas) se prueba con el checklist manual. Esa separación obliga a que las reglas reciban un objeto de "hechos" (`NotificationFacts`) y no la `StatusBarNotification`.

## ADR-0012: Estructura de documentación
- **Estado:** Aceptada.
- **Decisión:** `docs/00-proceso.md` (bitácora cronológica), `01` consejo, `02` decisiones (este archivo), `03` pendientes, `04` protocolo Fase 0, `05` checklist manual. Un dato vive en un solo lugar y el resto lo referencia. `CLAUDE.md` en la raíz resume spec y comandos.

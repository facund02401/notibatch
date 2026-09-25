# Bitácora de proceso

Cronológica. Las decisiones están en [`02-decisiones.md`](02-decisiones.md) y lo pendiente en [`03-pendientes.md`](03-pendientes.md); acá solo queda qué se hizo y con qué trazabilidad.

## 2026-09-25, sesión 1
- Se revisó el prompt (`docs/prompt/prompt_notibatch_v2.md`, único archivo de la carpeta) y se convocó al Consejo ([`01`](01-consejo-directivo.md)).
- Se escribió el andamiaje Gradle y el arnés de Fase 0 (`src/debug`). Sin compilar (sin JDK/SDK en la PC).
- Versiones (ADR-0009) tomadas de búsqueda web ese día; sin verificar en compilación.

## 2026-09-25, sesión 2
- Se retomó la sesión 1, que había quedado sin commit ni repo ni los docs 04/05/CLAUDE.md.
- Repo privado creado: `facund02401/notibatch` (ADR-0001). Commit inicial con el andamiaje.
- Lógica pura de la Fase 1 adelantada (ADR-0002): `rules` (retención, OTP, insistencia) y `scheduling` (perfiles, próxima entrega), con tests JUnit. **Escritos pero sin ejecutar**: no hay JDK (P1).
- Escritos `04-fase0-viabilidad.md`, `05-checklist-manual.md`, `CLAUDE.md` y `README.md`.
- Desvío del prompt anotado: retención por app elegida en vez de "retener todo" (P4, recomendación del Consejo, pendiente de confirmar).
- Decisión de diseño en `NextDelivery`: si ya no quedan horas fijas en la ventana, se libera al cierre de la ventana (`activeTo`), no antes ni después.

## 2026-09-25, sesión 2 (toolchain)
- Instalados: Temurin JDK 21 (winget), SDK de Android por `sdkmanager` (platform-tools, plataforma 37.0, build-tools 37.0.0; el build bajó además build-tools 36) en `%LOCALAPPDATA%\Android\Sdk`, wrapper de Gradle 9.8.0.
- `./gradlew testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL, 30 tests en verde**. Las versiones de ADR-0009 (AGP 9.4.0, compileSdk/targetSdk 37) funcionan.
- Bug encontrado por el compilador: regex de `OtpDetector` con escapes inválidos en string común; corregido con string crudo.
- El instalador de Android Studio (`winget`) se colgó porque ignora el modo silencioso y abre una ventana; queda para instalar a mano.

## 2026-09-25, sesión 2 (trabajo sin teléfono)
- Adelantado lo que no depende de A vs. B (ADR-0002): `SenderKeys` (normalización + HMAC), `WaitingList` (solo RAM), `BatchSummary` (resumen con versión pública sin nombres), `DeliveryWatchdog` (atraso > doble), `PauseOptions`, `SeeNowCounter`, `StatusText` y borrador del mensaje de encuadre (P9), todos con tests: **51 tests en verde**.
- `KeystoreCrypto` (AES-256-GCM + HMAC-SHA256 en Android Keystore, ADR-0006): **compila pero no se pudo ejecutar** (requiere dispositivo). Se prueba con el checklist manual.
- Bug encontrado por un test: el resumen contaba mensajes de grupo en vez de grupos distintos.
- Sigue bloqueado por la Fase 0: listener definitivo, decisión de snooze vs. base cifrada, y toda la UI.

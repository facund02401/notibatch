# Checklist de prueba manual

Lo que no cubren los tests automáticos (ADR-0011). Marcá cada ítem en tu teléfono antes de confiar en una versión.

## Instalación y permisos
- [ ] `./gradlew installDebug` (o APK release firmado) instala sin errores.
- [ ] Acceso a notificaciones activado (incluyendo "Permitir ajustes restringidos" en Android 13+).
- [ ] `POST_NOTIFICATIONS` concedido; excepción de optimización de batería concedida.
- [ ] **Verificación de manifiesto:** la app **no** pide `INTERNET` (`aapt dump permissions app-release.apk` o pestaña Manifest de Android Studio → "Merged Manifest"). Si aparece, es un bug.
- [ ] `allowBackup=false` en el manifiesto fusionado.

## Retención
- [ ] Mensaje individual de un contacto no marcado → llega a la próxima hora fija, con botón de responder, y abre el chat correcto.
- [ ] Mensaje de un contacto marcado "siempre pasa" → llega en el acto.
- [ ] Mensaje de un grupo → retenido hasta la hora fija.
- [ ] Notificación de otra app no elegida (p. ej. banco) → llega en el acto.
- [ ] **Recibir una llamada** (teléfono y llamada de WhatsApp) → suena en el acto.
- [ ] **Recibir una alarma** (reloj) → suena en el acto.
- [ ] **Recibir un código 2FA** (SMS o app) → llega en el acto.
- [ ] Música/navegación en curso → no se toca.

## Robustez (fail-open)
- [ ] **Forzar el cierre de la app** (Ajustes → Apps → Forzar detención) con mensajes pospuestos → no se pierde ninguno; llegan a la hora prevista o al reactivar.
- [ ] Revocar el acceso a notificaciones → la app muestra el estado de error y no retiene nada nuevo.
- [ ] Reiniciar el teléfono con mensajes pospuestos → se comportan según lo medido en Fase 0 (Q3).
- [ ] Apagar el ahorro de batería y volver a activarlo: el servicio se reconecta (`requestRebind`).

## Privacidad
- [ ] Pantalla de bloqueo: la notificación resumen no muestra nombres.
- [ ] Captura de pantalla en pantallas con remitentes → bloqueada (`FLAG_SECURE`).
- [ ] `adb logcat | grep -i NotiBatch` con mensajes reales → sin texto, sin nombres, sin keys.
- [ ] Almacenamiento de la app: solo datos cifrados (`adb shell run-as app.notibatch ls -R` en debug).

## UI (Fase 2)
- [ ] TalkBack lee las tres pantallas y el tile; áreas táctiles ≥ 48 dp.
- [ ] "Ver ahora" exige mantener apretado 1–2 s; tras el tercer uso del día aparece el aviso suave.
- [ ] El tile activa, pausa 1 h y pausa hasta mañana; cambia de aspecto en pausa; nunca pide huella.

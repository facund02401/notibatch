# Pendientes que necesitan tu decisión o tu teléfono

Estado: 🔴 bloquea trabajo · 🟡 conviene decidir pronto · 🟢 puede esperar.
Cada ítem trae la **recomendación del Consejo** (no es una decisión tomada).

## Bloqueantes

### P1 🔴 Instalar la toolchain en esta PC (o compilar en Android Studio)
- **Situación:** esta máquina no tiene JDK ni Android SDK. No pude compilar ni correr los tests. El código está escrito pero **sin compilar**.
- **Opciones:** (a) instalás Android Studio y abrís el proyecto (genera el wrapper de Gradle); (b) me autorizás a instalar JDK 21 + Android command-line tools por `winget`/descarga (varios GB, modifica tu PC); (c) compilás en otra máquina.
- **Recomendación:** (a) o (b). Con (b) puedo correr los tests de las reglas yo mismo.

### P2 🔴 Datos de tu teléfono
Necesarios para la Fase 0 y para elegir A vs. B.
- Marca/modelo y versión de Android (fabricantes con ahorro de batería agresivo —Xiaomi, Samsung, Huawei, Oppo— matan servicios).
- WhatsApp normal o Business; ¿usás dos instancias, perfil de trabajo, "Carpeta segura"/Second Space?
- ¿Depuración USB disponible? (Necesaria para el arnés de pruebas.)

### P3 🔴 Correr el protocolo de Fase 0 en el teléfono
- Instrucciones en [`04-fase0-viabilidad.md`](04-fase0-viabilidad.md). Con los resultados el Consejo recomienda A o B (ADR nuevo). Hasta entonces no se avanza con el listener definitivo ni la UI.

## Decisiones de producto

### P4 🟡 ¿Qué apps se retienen por defecto?
- El prompt dice "por app: apps que quiero dejar pasar", lo que implica que **por defecto se retiene todo**. Eso alcanzaría banco, transporte, delivery, etc.
- **Recomendación del Consejo:** al revés: se retienen solo las apps que elijas (WhatsApp / WhatsApp Business de entrada) y el resto pasa. Reduce el riesgo de perder algo, reduce el trabajo de configuración. Seguridad, Producto y Usuaria/o de acuerdo; Newport neutral (su foco es la mensajería).
- **Necesito:** confirmación. Si no respondés, implemento la recomendación en Fase 1 pero la marco como desvío del prompt.

### P5 🟡 ¿Aceptás guardar metadatos cifrados hasta la entrega?
- Plan A puro: la lista "quién espera" solo en RAM (se pierde si el sistema mata el proceso). Alternativa: guardar app + remitente (cifrado, con hash/alias) + hora, borrado al entregar y como máximo a las 24 h.
- **Recomendación:** empezar en RAM y decidir con lo que muestre la Fase 0 (qué tan seguido muere el proceso en tu teléfono).

### P6 🟡 Horarios reales
- El prompt tiene un ejemplo (:50 de 9 a 19) y otro (mensaje de encuadre con "13 y 20") que **no coinciden**.
- **Necesito:** por tipo de día, ¿a qué horas querés entregas? ¿Qué días de la semana son consultorio / escritura / libre?
- Mientras, los perfiles vienen precargados como en el prompt y son editables.

### P7 🟡 Grupos
- Retenidos por defecto (prompt). ¿Tenés grupos de padres o de trabajo donde pueda haber urgencias? ¿Querés que algún grupo concreto pase siempre? (Hoy no se puede identificar un grupo sin guardar su nombre: mismo tratamiento hash/alias que los contactos.)

### P8 🟢 Valores de la regla de insistencia
- Prompt: 3 mensajes en 10 minutos, apagada por defecto. ¿Te sirven esos números?

### P9 🟢 Texto del mensaje de encuadre para pacientes
- Preparo un borrador; el texto y los horarios reales dependen de P6 y de cómo hablás con tus pacientes. Newport lo considera pieza central del sistema.

## Seguridad y operación

### P10 🟡 Clave de firma del APK
- Hay que firmar el APK con una clave tuya. **Si la perdés, no podés actualizar la app instalada** (hay que desinstalar y perder la configuración).
- **Necesito:** dónde guardás el `.jks` y su contraseña (fuera del repo; nunca se sube). Recomendación: gestor de contraseñas + copia offline.

### P11 🟡 Marco normativo
- Que alguien te escriba como paciente es un dato sensible. Conviene que confirmes con tu colegio profesional/asesor legal si el uso de esta herramienta requiere algo (consentimiento, registro de bases). **No es asesoramiento legal y el Consejo no lo resuelve.**

### P12 🟢 `targetSdk`
- Puse 37 ("último estable"). Si al abrir Android Studio no existe o da problemas, se baja a 36. Confirmá qué versión de Android tiene tu teléfono (P2): no cambia nada para vos, pero sí los avisos de compilación.

### P13 🟢 Bloqueo con huella
- Opcional en el prompt. Confirmar si lo querés en el MVP o se difiere.

## Cómo se cierra un pendiente
Cuando decidas, se pasa a un ADR en [`02-decisiones.md`](02-decisiones.md) y se tacha aquí con el número de ADR.

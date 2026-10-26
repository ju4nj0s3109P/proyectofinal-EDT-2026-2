# Acuerdos de diseño

Este archivo es el contrato común. El equipo acuerda aquí los cambios antes de que afecten código de varios módulos. No cambien decisiones unilateralmente.

## Decisiones iniciales
1. **Java 17 + Maven**, paquete raíz `co.edu.uq.sga`.
2. **Capas:** `dominio` contiene modelos y enums; `estructuras` implementa las colecciones propias; `servicios` contiene reglas de negocio; `persistencia` carga/guarda; `interfaz` solo presenta datos y captura acciones.
3. **Reservas por apartamento:** una reserva puede asignarse a uno o varios apartamentos. Se crea una asignación por apartamento para revisar disponibilidad; todas las asignaciones comparten el folio y el ciclo de vida de la reserva.
4. **Fechas:** estancia como intervalo semiabierto `[entrada, salida)`. Hay conflicto si `inicioA < finB && inicioB < finA`. Cada noche facturada es una fecha desde entrada inclusive hasta salida exclusiva.
5. **Estados:** el estado operativo del apartamento y el estado de la reserva son conceptos distintos. No inferir uno automáticamente del otro salvo en servicios de check-in, check-out y preparación.
6. **Dinero:** usar `BigDecimal`; tarifas y cargos no se almacenan como `double`.
7. **Java Collections:** no usar implementaciones de colecciones de `java.util` en el núcleo. Para fechas, dinero, interfaces y excepciones sí se permite usar clases estándar. La GUI o carga de datos tampoco debe reemplazar las estructuras requeridas.
8. **Errores:** servicios validan reglas y devuelven resultado explícito o lanzan excepción de dominio definida; la interfaz no duplica validaciones de negocio.
9. **Persistencia inicial:** escenario de prueba legible en archivo plano; evitar agregar base de datos hasta que se solicite/ acuerde.

## Cómo cambiar un acuerdo
Abrir PR que modifique este documento, describir impacto en módulos y esperar revisión de al menos otro integrante antes de implementar el cambio transversal.

## Reglas acordadas para reservas
1. Una reserva pendiente bloquea sus noches durante 24 horas desde su creación. Si no se confirma a tiempo, se cancela y libera todas las asignaciones.
2. El canal Portal crea reservas pendientes. Se confirman al registrar un anticipo acumulado de al menos el 30 % de la cotización total.
3. Las reservas Directas quedan confirmadas cuando recepción las registra y el índice acepta todas sus asignaciones.
4. Las solicitudes Externas se procesan en orden FIFO. Una solicitud aceptada sin conflicto queda confirmada; una solicitud con conflicto se rechaza y se registra para reportes.
5. Cancelar está permitido antes del check-in, para reservas pendientes o confirmadas. Mientras no exista una política de penalidad aprobada por el alojamiento, se devuelve el saldo a favor completo del huésped; se informa el valor antes de confirmar la cancelación.
6. Los pagos devueltos se registran como movimientos `DEVOLUCION`. No se borran ni alteran pagos anteriores.
7. Las reservas Pendiente, Confirmada y En curso bloquean disponibilidad. Cancelada, No-show y Finalizada se retiran del índice activo; su historial y folio se conservan.
8. El check-in requiere que todos los apartamentos asignados estén Preparados. El check-out finaliza la reserva y marca todos los apartamentos asignados como Pendientes de preparación.

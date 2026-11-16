# Contratos y límites de módulos

## Regla de coordinación
Cada módulo tiene una persona responsable. Otros integrantes no editan esos archivos sin coordinarlo primero. La interfaz pública se acuerda antes del trabajo paralelo; cambios incompatibles se hacen mediante PR y aviso al equipo.

## Contratos iniciales
- **Dominio:** modelos sencillos, validación local e invariantes; no contiene GUI, archivos, ni estructuras de datos.
- **Estructuras:** genéricas, sin depender de clases del dominio. Iteradores y nodos se diseñan aquí. Añadir pruebas unitarias de cada estructura.
- **Servicios:** dependen de dominio y estructuras; concentran las reglas del proyecto. No importan Swing/JavaFX.
- **Persistencia:** convierte archivo ↔ objetos usando servicios/repositorios acordados. No mantiene una segunda versión de reglas.
- **Interfaz:** llama servicios; no accede directamente a árboles/colas ni muta modelos compartidos.

## Interfaces mínimas a acordar
Antes de iniciar implementación paralela, definir y revisar:
- `ArbolBusqueda<K,V>`: insertar, buscar, eliminar, recorrer.
- `Cola<T>` y `Pila<T>`: operaciones básicas, excepción/resultado en vacío.
- `Repositorio<T, ID>`: buscar, guardar y listar mediante estructura propia.
- `ServicioReservas`: disponibilidad, crear/confirmar/cancelar/check-in/check-out.
- `ServicioPreparacion`: encolar, despachar y calcular ruta.
Los nombres pueden cambiar por consenso, pero publiquen el contrato en un PR pequeño antes de depender de él.

## Contrato del índice de reservas por apartamento
La interfaz `co.edu.uq.sga.dominio.contratos.IndiceReservas` es el límite entre servicios y la estructura de búsqueda. La implementación del árbol pertenece al responsable de Estructuras.

- `agregarSiDisponible(Reserva)` comprueba de forma atómica todas las asignaciones de la reserva. Si cualquiera se solapa con una reserva activa, devuelve `false` y no inserta ninguna asignación. Si no hay conflictos, indexa todas y devuelve `true`.
- `retirarSiExiste(Reserva)` quita de forma atómica todas las asignaciones por folio y devuelve `true` si las retiró; devuelve `false` si no estaban indexadas.
- `contiene(Reserva)` informa si las asignaciones completas de esa reserva están en el índice.
- `estaDisponible(Reserva)` consulta todas las asignaciones de forma atómica, sin mutar el árbol. Devuelve `false` si hay solapamiento; omite del conflicto las entradas del mismo folio para que una reserva pueda consultar su propio intervalo.
- El árbol se organiza por código de apartamento y fecha de entrada. La comprobación de solapamiento recorre recursivamente las reservas relevantes y usa intervalos semiabiertos `[entrada, salida)`.
- Pendiente, Confirmada y En curso son estados activos que bloquean noches. Cancelada, Rechazada, No-show y Finalizada no bloquean.
- Cada reserva multia-apartamento se agrega o se retira como una sola operación; nunca debe quedar indexada parcialmente.

## Contratos de canal y cancelación
- Portal: se registra Pendiente y se confirma con anticipo acumulado mínimo del 30 %. El anticipo es el **dinero recibido** del huésped (`ServicioFolios.totalPagado`), no el saldo del folio: una estancia ya cargada al folio no debe impedir la confirmación.
- Directa: se confirma al registro, después de validar conflictos.
- Externa: se procesa FIFO; conflicto significa rechazo y registro del conflicto.
- Cancelación previa al check-in: permite Pendiente o Confirmada y devuelve el 100 % del dinero recibido mientras no se apruebe otra política. `ServicioReservas.calcularDevolucion(Folio)` informa el valor antes de ejecutarla.
- `ServicioReservas.cancelar(Reserva, Folio)` **no registra movimientos**: el folio es el único dueño de su historial. Quien cancela llama antes a `ServicioFolios.cerrarPorCancelacion(...)`; si el folio todavía tiene dinero del huésped, `cancelar` lanza `ExcepcionDominio` y la reserva sigue activa.

- `ServicioReservas.expirarPendiente(Reserva, LocalDateTime)` devuelve `true` si venció y fue retirada del índice; devuelve `false` antes de cumplir las 24 horas. Solo aplica a reservas pendientes del portal.
- `ServicioReservas.vencimientoDe(Reserva)` es el único lugar donde vive el plazo de 24 horas; la cola de pendientes y el servicio de vencimientos lo reutilizan.

- `ServicioReservas.consultarDisponibilidad(Reserva)` devuelve `false` ante conflicto o si algún apartamento está fuera de servicio; no registra ni modifica la reserva.


## Preparación de apartamentos
- `ColaPreparaciones` es un puerto hacia las estructuras del módulo Estructuras; no implementa una cola propia ni fija por sí mismo la política de extracción.
- `ColaPreparacionesEnCola` adapta la cola FIFO genérica y sirve para escenarios que requieren orden de llegada.
- `ColaPreparacionesPorPrioridad` adapta la cola de prioridad genérica y se configura con `TareaPreparacion.comparadorPorPrioridad(fechaActual)`.
- `TareaPreparacion` conserva por separado su fecha de creación y la fecha/hora de llegada asociada. El comparador coloca primero las llegadas de la fecha actual, ordenadas por hora; las demás quedan después en orden cronológico de llegada. La fecha de creación y el código resuelven empates de forma determinista.
- Al crear una tarea desde una reserva, el servicio que coordine el check-out debe suministrar la fecha/hora de la próxima llegada del apartamento. No se debe inferir ese dato a partir de la fecha de creación de la tarea.
- `CalculadorRutas` es un puerto hacia el grafo; devuelve una ruta con origen y destino incluidos, o un arreglo vacío si no hay ruta.
- `ServicioPreparacion.encolar(TareaPreparacion)` agrega tareas para apartamentos pendientes y evita duplicarlas al cambiar el estado operativo.
- `despacharSiguiente()` retira la siguiente tarea según la política del adaptador; se procesa una tarea a la vez y devuelve `Optional.empty()` cuando no hay tareas.
- `completar(TareaPreparacion)` cierra la tarea actual y marca el apartamento como Preparado.


## Procesamiento de solicitudes externas
- `ColaSolicitudesExternas` mantiene solicitudes FIFO y permite consultar si ya hay una solicitud con el mismo folio encolada.
- `ServicioSolicitudesExternas` añade solicitudes externas pendientes y procesa una por llamada.
- Una solicitud sin conflicto se confirma por medio de `ServicioReservas`.
- Un conflicto de fechas o un apartamento fuera de servicio marca la reserva como `RECHAZADA` y registra un `ConflictoReserva` mediante `RegistroConflictosReservas`.
- La estructura de cola pertenece al módulo Estructuras; la implementación del registro de conflictos debe coordinarse con Persistencia.
- `RegistroConflictosEnLista` implementa el registro sobre la `ListaEnlazada` propia y expone `conflictos()`, `conflictosDe(codigoApartamento)` y `tamanio()` para el reporte de conflictos de canal. Guarda en memoria; si Persistencia necesita conservarlos entre ejecuciones, implementa el mismo puerto sin cambiar los servicios.
- Una solicitud que dejó de ser procesable mientras esperaba en la cola (cancelada, de otro canal) no se descarta en silencio: se registra como conflicto con motivo `solicitud ya no procesable` y se devuelve como resultado rechazado.


## Folios
- `RepositorioFolios` es el puerto para guardar y buscar folios por código. `RepositorioFoliosArbol` lo implementa con el `DiccionarioArbol` propio.
- `ServicioFolios.abrir(Reserva)` crea el folio con el mismo código de la reserva. Una reserva tiene un solo folio; abrirlo dos veces lanza `ExcepcionDominio`.
- `obtener(codigo)` devuelve el folio o lanza `ExcepcionDominio` si no existe.
- `registrarCargo` y `registrarPago` crean el movimiento con código `<folio>-M<n>` y con la fecha/hora que indica quien llama. Los movimientos nunca se borran ni se modifican.
- `PilaDeshacerFolios` es el puerto hacia la pila propia; `PilaDeshacerFoliosArbol` guarda una `Pila` por folio. Cada cargo o pago se apila; los reversos no.
- `deshacerUltima(codigo, fechaHora)` desapila la última operación y registra su contrario (`REVERSO_CARGO` o `REVERSO_PAGO`) por el mismo monto. Sin operaciones lanza `ExcepcionDominio`; si el reverso falla, la operación vuelve a la pila.
- `registrarDevolucion` registra un movimiento `DEVOLUCION` solo hasta el saldo a favor del huésped; si lo supera lanza `ExcepcionDominio`. Las devoluciones no entran en la pila de deshacer.
- `totalPagado(codigo)` es el dinero recibido del huésped: pagos menos reversos de pago y devoluciones. Los cargos no cuentan. Es la base para medir el anticipo del 30 % y la devolución al cancelar. `totalPagado(Folio)` es la versión estática para quien ya tiene el folio.
- `cerrarPorCancelacion(codigo, fechaHora)` es el único paso financiero de una cancelación: reversa con `REVERSO_CARGO` los cargos que siguen vigentes, registra la `DEVOLUCION` por todo el dinero recibido y devuelve ese monto. Nada se borra y llamarlo dos veces no reversa dos veces el mismo cargo.
- Tras cerrar el folio se vacía su pila de deshacer (`PilaDeshacerFolios.limpiar`): un folio cerrado no se deshace operación por operación.
- Deshacer un pago no revisa el estado de la reserva. Si se deshace el anticipo de una reserva ya confirmada, recepción debe cancelarla o volver a cobrar: el folio no conoce la reserva.


## Vencimiento automático de reservas pendientes
- `ColaReservasPendientes` es el puerto de las reservas pendientes ordenadas por hora de vencimiento. `ColaReservasPendientesPorVencimiento` lo implementa con la `ColaPrioridad` propia y recibe el comparador, igual que la cola de preparación.
- El comparador lo da `ServicioVencimientoReservas.comparadorPorVencimiento()`: vence antes primero y el folio resuelve empates.
- `ServicioVencimientoReservas.vigilar(Reserva)` admite solo reservas pendientes del portal. `dejarDeVigilar(Reserva)` la saca cuando se confirma o se cancela.
- `procesarVencidas(ahora)` cancela todas las vencidas y devuelve cuántas venció. Se detiene en la primera que aún no vence, porque la cabeza de la cola es la que vence primero. Una reserva que dejó de estar pendiente solo sale de la cola.
- Quien abra la aplicación o atienda recepción debe llamar `procesarVencidas(LocalDateTime.now())` al iniciar y al refrescar los paneles de reservas.

## Disponibilidad y asignación de grupos
- `ReservasPorApartamento` es el puerto de consulta de las reservas activas de un apartamento. `ReservasDelIndice` lo implementa sobre `IndiceReservasArbol.reservasDe(...)`, sin crear un índice paralelo.
- `ServicioDisponibilidad.estaLibre(Apartamento, Estancia)` es `false` si el apartamento está fuera de servicio o si una reserva activa comparte noche. La noche de salida no ocupa.
- `buscarDisponibles(catalogo, estancia, personas, orden)` devuelve los libres en los que cabe el grupo, ordenados con el comparador que elija el huésped; con `null` usa el orden natural por código. El catálogo de apartamentos lo suministra quien llama (persistencia o interfaz).
- `asignarGrupo(catalogo, estancia, personas)` resuelve el grupo grande con recursividad y backtracking: devuelve la combinación de apartamentos libres con el menor número de apartamentos, o una lista vacía si el grupo no cabe.
- `ComparadoresApartamento` reúne los órdenes alternativos: `porPrecio()` (tarifa base), `porCapacidad()`, `porDormitorios()` y `porNumeroDeReservas(ReservasPorApartamento)`. Todos terminan en el orden natural por código para que el resultado sea estable.

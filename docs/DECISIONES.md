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
4. Las solicitudes Externas se procesan en orden FIFO. Una solicitud aceptada sin conflicto queda confirmada; una solicitud con conflicto se marca Rechazada y se registra para reportes. Apartamentos fuera de servicio también causan rechazo registrado.
5. Cancelar está permitido antes del check-in, para reservas pendientes o confirmadas. Mientras no exista una política de penalidad aprobada por el alojamiento, se devuelve **todo el dinero recibido** del huésped; se informa el valor antes de confirmar la cancelación. La cancelación reversa primero los cargos vigentes de la estancia, para que el folio de una reserva cancelada no quede con un saldo pendiente que nadie va a cobrar.
6. Los pagos devueltos se registran como movimientos `DEVOLUCION`. No se borran ni alteran pagos anteriores. El folio es el único que registra movimientos: `ServicioReservas` consulta y valida, nunca escribe en el folio.
7. Las reservas Pendiente, Confirmada y En curso bloquean disponibilidad. Cancelada, No-show y Finalizada se retiran del índice activo; su historial y folio se conservan.
8. El check-in requiere que todos los apartamentos asignados estén Preparados. El check-out finaliza la reserva y marca todos los apartamentos asignados como Pendientes de preparación.
9. El anticipo del 30 % se mide sobre el dinero recibido, no sobre el saldo del folio. Así da igual el orden en que recepción cobre la estancia y reciba el anticipo.
10. La búsqueda de disponibilidad consulta solo las reservas del apartamento preguntado, a través del índice del árbol. El catálogo de apartamentos lo suministra quien llama; los servicios no guardan un catálogo propio.
11. Un grupo que no cabe en un apartamento se resuelve con backtracking sobre los apartamentos libres, buscando el menor número de apartamentos. Se exploran de mayor a menor capacidad y se poda en cuanto una rama no pueda mejorar la mejor combinación encontrada.

## Caducidad de reservas
La fecha y hora de creación queda guardada en el modelo `Reserva`. El servicio cancela y libera del índice una reserva del portal si sigue pendiente al alcanzar las 24 horas exactas. El procesador que revise pendientes invoca el servicio con la hora actual; las confirmadas no se expiran.

Las pendientes esperan en una cola de prioridad propia ordenada por hora de vencimiento (`ColaReservasPendientesPorVencimiento`), y `ServicioVencimientoReservas.procesarVencidas(ahora)` las cancela en bloque. Se eligió un servicio aparte en vez de inyectar la cola en `ServicioReservas` para no cambiar el constructor que ya usan los demás módulos; es el mismo patrón de `ServicioSolicitudesExternas`. El plazo de 24 horas vive solo en `ServicioReservas.vencimientoDe(Reserva)`.

## Excepciones de dominio
Las reglas de negocio incumplidas lanzan una subclase de `ExcepcionDominio` (`IllegalStateException`), con tipos específicos para conflicto de disponibilidad, anticipo insuficiente y transición de reserva inválida. Los argumentos nulos o mal formados continúan usando `IllegalArgumentException`.


## Preparación de apartamentos
Una tarea recorre los estados Encolada → En proceso → Completada. Al ingresar a la cola, el apartamento pasa a En preparación para evitar duplicar tareas. El servicio procesa una tarea a la vez y deja el apartamento Preparado al completar. La cola de prioridad genérica despacha primero las tareas cuya llegada está programada para hoy, ordenadas por hora de llegada; las demás quedan después por orden cronológico de llegada.

## Estructuras propias y capa de adaptadores
1. **Paquetes:** `estructuras.lineales` (ListaEnlazada doble, Pila, Cola, ColaPrioridad, ListaCircular, Conjunto), `estructuras.arboles` (ArbolBinarioBusqueda, DiccionarioArbol), `estructuras.grafos` (Grafo, Ruta) y `estructuras.excepciones`. Ninguna clase de `estructuras` importa el dominio.
2. **Adaptadores:** los puertos de `dominio.contratos` se implementan en `co.edu.uq.sga.adaptadores` (`IndiceReservasArbol`, `ColaSolicitudesExternasEnCola`, `ColaPreparacionesEnCola`, `ColaPreparacionesPorPrioridad`, `CalculadorRutasGrafo`, `RepositorioFoliosArbol`, `PilaDeshacerFoliosArbol`, `RegistroConflictosEnLista`, `ColaReservasPendientesPorVencimiento`, `ReservasDelIndice`). Es la única capa que conoce a la vez el dominio y las estructuras.
   - Excepción acordada: un servicio puede **devolver** una estructura propia cuando el resultado es una colección que la interfaz va a recorrer (`ServicioDisponibilidad` y el desglose de `ServicioTarifas` devuelven `ListaEnlazada`), y `ServicioTarifas` guarda en una `ListaEnlazada` las temporadas definidas, porque son pocas y son parte de su propia configuración. El estado que se consulta y se indexa (reservas, folios, colas) sigue viviendo en los adaptadores. Los puertos de `dominio.contratos` no mencionan estructuras.
3. **Vacío:** operar sobre una estructura vacía lanza `EstructuraVaciaException` (no comprobada, hereda de `NoSuchElementException`). Los argumentos nulos lanzan `IllegalArgumentException`.
4. **Árbol binario:** sigue el orden natural (`Comparable`) y no admite duplicados (`insertar` devuelve `false`). Ofrece `rango`, `anterior` y `siguiente` recursivos para detectar solapamientos sin recorrer todo el árbol.
5. **Diccionario:** `DiccionarioArbol<K,V>` sobre el árbol reemplaza a `HashMap`/`TreeMap` cuando se necesita clave → valor.
6. **Cola de prioridad:** montículo binario con `Comparator`; sale primero el menor y en empate el más antiguo.
7. **Grafo:** no dirigido, con listas de adyacencia, peso `double` positivo y pasillos que se pueden cerrar y abrir. Ofrece Dijkstra, BFS, DFS recursivo, ruta por paradas, inalcanzables y componentes conexas.
8. **Permitido de `java.util`:** solo `Iterator`, `Comparator`, `NoSuchElementException`, `Objects`, `Optional` y `java.util.function`. Los arreglos nativos se permiten como almacenamiento interno.

## Temporadas y tarifas
`ServicioTarifas` es el dueño de las temporadas: `definirTemporada(Temporada)` las registra y rechaza cualquiera que se solape con otra ya definida, así que una noche nunca queda en dos temporadas. `cotizar(apartamento, estancia, ocupantes)` usa esas temporadas; ya no se pasan como parámetro en cada llamada. Una estancia que cruza varias temporadas se liquida noche por noche y el desglose va en la lista doblemente enlazada propia.

Las tarifas por apartamento son campos de `Apartamento` y hoy no se pueden editar después de crearlo. Cambiar eso requiere un cambio en el modelo, que pertenece al responsable del dominio: queda pendiente de coordinar.

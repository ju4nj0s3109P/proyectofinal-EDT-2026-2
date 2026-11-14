# equipo y responsabilidades

Este documento refleja el estado del repositorio en `main` después de integrar los PR de estructuras propias y la integración del dominio. La responsabilidad de cada módulo debe mantenerse clara: quien sea dueño de un módulo coordina los cambios y revisa las contribuciones que lo afecten.

Cada integrante debe acumular al menos 10 commits propios con Conventional Commits. Completar los nombres y usuarios de GitHub cuando el equipo los confirme.

## estado general

| área | responsable | estado |
|---|---|---|
| estructuras propias genéricas | integrante 1 | implementadas e integradas en `main` |
| dominio y contratos | integrante 2 | implementación del alcance asignado terminada e integrada |
| reservas, tarifas, solicitudes externas y folios | integrante 3 | servicios base existentes; falta completar folios y cerrar integración |
| preparación, grafo, persistencia, datos e interfaz | integrante 4 | lógica base parcial; falta configuración e integración de la aplicación |

## integrante 1 — estructuras propias

**estado: implementación principal terminada e integrada.**

El primer integrante desarrolló las estructuras genéricas del proyecto. No se debe volver a implementar una segunda versión de estas estructuras dentro del dominio o de los servicios: el resto del equipo debe reutilizarlas.

Implementado en `src/main/java/co/edu/uq/sga/estructuras/`:

- **Lineales:** `ListaEnlazada<T>` (doblemente enlazada), `Pila<T>`, `Cola<T>`, `ColaPrioridad<T>`, `ListaCircular<T>` y `Conjunto<T>`.
- **Árboles:** `ArbolBinarioBusqueda<T>` y `DiccionarioArbol<K,V>`.
- **Grafos:** `Grafo<T>` y `Ruta<T>`.
- **Excepciones:** `EstructuraVaciaException`.
- **Pruebas:** existen pruebas unitarias para las estructuras y sus operaciones principales.

El grafo incluye recorridos y algoritmos de búsqueda de rutas; la cola de prioridad utiliza un comparador; las estructuras ofrecen iteración. Los cambios de estructuras se integraron mediante los PR #1 y #2.

**Pendiente de este módulo:** apoyar la integración y corregir defectos que aparezcan al conectarlo con servicios o interfaz. No duplicar las estructuras ni sustituirlas por colecciones de `java.util`. Verificar que las nuevas funcionalidades respeten los contratos y las excepciones ya definidos.

## integrante 2 — dominio y contratos

**estado: alcance de dominio asignado terminado e integrado mediante el PR #3.**

El trabajo de dominio ya incorporado comprende:

- Modelos del negocio, enumeraciones, excepciones propias y contratos/puertos de los módulos.
- `Apartamento` implementa `Comparable<Apartamento>` por código.
- `Huesped` implementa `Comparable<Huesped>` por documento.
- `Reserva` implementa `Comparable<Reserva>` por fecha de entrada.
- `Estancia` implementa `Iterable<LocalDate>` y recorre las noches del intervalo semiabierto `[entrada, salida)`.
- `ServicioTarifas` reutiliza el recorrido de `Estancia`, sin mantener un iterador de noches duplicado.
- `TareaPreparacion` conserva por separado la fecha/hora de creación y la fecha/hora de la próxima llegada.
- `TareaPreparacion.comparadorPorPrioridad(fechaActual)` prioriza las llegadas del día por hora ascendente y después ordena las demás por fecha/hora de llegada.
- Los contratos del dominio permanecen desacoplados de las implementaciones concretas de estructuras.
- `IndiceReservasArbol` adapta el ABB y el diccionario del integrante 1; no crea un árbol paralelo. Permite ordenar reservas con igual fecha de entrada y comprobar solapamientos con intervalos semiabiertos.
- Se actualizaron las pruebas de los modelos, la iteración y los adaptadores afectados.

**estado de esta responsabilidad:** no queda pendiente rehacer modelos, comparables, iteración de estancia ni el criterio de prioridad de `TareaPreparacion`. Los demás integrantes deben consumir estos modelos y contratos, y coordinar cualquier cambio transversal antes de modificar sus interfaces.

## integrante 3 — reservas, tarifas, solicitudes externas y folios

**estado: servicios base presentes; falta completar y verificar el flujo financiero y de reservas.**

Ya existen:

- `ServicioReservas`: registro por canal Portal, Directa y Externa; consulta de disponibilidad; confirmación del Portal con anticipo mínimo del 30 %; cancelación y devolución; expiración de pendientes a las 24 horas; no-show; check-in y check-out.
- `ServicioTarifas`: cotización noche por noche, temporadas y regla de no cobro a menores de seis años.
- `ServicioSolicitudesExternas`: procesa solicitudes FIFO, intenta confirmarlas y registra como rechazadas las que presentan conflicto o apartamento fuera de servicio.
- `ColaSolicitudesExternasEnCola` e `IndiceReservasArbol` ya conectan esos servicios con las estructuras propias.

**pendientes por resolver:**

1. Implementar `ServicioFolios` y completar el flujo de movimientos del folio.
2. Incorporar la pila propia para deshacer una operación del folio mediante un movimiento contrario. No se deben borrar ni modificar movimientos históricos.
3. Verificar que los pagos, anticipos, cargos y devoluciones mantengan la inmutabilidad del historial y que las correcciones queden trazables.
4. Completar las pruebas de integración de reservas: creación por canal, confirmación, cancelación, expiración, no-show, check-in y check-out.
5. Verificar que las solicitudes externas se procesen realmente en FIFO y que cada rechazo quede registrado de forma persistente o en el registro acordado.
6. Coordinar con el integrante 4 la conexión del check-out con la creación de la tarea de preparación. El servicio que cree la tarea debe suministrar la fecha y hora de la próxima llegada; no debe reemplazarlas por la hora de creación de la tarea.

## integrante 4 — preparación, grafo, persistencia, datos e interfaz

**estado: adaptadores y lógica de preparación/rutas presentes; falta completar la configuración y conectar la aplicación.**

Ya existen:

- `ServicioPreparacion`: encola, despacha una tarea a la vez y completa la preparación del apartamento.
- `ColaPreparacionesEnCola` y `ColaPreparacionesPorPrioridad`: adaptadores sobre la cola FIFO y la cola de prioridad propias.
- `CalculadorRutasGrafo`: adapta `Grafo<String>` y utiliza Dijkstra para calcular rutas.
- El dominio ya define la información de llegada y el comparador de prioridad que debe utilizar la cola de preparación.

**pendientes por resolver:**

1. Construir y cargar el mapa real del alojamiento: recepción, apartamentos, escaleras, cuartos de aseo y pasillos con sus pesos. El algoritmo y el adaptador existen, pero el mapa concreto debe configurarse con los datos del edificio.
2. Conectar el check-out con la creación y encolado de `TareaPreparacion`. La reserva finalizada deja los apartamentos en estado Pendiente de preparación, pero `ServicioReservas.registrarCheckOut` todavía no crea ni encola la tarea.
3. Consultar la próxima reserva de cada apartamento para obtener su fecha y hora de llegada y pasar esos datos al constructor de `TareaPreparacion`. La prioridad del día depende de ese dato real.
4. Configurar la aplicación con `new ColaPreparacionesPorPrioridad(LocalDate.now())` e inyectarla en `ServicioPreparacion`.
5. Implementar persistencia/carga de datos y el escenario inicial de prueba acordado. Debe incluir los apartamentos, huéspedes, reservas, temporadas, ubicaciones y conexiones del grafo que necesite la demostración.
6. Implementar la interfaz gráfica y sus vistas principales: tablero, reservas/calendario, huésped, preparación, mapa y estadísticas. La interfaz debe llamar a los servicios; no debe manipular directamente las estructuras ni duplicar reglas de negocio.
7. Completar `Main`: actualmente solo imprime un mensaje de estructura inicial. Debe cargar los datos, construir adaptadores y servicios, y abrir la interfaz.
8. Coordinar con el integrante 3 el registro persistente de conflictos de solicitudes externas y con el integrante 2 cualquier cambio requerido en los contratos del dominio.

## pendientes transversales del proyecto

Estos puntos no deben darse por terminados solo porque existan algunas clases base:

- **Asignación de grupos grandes:** falta implementar y probar el algoritmo de backtracking que encuentre una asignación válida usando el menor número posible de apartamentos.
- **Comparadores requeridos:** comprobar que estén cubiertos los criterios de precio, capacidad, cantidad de reservas y preparación. El comparador de prioridad de `TareaPreparacion` ya existe; no volver a implementarlo. Si el enunciado exige comparadores concretos separados, agregarlos reutilizando ese criterio.
- **Expiración automática:** `ServicioReservas.expirarPendiente(...)` ya contiene la regla de 24 horas, pero falta asegurar que el flujo de la aplicación invoque periódicamente o en los momentos adecuados el procesamiento de reservas vencidas.
- **Integración de preparación:** completar el flujo check-out → consulta de próxima llegada → creación de tarea → cola priorizada → despacho → apartamento Preparado.
- **Pruebas de integración:** probar el flujo completo entre dominio, estructuras, adaptadores, servicios, persistencia e interfaz. No basta con probar cada clase por separado.
- **Sustentación y evidencias:** preparar diagramas de clases y estructuras, datos de demostración, recorrido funcional y explicación de qué estructura resuelve cada requisito.
- **Calidad del repositorio:** mantener ramas por integrante, PR revisables, mensajes Conventional Commits y al menos 10 commits propios por integrante.

## reglas para continuar

1. Reutilizar las estructuras del integrante 1; no crear implementaciones duplicadas.
2. Respetar los contratos de `docs/CONTRATOS-MODULOS.md` y los acuerdos de `docs/DECISIONES.md`.
3. Cada responsable coordina los cambios de su módulo y solicita revisión cuando afecte otro módulo.
4. No marcar una funcionalidad como terminada hasta que esté conectada al flujo de la aplicación y tenga pruebas o evidencia de funcionamiento.
5. Todos los integrantes deben comprender el flujo completo para la sustentación.

## enlaces de integración

- [PR #1 — Primera parte estructuras propias](https://github.com/ju4nj0s3109P/proyectofinal-EDT-2026-2/pull/1)
- [PR #2 — Segunda parte estructuras propias](https://github.com/ju4nj0s3109P/proyectofinal-EDT-2026-2/pull/2)
- [PR #3 — Integración de dominio con estructuras y prioridad de preparación](https://github.com/ju4nj0s3109P/proyectofinal-EDT-2026-2/pull/3)

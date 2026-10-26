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
- El árbol se organiza por código de apartamento y fecha de entrada. La comprobación de solapamiento recorre recursivamente las reservas relevantes y usa intervalos semiabiertos `[entrada, salida)`.
- Pendiente, Confirmada y En curso son estados activos que bloquean noches. Cancelada, No-show y Finalizada no bloquean.
- Cada reserva multia-partamento se agrega o se retira como una sola operación; nunca debe quedar indexada parcialmente.

## Contratos de canal y cancelación
- Portal: se registra Pendiente y se confirma con anticipo acumulado mínimo del 30 %.
- Directa: se confirma al registro, después de validar conflictos.
- Externa: se procesa FIFO; conflicto significa rechazo y registro del conflicto.
- Cancelación previa al check-in: permite Pendiente o Confirmada, muestra saldo a favor antes de ejecutarse y devuelve el 100 % del saldo positivo a favor mientras no se apruebe otra política.

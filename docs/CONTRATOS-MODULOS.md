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

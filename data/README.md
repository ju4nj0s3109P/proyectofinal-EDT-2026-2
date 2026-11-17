# Datos de prueba

Los archivos utilizan UTF-8, encabezados y punto y coma como separador. Los valores de lista dentro de una celda se separan con `|`.

## Escenario del alojamiento

- `apartamentos.csv`: cinco apartamentos, tarifas y estado operativo.
- `huespedes.csv`: diez huéspedes ficticios.
- `reservas.csv`: veinte reservas directas confirmadas. Los apartamentos se separan con `|`; cada ocupante usa `nombre:yyyy-MM-dd`. La llegada usa HH:mm y la creación ISO-8601.
- `ubicaciones.csv` y `pasillos.csv`: mapa del edificio. Los pasillos son bidireccionales y sus distancias deben ser positivas.
- `preparaciones.csv`: tareas iniciales de preparación por apartamento.

## Personal y perfiles

- `empleados.csv`: documento, nombre, teléfono, correo, rol y estado activo (`true`/`false`). Roles permitidos: ADMINISTRADOR, RECEPCIONISTA y PREPARACION.
- `turnos-recepcion.csv`: documentos de recepcionistas en el orden de la rotación circular. Solo se admiten empleados activos con rol RECEPCIONISTA.
- `perfiles-huespedes.csv`: documento del huésped y códigos de apartamentos favoritos separados por `|`. El historial se alimenta cuando una estancia se finaliza desde la aplicación.

## Folios

- `movimientos-folios.csv`: folio, tipo, monto, descripción y fecha/hora.
- La carga inicial admite CARGO y PAGO; los movimientos de reverso y devolución los genera el servicio para conservar las reglas del folio.
- Cada folio se crea al registrar su reserva y después se cargan sus movimientos iniciales.

Todos los datos personales de estos archivos son ficticios y se usan únicamente para demostración. Los cambios se leen al reiniciar la aplicación.

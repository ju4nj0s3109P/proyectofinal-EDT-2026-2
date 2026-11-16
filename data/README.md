# Datos de prueba

## Mapa del edificio

`mapa-edificio.txt` está codificado en UTF-8 y usa `|` para separar los campos.
Las líneas vacías y las que empiezan con `#` se ignoran.

- `NODO|id|tipo|nombre` declara una ubicación con un identificador único.
- `PASILLO|origen|destino|metros` conecta dos ubicaciones en ambos sentidos.

Los nodos deben declararse antes de los pasillos que los usan.

## Escenario CSV

Los archivos utilizan UTF-8, encabezados y punto y coma como separador.

- `apartamentos.csv`: datos, tarifas y estados de los apartamentos.
- `huespedes.csv`: perfiles personales de los huéspedes.
- `reservas.csv`: reservas de demostración y sus referencias a huéspedes y apartamentos.
- `ubicaciones.csv`: identificador, tipo y nombre de cada nodo.
- `pasillos.csv`: origen, destino y distancia en metros.
- `preparaciones.csv`: tareas iniciales de preparación.

En `reservas.csv`, los apartamentos se separan con `|`; cada ocupante usa
`nombre:yyyy-MM-dd`. La llegada se expresa como hora local HH:mm y puede
dejarse vacía para usar las 12:00. La fecha y hora de creación usa ISO-8601.

Los códigos de apartamentos deben coincidir con los nodos de tipo
APARTAMENTO del mapa. Debe existir exactamente una recepción.

Los pasillos son bidireccionales, tienen distancias positivas y no
pueden repetirse, aunque se escriban en sentido contrario.

Los cambios en los archivos se leen al reiniciar la aplicación.
El botón «Actualizar estados» consulta los datos en memoria.

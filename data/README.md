# Datos de prueba

## Mapa del edificio

`mapa-edificio.txt` está codificado en UTF-8 y usa `|` para separar los campos.
Las líneas vacías y las que empiezan con `#` se ignoran.

- `NODO|id|tipo|nombre` declara una ubicacin con un identificador único.
- `PASILLO|origen|destino|metros` conecta dos ubicaciones en ambos sentidos.

Los nodos deben declararse antes de los pasillos que los usan.

## Escenario CSV

Los archivos utilizan UTF-8, encabezados y punto y coma como separador.

- `apartamentos.csv`: datos, tarifas y estados de los apartamentos.
- `ubicaciones.csv`: identificador, tipo y nombre de cada nodo.
- `pasillos.csv`: origen, destino y distancia en metros.

Los códigos de apartamentos deben coincidir con los nodos de tipo
APARTAMENTO del mapa. Debe existir exactamente una recepción.

Los pasillos son bidireccionales, tienen distancias positivas y no
pueden repetirse, aunque se escriban en sentido contrario.

Los cambios en los archivos se leen al reiniciar la aplicación.
El botón «Actualizar estados» consulta los datos en memoria.

`mapa-edificio.txt` se conserva temporalmente para las pruebas del
cargador anterior; la aplicación utiliza los archivos CSV.
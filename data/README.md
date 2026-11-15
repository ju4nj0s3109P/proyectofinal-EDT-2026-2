# Datos de prueba

## Mapa del edificio

`mapa-edificio.txt` está codificado en UTF-8 y usa `|` para separar los campos.
Las líneas vacías y las que empiezan con `#` se ignoran.

- `NODO|id|tipo|nombre` declara una ubicacin con un identificador único.
- `PASILLO|origen|destino|metros` conecta dos ubicaciones en ambos sentidos.

Los nodos deben declararse antes de los pasillos que los usan.
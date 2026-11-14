# Estructuras propias

Estructuras genéricas, sin dependencias del dominio ni colecciones de `java.util`. Todas implementan `Iterable<T>` con iterador propio y lanzan `EstructuraVaciaException` al operar vacías.

| Clase | Paquete | Operaciones clave y complejidad |
|---|---|---|
| `ListaEnlazada<T>` | `lineales` | extremos O(1), por índice O(n), `ordenar(Comparator)` merge sort recursivo O(n log n) |
| `Pila<T>` | `lineales` | apilar, desapilar, verTope O(1) |
| `Cola<T>` | `lineales` | encolar, desencolar, verFrente O(1); contiene O(n) |
| `ColaPrioridad<T>` | `lineales` | montículo binario: insertar y extraer O(log n), verPrimero O(1), eliminar O(n) |
| `ListaCircular<T>` | `lineales` | actual, avanzar, retroceder, agregar O(1); eliminar O(n) |
| `Conjunto<T>` | `lineales` | agregar, contiene, eliminar O(n) (por `equals`) |
| `ArbolBinarioBusqueda<T extends Comparable<T>>` | `arboles` | insertar, buscar, eliminar O(h) recursivos; altura y recorridos O(n); rango O(h + k); anterior y siguiente O(h) |
| `DiccionarioArbol<K,V>` | `arboles` | poner, obtener, quitar O(h) |
| `Grafo<T>` / `Ruta<T>` | `grafos` | BFS y DFS recursivo O(V + E); Dijkstra O((V + E) log V); cierre de pasillos, inalcanzables, componentes |

Las pruebas están en `src/test/java/co/edu/uq/sga/estructuras`.

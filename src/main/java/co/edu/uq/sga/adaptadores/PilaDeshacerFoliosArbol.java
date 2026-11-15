package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.PilaDeshacerFolios;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import co.edu.uq.sga.estructuras.lineales.Pila;
import java.util.Optional;

/** Una pila propia por folio, guardadas en el diccionario por código de folio. */
public class PilaDeshacerFoliosArbol implements PilaDeshacerFolios {

    private final DiccionarioArbol<String, Pila<MovimientoFolio>> pilas = new DiccionarioArbol<>();

    @Override
    public void apilar(String codigoFolio, MovimientoFolio operacion) {
        if (operacion == null) {
            throw new IllegalArgumentException("La operación es obligatoria.");
        }
        pilas.obtenerOCrear(codigoFolio, Pila::new).apilar(operacion);
    }

    @Override
    public Optional<MovimientoFolio> desapilar(String codigoFolio) {
        return pilas.obtener(codigoFolio)
                .filter(pila -> !pila.estaVacia())
                .map(Pila::desapilar);
    }
}

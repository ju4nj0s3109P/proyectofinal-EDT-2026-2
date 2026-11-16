package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import java.util.Optional;

/** Puerto de las operaciones que se pueden deshacer en cada folio, implementado con la pila propia. */
public interface PilaDeshacerFolios {

    void apilar(String codigoFolio, MovimientoFolio operacion);

    /** Retira la última operación apilada del folio; vacío si no queda ninguna. */
    Optional<MovimientoFolio> desapilar(String codigoFolio);
}

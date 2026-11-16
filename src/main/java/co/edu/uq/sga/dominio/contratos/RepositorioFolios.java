package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.Folio;
import java.util.Optional;

/** Puerto para guardar y buscar folios por código, implementado con una estructura propia. */
public interface RepositorioFolios {

    /**
     * Guarda un folio nuevo.
     *
     * @return true si lo guardó; false si ya existe un folio con el mismo código
     */
    boolean agregarSiNoExiste(Folio folio);

    /** Busca un folio por su código exacto. */
    Optional<Folio> buscar(String codigo);
}

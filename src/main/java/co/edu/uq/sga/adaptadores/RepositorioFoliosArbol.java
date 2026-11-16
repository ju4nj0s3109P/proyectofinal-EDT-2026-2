package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.RepositorioFolios;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import java.util.Optional;

/** Guarda los folios en el diccionario propio, ordenados por código. */
public class RepositorioFoliosArbol implements RepositorioFolios {

    private final DiccionarioArbol<String, Folio> folios = new DiccionarioArbol<>();

    @Override
    public boolean agregarSiNoExiste(Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("El folio es obligatorio.");
        }
        if (folios.contieneClave(folio.getCodigo())) {
            return false;
        }
        folios.poner(folio.getCodigo(), folio);
        return true;
    }

    @Override
    public Optional<Folio> buscar(String codigo) {
        return folios.obtener(codigo);
    }
}

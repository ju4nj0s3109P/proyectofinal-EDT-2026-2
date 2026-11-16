package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.modelo.Huesped;
import java.util.Objects;
import java.util.Optional;

/** Casos de uso administrativos para el catálogo de huéspedes por documento. */
public final class ServicioHuespedes {
    private final CatalogoHuespedesArbol catalogo;

    public ServicioHuespedes(CatalogoHuespedesArbol catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo de huéspedes es obligatorio.");
    }

    public void registrar(Huesped huesped) {
        catalogo.agregar(Objects.requireNonNull(huesped, "El huésped es obligatorio."));
    }

    public Optional<Huesped> buscarPorDocumento(String documento) {
        return catalogo.buscarPorDocumento(documento);
    }

    public Huesped[] consultarTodos() {
        return catalogo.listarPorDocumento();
    }

    public int cantidad() {
        return catalogo.cantidad();
    }
}

package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;

import java.util.Optional;

public final class CatalogoHuespedesArbol {

    private final DiccionarioArbol<String, Huesped> huespedes =
            new DiccionarioArbol<>();

    public void agregar(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException(
                    "El huésped es obligatorio."
            );
        }

        String documento = huesped.getDocumento();

        if (huespedes.contieneClave(documento)) {
            throw new IllegalArgumentException(
                    "Ya existe un huésped con documento " + documento
            );
        }

        huespedes.poner(documento, huesped);
    }

    public Optional<Huesped> buscarPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException(
                    "El documento es obligatorio."
            );
        }

        return huespedes.obtener(documento.trim());
    }

    public Huesped[] listarPorDocumento() {
        return huespedes.valores().aArreglo(Huesped[]::new);
    }

    public int cantidad() {
        return huespedes.tamanio();
    }
}
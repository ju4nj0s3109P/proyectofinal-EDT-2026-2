package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;

import java.util.Optional;

public final class CatalogoApartamentosArbol {

    private final DiccionarioArbol<String, Apartamento> apartamentos =
            new DiccionarioArbol<>();

    public void agregar(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException(
                    "El apartamento es obligatorio."
            );
        }

        String codigo = apartamento.getCodigo();

        if (apartamentos.contieneClave(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un apartamento con código " + codigo
            );
        }

        apartamentos.poner(codigo, apartamento);
    }

    public Optional<Apartamento> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del apartamento es obligatorio."
            );
        }

        return apartamentos.obtener(codigo.trim());
    }

    public Apartamento[] listarPorCodigo() {
        return apartamentos.valores().aArreglo(Apartamento[]::new);
    }

    public int cantidad() {
        return apartamentos.tamanio();
    }
}
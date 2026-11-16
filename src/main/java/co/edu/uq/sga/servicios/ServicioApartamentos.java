package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;

import java.util.Objects;
import java.util.Optional;

public final class ServicioApartamentos {

    public record ResumenApartamento(
            String codigo,
            String nombre,
            String bloque,
            int piso,
            int dormitorios,
            int capacidad,
            EstadoApartamento estado,
            String motivoFueraDeServicio
    ) {}

    private final CatalogoApartamentosArbol catalogo;

    public ServicioApartamentos(CatalogoApartamentosArbol catalogo) {
        this.catalogo = Objects.requireNonNull(
                catalogo,
                "El catálogo de apartamentos es obligatorio."
        );
    }

    public ResumenApartamento[] consultarTodos() {
        Apartamento[] apartamentos = catalogo.listarPorCodigo();

        ResumenApartamento[] resumenes =
                new ResumenApartamento[apartamentos.length];

        for (int i = 0; i < apartamentos.length; i++) {
            resumenes[i] = resumir(apartamentos[i]);
        }

        return resumenes;
    }

    public Optional<ResumenApartamento> consultarPorCodigo(String codigo) {
        return catalogo.buscarPorCodigo(codigo).map(this::resumir);
    }

    private ResumenApartamento resumir(Apartamento apartamento) {
        return new ResumenApartamento(
                apartamento.getCodigo(),
                apartamento.getNombre(),
                apartamento.getBloque(),
                apartamento.getPiso(),
                apartamento.getNumeroDormitorios(),
                apartamento.getCapacidadMaxima(),
                apartamento.getEstadoOperativo(),
                apartamento.getMotivoFueraDeServicio()
        );
    }
}
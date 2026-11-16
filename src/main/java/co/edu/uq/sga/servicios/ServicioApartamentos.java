package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import java.util.Objects;
import java.util.Optional;

/** Casos de uso administrativos y de consulta del catálogo de apartamentos. */
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
                catalogo, "El catálogo de apartamentos es obligatorio.");
    }

    /** Registra un apartamento en el árbol indexado por código. */
    public void registrar(Apartamento apartamento) {
        catalogo.agregar(Objects.requireNonNull(apartamento, "El apartamento es obligatorio."));
    }

    public void marcarFueraDeServicio(String codigo, String motivo) {
        obtener(codigo).marcarFueraDeServicio(motivo);
    }

    /** Devuelve a operación un apartamento fuera de servicio, dejándolo preparado. */
    public void restablecerPreparado(String codigo) {
        Apartamento apartamento = obtener(codigo);
        if (apartamento.getEstadoOperativo() != EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("El apartamento no está fuera de servicio.");
        }
        apartamento.cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
    }

    public ResumenApartamento[] consultarTodos() {
        Apartamento[] apartamentos = catalogo.listarPorCodigo();
        ResumenApartamento[] resumenes = new ResumenApartamento[apartamentos.length];
        for (int i = 0; i < apartamentos.length; i++) {
            resumenes[i] = resumir(apartamentos[i]);
        }
        return resumenes;
    }

    public Optional<ResumenApartamento> consultarPorCodigo(String codigo) {
        return catalogo.buscarPorCodigo(codigo).map(this::resumir);
    }

    private Apartamento obtener(String codigo) {
        return catalogo.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el apartamento " + codigo + "."));
    }

    private ResumenApartamento resumir(Apartamento apartamento) {
        return new ResumenApartamento(
                apartamento.getCodigo(), apartamento.getNombre(), apartamento.getBloque(),
                apartamento.getPiso(), apartamento.getNumeroDormitorios(),
                apartamento.getCapacidadMaxima(), apartamento.getEstadoOperativo(),
                apartamento.getMotivoFueraDeServicio());
    }
}

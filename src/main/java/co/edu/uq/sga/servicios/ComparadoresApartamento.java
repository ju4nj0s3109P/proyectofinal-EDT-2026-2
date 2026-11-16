package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.ReservasPorApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import java.util.Comparator;

/**
 * Órdenes alternativos de los apartamentos para la búsqueda de disponibilidad y
 * los reportes. El orden natural de {@link Apartamento} es por código, así que
 * todos terminan con él y dos apartamentos empatados no quedan al azar.
 */
public final class ComparadoresApartamento {

    private ComparadoresApartamento() {}

    /** Más barato primero, por la tarifa de temporada baja, que es la tarifa base. */
    public static Comparator<Apartamento> porPrecio() {
        Comparator<Apartamento> porTarifa = Comparator.comparing(Apartamento::getTarifaBaja);
        return porTarifa.thenComparing(Comparator.naturalOrder());
    }

    /** Capacidad más pequeña primero: ofrece el apartamento más justo para el grupo. */
    public static Comparator<Apartamento> porCapacidad() {
        Comparator<Apartamento> porCupo =
                Comparator.comparingInt(Apartamento::getCapacidadMaxima);
        return porCupo.thenComparing(Comparator.naturalOrder());
    }

    /** Menos dormitorios primero. */
    public static Comparator<Apartamento> porDormitorios() {
        Comparator<Apartamento> porCuartos =
                Comparator.comparingInt(Apartamento::getNumeroDormitorios);
        return porCuartos.thenComparing(Comparator.naturalOrder());
    }

    /** Más reservado primero, para el reporte de ocupación del administrador. */
    public static Comparator<Apartamento> porNumeroDeReservas(ReservasPorApartamento reservas) {
        if (reservas == null) {
            throw new IllegalArgumentException("La consulta de reservas es obligatoria.");
        }
        Comparator<Apartamento> porCantidad = Comparator.comparingInt(
                apartamento -> reservas.de(apartamento.getCodigo()).length);
        return porCantidad.reversed().thenComparing(Comparator.naturalOrder());
    }
}

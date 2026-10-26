package co.edu.uq.sga.dominio.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Rango de una estancia con fecha de entrada incluida y fecha de salida
 * excluida.
 */
public class Estancia {
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;

    public Estancia(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada == null) {
            throw new IllegalArgumentException("La fecha de entrada es obligatoria.");
        }
        if (fechaSalida == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de entrada.");
        }

        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    /** Devuelve el número de noches facturables de la estancia. */
    public long cantidadNoches() {
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    /** Indica si la noche de la fecha consultada pertenece a la estancia. */
    public boolean incluyeNoche(LocalDate noche) {
        if (noche == null) {
            throw new IllegalArgumentException("La noche consultada es obligatoria.");
        }
        return !noche.isBefore(fechaEntrada) && noche.isBefore(fechaSalida);
    }

    /**
     * Dos estancias se solapan si cada una empieza antes de que termine la otra.
     * La fecha de salida es exclusiva.
     */
    public boolean seSolapaCon(Estancia otra) {
        if (otra == null) {
            throw new IllegalArgumentException("La estancia a comparar es obligatoria.");
        }
        return fechaEntrada.isBefore(otra.fechaSalida)
                && otra.fechaEntrada.isBefore(fechaSalida);
    }
}

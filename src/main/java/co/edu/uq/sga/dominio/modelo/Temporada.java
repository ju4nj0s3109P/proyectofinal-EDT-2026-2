package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import java.time.LocalDate;

/**
 * Periodo de vigencia de una temporada. Las fechas siguen el intervalo
 * semiabierto [fechaInicio, fechaFin).
 */
public class Temporada {
    private final TipoTemporada tipo;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    public Temporada(TipoTemporada tipo, LocalDate fechaInicio, LocalDate fechaFin) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de temporada es obligatorio.");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }
        if (fechaFin == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria.");
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public TipoTemporada getTipo() {
        return tipo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /** Indica si la fecha corresponde a una noche de esta temporada. */
    public boolean incluyeNoche(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha consultada es obligatoria.");
        }
        return !fecha.isBefore(fechaInicio) && fecha.isBefore(fechaFin);
    }
}

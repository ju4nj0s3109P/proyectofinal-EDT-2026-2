package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.excepciones.FechasInvalidasException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Rango de estancia con entrada incluida y salida excluida. */
public class Estancia implements Iterable<LocalDate> {
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;

    public Estancia(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada == null) throw new FechasInvalidasException("La fecha de entrada es obligatoria.");
        if (fechaSalida == null) throw new FechasInvalidasException("La fecha de salida es obligatoria.");
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new FechasInvalidasException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public long cantidadNoches() { return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida); }

    public boolean incluyeNoche(LocalDate noche) {
        if (noche == null) throw new IllegalArgumentException("La noche consultada es obligatoria.");
        return !noche.isBefore(fechaEntrada) && noche.isBefore(fechaSalida);
    }

    public boolean seSolapaCon(Estancia otra) {
        if (otra == null) throw new IllegalArgumentException("La estancia a comparar es obligatoria.");
        return fechaEntrada.isBefore(otra.fechaSalida) && otra.fechaEntrada.isBefore(fechaSalida);
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return new Iterator<>() {
            private LocalDate siguiente = fechaEntrada;
            @Override public boolean hasNext() { return siguiente.isBefore(fechaSalida); }
            @Override public LocalDate next() {
                if (!hasNext()) throw new NoSuchElementException("No quedan noches por recorrer.");
                LocalDate actual = siguiente;
                siguiente = siguiente.plusDays(1);
                return actual;
            }
        };
    }
}

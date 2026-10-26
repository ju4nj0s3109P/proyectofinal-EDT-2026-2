package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Temporada;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Cotiza una estancia noche por noche para un apartamento y sus ocupantes. */
public class ServicioTarifas {

    /**
     * Produce el desglose por noche. La edad se evalúa en la fecha de cada noche:
     * un ocupante comienza a pagar desde la noche en que ya tiene seis años.
     */
    public Cotizacion cotizar(
            Apartamento apartamento,
            Estancia estancia,
            Ocupante[] ocupantes,
            Temporada[] temporadas) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento es obligatorio.");
        }
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia es obligatoria.");
        }
        if (ocupantes == null || ocupantes.length == 0) {
            throw new IllegalArgumentException("Se requiere al menos un ocupante.");
        }
        if (temporadas == null || temporadas.length == 0) {
            throw new IllegalArgumentException("Se requiere al menos una temporada.");
        }
        for (Ocupante ocupante : ocupantes) {
            if (ocupante == null) {
                throw new IllegalArgumentException("Los ocupantes no pueden ser nulos.");
            }
        }
        for (Temporada temporada : temporadas) {
            if (temporada == null) {
                throw new IllegalArgumentException("Las temporadas no pueden ser nulas.");
            }
        }

        DetalleNoche[] desglose = new DetalleNoche[(int) estancia.cantidadNoches()];
        BigDecimal total = BigDecimal.ZERO;
        Iterator<LocalDate> iteradorNoches =
                new IteradorNoches(estancia.getFechaEntrada(), estancia.getFechaSalida());
        int indice = 0;

        while (iteradorNoches.hasNext()) {
            LocalDate noche = iteradorNoches.next();
            Temporada temporada = buscarTemporada(noche, temporadas);
            BigDecimal tarifa = obtenerTarifa(apartamento, temporada.getTipo());
            int pagantes = contarOcupantesPagantes(ocupantes, noche);
            BigDecimal subtotal = tarifa.multiply(BigDecimal.valueOf(pagantes));

            desglose[indice++] =
                    new DetalleNoche(noche, temporada.getTipo(), pagantes, tarifa, subtotal);
            total = total.add(subtotal);
        }

        return new Cotizacion(total, desglose);
    }

    private Temporada buscarTemporada(LocalDate noche, Temporada[] temporadas) {
        Temporada encontrada = null;
        for (Temporada temporada : temporadas) {
            if (temporada.incluyeNoche(noche)) {
                if (encontrada != null) {
                    throw new IllegalStateException(
                            "Hay más de una temporada definida para la noche " + noche + ".");
                }
                encontrada = temporada;
            }
        }
        if (encontrada == null) {
            throw new IllegalStateException(
                    "No hay una temporada definida para la noche " + noche + ".");
        }
        return encontrada;
    }

    private BigDecimal obtenerTarifa(Apartamento apartamento, TipoTemporada tipo) {
        return switch (tipo) {
            case BAJA -> apartamento.getTarifaBaja();
            case MEDIA -> apartamento.getTarifaMedia();
            case ALTA -> apartamento.getTarifaAlta();
        };
    }

    private int contarOcupantesPagantes(Ocupante[] ocupantes, LocalDate noche) {
        int pagantes = 0;
        for (Ocupante ocupante : ocupantes) {
            if (!ocupante.esMenorDeSeisEn(noche)) {
                pagantes++;
            }
        }
        return pagantes;
    }

    /** Desglose inmutable de una noche cotizada. */
    public record DetalleNoche(
            LocalDate fecha,
            TipoTemporada temporada,
            int ocupantesPagantes,
            BigDecimal tarifaPorPersona,
            BigDecimal subtotal) {}

    /** Resultado de una cotización con copia defensiva de su desglose. */
    public static final class Cotizacion {
        private final BigDecimal total;
        private final DetalleNoche[] desglose;

        private Cotizacion(BigDecimal total, DetalleNoche[] desglose) {
            this.total = total;
            this.desglose = desglose.clone();
        }

        public BigDecimal getTotal() {
            return total;
        }

        public DetalleNoche[] getDesglose() {
            return desglose.clone();
        }
    }

    /** Iterador propio de fechas que recorre [inicio, fin). */
    private static final class IteradorNoches implements Iterator<LocalDate> {
        private LocalDate siguiente;
        private final LocalDate fechaFin;

        private IteradorNoches(LocalDate fechaInicio, LocalDate fechaFin) {
            this.siguiente = fechaInicio;
            this.fechaFin = fechaFin;
        }

        @Override
        public boolean hasNext() {
            return siguiente.isBefore(fechaFin);
        }

        @Override
        public LocalDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No quedan noches por recorrer.");
            }
            LocalDate actual = siguiente;
            siguiente = siguiente.plusDays(1);
            return actual;
        }
    }
}

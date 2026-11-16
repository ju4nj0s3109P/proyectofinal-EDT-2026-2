package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Temporada;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Guarda las temporadas del alojamiento y cotiza una estancia noche por noche
 * para un apartamento y sus ocupantes.
 */
public class ServicioTarifas {

    private final ListaEnlazada<Temporada> temporadas = new ListaEnlazada<>();

    /** Define las fechas de una temporada. No puede solaparse con otra ya definida. */
    public void definirTemporada(Temporada temporada) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada es obligatoria.");
        }
        if (temporadas.existe(definida -> seSolapan(definida, temporada))) {
            throw new ExcepcionDominio(
                    "La temporada se solapa con una temporada ya definida.");
        }
        temporadas.agregarAlFinal(temporada);
    }

    /** Copia de las temporadas definidas, en el orden en que se definieron. */
    public ListaEnlazada<Temporada> getTemporadas() {
        return temporadas.copiar();
    }

    /** La temporada que cubre esa noche; lanza excepción de dominio si ninguna la cubre. */
    public Temporada temporadaDe(LocalDate noche) {
        return buscarTemporada(noche);
    }

    /**
     * Produce el desglose por noche recorriendo la estancia. La edad se evalúa en la
     * fecha de cada noche: un ocupante comienza a pagar desde la noche en que ya
     * tiene seis años.
     */
    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia, Ocupante[] ocupantes) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento es obligatorio.");
        }
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia es obligatoria.");
        }
        if (ocupantes == null || ocupantes.length == 0) {
            throw new IllegalArgumentException("Se requiere al menos un ocupante.");
        }
        for (Ocupante ocupante : ocupantes) {
            if (ocupante == null) {
                throw new IllegalArgumentException("Los ocupantes no pueden ser nulos.");
            }
        }
        if (temporadas.estaVacia()) {
            throw new ExcepcionDominio("No hay temporadas definidas.");
        }

        ListaEnlazada<DetalleNoche> desglose = new ListaEnlazada<>();
        BigDecimal total = BigDecimal.ZERO;

        for (LocalDate noche : estancia) {
            Temporada temporada = buscarTemporada(noche);
            BigDecimal tarifa = obtenerTarifa(apartamento, temporada.getTipo());
            int pagantes = contarOcupantesPagantes(ocupantes, noche);
            BigDecimal subtotal = tarifa.multiply(BigDecimal.valueOf(pagantes));

            desglose.agregarAlFinal(
                    new DetalleNoche(noche, temporada.getTipo(), pagantes, tarifa, subtotal));
            total = total.add(subtotal);
        }

        return new Cotizacion(total, desglose);
    }

    private static boolean seSolapan(Temporada una, Temporada otra) {
        return una.getFechaInicio().isBefore(otra.getFechaFin())
                && otra.getFechaInicio().isBefore(una.getFechaFin());
    }

    private Temporada buscarTemporada(LocalDate noche) {
        if (noche == null) {
            throw new IllegalArgumentException("La noche consultada es obligatoria.");
        }
        return temporadas.buscar(temporada -> temporada.incluyeNoche(noche))
                .orElseThrow(() -> new ExcepcionDominio(
                        "No hay una temporada definida para la noche " + noche + "."));
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

    /** Resultado de una cotización; el desglose va en la lista doblemente enlazada propia. */
    public static final class Cotizacion {
        private final BigDecimal total;
        private final ListaEnlazada<DetalleNoche> desglose;

        private Cotizacion(BigDecimal total, ListaEnlazada<DetalleNoche> desglose) {
            this.total = total;
            this.desglose = desglose;
        }

        public BigDecimal getTotal() {
            return total;
        }

        /** Copia del desglose noche por noche, recorrible con for-each. */
        public ListaEnlazada<DetalleNoche> getDesglose() {
            return desglose.copiar();
        }

        public int cantidadNoches() {
            return desglose.tamanio();
        }
    }
}

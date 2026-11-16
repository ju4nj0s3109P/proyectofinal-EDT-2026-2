package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.ReservasPorApartamento;
import co.edu.uq.sga.dominio.modelo.Reserva;

/** Expone las reservas activas que el índice del árbol ya guarda por apartamento. */
public class ReservasDelIndice implements ReservasPorApartamento {

    private final IndiceReservasArbol indice;

    public ReservasDelIndice(IndiceReservasArbol indice) {
        if (indice == null) {
            throw new IllegalArgumentException("El índice de reservas es obligatorio.");
        }
        this.indice = indice;
    }

    @Override
    public Reserva[] de(String codigoApartamento) {
        if (codigoApartamento == null || codigoApartamento.isBlank()) {
            throw new IllegalArgumentException("El código del apartamento es obligatorio.");
        }
        return indice.reservasDe(codigoApartamento.trim()).aArreglo(Reserva[]::new);
    }
}

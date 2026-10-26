package co.edu.uq.sga.dominio.modelo;

/** Vincula una reserva con uno de los apartamentos que ocupará. */
public class AsignacionApartamento {
    private final Apartamento apartamento;

    public AsignacionApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento asignado es obligatorio.");
        }
        this.apartamento = apartamento;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }
}

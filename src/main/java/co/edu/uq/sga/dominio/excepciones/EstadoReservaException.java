package co.edu.uq.sga.dominio.excepciones;

/** Transición de estado de reserva no permitida. */
public class EstadoReservaException extends ExcepcionDominio {
    private static final long serialVersionUID = 1L;

    public EstadoReservaException(String mensaje) {
        super(mensaje);
    }
}

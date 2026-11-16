package co.edu.uq.sga.dominio.excepciones;

/** Error de dominio cuando la capacidad asignada no alcanza para el grupo. */
public class CapacidadExcedidaException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public CapacidadExcedidaException(String mensaje) { super(mensaje); }
}

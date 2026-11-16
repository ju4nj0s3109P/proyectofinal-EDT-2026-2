package co.edu.uq.sga.dominio.excepciones;

/** Error de dominio cuando el rango de fechas de una estancia no es válido. */
public class FechasInvalidasException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public FechasInvalidasException(String mensaje) { super(mensaje); }
}

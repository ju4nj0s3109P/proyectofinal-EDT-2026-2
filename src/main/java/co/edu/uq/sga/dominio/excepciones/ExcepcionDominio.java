package co.edu.uq.sga.dominio.excepciones;

/** Error de una regla de negocio incumplida en el dominio. */
public class ExcepcionDominio extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public ExcepcionDominio(String mensaje) {
        super(mensaje);
    }
}

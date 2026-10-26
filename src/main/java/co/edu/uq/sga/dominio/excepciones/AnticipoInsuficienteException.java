package co.edu.uq.sga.dominio.excepciones;

/** Anticipo por debajo del mínimo requerido para confirmar. */
public class AnticipoInsuficienteException extends ExcepcionDominio {
    private static final long serialVersionUID = 1L;

    public AnticipoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}

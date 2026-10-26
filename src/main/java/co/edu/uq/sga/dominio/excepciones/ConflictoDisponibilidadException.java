package co.edu.uq.sga.dominio.excepciones;

/** Conflicto al intentar ocupar un intervalo reservado. */
public class ConflictoDisponibilidadException extends ExcepcionDominio {
    private static final long serialVersionUID = 1L;

    public ConflictoDisponibilidadException(String mensaje) {
        super(mensaje);
    }
}

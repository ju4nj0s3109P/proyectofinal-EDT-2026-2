package co.edu.uq.sga.estructuras.excepciones;

import java.util.NoSuchElementException;

public class EstructuraVaciaException extends NoSuchElementException {
    private static final long serialVersionUID = 1L;

    public EstructuraVaciaException(String mensaje) {
        super(mensaje);
    }
}

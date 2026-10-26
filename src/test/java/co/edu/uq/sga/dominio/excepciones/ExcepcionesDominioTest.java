package co.edu.uq.sga.dominio.excepciones;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ExcepcionesDominioTest {

    @Test
    void excepcionesDeReglaConservanCompatibilidadConIllegalStateException() {
        ExcepcionDominio base = new ExcepcionDominio("regla");
        assertInstanceOf(IllegalStateException.class, base);
        assertEquals("regla", base.getMessage());

        assertInstanceOf(ExcepcionDominio.class,
                new ConflictoDisponibilidadException("conflicto"));
        assertInstanceOf(ExcepcionDominio.class,
                new AnticipoInsuficienteException("anticipo"));
        assertInstanceOf(ExcepcionDominio.class,
                new EstadoReservaException("estado"));
    }

    @Test
    void construyeExcepcionConMensajeLegible() {
        ConflictoDisponibilidadException excepcion = assertThrows(
                ConflictoDisponibilidadException.class,
                () -> { throw new ConflictoDisponibilidadException("sin disponibilidad"); });
        assertEquals("sin disponibilidad", excepcion.getMessage());
    }
}

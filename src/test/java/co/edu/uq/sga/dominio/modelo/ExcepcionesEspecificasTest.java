package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.excepciones.CapacidadExcedidaException;
import co.edu.uq.sga.dominio.excepciones.FechasInvalidasException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExcepcionesEspecificasTest {
    @Test
    void estanciaLanzaExcepcionPropiaParaFechasInvalidas() {
        assertThrows(FechasInvalidasException.class,
                () -> new Estancia(LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 10)));
    }

    @Test
    void reservaLanzaExcepcionPropiaCuandoSeExcedeCapacidad() {
        Apartamento apartamento = new Apartamento("A1", "Suite", "A", 1, 1, 1,
                new String[0], BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        Huesped titular = new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null);
        Ocupante[] ocupantes = {
                new Ocupante("Ana", LocalDate.of(1990, 1, 1)),
                new Ocupante("Luis", LocalDate.of(1991, 1, 1))};
        assertThrows(CapacidadExcedidaException.class, () -> new Reserva("F1", titular,
                ocupantes, new Estancia(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12)),
                CanalReserva.DIRECTA,
                new AsignacionApartamento[]{new AsignacionApartamento(apartamento)}));
    }
}

package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ServicioPerfilesHuespedTest {
    @Test
    void favoritosNoRepiteApartamentosPorCodigo() {
        ServicioPerfilesHuesped perfiles = new ServicioPerfilesHuesped();
        Huesped huesped = huesped();
        Apartamento primero = apartamento("A1");
        Apartamento mismaIdentidad = apartamento("A1");

        assertTrue(perfiles.agregarFavorito(huesped, primero));
        assertFalse(perfiles.agregarFavorito(huesped, mismaIdentidad));
        assertEquals(1, perfiles.consultarFavoritos(huesped).tamanio());
        assertTrue(perfiles.quitarFavorito(huesped, "A1"));
    }

    @Test
    void historialSoloAceptaReservasFinalizadasYNoDuplicaFolios() {
        ServicioPerfilesHuesped perfiles = new ServicioPerfilesHuesped();
        Huesped huesped = huesped();
        Reserva reserva = reserva(huesped);
        assertThrows(IllegalStateException.class,
                () -> perfiles.registrarEstanciaFinalizada(reserva));
        reserva.confirmar();
        reserva.iniciar();
        reserva.finalizar();

        assertTrue(perfiles.registrarEstanciaFinalizada(reserva));
        assertFalse(perfiles.registrarEstanciaFinalizada(reserva));
        assertEquals(1, perfiles.consultarHistorial(huesped).tamanio());
    }

    private static Huesped huesped() {
        return new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null);
    }

    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, codigo, "A", 1, 1, 2, new String[0],
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }

    private static Reserva reserva(Huesped huesped) {
        Apartamento apartamento = apartamento("A1");
        return new Reserva("F-1", huesped,
                new Ocupante[]{new Ocupante("Ana", LocalDate.of(1990, 1, 1))},
                new Estancia(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)),
                CanalReserva.DIRECTA,
                new AsignacionApartamento[]{new AsignacionApartamento(apartamento)});
    }
}

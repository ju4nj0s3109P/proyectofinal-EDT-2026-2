package co.edu.uq.sga.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Huesped;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CargadorReservasTest {
    @TempDir Path temporal;
    @Test
    void cargaReservasYResuelveReferenciasDeCatalogos() throws Exception {
        CatalogoApartamentosArbol apartamentos = new CatalogoApartamentosArbol();
        apartamentos.agregar(new Apartamento("A1", "Suite", "A", 1, 2, 4,
                new String[0], BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN));
        CatalogoHuespedesArbol huespedes = new CatalogoHuespedesArbol();
        huespedes.agregar(new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null));
        Path archivo = temporal.resolve("reservas.csv");
        Files.writeString(archivo, "folio;documentoTitular;apartamentos;ocupantes;fechaEntrada;fechaSalida;canal;estado;fechaHoraCreacion;horaLlegada\n"
                + "F1;123;A1;Ana:1990-01-01;2026-12-10;2026-12-12;DIRECTA;CONFIRMADA;2026-11-15T10:00:00;15:00\n");
        var reservas = new CargadorReservas().cargar(archivo, apartamentos, huespedes);
        assertEquals(1, reservas.length);
        assertEquals("F1", reservas[0].reserva().getFolio());
        assertEquals(EstadoReserva.CONFIRMADA, reservas[0].reserva().getEstado());
        assertEquals(1, reservas[0].reserva().getAsignaciones().length);
    }
}

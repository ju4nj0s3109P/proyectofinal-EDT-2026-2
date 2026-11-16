package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Huesped;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ServicioAdministracionCatalogosTest {
    @Test
    void administraApartamentosMedianteCatalogoArbol() {
        ServicioApartamentos servicio = new ServicioApartamentos(new CatalogoApartamentosArbol());
        Apartamento apartamento = new Apartamento("A1", "Suite", "A", 1, 2, 4,
                new String[]{"balcon"}, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        servicio.registrar(apartamento);
        servicio.marcarFueraDeServicio("A1", "Mantenimiento");
        assertEquals(EstadoApartamento.FUERA_DE_SERVICIO, servicio.consultarPorCodigo("A1").orElseThrow().estado());
        assertEquals("Mantenimiento", servicio.consultarPorCodigo("A1").orElseThrow().motivoFueraDeServicio());
        servicio.restablecerPreparado("A1");
        assertEquals(EstadoApartamento.PREPARADO, servicio.consultarPorCodigo("A1").orElseThrow().estado());
    }

    @Test
    void administraHuespedesMedianteCatalogoArbol() {
        ServicioHuespedes servicio = new ServicioHuespedes(new CatalogoHuespedesArbol());
        Huesped huesped = new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null);
        servicio.registrar(huesped);
        assertSame(huesped, servicio.buscarPorDocumento("123").orElseThrow());
        assertEquals(1, servicio.cantidad());
    }
}

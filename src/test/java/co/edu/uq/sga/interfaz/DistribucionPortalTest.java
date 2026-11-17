package co.edu.uq.sga.interfaz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.servicios.ServicioPanelReservas.ApartamentoDisponible;
import co.edu.uq.sga.servicios.ServicioPanelReservas.DatosOcupante;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DistribucionPortalTest {

    private static final LocalDate NACIMIENTO = LocalDate.of(1995, 5, 10);

    @Test
    void distribuyeOcupantesEnVariosApartamentosSinSuperarCapacidades() {
        DatosOcupante[] ocupantes = {
            new DatosOcupante("Ana", NACIMIENTO),
            new DatosOcupante("Luis", NACIMIENTO),
            new DatosOcupante("Eva", NACIMIENTO),
            new DatosOcupante("Noah", NACIMIENTO),
            new DatosOcupante("Mia", NACIMIENTO)
        };
        ApartamentoDisponible[] apartamentos = {
            apartamento("A101", 2),
            apartamento("A103", 3)
        };

        DatosOcupante[] resultado =
                PanelPortalHuespedes.distribuirOcupantesPortal(ocupantes, apartamentos);

        assertEquals(5, resultado.length);
        assertEquals("A101", resultado[0].apartamento());
        assertEquals("A101", resultado[1].apartamento());
        assertEquals("A103", resultado[2].apartamento());
        assertEquals("A103", resultado[3].apartamento());
        assertEquals("A103", resultado[4].apartamento());
    }

    @Test
    void permiteUsarUnSoloApartamentoCuandoTieneCapacidadSuficiente() {
        DatosOcupante[] ocupantes = {
            new DatosOcupante("Ana", NACIMIENTO),
            new DatosOcupante("Luis", NACIMIENTO)
        };

        DatosOcupante[] resultado = PanelPortalHuespedes.distribuirOcupantesPortal(
                ocupantes, new ApartamentoDisponible[] {apartamento("A102", 2)});

        assertEquals("A102", resultado[0].apartamento());
        assertEquals("A102", resultado[1].apartamento());
    }

    @Test
    void rechazaGrupoSinOcupantesOSinApartamentos() {
        assertThrows(IllegalArgumentException.class,
                () -> PanelPortalHuespedes.distribuirOcupantesPortal(
                        new DatosOcupante[0], new ApartamentoDisponible[] {apartamento("A101", 2)}));
        assertThrows(IllegalArgumentException.class,
                () -> PanelPortalHuespedes.distribuirOcupantesPortal(
                        new DatosOcupante[] {new DatosOcupante("Ana", NACIMIENTO)},
                        new ApartamentoDisponible[0]));
    }

    @Test
    void rechazaCapacidadInsuficienteYDatosIncompletos() {
        DatosOcupante[] tres = {
            new DatosOcupante("Ana", NACIMIENTO),
            new DatosOcupante("Luis", NACIMIENTO),
            new DatosOcupante("Eva", NACIMIENTO)
        };
        assertThrows(IllegalArgumentException.class,
                () -> PanelPortalHuespedes.distribuirOcupantesPortal(
                        tres, new ApartamentoDisponible[] {apartamento("A101", 2)}));
        assertThrows(IllegalArgumentException.class,
                () -> PanelPortalHuespedes.distribuirOcupantesPortal(
                        new DatosOcupante[] {new DatosOcupante(" ", NACIMIENTO)},
                        new ApartamentoDisponible[] {apartamento("A101", 2)}));
        assertThrows(IllegalArgumentException.class,
                () -> PanelPortalHuespedes.distribuirOcupantesPortal(
                        new DatosOcupante[] {new DatosOcupante("Ana", null)},
                        new ApartamentoDisponible[] {apartamento("A101", 2)}));
    }

    private static ApartamentoDisponible apartamento(String codigo, int capacidad) {
        return new ApartamentoDisponible(codigo, "Suite " + codigo, capacidad, 1,
                new BigDecimal("100"));
    }
}

package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.excepciones.CapacidadExcedidaException;
import org.junit.jupiter.api.Test;
import co.edu.uq.sga.dominio.enums.CanalReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionOcupantesTest {

    @Test
    void conservaLaAsignacionAnteriorSinInventarOcupantes() {
        var asignacion = new AsignacionApartamento(apartamento());

        assertFalse(asignacion.tieneDistribucion());
        assertThrows(
                IllegalStateException.class,
                asignacion::getOcupantes
        );
    }

    @Test
    void guardaOcupantesSinExponerElArregloInterno() {
        Ocupante adulto = persona("Adulto", 1990);
        Ocupante menor = persona("Menor", 2023);
        Ocupante[] entrada = {adulto, menor};

        var asignacion = new AsignacionApartamento(
                apartamento(), entrada
        );

        entrada[0] = null;
        Ocupante[] consulta = asignacion.getOcupantes();
        consulta[1] = null;

        assertTrue(asignacion.tieneDistribucion());
        assertSame(adulto, asignacion.getOcupantes()[0]);
        assertSame(menor, asignacion.getOcupantes()[1]);
    }

    @Test
    void losMenoresTambienOcupanCupo() {
        Ocupante[] personas = {
                persona("Adulto", 1990),
                persona("Menor uno", 2023),
                persona("Menor dos", 2024)
        };

        assertThrows(
                CapacidadExcedidaException.class,
                () -> new AsignacionApartamento(apartamento(), personas)
        );
    }

    @Test
    void rechazaElMismoOcupanteDosVeces() {
        Ocupante persona = persona("Ana", 1990);

        assertThrows(
                IllegalArgumentException.class,
                () -> new AsignacionApartamento(
                        apartamento(),
                        new Ocupante[]{persona, persona}
                )
        );
    }

    @Test
    void rechazaDistribucionesVaciasONulas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AsignacionApartamento(
                        apartamento(), new Ocupante[0]
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new AsignacionApartamento(
                        apartamento(), new Ocupante[]{null}
                )
        );
    }

    private Ocupante persona(String nombre, int anio) {
        return new Ocupante(nombre, LocalDate.of(anio, 1, 1));
    }

    private Apartamento apartamento() {
        return new Apartamento(
                "A102", "Apartamento 102", "A",
                1, 1, 2,
                new String[0],
                new BigDecimal("55000"),
                new BigDecimal("75000"),
                new BigDecimal("95000")
        );
    }

    @Test
    void permiteUnaDistribucionCompleta() {
        Ocupante ana = persona("Ana", 1990);
        Ocupante luis = persona("Luis", 1995);

        assertDoesNotThrow(() -> reservar(
                new Ocupante[]{ana, luis},
                new AsignacionApartamento(
                        apartamento(), new Ocupante[]{ana, luis}
                )
        ));
    }

    @Test
    void rechazaUnOcupanteSinApartamento() {
        Ocupante ana = persona("Ana", 1990);
        Ocupante luis = persona("Luis", 1995);

        assertThrows(IllegalArgumentException.class, () -> reservar(
                new Ocupante[]{ana, luis},
                new AsignacionApartamento(
                        apartamento(), new Ocupante[]{ana}
                )
        ));
    }

    @Test
    void rechazaUnOcupanteAjenoALaReserva() {
        Ocupante ana = persona("Ana", 1990);
        Ocupante otraPersona = persona("Otra persona", 1995);

        assertThrows(IllegalArgumentException.class, () -> reservar(
                new Ocupante[]{ana},
                new AsignacionApartamento(
                        apartamento(), new Ocupante[]{otraPersona}
                )
        ));
    }

    @Test
    void rechazaAsignarLaMismaPersonaADosApartamentos() {
        Ocupante ana = persona("Ana", 1990);

        Apartamento otro = new Apartamento(
                "A103", "Apartamento 103", "A",
                1, 1, 2,
                new String[0],
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE
        );

        assertThrows(IllegalArgumentException.class, () -> reservar(
                new Ocupante[]{ana},
                new AsignacionApartamento(
                        apartamento(), new Ocupante[]{ana}
                ),
                new AsignacionApartamento(
                        otro, new Ocupante[]{ana}
                )
        ));
    }

    private Reserva reservar(
            Ocupante[] ocupantes,
            AsignacionApartamento... asignaciones
    ) {
        Huesped titular = new Huesped(
                "Titular de prueba",
                "DOC-PRUEBA",
                LocalDate.of(1990, 1, 1),
                "3001234567",
                "prueba@example.com",
                null
        );

        return new Reserva(
                "R-PRUEBA",
                titular,
                ocupantes,
                new Estancia(
                        LocalDate.of(2026, 11, 19),
                        LocalDate.of(2026, 11, 21)
                ),
                CanalReserva.DIRECTA,
                asignaciones
        );
    }
}
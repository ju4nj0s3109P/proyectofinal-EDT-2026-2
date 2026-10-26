package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Temporada;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ServicioTarifasTest {

    private final ServicioTarifas servicio = new ServicioTarifas();

    @Test
    void cotizaCadaNocheConSuTemporadaYExcluyeMenoresDeSeis() {
        Apartamento apartamento = crearApartamento();
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 13));
        Ocupante[] ocupantes = {
            new Ocupante("Adulto 1", LocalDate.of(1990, 1, 1)),
            new Ocupante("Adulto 2", LocalDate.of(1992, 1, 1)),
            new Ocupante("Niño", LocalDate.of(2021, 1, 1))
        };
        Temporada[] temporadas = {
            temporada(TipoTemporada.BAJA, "2026-03-10", "2026-03-11"),
            temporada(TipoTemporada.MEDIA, "2026-03-11", "2026-03-12"),
            temporada(TipoTemporada.ALTA, "2026-03-12", "2026-03-20")
        };

        ServicioTarifas.Cotizacion cotizacion =
                servicio.cotizar(apartamento, estancia, ocupantes, temporadas);

        assertEquals(new BigDecimal("1200"), cotizacion.getTotal());
        assertEquals(3, cotizacion.getDesglose().length);
        assertEquals(TipoTemporada.BAJA, cotizacion.getDesglose()[0].temporada());
        assertEquals(new BigDecimal("200"), cotizacion.getDesglose()[0].subtotal());
        assertEquals(TipoTemporada.MEDIA, cotizacion.getDesglose()[1].temporada());
        assertEquals(new BigDecimal("400"), cotizacion.getDesglose()[1].subtotal());
        assertEquals(TipoTemporada.ALTA, cotizacion.getDesglose()[2].temporada());
        assertEquals(new BigDecimal("600"), cotizacion.getDesglose()[2].subtotal());
    }

    @Test
    void empiezaACobrarAlOcupanteDesdeLaNocheEnQueCumpleSeis() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12));
        Ocupante[] ocupantes = {
            new Ocupante("Niño", LocalDate.of(2020, 3, 11))
        };
        Temporada[] temporadas = {
            temporada(TipoTemporada.BAJA, "2026-03-10", "2026-03-12")
        };

        ServicioTarifas.Cotizacion cotizacion = servicio.cotizar(
                crearApartamento(), estancia, ocupantes, temporadas);

        assertEquals(new BigDecimal("100"), cotizacion.getTotal());
        assertEquals(0, cotizacion.getDesglose()[0].ocupantesPagantes());
        assertEquals(1, cotizacion.getDesglose()[1].ocupantesPagantes());
    }

    @Test
    void rechazaNocheSinTemporadaOConTemporadasSolapadas() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 11));
        Ocupante[] ocupantes = {new Ocupante("Adulto", LocalDate.of(1990, 1, 1))};

        assertThrows(
                IllegalStateException.class,
                () -> servicio.cotizar(
                        crearApartamento(),
                        estancia,
                        ocupantes,
                        new Temporada[] {
                            temporada(TipoTemporada.BAJA, "2026-03-11", "2026-03-12")
                        }));
        assertThrows(
                IllegalStateException.class,
                () -> servicio.cotizar(
                        crearApartamento(),
                        estancia,
                        ocupantes,
                        new Temporada[] {
                            temporada(TipoTemporada.BAJA, "2026-03-10", "2026-03-12"),
                            temporada(TipoTemporada.ALTA, "2026-03-10", "2026-03-11")
                        }));
    }

    @Test
    void validaArgumentosYProtegeDesglose() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 11));
        Ocupante[] ocupantes = {new Ocupante("Adulto", LocalDate.of(1990, 1, 1))};
        Temporada[] temporadas = {
            temporada(TipoTemporada.BAJA, "2026-03-10", "2026-03-11")
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cotizar(null, estancia, ocupantes, temporadas));
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cotizar(crearApartamento(), estancia, new Ocupante[0], temporadas));
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cotizar(
                        crearApartamento(), estancia, new Ocupante[] {null}, temporadas));
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cotizar(
                        crearApartamento(), estancia, ocupantes, new Temporada[0]));

        ServicioTarifas.Cotizacion cotizacion =
                servicio.cotizar(crearApartamento(), estancia, ocupantes, temporadas);
        ServicioTarifas.DetalleNoche[] copia = cotizacion.getDesglose();
        copia[0] = null;
        assertEquals(
                LocalDate.of(2026, 3, 10),
                cotizacion.getDesglose()[0].fecha());
    }

    private Apartamento crearApartamento() {
        return new Apartamento(
                "A-1",
                "Suite",
                "A",
                1,
                1,
                4,
                new String[0],
                new BigDecimal("100"),
                new BigDecimal("200"),
                new BigDecimal("300"));
    }

    private Temporada temporada(TipoTemporada tipo, String inicio, String fin) {
        return new Temporada(tipo, LocalDate.parse(inicio), LocalDate.parse(fin));
    }
}

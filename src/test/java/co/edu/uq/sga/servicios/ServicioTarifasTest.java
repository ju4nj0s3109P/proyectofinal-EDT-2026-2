package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Temporada;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
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
        definir(TipoTemporada.BAJA, "2026-03-10", "2026-03-11");
        definir(TipoTemporada.MEDIA, "2026-03-11", "2026-03-12");
        definir(TipoTemporada.ALTA, "2026-03-12", "2026-03-20");

        ServicioTarifas.Cotizacion cotizacion =
                servicio.cotizar(apartamento, estancia, ocupantes);

        ListaEnlazada<ServicioTarifas.DetalleNoche> desglose = cotizacion.getDesglose();
        assertEquals(new BigDecimal("1200"), cotizacion.getTotal());
        assertEquals(3, desglose.tamanio());
        assertEquals(TipoTemporada.BAJA, desglose.obtener(0).temporada());
        assertEquals(new BigDecimal("200"), desglose.obtener(0).subtotal());
        assertEquals(TipoTemporada.MEDIA, desglose.obtener(1).temporada());
        assertEquals(new BigDecimal("400"), desglose.obtener(1).subtotal());
        assertEquals(TipoTemporada.ALTA, desglose.obtener(2).temporada());
        assertEquals(new BigDecimal("600"), desglose.obtener(2).subtotal());
    }

    @Test
    void empiezaACobrarAlOcupanteDesdeLaNocheEnQueCumpleSeis() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12));
        Ocupante[] ocupantes = {new Ocupante("Niño", LocalDate.of(2020, 3, 11))};
        definir(TipoTemporada.BAJA, "2026-03-10", "2026-03-12");

        ServicioTarifas.Cotizacion cotizacion =
                servicio.cotizar(crearApartamento(), estancia, ocupantes);

        assertEquals(new BigDecimal("100"), cotizacion.getTotal());
        assertEquals(0, cotizacion.getDesglose().obtener(0).ocupantesPagantes());
        assertEquals(1, cotizacion.getDesglose().obtener(1).ocupantesPagantes());
    }

    @Test
    void guardaLasTemporadasDefinidasYRechazaLasQueSeSolapan() {
        definir(TipoTemporada.BAJA, "2026-03-10", "2026-03-12");

        assertThrows(IllegalStateException.class,
                () -> definir(TipoTemporada.ALTA, "2026-03-11", "2026-03-15"),
                "una noche no puede quedar en dos temporadas");
        definir(TipoTemporada.ALTA, "2026-03-12", "2026-03-15");

        assertEquals(2, servicio.getTemporadas().tamanio());
        assertEquals(TipoTemporada.ALTA,
                servicio.temporadaDe(LocalDate.of(2026, 3, 12)).getTipo());
        assertThrows(IllegalArgumentException.class, () -> servicio.definirTemporada(null));
    }

    @Test
    void rechazaCotizarSinTemporadasOConUnaNocheSinTemporada() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 11));
        Ocupante[] ocupantes = {new Ocupante("Adulto", LocalDate.of(1990, 1, 1))};

        assertThrows(IllegalStateException.class,
                () -> servicio.cotizar(crearApartamento(), estancia, ocupantes),
                "sin temporadas definidas no se puede cotizar");

        definir(TipoTemporada.BAJA, "2026-03-11", "2026-03-12");
        assertThrows(IllegalStateException.class,
                () -> servicio.cotizar(crearApartamento(), estancia, ocupantes));
        assertThrows(IllegalStateException.class,
                () -> servicio.temporadaDe(LocalDate.of(2026, 3, 10)));
    }

    @Test
    void validaArgumentosYProtegeDesglose() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 11));
        Ocupante[] ocupantes = {new Ocupante("Adulto", LocalDate.of(1990, 1, 1))};
        definir(TipoTemporada.BAJA, "2026-03-10", "2026-03-11");

        assertThrows(IllegalArgumentException.class,
                () -> servicio.cotizar(null, estancia, ocupantes));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.cotizar(crearApartamento(), null, ocupantes));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.cotizar(crearApartamento(), estancia, new Ocupante[0]));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.cotizar(crearApartamento(), estancia, new Ocupante[] {null}));

        ServicioTarifas.Cotizacion cotizacion =
                servicio.cotizar(crearApartamento(), estancia, ocupantes);
        cotizacion.getDesglose().eliminarPrimero();
        assertEquals(1, cotizacion.cantidadNoches(), "el desglose devuelto es una copia");
        assertEquals(LocalDate.of(2026, 3, 10), cotizacion.getDesglose().obtener(0).fecha());
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

    private void definir(TipoTemporada tipo, String inicio, String fin) {
        servicio.definirTemporada(
                new Temporada(tipo, LocalDate.parse(inicio), LocalDate.parse(fin)));
    }
}

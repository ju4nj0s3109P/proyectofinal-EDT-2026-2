package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.dominio.enums.*;
import co.edu.uq.sga.dominio.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioReportesTest {
    @Test
    void calculaOcupacionPorApartamentoYTemporada() {
        Apartamento apartamento = apartamento();
        Reserva reserva = reserva(apartamento, EstadoReserva.FINALIZADA, CanalReserva.DIRECTA);
        ServicioReportes reportes = new ServicioReportes();

        var ocupacion = reportes.ocupacionPorApartamento(new Apartamento[]{apartamento},
                new Reserva[]{reserva}, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 4))[0];
        assertEquals(2, ocupacion.nochesOcupadas());
        assertEquals(3, ocupacion.nochesDisponibles());

        Temporada temporada = new Temporada(TipoTemporada.ALTA,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 4));
        var porTemporada = reportes.ocupacionPorTemporada(new Apartamento[]{apartamento},
                new Reserva[]{reserva}, new Temporada[]{temporada})[0];
        assertEquals(2, porTemporada.nochesOcupadas());
        assertEquals(3, porTemporada.nochesDisponibles());
    }

    @Test
    void cuentaReservasCanceladasYNoShowPorCanal() {
        Apartamento apartamento = apartamento();
        Reserva cancelada = reserva(apartamento, EstadoReserva.CANCELADA, CanalReserva.PORTAL);
        Reserva noShow = reserva(apartamento, EstadoReserva.NO_SHOW, CanalReserva.PORTAL);
        var resumen = new ServicioReportes().reservasPorCanal(
                new Reserva[]{cancelada, noShow})[0];
        assertEquals(CanalReserva.PORTAL, resumen.canal());
        assertEquals(2, resumen.total());
        assertEquals(1, resumen.canceladas());
        assertEquals(1, resumen.noShow());
    }

    @Test
    void calculaIngresoNetoYPromedioDePreparacion() {
        Apartamento apartamento = apartamento();
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(new MovimientoFolio("M1", TipoMovimiento.PAGO,
                new BigDecimal("100"), "Pago", LocalDateTime.of(2026, 1, 1, 10, 0), null));
        folio.registrarMovimiento(new MovimientoFolio("M2", TipoMovimiento.DEVOLUCION,
                new BigDecimal("20"), "Devolución", LocalDateTime.of(2026, 1, 2, 10, 0), null));
        var ingreso = new ServicioReportes().ingresosPorPeriodo(new Folio[]{folio},
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 3, 0, 0));
        assertEquals(new BigDecimal("80"), ingreso.ingresosRecibidosNetos());

        TareaPreparacion tarea = new TareaPreparacion("T1", apartamento,
                LocalDateTime.of(2026, 1, 1, 8, 0), null);
        tarea.iniciar(LocalDateTime.of(2026, 1, 1, 8, 15));
        tarea.completar(LocalDateTime.of(2026, 1, 1, 9, 0));
        var promedio = new ServicioReportes().tiempoPromedioPreparacion(new TareaPreparacion[]{tarea});
        assertEquals(45.0, promedio.minutosPromedio());
        assertEquals(1, promedio.tareasIncluidas());
    }

    private static Apartamento apartamento() {
        return new Apartamento("A1", "Suite", "A", 1, 1, 2, new String[0],
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }

    private static Reserva reserva(Apartamento apartamento, EstadoReserva estado, CanalReserva canal) {
        Huesped titular = new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null);
        Reserva reserva = new Reserva("F-" + estado + canal, titular,
                new Ocupante[]{new Ocupante("Ana", LocalDate.of(1990, 1, 1))},
                new Estancia(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3)),
                canal, new AsignacionApartamento[]{new AsignacionApartamento(apartamento)});
        if (estado == EstadoReserva.CANCELADA) reserva.cancelar();
        if (estado == EstadoReserva.NO_SHOW) {
            reserva.confirmar();
            reserva.marcarNoShow();
        }
        if (estado == EstadoReserva.FINALIZADA) {
            reserva.confirmar();
            reserva.iniciar();
            reserva.finalizar();
        }
        return reserva;
    }
}

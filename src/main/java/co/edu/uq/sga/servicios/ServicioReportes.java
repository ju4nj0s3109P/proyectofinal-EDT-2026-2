package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.ConflictoReserva;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import co.edu.uq.sga.dominio.modelo.Temporada;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Genera indicadores administrativos recorriendo los modelos y estructuras recibidos. */
public final class ServicioReportes {

    public record OcupacionApartamento(
            String codigoApartamento, long nochesOcupadas, long nochesDisponibles,
            double porcentajeOcupacion) {}

    public record OcupacionTemporada(
            TipoTemporada tipo, LocalDate inicio, LocalDate fin,
            long nochesOcupadas, long nochesDisponibles, double porcentajeOcupacion) {}

    public record IngresosPeriodo(
            BigDecimal cargosNetos, BigDecimal ingresosRecibidosNetos) {}

    public record ReservasPorCanal(
            CanalReserva canal, int total, int pendientes, int confirmadas,
            int enCurso, int finalizadas, int canceladas, int noShow, int rechazadas) {}

    public record ResumenConflictos(
            int total, int porDisponibilidad, int porFueraDeServicio,
            int noProcesables, int otros) {}

    public record PromedioPreparacion(double minutosPromedio, int tareasIncluidas) {}

    public OcupacionApartamento[] ocupacionPorApartamento(
            Apartamento[] apartamentos, Reserva[] reservas, LocalDate inicio, LocalDate fin) {
        validarPeriodo(inicio, fin);
        Objects.requireNonNull(apartamentos, "El catálogo de apartamentos es obligatorio.");
        Objects.requireNonNull(reservas, "Las reservas son obligatorias.");
        long nochesPeriodo = ChronoUnit.DAYS.between(inicio, fin);
        OcupacionApartamento[] resultado = new OcupacionApartamento[apartamentos.length];
        for (int i = 0; i < apartamentos.length; i++) {
            Apartamento apartamento = Objects.requireNonNull(apartamentos[i]);
            long ocupadas = 0;
            for (Reserva reserva : reservas) {
                if (reserva == null || !cuentaComoOcupacion(reserva)) continue;
                for (var asignacion : reserva.getAsignaciones()) {
                    if (asignacion.getApartamento().getCodigo().equals(apartamento.getCodigo())) {
                        ocupadas += nochesSolapadas(reserva.getEstancia(), inicio, fin);
                        break;
                    }
                }
            }
            resultado[i] = new OcupacionApartamento(apartamento.getCodigo(), ocupadas,
                    nochesPeriodo, porcentaje(ocupadas, nochesPeriodo));
        }
        return resultado;
    }

    public OcupacionTemporada[] ocupacionPorTemporada(
            Apartamento[] apartamentos, Reserva[] reservas, Temporada[] temporadas) {
        Objects.requireNonNull(apartamentos, "El catálogo de apartamentos es obligatorio.");
        Objects.requireNonNull(reservas, "Las reservas son obligatorias.");
        Objects.requireNonNull(temporadas, "Las temporadas son obligatorias.");
        OcupacionTemporada[] resultado = new OcupacionTemporada[temporadas.length];
        for (int i = 0; i < temporadas.length; i++) {
            Temporada temporada = Objects.requireNonNull(temporadas[i]);
            long noches = ChronoUnit.DAYS.between(temporada.getFechaInicio(), temporada.getFechaFin());
            long ocupadas = 0;
            for (Reserva reserva : reservas) {
                if (reserva == null || !cuentaComoOcupacion(reserva)) continue;
                ocupadas += nochesSolapadas(reserva.getEstancia(),
                        temporada.getFechaInicio(), temporada.getFechaFin())
                        * asignacionesEnCatalogo(reserva, apartamentos);
            }
            long disponibles = noches * apartamentos.length;
            resultado[i] = new OcupacionTemporada(temporada.getTipo(),
                    temporada.getFechaInicio(), temporada.getFechaFin(), ocupadas,
                    disponibles, porcentaje(ocupadas, disponibles));
        }
        return resultado;
    }

    /** Los cargos netos y los pagos netos se reportan por separado para no confundir venta con recaudo. */
    public IngresosPeriodo ingresosPorPeriodo(
            Folio[] folios, LocalDateTime desde, LocalDateTime hasta) {
        validarPeriodo(desde, hasta);
        Objects.requireNonNull(folios, "Los folios son obligatorios.");
        BigDecimal cargos = BigDecimal.ZERO;
        BigDecimal recibidos = BigDecimal.ZERO;
        for (Folio folio : folios) {
            if (folio == null) continue;
            for (MovimientoFolio movimiento : folio.getMovimientos()) {
                LocalDateTime fecha = movimiento.getFechaHora();
                if (fecha.isBefore(desde) || !fecha.isBefore(hasta)) continue;
                BigDecimal monto = movimiento.getMonto();
                switch (movimiento.getTipo()) {
                    case CARGO -> cargos = cargos.add(monto);
                    case REVERSO_CARGO -> cargos = cargos.subtract(monto);
                    case PAGO -> recibidos = recibidos.add(monto);
                    case REVERSO_PAGO, DEVOLUCION -> recibidos = recibidos.subtract(monto);
                }
            }
        }
        return new IngresosPeriodo(cargos, recibidos);
    }

    /** Cuenta todos los estados, incluidos canceladas y no-show, por canal de origen. */
    public ReservasPorCanal[] reservasPorCanal(Reserva[] reservas) {
        Objects.requireNonNull(reservas, "Las reservas son obligatorias.");
        CanalReserva[] canales = CanalReserva.values();
        ReservasPorCanal[] resultado = new ReservasPorCanal[canales.length];
        for (int i = 0; i < canales.length; i++) {
            int total = 0, pendientes = 0, confirmadas = 0, enCurso = 0;
            int finalizadas = 0, canceladas = 0, noShow = 0, rechazadas = 0;
            for (Reserva reserva : reservas) {
                if (reserva == null || reserva.getCanal() != canales[i]) continue;
                total++;
                switch (reserva.getEstado()) {
                    case PENDIENTE -> pendientes++;
                    case CONFIRMADA -> confirmadas++;
                    case EN_CURSO -> enCurso++;
                    case FINALIZADA -> finalizadas++;
                    case CANCELADA -> canceladas++;
                    case NO_SHOW -> noShow++;
                    case RECHAZADA -> rechazadas++;
                }
            }
            resultado[i] = new ReservasPorCanal(canales[i], total, pendientes,
                    confirmadas, enCurso, finalizadas, canceladas, noShow, rechazadas);
        }
        return resultado;
    }

    public ResumenConflictos resumirConflictos(ConflictoReserva[] conflictos) {
        Objects.requireNonNull(conflictos, "Los conflictos son obligatorios.");
        int disponibilidad = 0, fueraServicio = 0, noProcesables = 0, otros = 0, total = 0;
        for (ConflictoReserva conflicto : conflictos) {
            if (conflicto == null) continue;
            total++;
            String motivo = conflicto.getMotivo().toLowerCase();
            if (motivo.contains("disponibilidad") || motivo.contains("solapamiento")) {
                disponibilidad++;
            } else if (motivo.contains("fuera de servicio")) {
                fueraServicio++;
            } else if (motivo.contains("no procesable")) {
                noProcesables++;
            } else {
                otros++;
            }
        }
        return new ResumenConflictos(total, disponibilidad, fueraServicio, noProcesables, otros);
    }

    /** Promedia solo tareas completadas que tengan marcas de inicio y finalización válidas. */
    public PromedioPreparacion tiempoPromedioPreparacion(TareaPreparacion[] tareas) {
        Objects.requireNonNull(tareas, "Las tareas son obligatorias.");
        long segundos = 0;
        int cantidad = 0;
        for (TareaPreparacion tarea : tareas) {
            if (tarea == null || tarea.getFechaHoraInicio() == null
                    || tarea.getFechaHoraFinalizacion() == null) continue;
            long duracion = Duration.between(
                    tarea.getFechaHoraInicio(), tarea.getFechaHoraFinalizacion()).getSeconds();
            if (duracion < 0) continue;
            segundos += duracion;
            cantidad++;
        }
        return new PromedioPreparacion(cantidad == 0 ? 0.0 : segundos / 60.0 / cantidad, cantidad);
    }

    private static boolean cuentaComoOcupacion(Reserva reserva) {
        return reserva.getEstado() != EstadoReserva.CANCELADA
                && reserva.getEstado() != EstadoReserva.RECHAZADA
                && reserva.getEstado() != EstadoReserva.NO_SHOW;
    }

    private static long nochesSolapadas(Estancia estancia, LocalDate inicio, LocalDate fin) {
        LocalDate desde = estancia.getFechaEntrada().isAfter(inicio)
                ? estancia.getFechaEntrada() : inicio;
        LocalDate hasta = estancia.getFechaSalida().isBefore(fin)
                ? estancia.getFechaSalida() : fin;
        return hasta.isAfter(desde) ? ChronoUnit.DAYS.between(desde, hasta) : 0;
    }

    private static int asignacionesEnCatalogo(Reserva reserva, Apartamento[] apartamentos) {
        int cantidad = 0;
        for (var asignacion : reserva.getAsignaciones()) {
            for (Apartamento apartamento : apartamentos) {
                if (apartamento != null && apartamento.getCodigo()
                        .equals(asignacion.getApartamento().getCodigo())) {
                    cantidad++;
                    break;
                }
            }
        }
        return cantidad;
    }

    private static double porcentaje(long ocupadas, long disponibles) {
        return disponibles == 0 ? 0.0 : ocupadas * 100.0 / disponibles;
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new IllegalArgumentException("El periodo debe tener fechas válidas y fin posterior al inicio.");
        }
    }

    private static void validarPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new IllegalArgumentException("El periodo debe tener fechas válidas y fin posterior al inicio.");
        }
    }
}

package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioReportes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Reportes operativos y financieros calculados desde los modelos actuales. */
public final class PanelEstadisticas extends BorderPane {
    private final ServicioPanelReservas servicio;
    private final ServicioReportes reportes = new ServicioReportes();
    private final DatePicker desde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker hasta = new DatePicker(LocalDate.now().plusDays(1));
    private final TextArea contenido = new TextArea();

    public PanelEstadisticas(ServicioPanelReservas servicio) {
        this.servicio = servicio;
        desde.setEditable(false);
        hasta.setEditable(false);
        Button actualizar = new Button("Generar reportes");
        actualizar.getStyleClass().add("accion-principal");
        actualizar.setOnAction(e -> actualizar());
        HBox filtros = new HBox(10, new Label("Desde"), desde, new Label("Hasta (exclusivo)"), hasta, actualizar);
        setTop(new VBox(10, new Label("Indicadores y reportes del alojamiento"), filtros));
        contenido.setEditable(false);
        contenido.setWrapText(false);
        contenido.setPrefRowCount(28);
        setCenter(contenido);
        actualizar();
    }

    public void actualizar() {
        if (desde.getValue() == null || hasta.getValue() == null || !hasta.getValue().isAfter(desde.getValue())) {
            contenido.setText("Selecciona un periodo válido. La fecha final es exclusiva.");
            return;
        }
        StringBuilder salida = new StringBuilder();
        var apartamentos = servicio.consultarApartamentosDominio();
        var reservas = servicio.consultarReservasDominio();
        var temporada = servicio.consultarTemporadas();
        var inicio = desde.getValue();
        var fin = hasta.getValue();

        salida.append("OCUPACIÓN POR APARTAMENTO\n");
        for (var r : reportes.ocupacionPorApartamento(apartamentos, reservas, inicio, fin)) {
            salida.append(String.format("%s: %d/%d noches · %.1f %%\n",
                    r.codigoApartamento(), r.nochesOcupadas(), r.nochesDisponibles(), r.porcentajeOcupacion()));
        }

        salida.append("\nOCUPACIÓN POR TEMPORADA\n");
        for (var r : reportes.ocupacionPorTemporada(apartamentos, reservas, temporada)) {
            salida.append(String.format("%s (%s a %s): %d/%d noches-apartamento · %.1f %%\n",
                    r.tipo(), r.inicio(), r.fin(), r.nochesOcupadas(), r.nochesDisponibles(), r.porcentajeOcupacion()));
        }

        salida.append("\nINGRESOS Y CARGOS DEL PERIODO\n");
        var ingresos = reportes.ingresosPorPeriodo(servicio.consultarFolios(),
                inicio.atStartOfDay(), fin.atStartOfDay());
        salida.append("Cargos netos: $").append(ingresos.cargosNetos()).append("\n");
        salida.append("Pagos recibidos netos: $").append(ingresos.ingresosRecibidosNetos()).append("\n");

        salida.append("\nRESERVAS POR CANAL\n");
        for (var r : reportes.reservasPorCanal(reservas)) {
            salida.append(String.format("%s: %d total | pendientes %d | confirmadas %d | en curso %d | finalizadas %d | canceladas %d | no-show %d | rechazadas %d\n",
                    r.canal(), r.total(), r.pendientes(), r.confirmadas(), r.enCurso(),
                    r.finalizadas(), r.canceladas(), r.noShow(), r.rechazadas()));
        }

        salida.append("\nCONFLICTOS DE SOLICITUDES EXTERNAS\n");
        var conflictos = servicio.consultarConflictosExternos();
        var resumen = reportes.resumirConflictos(conflictos);
        salida.append("Total: ").append(resumen.total())
                .append(" | disponibilidad: ").append(resumen.porDisponibilidad())
                .append(" | fuera de servicio: ").append(resumen.porFueraDeServicio())
                .append(" | no procesables: ").append(resumen.noProcesables())
                .append(" | otros: ").append(resumen.otros()).append("\n");
        for (var conflicto : conflictos) {
            salida.append(conflicto.getFechaHora()).append(" · ").append(conflicto.getFolioReserva())
                    .append(" · ").append(String.join(", ", conflicto.getCodigosApartamentos()))
                    .append(" · ").append(conflicto.getMotivo()).append("\n");
        }

        salida.append("\nTIEMPO PROMEDIO DE PREPARACIÓN\n");
        var promedio = reportes.tiempoPromedioPreparacion(servicio.consultarPreparacionesCompletadas());
        salida.append(String.format("Promedio: %.1f minutos · tareas incluidas: %d\n",
                promedio.minutosPromedio(), promedio.tareasIncluidas()));

        salida.append("\nNota: ocupación usa reservas activas y finalizadas; canceladas, rechazadas y no-show no ocupan noches. ");
        salida.append("El periodo de ingresos usa [desde, hasta).");
        contenido.setText(salida.toString());
    }
}

package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Calendario mensual de ocupación construido desde el catálogo de reservas. */
public final class PanelCalendario extends BorderPane {
    private final ServicioPanelReservas servicio;
    private final GridPane calendario = new GridPane();
    private final Label periodo = new Label();
    private YearMonth mes = YearMonth.now();

    public PanelCalendario(ServicioPanelReservas servicio) {
        this.servicio = servicio;
        Button anterior = new Button("Mes anterior");
        Button siguiente = new Button("Mes siguiente");
        Button hoy = new Button("Mes actual");
        anterior.setOnAction(e -> { mes = mes.minusMonths(1); actualizar(); });
        siguiente.setOnAction(e -> { mes = mes.plusMonths(1); actualizar(); });
        hoy.setOnAction(e -> { mes = YearMonth.now(); actualizar(); });
        HBox controles = new HBox(10, anterior, hoy, siguiente, periodo);
        controles.setAlignment(Pos.CENTER_LEFT);
        controles.getStyleClass().add("acciones-calendario");
        calendario.setHgap(8);
        calendario.setVgap(8);
        setTop(controles);
        setCenter(calendario);
        actualizar();
    }

    public void actualizar() {
        calendario.getChildren().clear();
        periodo.setText(mes.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        String[] dias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < dias.length; i++) {
            Label encabezado = new Label(dias[i]);
            encabezado.getStyleClass().add("subtitulo");
            calendario.add(encabezado, i, 0);
        }
        ResumenReserva[] reservas = servicio.consultarTodas();
        int columnaInicial = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate fecha = mes.atDay(dia);
            VBox celda = new VBox(4);
            celda.getStyleClass().add("celda-calendario");
            Label numero = new Label(Integer.toString(dia));
            numero.getStyleClass().add("subtitulo");
            celda.getChildren().add(numero);
            int cantidad = 0;
            for (ResumenReserva reserva : reservas) {
                if (reserva.estado() == EstadoReserva.CANCELADA
                        || reserva.estado() == EstadoReserva.RECHAZADA
                        || reserva.estado() == EstadoReserva.NO_SHOW) continue;
                if (!fecha.isBefore(reserva.entrada()) && fecha.isBefore(reserva.salida())) {
                    cantidad++;
                    Label evento = new Label(reserva.folio() + " · " + reserva.apartamentos());
                    evento.setWrapText(true);
                    evento.getStyleClass().add("evento-calendario");
                    celda.getChildren().add(evento);
                }
            }
            if (cantidad == 0) {
                Label libre = new Label("Sin reservas");
                libre.getStyleClass().add("texto-suave");
                celda.getChildren().add(libre);
            }
            celda.setMinHeight(105);
            celda.setPrefWidth(150);
            int indice = columnaInicial + dia - 1;
            calendario.add(celda, indice % 7, indice / 7 + 1);
        }
    }
}

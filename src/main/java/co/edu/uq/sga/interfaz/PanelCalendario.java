package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Calendario de ocupación por apartamento con intervalo de estancia [entrada, salida). */
public final class PanelCalendario extends BorderPane {
    private final ServicioPanelReservas servicio;
    private final GridPane calendario = new GridPane();
    private final Label periodo = new Label();
    private final ComboBox<String> apartamento = new ComboBox<>();
    private YearMonth mes = YearMonth.now();

    public PanelCalendario(ServicioPanelReservas servicio) {
        this.servicio = servicio;
        apartamento.getItems().add("Todos los apartamentos");
        for (Apartamento a : servicio.consultarApartamentosDominio()) {
            apartamento.getItems().add(a.getCodigo());
        }
        apartamento.setValue("Todos los apartamentos");
        apartamento.setOnAction(e -> actualizar());
        Button anterior = new Button("Mes anterior");
        Button siguiente = new Button("Mes siguiente");
        Button hoy = new Button("Mes actual");
        anterior.setOnAction(e -> { mes = mes.minusMonths(1); actualizar(); });
        siguiente.setOnAction(e -> { mes = mes.plusMonths(1); actualizar(); });
        hoy.setOnAction(e -> { mes = YearMonth.now(); actualizar(); });
        HBox controles = new HBox(10, new Label("Apartamento"), apartamento, anterior, hoy, siguiente, periodo);
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
        Apartamento seleccionado = null;
        if (apartamento.getValue() != null && !"Todos los apartamentos".equals(apartamento.getValue())) {
            for (Apartamento a : servicio.consultarApartamentosDominio()) {
                if (a.getCodigo().equals(apartamento.getValue())) seleccionado = a;
            }
        }
        int columnaInicial = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate fecha = mes.atDay(dia);
            VBox celda = new VBox(4);
            celda.getStyleClass().add("celda-calendario");
            Label numero = new Label(Integer.toString(dia));
            numero.getStyleClass().add("subtitulo");
            celda.getChildren().add(numero);
            boolean bloqueado = seleccionado != null
                    && seleccionado.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO;
            int ocupacion = 0;
            for (ResumenReserva reserva : reservas) {
                if (reserva.estado() == EstadoReserva.CANCELADA || reserva.estado() == EstadoReserva.RECHAZADA
                        || reserva.estado() == EstadoReserva.NO_SHOW) continue;
                if (fecha.isBefore(reserva.entrada()) || !fecha.isBefore(reserva.salida())) continue;
                if (seleccionado != null && !contieneApartamento(reserva.apartamentos(), seleccionado.getCodigo())) continue;
                ocupacion++;
                Label evento = new Label(reserva.folio() + " · " + reserva.estado());
                evento.setWrapText(true);
                evento.getStyleClass().add("evento-calendario");
                celda.getChildren().add(evento);
            }
            if (bloqueado) {
                Label estado = new Label("Fuera de servicio");
                estado.getStyleClass().add("estado-alerta");
                celda.getChildren().add(estado);
            } else if (ocupacion == 0) {
                Label libre = new Label("Libre");
                libre.getStyleClass().add("texto-suave");
                celda.getChildren().add(libre);
            } else {
                Label estado = new Label(ocupacion + (ocupacion == 1 ? " estancia" : " estancias"));
                celda.getChildren().add(estado);
            }
            celda.setMinHeight(105);
            celda.setPrefWidth(150);
            int indice = columnaInicial + dia - 1;
            calendario.add(celda, indice % 7, indice / 7 + 1);
        }
    }

    private boolean contieneApartamento(String lista, String codigo) {
        for (String item : lista.split(",")) if (item.trim().equals(codigo)) return true;
        return false;
    }
}

package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Matriz mensual de disponibilidad por apartamento y noche. */
public final class PanelCalendario extends BorderPane {
    private final ServicioPanelReservas servicio;
    private final GridPane calendario = new GridPane();
    private final Label periodo = new Label();
    private final Label resumen = new Label();
    private final ComboBox<String> apartamento = new ComboBox<>();
    private YearMonth mes = YearMonth.now();

    public PanelCalendario(ServicioPanelReservas servicio) {
        this.servicio = servicio;
        resumen.getStyleClass().add("texto-suave");
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

        HBox controles = new HBox(10, new Label("Apartamento"), apartamento,
                anterior, hoy, siguiente, periodo);
        controles.setAlignment(Pos.CENTER_LEFT);
        controles.getStyleClass().add("acciones-calendario");
        controles.setPadding(new Insets(0, 0, 12, 0));

        HBox leyenda = new HBox(14,
                indicador("Libre", "celda-libre"),
                indicador("Ocupado", "celda-ocupada"),
                indicador("Bloqueado", "celda-bloqueada"));
        leyenda.setAlignment(Pos.CENTER_LEFT);
        VBox encabezado = new VBox(8, controles, resumen, leyenda);
        encabezado.setPadding(new Insets(0, 0, 12, 0));

        calendario.setHgap(3);
        calendario.setVgap(3);
        calendario.setPadding(new Insets(4));
        ScrollPane scroll = new ScrollPane(calendario);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPannable(true);
        scroll.getStyleClass().add("tablero-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        setPadding(new Insets(20));
        setTop(encabezado);
        setCenter(scroll);
        actualizar();
    }

    private Label indicador(String texto, String estilo) {
        Label label = new Label(texto);
        label.getStyleClass().addAll("leyenda-calendario", estilo);
        return label;
    }

    public void actualizar() {
        calendario.getChildren().clear();
        periodo.setText(mes.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendario.setMinWidth(190 + mes.lengthOfMonth() * 94.0);

        Label encabezadoApartamento = new Label("Apartamento");
        encabezadoApartamento.getStyleClass().add("calendario-encabezado");
        encabezadoApartamento.setMinWidth(180);
        calendario.add(encabezadoApartamento, 0, 0);

        DateTimeFormatter diaSemana = DateTimeFormatter.ofPattern("EEE");
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            LocalDate fecha = mes.atDay(dia);
            VBox encabezadoDia = new VBox(2,
                    new Label(Integer.toString(dia)),
                    new Label(diaSemana.format(fecha)));
            encabezadoDia.setAlignment(Pos.CENTER);
            encabezadoDia.setMinWidth(90);
            encabezadoDia.setMinHeight(42);
            encabezadoDia.getStyleClass().add("calendario-encabezado");
            calendario.add(encabezadoDia, dia, 0);
        }

        ResumenReserva[] reservas = servicio.consultarTodas();
        List<Apartamento> apartamentos = apartamentosVisibles();
        int libres = 0;
        int ocupados = 0;
        int bloqueados = 0;
        int fila = 1;

        for (Apartamento unidad : apartamentos) {
            Label nombre = new Label(unidad.getCodigo() + " - " + unidad.getNombre());
            nombre.setMinWidth(180);
            nombre.setMinHeight(48);
            nombre.setWrapText(true);
            nombre.getStyleClass().add("calendario-apartamento");
            calendario.add(nombre, 0, fila);

            for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
                LocalDate fecha = mes.atDay(dia);
                List<ResumenReserva> delDia = reservasDe(unidad.getCodigo(), fecha, reservas);
                Label celda;
                if (unidad.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO) {
                    celda = crearCelda("Bloqueado", "celda-bloqueada",
                            "Fuera de servicio: " + unidad.getMotivoFueraDeServicio());
                    bloqueados++;
                } else if (!delDia.isEmpty()) {
                    String folios = delDia.stream().map(ResumenReserva::folio)
                            .reduce((a, b) -> a + ", " + b).orElse("");
                    String texto = delDia.size() == 1 ? "Ocupado\n" + folios
                            : "Ocupado (" + delDia.size() + ")\n" + folios;
                    celda = crearCelda(texto, "celda-ocupada",
                            detalleReservas(delDia));
                    ocupados++;
                } else {
                    celda = crearCelda("Libre", "celda-libre",
                            "Disponible durante la noche del " + fecha);
                    libres++;
                }
                calendario.add(celda, dia, fila);
            }
            fila++;
        }

        resumen.setText("Matriz de noches: " + libres + " libres · "
                + ocupados + " ocupadas · " + bloqueados + " bloqueadas");
    }

    private List<Apartamento> apartamentosVisibles() {
        List<Apartamento> resultado = new ArrayList<>();
        String filtro = apartamento.getValue();
        for (Apartamento unidad : servicio.consultarApartamentosDominio()) {
            if (filtro == null || "Todos los apartamentos".equals(filtro)
                    || unidad.getCodigo().equals(filtro)) {
                resultado.add(unidad);
            }
        }
        return resultado;
    }

    private List<ResumenReserva> reservasDe(
            String codigo, LocalDate fecha, ResumenReserva[] reservas) {
        List<ResumenReserva> resultado = new ArrayList<>();
        for (ResumenReserva reserva : reservas) {
            if (reservaOcupa(reserva, codigo, fecha)) resultado.add(reserva);
        }
        return resultado;
    }

    static boolean reservaOcupa(ResumenReserva reserva, String codigo, LocalDate fecha) {
        if (reserva == null || codigo == null || fecha == null) return false;
        if (reserva.estado() == EstadoReserva.CANCELADA
                || reserva.estado() == EstadoReserva.RECHAZADA
                || reserva.estado() == EstadoReserva.NO_SHOW) return false;
        if (fecha.isBefore(reserva.entrada()) || !fecha.isBefore(reserva.salida())) return false;
        return contieneApartamento(reserva.apartamentos(), codigo);
    }

    private static boolean contieneApartamento(String lista, String codigo) {
        if (lista == null || lista.isBlank()) return false;
        for (String item : lista.split(",")) {
            if (item.trim().equals(codigo)) return true;
        }
        return false;
    }

    private String detalleReservas(List<ResumenReserva> reservas) {
        StringBuilder detalle = new StringBuilder();
        for (ResumenReserva reserva : reservas) {
            if (!detalle.isEmpty()) detalle.append("\n");
            detalle.append(reserva.folio()).append(" · ")
                    .append(reserva.titular()).append(" · ")
                    .append(reserva.estado());
        }
        return detalle.toString();
    }

    private Label crearCelda(String texto, String estilo, String ayuda) {
        Label celda = new Label(texto);
        celda.setMinWidth(90);
        celda.setPrefWidth(90);
        celda.setMinHeight(48);
        celda.setMaxWidth(90);
        celda.setWrapText(true);
        celda.setAlignment(Pos.CENTER);
        celda.getStyleClass().addAll("celda-matriz-calendario", estilo);
        celda.setTooltip(new Tooltip(ayuda));
        return celda;
    }
}

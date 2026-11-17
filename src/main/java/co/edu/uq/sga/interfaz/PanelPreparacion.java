package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.controladores.ControladorPreparacion;
import co.edu.uq.sga.servicios.ServicioPanelPreparacion;
import co.edu.uq.sga.servicios.ServicioPanelPreparacion.ResumenTarea;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public final class PanelPreparacion extends VBox
        implements ControladorPreparacion.VistaPreparacion {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TableView<ResumenTarea> tabla = new TableView<>();

    private final Label tareaActual = new Label("Sin tarea en proceso");
    private final Label resumen = new Label();

    private final Button despachar = new Button("Despachar siguiente");
    private final Button completar = new Button("Completar preparación");
    private final Button verRuta = new Button("Ver ruta de la tarea");
    private final Button verRecorrido = new Button("Ver recorrido de todas las pendientes");
    private String apartamentoActual;

    private final ControladorPreparacion controlador;
    private final String origenRuta;

    public PanelPreparacion(ServicioPanelPreparacion servicio, String origen,
            Consumer<String> alVerRuta, Consumer<String[]> alVerRecorrido) {
        super(18);

        controlador = new ControladorPreparacion(servicio, this);
        this.origenRuta = origen;

        Label fecha = new Label(
                "Prioridad calculada para "
                        + servicio.consultarFechaOperacion().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );
        fecha.getStyleClass().add("texto-suave");

        TableColumn<ResumenTarea, String> codigo =
                new TableColumn<>("Tarea");
        codigo.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().codigo())
        );

        TableColumn<ResumenTarea, String> apartamento =
                new TableColumn<>("Apartamento");
        apartamento.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().apartamento())
        );

        TableColumn<ResumenTarea, String> llegada =
                new TableColumn<>("Próxima llegada");
        llegada.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(
                        celda.getValue().llegada() == null
                                ? "Sin próxima reserva"
                                : celda.getValue().llegada().format(FORMATO)
                )
        );

        // Conserva el orden de despacho de la cola.
        codigo.setSortable(false);
        apartamento.setSortable(false);
        llegada.setSortable(false);

        tabla.getColumns().add(codigo);
        tabla.getColumns().add(apartamento);
        tabla.getColumns().add(llegada);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tabla.setFixedCellSize(48);
        tabla.setPlaceholder(
                new Label("No hay tareas pendientes de despacho.")
        );

        tareaActual.setWrapText(true);
        tareaActual.getStyleClass().add("subtitulo");
        resumen.getStyleClass().add("texto-suave");

        despachar.getStyleClass().add("accion-principal");
        completar.getStyleClass().add("accion-principal");

        despachar.setOnAction(evento -> controlador.despachar());
        completar.setOnAction(evento -> controlador.completar());
        verRecorrido.setOnAction(evento -> {
            try { alVerRecorrido.accept(servicio.calcularRutaPendientes(origenRuta)); }
            catch (RuntimeException ex) { mostrarError(ex.getMessage()); }
        });
        verRuta.setOnAction(evento -> {
            if (apartamentoActual != null) {
                alVerRuta.accept(apartamentoActual);
            }
        });

        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(evento -> actualizar());

        verRuta.getStyleClass().add("accion-secundaria");
        actualizar.getStyleClass().add("accion-secundaria");

        FlowPane acciones = new FlowPane(
                12, 10, despachar, completar, verRuta, verRecorrido, actualizar
        );

        getChildren().addAll(
                fecha, tareaActual, resumen, tabla, acciones
        );

        VBox.setVgrow(tabla, Priority.ALWAYS);

        actualizar();
    }

    public void actualizar() {
        controlador.actualizar();
    }

    @Override
    public void mostrar(
            ResumenTarea[] pendientes,
            Optional<ResumenTarea> actual
    ) {
        apartamentoActual = actual
                .map(ResumenTarea::apartamento)
                .orElse(null);

        verRuta.setDisable(apartamentoActual == null);
        verRecorrido.setDisable(pendientes.length == 0);
        tabla.getItems().setAll(pendientes);
        resumen.setText(pendientes.length + (pendientes.length == 1 ? " tarea en espera" : " tareas en espera"));

        if (actual.isPresent()) {
            ResumenTarea tarea = actual.get();

            tareaActual.setText(
                    "En proceso: " + tarea.apartamento()
                            + " · " + tarea.codigo()
            );
        } else {
            tareaActual.setText("Sin tarea en proceso");
        }

        despachar.setDisable(
                actual.isPresent() || pendientes.length == 0
        );

        completar.setDisable(actual.isEmpty());
    }

    @Override
    public void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Preparación de apartamentos");
        alerta.setHeaderText("No se pudo realizar la operación");
        alerta.setContentText(mensaje);
        alerta.getDialogPane().setPrefWidth(520);
        alerta.showAndWait();
    }
}
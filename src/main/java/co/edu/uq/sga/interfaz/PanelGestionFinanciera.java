package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/** Consulta folios y permite registrar cargos, pagos y reversos desde el panel existente. */
public final class PanelGestionFinanciera extends BorderPane {
    private final ServicioPanelReservas servicio;
    private final TableView<ResumenReserva> tabla = new TableView<>();

    public PanelGestionFinanciera(ServicioPanelReservas servicio) {
        this.servicio = servicio;
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().add(columna("Folio", ResumenReserva::folio));
        tabla.getColumns().add(columna("Titular", ResumenReserva::titular));
        tabla.getColumns().add(columna("Estado", r -> r.estado().toString()));
        tabla.getColumns().add(columna("Apartamentos", ResumenReserva::apartamentos));
        Button abrir = new Button("Abrir folio seleccionado");
        abrir.getStyleClass().add("accion-principal");
        abrir.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        abrir.setOnAction(e -> mostrarFolio());
        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> actualizar());
        HBox acciones = new HBox(10, abrir, actualizar);
        setTop(acciones);
        setCenter(tabla);
        setBottom(new Label("Los movimientos se conservan en el folio; deshacer agrega un movimiento contrario."));
        actualizar();
    }

    public void actualizar() { tabla.getItems().setAll(servicio.consultarTodas()); }

    private void mostrarFolio() {
        ResumenReserva reserva = tabla.getSelectionModel().getSelectedItem();
        if (reserva == null) return;
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Cuenta y movimientos · " + reserva.folio());
        dialogo.setHeaderText("Titular: " + reserva.titular());
        dialogo.getDialogPane().setContent(new PanelFolio(servicio, reserva.folio()));
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.getDialogPane().setPrefWidth(900);
        dialogo.setResizable(true);
        dialogo.showAndWait();
        actualizar();
    }

    private TableColumn<ResumenReserva, String> columna(
            String titulo, java.util.function.Function<ResumenReserva, String> valor) {
        TableColumn<ResumenReserva, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
        return columna;
    }
}

package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoEmpleadosArbol;
import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import co.edu.uq.sga.servicios.ServicioEmpleados;
import co.edu.uq.sga.servicios.ServicioTurnosRecepcion;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Administración básica de personal y rotación circular de recepción. */
public final class PanelAdministracion extends BorderPane {
    private final ServicioEmpleados empleados;
    private final ServicioTurnosRecepcion turnos;
    private final TableView<Empleado> tabla = new TableView<>();
    private final Label mensaje = new Label();

    public PanelAdministracion(ServicioEmpleados empleados, ServicioTurnosRecepcion turnos) {
        this.empleados = empleados;
        this.turnos = turnos;
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().add(columna("Documento", Empleado::getDocumento));
        tabla.getColumns().add(columna("Nombre", Empleado::getNombre));
        tabla.getColumns().add(columna("Rol", e -> e.getRol().toString()));
        tabla.getColumns().add(columna("Correo", Empleado::getCorreo));
        tabla.getColumns().add(columna("Estado", e -> e.estaActivo() ? "Activo" : "Inactivo"));
        Button nuevo = new Button("Registrar empleado");
        Button activar = new Button("Activar");
        Button desactivar = new Button("Desactivar");
        Button turno = new Button("Agregar a rotación");
        Button siguiente = new Button("Siguiente turno");
        Button actualizar = new Button("Actualizar");
        nuevo.setOnAction(e -> registrar());
        activar.setOnAction(e -> cambiarEstado(true));
        desactivar.setOnAction(e -> cambiarEstado(false));
        turno.setOnAction(e -> agregarTurno());
        siguiente.setOnAction(e -> avanzarTurno());
        actualizar.setOnAction(e -> actualizar());
        FlowPane acciones = new FlowPane(8, 8, nuevo, activar, desactivar, turno, siguiente, actualizar);
        VBox superior = new VBox(10, acciones, mensaje);
        setTop(superior);
        setCenter(tabla);
        actualizar();
    }

    public void actualizar() {
        tabla.getItems().setAll(empleados.consultarTodos());
        var actual = turnos.consultarTurnoActual();
        mensaje.setText(actual.map(e -> "Turno actual: " + e.getNombre())
                .orElse("No hay recepcionistas en rotación."));
    }

    private void registrar() {
        Dialog<Empleado> dialogo = new Dialog<>();
        dialogo.setTitle("Registrar empleado");
        TextField documento = new TextField();
        TextField nombre = new TextField();
        TextField telefono = new TextField();
        TextField correo = new TextField();
        ComboBox<RolEmpleado> rol = new ComboBox<>();
        rol.getItems().setAll(RolEmpleado.values());
        rol.setValue(RolEmpleado.RECEPCIONISTA);
        GridPane campos = new GridPane();
        campos.setHgap(10); campos.setVgap(8);
        campos.addRow(0, new Label("Documento"), documento);
        campos.addRow(1, new Label("Nombre"), nombre);
        campos.addRow(2, new Label("Teléfono"), telefono);
        campos.addRow(3, new Label("Correo"), correo);
        campos.addRow(4, new Label("Rol"), rol);
        dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.setResultConverter(b -> b == ButtonType.OK
                ? new Empleado(documento.getText(), nombre.getText(), telefono.getText(),
                    correo.getText(), rol.getValue()) : null);
        dialogo.showAndWait().ifPresent(empleado -> {
            try { empleados.registrar(empleado); actualizar(); }
            catch (RuntimeException ex) { mensaje.setText(ex.getMessage()); }
        });
    }

    private void cambiarEstado(boolean activo) {
        Empleado e = tabla.getSelectionModel().getSelectedItem();
        if (e == null) return;
        try {
            if (activo) empleados.activar(e.getDocumento());
            else empleados.desactivar(e.getDocumento());
            actualizar();
        } catch (RuntimeException ex) { mensaje.setText(ex.getMessage()); }
    }

    private void agregarTurno() {
        Empleado e = tabla.getSelectionModel().getSelectedItem();
        if (e == null) return;
        try { turnos.agregarRecepcionista(e); actualizar(); }
        catch (RuntimeException ex) { mensaje.setText(ex.getMessage()); }
    }

    private void avanzarTurno() {
        turnos.avanzarTurno();
        actualizar();
    }

    private TableColumn<Empleado, String> columna(String titulo,
            java.util.function.Function<Empleado, String> valor) {
        TableColumn<Empleado, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
        return columna;
    }
}

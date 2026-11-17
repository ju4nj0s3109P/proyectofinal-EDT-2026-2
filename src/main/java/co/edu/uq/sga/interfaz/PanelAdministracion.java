package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Empleado;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Temporada;
import co.edu.uq.sga.servicios.ServicioEmpleados;
import co.edu.uq.sga.servicios.ServicioTurnosRecepcion;
import co.edu.uq.sga.servicios.ServicioApartamentos;
import co.edu.uq.sga.servicios.ServicioTarifas;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Consola administrativa de personal, apartamentos, huéspedes y temporadas. */
public final class PanelAdministracion extends BorderPane {
    private final ServicioEmpleados empleados;
    private final ServicioTurnosRecepcion turnos;
    private final ServicioApartamentos apartamentos;
    private final CatalogoHuespedesArbol huespedes;
    private final ServicioTarifas tarifas;
    private final TableView<Empleado> tablaEmpleados = new TableView<>();
    private final TableView<Apartamento> tablaApartamentos = new TableView<>();
    private final TableView<Huesped> tablaHuespedes = new TableView<>();
    private final TableView<Temporada> tablaTemporadas = new TableView<>();
    private final Label mensaje = new Label();

    public PanelAdministracion(ServicioEmpleados empleados, ServicioTurnosRecepcion turnos,
            ServicioApartamentos apartamentos, CatalogoHuespedesArbol huespedes,
            ServicioTarifas tarifas) {
        this.empleados = empleados;
        this.turnos = turnos;
        this.apartamentos = apartamentos;
        this.huespedes = huespedes;
        this.tarifas = tarifas;
        VBox.setVgrow(tablaEmpleados, Priority.ALWAYS);
        VBox.setVgrow(tablaApartamentos, Priority.ALWAYS);
        VBox.setVgrow(tablaHuespedes, Priority.ALWAYS);
        VBox.setVgrow(tablaTemporadas, Priority.ALWAYS);
        TabPane pestañas = new TabPane(tabEmpleados(), tabApartamentos(), tabHuespedes(), tabTemporadas());
        pestañas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setTop(new VBox(8, new Label("Administración del alojamiento"), mensaje));
        setCenter(pestañas);
        actualizar();
    }

    public void actualizar() {
        tablaEmpleados.getItems().setAll(empleados.consultarTodos());
        tablaApartamentos.getItems().setAll(apartamentos.consultarModelos());
        // El catálogo de huéspedes conserva el orden de su árbol.
        tablaHuespedes.getItems().setAll(huespedes.listarPorDocumento());
        tablaTemporadas.getItems().setAll(tarifas.getTemporadas().aArreglo(Temporada[]::new));
        var actual = turnos.consultarTurnoActual();
        mensaje.setText(actual.map(e -> "Turno actual: " + e.getNombre())
                .orElse("No hay recepcionistas en rotación."));
    }

    private Tab tabEmpleados() {
        tablaEmpleados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaEmpleados.getColumns().setAll(
                columna("Documento", Empleado::getDocumento), columna("Nombre", Empleado::getNombre),
                columna("Rol", e -> e.getRol().toString()), columna("Correo", Empleado::getCorreo),
                columna("Estado", e -> e.estaActivo() ? "Activo" : "Inactivo"));
        Button nuevo = boton("Registrar empleado", this::registrarEmpleado);
        Button activar = boton("Activar", () -> cambiarEmpleado(true));
        Button desactivar = boton("Desactivar", () -> cambiarEmpleado(false));
        Button turno = boton("Agregar a rotación", this::agregarTurno);
        Button siguiente = boton("Siguiente turno", () -> { turnos.avanzarTurno(); actualizar(); });
        Button retirar = boton("Retirar empleado inactivo", this::retirarEmpleado);
        Button actualizar = boton("Actualizar", this::actualizar);
        return new Tab("Personal y turnos", new VBox(10, new FlowPane(8, 8, nuevo, activar, desactivar, turno, siguiente, retirar, actualizar), tablaEmpleados));
    }

    private Tab tabApartamentos() {
        tablaApartamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaApartamentos.getColumns().setAll(
                columna("Código", Apartamento::getCodigo), columna("Nombre", Apartamento::getNombre),
                columna("Bloque", Apartamento::getBloque), columna("Piso", a -> String.valueOf(a.getPiso())),
                columna("Capacidad", a -> String.valueOf(a.getCapacidadMaxima())),
                columna("Estado", a -> a.getEstadoOperativo().toString()),
                columna("Motivo", a -> a.getMotivoFueraDeServicio() == null ? "—" : a.getMotivoFueraDeServicio()));
        Button fuera = boton("Marcar fuera de servicio", this::marcarFueraServicio);
        Button restaurar = boton("Restablecer preparado", this::restablecerApartamento);
        Button actualizar = boton("Actualizar", this::actualizar);
        return new Tab("Apartamentos", new VBox(10, new FlowPane(8, 8, fuera, restaurar, actualizar), tablaApartamentos));
    }

    private Tab tabHuespedes() {
        tablaHuespedes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaHuespedes.getColumns().setAll(
                columna("Documento", Huesped::getDocumento), columna("Nombre", Huesped::getNombre),
                columna("Nacimiento", h -> h.getFechaNacimiento().toString()),
                columna("Teléfono", Huesped::getTelefono), columna("Correo", Huesped::getCorreo),
                columna("Fotografía", h -> h.getRutaFotografia() == null ? "No registrada" : h.getRutaFotografia()));
        return new Tab("Huéspedes", new VBox(10, new FlowPane(8, 8,
                boton("Registrar huésped", this::registrarHuesped), boton("Actualizar", this::actualizar)), tablaHuespedes));
    }

    private Tab tabTemporadas() {
        tablaTemporadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaTemporadas.getColumns().setAll(
                columna("Tipo", t -> t.getTipo().toString()),
                columna("Inicio", t -> t.getFechaInicio().toString()),
                columna("Fin (exclusivo)", t -> t.getFechaFin().toString()));
        return new Tab("Temporadas y tarifas", new VBox(10, new FlowPane(8, 8,
                boton("Definir temporada", this::registrarTemporada), boton("Actualizar", this::actualizar)),
                new Label("Las tarifas por apartamento se administran en su ficha; cada temporada define el periodo de aplicación."),
                tablaTemporadas));
    }

    private void registrarEmpleado() {
        Dialog<Empleado> d = new Dialog<>();
        d.setTitle("Registrar empleado");
        TextField doc = new TextField(), nombre = new TextField(), telefono = new TextField(), correo = new TextField();
        ComboBox<RolEmpleado> rol = new ComboBox<>();
        rol.getItems().setAll(RolEmpleado.values()); rol.setValue(RolEmpleado.RECEPCIONISTA);
        GridPane g = campos(new String[]{"Documento", "Nombre", "Teléfono", "Correo", "Rol"},
                new Control[]{doc, nombre, telefono, correo, rol});
        d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(b -> b == ButtonType.OK ? new Empleado(doc.getText(), nombre.getText(), telefono.getText(), correo.getText(), rol.getValue()) : null);
        d.showAndWait().ifPresent(e -> ejecutar(() -> empleados.registrar(e)));
    }

    private void registrarHuesped() {
        Dialog<Huesped> d = new Dialog<>();
        d.setTitle("Registrar huésped");
        TextField nombre = new TextField(), doc = new TextField(), telefono = new TextField(), correo = new TextField(), foto = new TextField();
        DatePicker nacimiento = new DatePicker(LocalDate.of(1990, 1, 1));
        GridPane g = campos(new String[]{"Nombre", "Documento", "Nacimiento", "Teléfono", "Correo", "Fotografía (opcional)"},
                new Control[]{nombre, doc, nacimiento, telefono, correo, foto});
        d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(b -> b == ButtonType.OK ? new Huesped(nombre.getText(), doc.getText(), nacimiento.getValue(),
                telefono.getText(), correo.getText(), foto.getText()) : null);
        d.showAndWait().ifPresent(h -> ejecutar(() -> huespedes.agregar(h)));
    }

    private void registrarTemporada() {
        Dialog<Temporada> d = new Dialog<>();
        d.setTitle("Definir temporada");
        ComboBox<TipoTemporada> tipo = new ComboBox<>(); tipo.getItems().setAll(TipoTemporada.values());
        tipo.setValue(TipoTemporada.BAJA);
        DatePicker inicio = new DatePicker(LocalDate.now()), fin = new DatePicker(LocalDate.now().plusMonths(1));
        d.getDialogPane().setContent(campos(new String[]{"Tipo", "Inicio", "Fin (exclusivo)"}, new Control[]{tipo, inicio, fin}));
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(b -> b == ButtonType.OK ? new Temporada(tipo.getValue(), inicio.getValue(), fin.getValue()) : null);
        d.showAndWait().ifPresent(t -> ejecutar(() -> tarifas.definirTemporada(t)));
    }

    private void marcarFueraServicio() {
        Apartamento a = tablaApartamentos.getSelectionModel().getSelectedItem();
        if (a == null) { mensaje.setText("Selecciona un apartamento."); return; }
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Fuera de servicio"); d.setHeaderText("Motivo obligatorio para " + a.getCodigo());
        d.setContentText("Motivo:");
        d.showAndWait().ifPresent(m -> ejecutar(() -> apartamentos.marcarFueraDeServicio(a.getCodigo(), m)));
    }

    private void restablecerApartamento() {
        Apartamento a = tablaApartamentos.getSelectionModel().getSelectedItem();
        if (a == null) { mensaje.setText("Selecciona un apartamento."); return; }
        ejecutar(() -> apartamentos.restablecerPreparado(a.getCodigo()));
    }

    private void cambiarEmpleado(boolean activo) {
        Empleado e = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (e == null) { mensaje.setText("Selecciona un empleado."); return; }
        ejecutar(() -> { if (activo) empleados.activar(e.getDocumento()); else empleados.desactivar(e.getDocumento()); });
    }
    private void retirarEmpleado() {
        Empleado e = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (e == null) { mensaje.setText("Selecciona un empleado."); return; }
        ejecutar(() -> {
            turnos.retirarRecepcionista(e.getDocumento());
            empleados.eliminarInactivo(e.getDocumento());
        });
    }
    private void agregarTurno() {
        Empleado e = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (e == null) { mensaje.setText("Selecciona un empleado."); return; }
        ejecutar(() -> turnos.agregarRecepcionista(e));
    }
    private void ejecutar(Runnable accion) {
        try { accion.run(); actualizar(); mensaje.setText("Operación realizada correctamente."); }
        catch (RuntimeException ex) { mensaje.setText(ex.getMessage()); }
    }
    private Button boton(String texto, Runnable accion) {
        Button b = new Button(texto); b.setOnAction(e -> accion.run()); return b;
    }
    private GridPane campos(String[] nombres, Control[] controles) {
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8);
        for (int i = 0; i < nombres.length; i++) g.addRow(i, new Label(nombres[i]), controles[i]);
        return g;
    }
    private <T> TableColumn<T, String> columna(String titulo, java.util.function.Function<T, String> valor) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(v -> new ReadOnlyStringWrapper(valor.apply(v.getValue())));
        return c;
    }
}

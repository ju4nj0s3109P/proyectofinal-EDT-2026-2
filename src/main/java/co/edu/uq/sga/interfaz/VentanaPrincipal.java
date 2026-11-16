package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.controladores.ControladorMapa;
import co.edu.uq.sga.servicios.ServicioMapa;
import co.edu.uq.sga.servicios.ServicioMapa.Recorrido;
import co.edu.uq.sga.servicios.ServicioMapa.Ubicacion;
import co.edu.uq.sga.servicios.ServicioMapa.Pasillo;
import co.edu.uq.sga.servicios.ServicioApartamentos;
import co.edu.uq.sga.servicios.ServicioPanelPreparacion;
import co.edu.uq.sga.servicios.ServicioPanelReservas;


import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;

public class VentanaPrincipal extends BorderPane implements ControladorMapa.VistaMapa {

    private final ControladorMapa controlador;
    private final ServicioApartamentos servicioApartamentos;

    private final Label titulo =
            etiqueta("Mapa del edificio", "titulo");

    private final Label distancia =
            etiqueta("—", "distancia");

    private final Label resultado =
            etiqueta("Selecciona un destino", "texto-suave");

    private final VBox recorrido = new VBox(10);

    private final ComboBox<Ubicacion> destinos = new ComboBox<>();

    private final ToggleButton botonMapa =
            new ToggleButton("Mapa del edificio");

    private final ToggleButton botonUbicaciones =
            new ToggleButton("Ubicaciones");

    private final PanelReservas vistaReservas;

    private final ToggleButton botonReservas =
            new ToggleButton("Reservas");

    private final PanelMapa mapa;

    private final StackPane vistas = new StackPane();

    private final Node vistaMapa;
    private final Node vistaTabla;

    private final PanelTablero vistaTablero;

    private final ToggleButton botonTablero =
            new ToggleButton("Inicio");

    private final PanelPreparacion vistaPreparacion;

    private final PanelAdministracion vistaAdministracion;
    private final PanelPortalHuespedes vistaPortal;
    private final PanelCalendario vistaCalendario;
    private final PanelEstadisticas vistaEstadisticas;
    private final PanelGestionFinanciera vistaFinanzas;
    private final ToggleButton botonAdministracion = new ToggleButton("Administración");
    private final ToggleButton botonPortal = new ToggleButton("Portal huéspedes");
    private final ToggleButton botonCalendario = new ToggleButton("Calendario");
    private final ToggleButton botonEstadisticas = new ToggleButton("Estadísticas");
    private final ToggleButton botonFinanzas = new ToggleButton("Gestión financiera");

    private final ToggleButton botonPreparacion =
            new ToggleButton("Preparación");

    public VentanaPrincipal(ServicioMapa servicio,
                            ServicioApartamentos servicioApartamentos,
                            ServicioPanelPreparacion servicioPreparacion,
                            ServicioPanelReservas servicioPanelReservas,
                            PanelAdministracion administracion,
                            PanelPortalHuespedes portal,
                            PanelCalendario calendario,
                            PanelEstadisticas estadisticas,
                            PanelGestionFinanciera finanzas
    ) {
        this.controlador = new ControladorMapa(servicio, this);
        this.servicioApartamentos = servicioApartamentos;

        getStyleClass().add("ventana");

        Ubicacion[] ubicaciones = controlador.consultarUbicaciones();

        destinos.getItems().addAll(ubicaciones);
        destinos.setPromptText("Elegir ubicación");
        destinos.setId("destino");
        destinos.setAccessibleText("Destino de la ruta desde recepción");
        destinos.setMaxWidth(Double.MAX_VALUE);

        distancia.setId("distancia");
        resultado.setWrapText(true);
        recorrido.setId("recorrido");

        mapa = new PanelMapa(
                ubicaciones,
                controlador.consultarPasillos(),
                this::seleccionarDestino
        );

        vistaPreparacion = new PanelPreparacion(
                servicioPreparacion,
                codigo -> {
                    seleccionarDestino(codigo);
                    mostrarVista(true);
                }
        );
        vistaReservas = new PanelReservas(servicioPanelReservas);
        vistaAdministracion = administracion;
        vistaPortal = portal;
        vistaCalendario = calendario;
        vistaEstadisticas = estadisticas;
        vistaFinanzas = finanzas;

        setLeft(crearBarraLateral());
        vistaTablero = new PanelTablero(servicioApartamentos);

        vistaMapa = crearVistaMapa();
        vistaTabla = crearTabla(ubicaciones);

        vistas.getChildren().addAll(vistaTablero, vistaMapa, vistaTabla, vistaPreparacion,
                vistaReservas, vistaAdministracion, vistaPortal, vistaCalendario,
                vistaEstadisticas, vistaFinanzas);

        VBox contenido = new VBox(24, crearEncabezado(), vistas);
        contenido.getStyleClass().add("contenido");

        VBox.setVgrow(vistas, Priority.ALWAYS);
        setCenter(contenido);

        destinos.setOnAction(evento -> actualizarRuta());

        recorrido.getChildren().add(
                etiqueta(
                        "Pulsa una ubicación del mapa o elige un destino.",
                        "texto-suave"
                )
        );

        mostrarTablero();
    }

    private VBox crearBarraLateral() {
        Label marca = etiqueta("APARTA-SUITES", "marca");
        Label lugar = etiqueta("Universidad del Quindío", "lateral-suave");
        VBox identidad = new VBox(6, marca, lugar);

        ToggleGroup grupo = new ToggleGroup();

        botonMapa.setToggleGroup(grupo);
        botonUbicaciones.setToggleGroup(grupo);
        botonPreparacion.setToggleGroup(grupo);
        botonPreparacion.setId("nav-preparacion");

        botonMapa.setId("nav-mapa");
        botonUbicaciones.setId("nav-ubicaciones");
        botonTablero.setToggleGroup(grupo);
        botonTablero.setId("nav-tablero");
        botonReservas.setToggleGroup(grupo);
        botonReservas.setId("nav-reservas");
        botonAdministracion.setToggleGroup(grupo);
        botonPortal.setToggleGroup(grupo);
        botonCalendario.setToggleGroup(grupo);
        botonEstadisticas.setToggleGroup(grupo);
        botonFinanzas.setToggleGroup(grupo);

        for (ToggleButton boton :
                new ToggleButton[]{botonTablero, botonReservas, botonMapa, botonUbicaciones,
                        botonPreparacion, botonAdministracion, botonPortal,
                        botonCalendario, botonEstadisticas, botonFinanzas}) {
            boton.getStyleClass().add("navegacion");
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
        }

        botonTablero.setOnAction(evento -> mostrarTablero());
        botonMapa.setOnAction(evento -> mostrarVista(true));
        botonUbicaciones.setOnAction(evento -> mostrarVista(false));
        botonPreparacion.setOnAction(evento -> mostrarPreparacion());
        botonReservas.setOnAction(evento -> mostrarReservas());
        botonAdministracion.setOnAction(evento -> mostrarComplementaria(vistaAdministracion, botonAdministracion, "Administración"));
        botonPortal.setOnAction(evento -> mostrarComplementaria(vistaPortal, botonPortal, "Portal de huéspedes"));
        botonCalendario.setOnAction(evento -> mostrarComplementaria(vistaCalendario, botonCalendario, "Calendario de ocupación"));
        botonEstadisticas.setOnAction(evento -> mostrarComplementaria(vistaEstadisticas, botonEstadisticas, "Estadísticas"));
        botonFinanzas.setOnAction(evento -> mostrarComplementaria(vistaFinanzas, botonFinanzas, "Gestión financiera"));

        VBox menu = new VBox(8, botonTablero, botonReservas, botonMapa, botonUbicaciones,
                botonPreparacion, botonPortal, botonCalendario, botonFinanzas,
                botonEstadisticas, botonAdministracion);

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        VBox lateral = new VBox(
                36,
                identidad,
                menu,
                espacio,
                etiqueta("Gestión de alojamiento", "lateral-suave")
        );

        lateral.getStyleClass().add("barra-lateral");
        lateral.setPrefWidth(210);
        lateral.setMinWidth(210);

        return lateral;
    }

    private VBox crearEncabezado() {
        String resumen =
                controlador.consultarUbicaciones().length + " ubicaciones · "
                        + controlador.consultarPasillos().length + " pasillos";

        return new VBox(
                9,
                etiqueta("APARTA-SUITES UQ / EDIFICIO", "contexto"),
                titulo,
                etiqueta(resumen, "texto-suave")
        );
    }

    private HBox crearVistaMapa() {
        ScrollPane scroll = new ScrollPane(mapa);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.getStyleClass().add("mapa-scroll");
        Button gestionarPasillos = new Button("Gestionar pasillos");
        gestionarPasillos.getStyleClass().add("accion-principal");
        gestionarPasillos.setOnAction(evento -> abrirGestionPasillos());

        VBox zonaMapa = new VBox(
                12,
                gestionarPasillos,
                scroll,
                etiqueta(
                        "Esquema de conexiones · Distancias en metros",
                        "nota"
                )
        );

        zonaMapa.setMinWidth(200);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(zonaMapa, Priority.ALWAYS);

        return new HBox(20, zonaMapa, crearDetalle());
    }

    private VBox crearDetalle() {
        Label origen = etiqueta(
                "Desde " + controlador.consultarRecepcion(),
                "texto-suave"
        );

        VBox seleccion = new VBox(
                8,
                etiqueta("Destino", "campo"),
                destinos
        );

        VBox total = new VBox(4, distancia, resultado);

        ScrollPane pasos = new ScrollPane(recorrido);
        pasos.setFitToWidth(true);
        pasos.getStyleClass().add("recorrido-scroll");
        VBox.setVgrow(pasos, Priority.ALWAYS);

        VBox panel = new VBox(
                22,
                new VBox(8, etiqueta("Tu recorrido", "subtitulo"), origen),
                seleccion,
                total,
                pasos,
                etiqueta("Distancia desde recepción", "nota")
        );

        panel.setMinWidth(260);
        panel.setPrefWidth(260);
        panel.setMaxWidth(260);
        panel.getStyleClass().add("detalle");

        return panel;
    }

    private VBox crearTabla(Ubicacion[] ubicaciones) {
        TableView<Ubicacion> tabla = new TableView<>();
        tabla.setId("tabla-ubicaciones");
        tabla.getItems().addAll(ubicaciones);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabla.setFixedCellSize(48);
        tabla.setPlaceholder(
                etiqueta("No hay ubicaciones cargadas.", "texto-suave")
        );

        TableColumn<Ubicacion, String> codigo =
                new TableColumn<>("Código");

        codigo.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().id())
        );

        TableColumn<Ubicacion, String> nombre =
                new TableColumn<>("Nombre");

        nombre.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(celda.getValue().nombre())
        );

        TableColumn<Ubicacion, String> tipo =
                new TableColumn<>("Tipo");

        tipo.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(
                        celda.getValue().tipo().replace('_', ' ')
                )
        );

        codigo.setMinWidth(110);
        codigo.setPrefWidth(130);
        codigo.setMaxWidth(150);

        nombre.setMinWidth(180);
        nombre.setPrefWidth(360);

        tipo.setMinWidth(180);
        tipo.setPrefWidth(300);

        tabla.getColumns().add(codigo);
        tabla.getColumns().add(nombre);
        tabla.getColumns().add(tipo);

        Button verRuta = new Button("Ver ruta en el mapa");
        verRuta.setId("ver-ruta");
        verRuta.getStyleClass().add("accion-principal");

        verRuta.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        verRuta.setOnAction(evento -> {
            Ubicacion seleccionada =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionada != null) {
                seleccionarDestino(seleccionada.id());
                mostrarVista(true);
            }
        });

        HBox acciones = new HBox(verRuta);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        VBox panel = new VBox(16, tabla, acciones);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        return panel;
    }

    private void seleccionarDestino(String codigo) {
        for (Ubicacion ubicacion : destinos.getItems()) {
            if (ubicacion.id().equals(codigo)) {
                if (ubicacion.equals(destinos.getValue())) {
                    actualizarRuta();
                } else {
                    destinos.getSelectionModel().select(ubicacion);
                }

                return;
            }
        }
    }

    private void actualizarRuta() {
        Ubicacion destino = destinos.getValue();

        if (destino == null) {
            return;
        }

        controlador.seleccionarDestino(destino.id());
    }

    @Override
    public void mostrarRecorrido(String codigoDestino, Recorrido ruta) {
        String[] pasos = ruta.ubicaciones();

        mapa.mostrarRuta(codigoDestino, pasos);
        recorrido.getChildren().clear();

        if (!ruta.existe()) {
            distancia.setText("—");
            resultado.setText("Sin conexión disponible");

            recorrido.getChildren().add(
                    etiqueta(
                            "No hay una ruta desde recepción hasta "
                                    + codigoDestino + ".",
                            "texto-suave"
                    )
            );

            return;
        }

        distancia.setText(
                new DecimalFormat("0.##").format(ruta.metros()) + " m"
        );

        resultado.setText("Distancia total");

        for (int i = 0; i < pasos.length; i++) {
            if (i > 0) {
                recorrido.getChildren().add(etiqueta("↓", "flecha"));
            }

            recorrido.getChildren().add(
                    etiqueta(pasos[i], "paso-ruta")
            );
        }
    }

    private void mostrarVista(boolean mostrarMapa) {
        ocultarComplementarias();
        vistaReservas.setVisible(false);
        vistaReservas.setManaged(false);
        botonReservas.setSelected(false);

        if (mostrarMapa) {
            mapa.actualizarEstados(servicioApartamentos.consultarTodos());
            mapa.actualizarPasillosCerrados(
                    controlador.consultarPasillosCerrados()
            );

            actualizarRuta();
        }


        vistaTablero.setVisible(false);
        vistaTablero.setManaged(false);
        botonTablero.setSelected(false);
        vistaMapa.setVisible(mostrarMapa);
        vistaMapa.setManaged(mostrarMapa);
        vistaPreparacion.setVisible(false);
        vistaPreparacion.setManaged(false);
        botonPreparacion.setSelected(false);

        vistaTabla.setVisible(!mostrarMapa);
        vistaTabla.setManaged(!mostrarMapa);

        botonMapa.setSelected(mostrarMapa);
        botonUbicaciones.setSelected(!mostrarMapa);

        titulo.setText(
                mostrarMapa
                        ? "Mapa del edificio"
                        : "Ubicaciones del edificio"
        );
    }

    private void mostrarTablero() {
        ocultarComplementarias();
        vistaReservas.setVisible(false);
        vistaReservas.setManaged(false);
        botonReservas.setSelected(false);

        vistaPreparacion.setVisible(false);
        vistaPreparacion.setManaged(false);
        botonPreparacion.setSelected(false);
        vistaTablero.actualizar();

        vistaTablero.setVisible(true);
        vistaTablero.setManaged(true);

        vistaMapa.setVisible(false);
        vistaMapa.setManaged(false);

        vistaTabla.setVisible(false);
        vistaTabla.setManaged(false);

        botonTablero.setSelected(true);
        botonMapa.setSelected(false);
        botonUbicaciones.setSelected(false);

        titulo.setText("Estado de los apartamentos");
    }

    private static Label etiqueta(String texto, String estilo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add(estilo);
        etiqueta.setWrapText(true);

        return etiqueta;
    }

    private void mostrarPreparacion() {
        ocultarComplementarias();
        vistaReservas.setVisible(false);
        vistaReservas.setManaged(false);


        vistaTablero.setVisible(false);
        vistaTablero.setManaged(false);

        vistaMapa.setVisible(false);
        vistaMapa.setManaged(false);

        vistaTabla.setVisible(false);
        vistaTabla.setManaged(false);

        vistaPreparacion.setVisible(true);
        vistaPreparacion.setManaged(true);

        vistaPreparacion.actualizar();

        botonTablero.setSelected(false);
        botonMapa.setSelected(false);
        botonUbicaciones.setSelected(false);
        botonPreparacion.setSelected(true);
        botonReservas.setSelected(false);

        titulo.setText("Preparación de apartamentos");
    }

    private void abrirGestionPasillos() {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Pasillos del edificio");
        dialogo.setHeaderText(
                "Los cierres afectan las rutas desde recepción."
        );
        dialogo.initOwner(getScene().getWindow());
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ListView<Pasillo> lista = new ListView<>();
        lista.getItems().addAll(controlador.consultarPasillos());
        lista.setPrefHeight(240);

        lista.setCellFactory(vista -> new ListCell<>() {
            @Override
            protected void updateItem(Pasillo pasillo, boolean vacio) {
                super.updateItem(pasillo, vacio);

                if (vacio || pasillo == null) {
                    setText(null);
                    return;
                }

                String estado = controlador.estaAbierto(pasillo)
                        ? "Abierto"
                        : "Cerrado";

                setText(
                        pasillo.origen() + " ↔ " + pasillo.destino()
                                + " · " + pasillo.metros() + " m"
                                + " · " + estado
                );
            }
        });

        Label conectividad = new Label();
        conectividad.setWrapText(true);
        actualizarConectividad(conectividad);

        Button cerrar = new Button("Cerrar pasillo");
        Button abrir = new Button("Abrir pasillo");

        cerrar.disableProperty().bind(
                lista.getSelectionModel().selectedItemProperty().isNull()
        );

        abrir.disableProperty().bind(
                lista.getSelectionModel().selectedItemProperty().isNull()
        );

        cerrar.setOnAction(evento ->
                cambiarPasilloSeleccionado(lista, false, conectividad)
        );

        abrir.setOnAction(evento ->
                cambiarPasilloSeleccionado(lista, true, conectividad)
        );

        VBox contenido = new VBox(
                16,
                lista,
                new HBox(12, cerrar, abrir),
                conectividad
        );

        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(560);
        dialogo.setResizable(true);
        dialogo.showAndWait();
    }

    private void cambiarPasilloSeleccionado(
            ListView<Pasillo> lista,
            boolean abierto,
            Label conectividad
    ) {
        Pasillo seleccionado =
                lista.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        try {
            controlador.cambiarEstadoPasillo(seleccionado, abierto);

            lista.refresh();

            mapa.actualizarPasillosCerrados(
                    controlador.consultarPasillosCerrados()
            );

            actualizarRuta();
            actualizarConectividad(conectividad);

        } catch (IllegalArgumentException | IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.initOwner(getScene().getWindow());
            alerta.setTitle("Gestión de pasillos");
            alerta.setHeaderText("No se pudo cambiar el pasillo");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    private void actualizarConectividad(Label etiqueta) {
        String[] aislados =
                controlador.consultarApartamentosInalcanzables();

        etiqueta.setText(
                aislados.length == 0
                        ? "Todos los apartamentos son accesibles desde recepción."
                        : "Apartamentos inalcanzables desde recepción: "
                        + String.join(", ", aislados)
        );
    }

    private void mostrarReservas() {
        vistaReservas.actualizar();

        for (Node vista : new Node[]{
                vistaTablero, vistaMapa, vistaTabla, vistaPreparacion,
                vistaAdministracion, vistaPortal, vistaCalendario, vistaEstadisticas, vistaFinanzas
        }) {
            vista.setVisible(false);
            vista.setManaged(false);
        }

        vistaReservas.setVisible(true);
        vistaReservas.setManaged(true);

        botonTablero.setSelected(false);
        botonMapa.setSelected(false);
        botonUbicaciones.setSelected(false);
        botonPreparacion.setSelected(false);
        botonReservas.setSelected(true);

        titulo.setText("Reservas del alojamiento");
    }

    private void ocultarComplementarias() {
        for (Node vista : new Node[]{vistaAdministracion, vistaPortal, vistaCalendario,
                vistaEstadisticas, vistaFinanzas}) {
            vista.setVisible(false);
            vista.setManaged(false);
        }
    }

    private void mostrarComplementaria(Node seleccionada, ToggleButton boton, String encabezado) {
        for (Node vista : new Node[]{vistaTablero, vistaMapa, vistaTabla, vistaPreparacion,
                vistaReservas, vistaAdministracion, vistaPortal, vistaCalendario,
                vistaEstadisticas, vistaFinanzas}) {
            vista.setVisible(vista == seleccionada);
            vista.setManaged(vista == seleccionada);
        }
        if (seleccionada instanceof PanelCalendario panel) panel.actualizar();
        if (seleccionada instanceof PanelEstadisticas panel) panel.actualizar();
        if (seleccionada instanceof PanelGestionFinanciera panel) panel.actualizar();
        if (seleccionada instanceof PanelAdministracion panel) panel.actualizar();
        for (ToggleButton opcion : new ToggleButton[]{botonTablero, botonReservas, botonMapa,
                botonUbicaciones, botonPreparacion, botonAdministracion, botonPortal,
                botonCalendario, botonEstadisticas, botonFinanzas}) {
            opcion.setSelected(opcion == boton);
        }
        titulo.setText(encabezado);
    }
}
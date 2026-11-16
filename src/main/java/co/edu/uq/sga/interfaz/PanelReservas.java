package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.controladores.ControladorReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.DatosOcupante;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import co.edu.uq.sga.servicios.ServicioTarifas;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

public final class PanelReservas extends VBox
        implements ControladorReservas.VistaReservas {

    private final TableView<ResumenReserva> tabla = new TableView<>();
    private final ControladorReservas controlador;
    private final Button checkIn = new Button("Registrar entrada");
    private final Button checkOut = new Button("Registrar salida");

    public PanelReservas(ServicioPanelReservas servicio) {
        super(18);

        controlador = new ControladorReservas(servicio, this);

        Button nueva = new Button("Nueva reserva directa");
        nueva.getStyleClass().add("accion-principal");
        nueva.setOnAction(evento -> abrirFormulario());

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tabla.setFixedCellSize(48);
        tabla.setPlaceholder(new Label("No hay reservas registradas."));

        agregarColumna("Folio", ResumenReserva::folio);
        agregarColumna("Titular", ResumenReserva::titular);
        agregarColumna("Apartamentos", ResumenReserva::apartamentos);
        agregarColumna("Entrada", r -> r.entrada().toString());
        agregarColumna("Salida", r -> r.salida().toString());
        agregarColumna(
                "Hora llegada",
                r -> r.llegadaEstimada().toLocalTime().toString()
        );
        agregarColumna("Estado", r -> r.estado().toString());
        agregarColumna("Personas", r -> String.valueOf(r.ocupantes()));

        checkIn.getStyleClass().add("accion-principal");
        checkOut.getStyleClass().add("accion-principal");

        checkIn.setDisable(true);
        checkOut.setDisable(true);

        tabla.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> {
                    checkIn.setDisable(
                            seleccionada == null
                                    || seleccionada.estado() != EstadoReserva.CONFIRMADA
                    );

                    checkOut.setDisable(
                            seleccionada == null
                                    || seleccionada.estado() != EstadoReserva.EN_CURSO
                    );
                }
        );

        checkIn.setOnAction(evento -> {
            ResumenReserva seleccionada =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionada != null) {
                controlador.registrarCheckIn(seleccionada.folio());
            }
        });

        checkOut.setOnAction(evento -> {
            ResumenReserva seleccionada =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionada != null) {
                controlador.registrarCheckOut(seleccionada.folio());
            }
        });

        Button verFolio = new Button("Ver folio");

        verFolio.getStyleClass().add("accion-secundaria");

        verFolio.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        verFolio.setOnAction(evento -> {
            ResumenReserva seleccionada =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionada == null) {
                return;
            }

            Dialog<Void> dialogo = new Dialog<>();
            dialogo.initOwner(getScene().getWindow());
            dialogo.setTitle("Folio " + seleccionada.folio());
            dialogo.setHeaderText("Titular: " + seleccionada.titular());

            dialogo.getDialogPane().setContent(
                    new PanelFolio(servicio, seleccionada.folio())
            );

            dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialogo.getDialogPane()
                    .lookupButton(ButtonType.CLOSE)
                    .getStyleClass()
                    .add("accion-secundaria");
            dialogo.getDialogPane().setPrefWidth(960);
            dialogo.setResizable(true);
            dialogo.showAndWait();
        });

        Button cotizar = new Button("Ver cotización");
        cotizar.getStyleClass().add("accion-secundaria");

        cotizar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        cotizar.setOnAction(evento -> {
            ResumenReserva seleccionada =
                    tabla.getSelectionModel().getSelectedItem();

            if (seleccionada != null) {
                controlador.cotizar(seleccionada.folio())
                        .ifPresent(cotizacion ->
                                mostrarCotizacion(seleccionada, cotizacion)
                        );
            }
        });

        FlowPane acciones = new FlowPane(
                12, 10, nueva, checkIn, checkOut, verFolio, cotizar
        );
        getChildren().addAll(acciones, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        actualizar();
    }

    public void actualizar() {
        controlador.actualizar();
    }

    private void agregarColumna(
            String titulo,
            Function<ResumenReserva, String> valor
    ) {
        TableColumn<ResumenReserva, String> columna =
                new TableColumn<>(titulo);

        columna.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(valor.apply(celda.getValue()))
        );

        tabla.getColumns().add(columna);
    }

    @Override
    public void mostrarReservas(ResumenReserva[] reservas) {
        ResumenReserva seleccionada =
                tabla.getSelectionModel().getSelectedItem();

        String folio = seleccionada == null
                ? null
                : seleccionada.folio();

        tabla.getSelectionModel().clearSelection();
        tabla.getItems().setAll(reservas);

        if (folio != null) {
            for (ResumenReserva reserva : reservas) {
                if (folio.equals(reserva.folio())) {
                    tabla.getSelectionModel().select(reserva);
                    break;
                }
            }
        }
    }

    private void mostrarCotizacion(
            ResumenReserva reserva,
            ServicioTarifas.Cotizacion cotizacion
    ) {
        NumberFormat moneda = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("es-CO")
        );

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Label total = new Label(
                "Total: " + moneda.format(cotizacion.getTotal())
        );
        total.getStyleClass().add("subtitulo");

        Label resumen = new Label(
                cotizacion.cantidadNoches() + " noches · "
                        + reserva.ocupantes() + " ocupantes · "
                        + reserva.apartamentos()
        );
        resumen.getStyleClass().add("texto-suave");
        resumen.setWrapText(true);

        VBox noches = new VBox(10);

        for (var noche : cotizacion.getDesglose()) {
            Label fecha = new Label(
                    noche.fecha().format(formato)
                            + " · Temporada " + noche.temporada()
            );
            fecha.getStyleClass().add("campo");

            Label calculo = new Label(
                    noche.ocupantesPagantes() + " personas que pagan × "
                            + moneda.format(noche.tarifaPorPersona())
                            + " = " + moneda.format(noche.subtotal())
            );
            calculo.setWrapText(true);

            VBox detalle = new VBox(6, fecha, calculo);
            detalle.getStyleClass().add("detalle");
            noches.getChildren().add(detalle);
        }

        ScrollPane scroll = new ScrollPane(noches);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.getStyleClass().add("tablero-scroll");

        Label nota = new Label(
                "Los menores de 6 años ocupan cupo, pero no pagan. "
                        + "La noche de salida no se cobra.\n"
                        + "Esta consulta no registra cargos en el folio."
        );
        nota.setWrapText(true);
        nota.getStyleClass().add("texto-suave");

        VBox contenido = new VBox(14, total, resumen, scroll, nota);

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Cotización " + reserva.folio());
        dialogo.setHeaderText("Titular: " + reserva.titular());
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(650);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialogo.getDialogPane()
                .lookupButton(ButtonType.CLOSE)
                .getStyleClass()
                .add("accion-secundaria");

        dialogo.setResizable(true);
        dialogo.showAndWait();
    }

    private void abrirFormulario() {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Nueva reserva directa");

        TextField folio = new TextField();
        TextField documento = new TextField();
        TextField apartamentos = new TextField();
        apartamentos.setPromptText("Ejemplo: A101 o A101,A102");

        DatePicker entrada = new DatePicker(LocalDate.now());
        DatePicker salida = new DatePicker(LocalDate.now().plusDays(1));
        entrada.setEditable(false);
        salida.setEditable(false);

        TextField hora = new TextField();
        hora.setPromptText("Hora estimada, ejemplo: 15:30");

        GridPane datos = new GridPane();
        datos.setHgap(12);
        datos.setVgap(10);

        datos.addRow(0, new Label("Folio de reserva"), folio);
        datos.addRow(1, new Label("Documento del titular"), documento);
        datos.addRow(2, new Label("Códigos de apartamentos"), apartamentos);
        datos.addRow(3, new Label("Entrada"), entrada);
        datos.addRow(4, new Label("Salida"), salida);
        datos.addRow(5, new Label("Hora de llegada"), hora);

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre del ocupante");

        DatePicker nacimiento = new DatePicker();
        nacimiento.setPromptText("Fecha de nacimiento");
        nacimiento.setEditable(false);

        ListView<DatosOcupante> ocupantes = new ListView<>();
        ocupantes.setPrefHeight(130);

        ocupantes.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(DatosOcupante dato, boolean vacio) {
                super.updateItem(dato, vacio);
                setText(vacio || dato == null
                        ? null
                        : dato.nombre() + " · " + dato.fechaNacimiento());
            }
        });

        Label error = new Label();
        error.setWrapText(true);

        Button agregar = new Button("Agregar ocupante");
        agregar.getStyleClass().add("accion-secundaria");
        agregar.setOnAction(evento -> {
            error.setText("");


            if (nombre.getText().isBlank()
                    || nacimiento.getValue() == null) {
                error.setText("Completa el nombre y el nacimiento.");
                return;
            }

            if (nacimiento.getValue().isAfter(LocalDate.now())) {
                error.setText("El nacimiento no puede ser futuro.");
                return;
            }

            ocupantes.getItems().add(new DatosOcupante(
                    nombre.getText().trim(),
                    nacimiento.getValue()
            ));

            nombre.clear();
            nacimiento.setValue(null);
        });

        Button quitar = new Button("Quitar seleccionado");
        quitar.disableProperty().bind(
                ocupantes.getSelectionModel()
                        .selectedItemProperty().isNull()
        );
        quitar.getStyleClass().add("accion-secundaria");

        quitar.setOnAction(evento -> {
            int indice = ocupantes.getSelectionModel().getSelectedIndex();

            if (indice >= 0) {
                ocupantes.getItems().remove(indice);
            }
        });

        Label aviso = new Label(
                "Incluye a todas las personas que se hospedarán, "
                        + "también al titular si ocupa un cupo."
        );
        aviso.setWrapText(true);

        VBox contenido = new VBox(
                12,
                datos,
                new Label("Ocupantes"),
                aviso,
                new HBox(10, nombre, nacimiento),
                new HBox(10, agregar, quitar),
                ocupantes,
                error
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(520);

        dialogo.getDialogPane().setContent(scroll);
        dialogo.getDialogPane().setPrefWidth(650);
        dialogo.setResizable(true);

        ButtonType registrar = new ButtonType(
                "Registrar",
                ButtonBar.ButtonData.OK_DONE
        );

        dialogo.getDialogPane().getButtonTypes().addAll(
                registrar, ButtonType.CANCEL
        );

        Button botonRegistrar = (Button)
                dialogo.getDialogPane().lookupButton(registrar);

        botonRegistrar.getStyleClass().add("accion-principal");

        dialogo.getDialogPane()
                .lookupButton(ButtonType.CANCEL)
                .getStyleClass()
                .add("accion-secundaria");

        botonRegistrar.addEventFilter(ActionEvent.ACTION, evento -> {
            error.setText("");

            try {
                LocalTime horaLlegada =
                        LocalTime.parse(hora.getText().trim());

                String[] codigos = apartamentos.getText().split(",", -1);

                for (int i = 0; i < codigos.length; i++) {
                    codigos[i] = codigos[i].trim();
                }

                DatosOcupante[] personas = ocupantes.getItems()
                        .toArray(new DatosOcupante[0]);

                boolean registrada = controlador.registrar(
                        folio.getText().trim(),
                        documento.getText().trim(),
                        codigos,
                        personas,
                        entrada.getValue(),
                        salida.getValue(),
                        horaLlegada
                );

                if (!registrada) {
                    evento.consume();
                }

            } catch (DateTimeParseException e) {
                error.setText("Escribe una hora válida, por ejemplo 15:30.");
                evento.consume();
            }
        });

        dialogo.showAndWait();
    }

    @Override
    public void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.initOwner(getScene().getWindow());
        alerta.setTitle("Reservas");
        alerta.setHeaderText("No se pudo realizar la operación");
        alerta.setContentText(mensaje);
        alerta.getDialogPane().setPrefWidth(550);
        alerta.showAndWait();
    }
}
package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.controladores.ControladorFolio;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.servicios.ServicioPanelReservas;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

public final class PanelFolio extends VBox
        implements ControladorFolio.VistaFolio {

    private final Label saldo = new Label();
    private final Label mensaje = new Label();
    private final TextField monto = new TextField();
    private final TextField descripcion = new TextField();

    private final TableView<MovimientoFolio> tabla = new TableView<>();
    private final ControladorFolio controlador;

    private final NumberFormat moneda =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-CO")
            );

    public PanelFolio(ServicioPanelReservas servicio, String codigo) {
        super(14);

        controlador = new ControladorFolio(servicio, codigo, this);

        saldo.getStyleClass().add("subtitulo");
        mensaje.setWrapText(true);

        monto.setPromptText("Valor: 65000.50");
        descripcion.setPromptText("Descripción del movimiento");
        descripcion.setPrefWidth(280);

        DateTimeFormatter fecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        agregarColumna("Código", MovimientoFolio::getCodigo);
        agregarColumna("Tipo", m -> m.getTipo().toString());
        agregarColumna("Valor", m -> moneda.format(m.getMonto()));
        agregarColumna("Descripción", MovimientoFolio::getDescripcion);
        agregarColumna("Fecha", m -> m.getFechaHora().format(fecha));
        agregarColumna(
                "Revierte a",
                m -> m.getCodigoMovimientoReversado() == null
                        ? ""
                        : m.getCodigoMovimientoReversado()
        );

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("El folio aún no tiene movimientos."));
        tabla.setPrefHeight(320);

        Button cargo = new Button("Registrar cargo");
        Button pago = new Button("Registrar pago");
        Button deshacer = new Button("Deshacer última operación");

        cargo.getStyleClass().add("accion-principal");
        pago.getStyleClass().add("accion-principal");

        cargo.setOnAction(evento -> registrar(true));
        pago.setOnAction(evento -> registrar(false));

        deshacer.getStyleClass().add("accion-secundaria");

        deshacer.setOnAction(evento -> {
            mensaje.setText("");
            controlador.deshacer();
        });

        Label aviso = new Label(
                "Valores sin separadores de miles. "
                        + "Deshacer conserva el original y agrega un movimiento contrario."
        );
        aviso.setWrapText(true);
        aviso.getStyleClass().add("texto-suave");

        getChildren().addAll(
                saldo,
                tabla,
                new FlowPane(10, 10, monto, descripcion),
                new FlowPane(10, 10, cargo, pago, deshacer),
                aviso,
                mensaje
        );

        VBox.setVgrow(tabla, Priority.ALWAYS);
        controlador.actualizar();
    }

    private void registrar(boolean esCargo) {
        mensaje.setText("");
        controlador.registrar(
                esCargo, monto.getText(), descripcion.getText()
        );
    }

    private void agregarColumna(
            String titulo,
            Function<MovimientoFolio, String> valor
    ) {
        TableColumn<MovimientoFolio, String> columna =
                new TableColumn<>(titulo);

        columna.setCellValueFactory(celda ->
                new ReadOnlyStringWrapper(valor.apply(celda.getValue()))
        );

        tabla.getColumns().add(columna);
    }

    @Override
    public void mostrar(
            BigDecimal valor,
            MovimientoFolio[] movimientos
    ) {
        tabla.getItems().setAll(movimientos);

        if (valor.signum() > 0) {
            saldo.setText("Saldo pendiente: " + moneda.format(valor));
        } else if (valor.signum() < 0) {
            saldo.setText(
                    "Saldo a favor: " + moneda.format(valor.abs())
            );
        } else {
            saldo.setText("Cuenta saldada: " + moneda.format(valor));
        }

        monto.clear();
        descripcion.clear();
    }

    @Override
    public void mostrarError(String texto) {
        mensaje.setText(texto);
    }
}

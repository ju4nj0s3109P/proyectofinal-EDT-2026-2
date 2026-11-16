package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.controladores.ControladorTablero;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.servicios.ServicioApartamentos;
import co.edu.uq.sga.servicios.ServicioApartamentos.ResumenApartamento;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class PanelTablero extends BorderPane
        implements ControladorTablero.VistaTablero {

    private final FlowPane tarjetas = new FlowPane(16, 16);
    private final Label resumen = new Label();
    private final ControladorTablero controlador;

    public PanelTablero(ServicioApartamentos servicio) {
        controlador = new ControladorTablero(servicio, this);

        resumen.getStyleClass().add("texto-suave");

        Button actualizar = new Button("Actualizar estados");
        actualizar.getStyleClass().add("accion-principal");
        actualizar.setOnAction(evento -> controlador.actualizar());

        HBox cabecera = new HBox(20, actualizar, resumen);
        cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cabecera.setPadding(new javafx.geometry.Insets(0, 0, 20, 0));

        tarjetas.getStyleClass().add("tarjetas-apartamentos");

        ScrollPane scroll = new ScrollPane(tarjetas);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("tablero-scroll");

        setTop(cabecera);
        setCenter(scroll);

        actualizar();
    }

    public void actualizar() {
        controlador.actualizar();
    }

    @Override
    public void mostrarApartamentos(ResumenApartamento[] apartamentos) {
        tarjetas.getChildren().clear();

        resumen.setText(apartamentos.length + " apartamentos registrados");

        if (apartamentos.length == 0) {
            tarjetas.getChildren().add(
                    new Label("No hay apartamentos cargados.")
            );
            return;
        }

        for (ResumenApartamento apartamento : apartamentos) {
            tarjetas.getChildren().add(crearTarjeta(apartamento));
        }
    }

    private VBox crearTarjeta(ResumenApartamento apartamento) {
        Label codigo = new Label(apartamento.codigo());
        codigo.getStyleClass().add("subtitulo");

        Label nombre = new Label(apartamento.nombre());
        nombre.setWrapText(true);

        Label ubicacion = new Label(
                "Bloque " + apartamento.bloque()
                        + " · Piso " + apartamento.piso()
        );
        ubicacion.getStyleClass().add("texto-suave");

        Label capacidad = new Label(
                apartamento.capacidad() + " personas · "
                        + apartamento.dormitorios() + " dormitorios"
        );
        capacidad.setWrapText(true);

        Label estado = new Label(textoEstado(apartamento.estado()));
        estado.setWrapText(true);
        estado.getStyleClass().add("estado-apartamento");
        estado.getStyleClass().add(claseEstado(apartamento.estado()));

        VBox tarjeta = new VBox(
                12, codigo, nombre, ubicacion, capacidad, estado
        );

        tarjeta.setPrefWidth(260);
        tarjeta.setMinWidth(260);
        tarjeta.setMaxWidth(260);
        tarjeta.getStyleClass().add("tarjeta-apartamento");

        if (apartamento.estado() == EstadoApartamento.FUERA_DE_SERVICIO) {
            Label motivo = new Label(
                    "Motivo: " + apartamento.motivoFueraDeServicio()
            );
            motivo.setWrapText(true);
            motivo.getStyleClass().add("texto-suave");
            tarjeta.getChildren().add(motivo);
        }

        return tarjeta;
    }

    private String textoEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> "Preparado";
            case OCUPADO -> "Ocupado";
            case PENDIENTE_PREPARACION -> "Pendiente de preparación";
            case EN_PREPARACION -> "En preparación";
            case FUERA_DE_SERVICIO -> "Fuera de servicio";
        };
    }

    private String claseEstado(EstadoApartamento estado) {
        return switch (estado) {
            case PREPARADO -> "estado-preparado";
            case OCUPADO -> "estado-ocupado";
            case PENDIENTE_PREPARACION, EN_PREPARACION -> "estado-pendiente";
            case FUERA_DE_SERVICIO -> "estado-fuera-servicio";
        };
    }
}
package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Indicadores operativos derivados del catálogo actual, sin duplicar reglas de negocio. */
public final class PanelEstadisticas extends VBox {
    private final ServicioPanelReservas servicio;
    private final Label total = new Label();
    private final Label pendientes = new Label();
    private final Label confirmadas = new Label();
    private final Label enCurso = new Label();
    private final Label finalizadas = new Label();
    private final Label canceladas = new Label();

    public PanelEstadisticas(ServicioPanelReservas servicio) {
        super(16);
        this.servicio = servicio;
        getChildren().addAll(
                new Label("Indicadores de reservas"),
                new FlowPane(12, 12, tarjeta("Reservas registradas", total),
                        tarjeta("Pendientes", pendientes), tarjeta("Confirmadas", confirmadas),
                        tarjeta("En curso", enCurso), tarjeta("Finalizadas", finalizadas),
                        tarjeta("Canceladas / no-show", canceladas)));
        Button actualizar = new Button("Actualizar indicadores");
        actualizar.getStyleClass().add("accion-principal");
        actualizar.setOnAction(e -> actualizar());
        getChildren().add(actualizar);
        actualizar();
    }

    public void actualizar() {
        ResumenReserva[] reservas = servicio.consultarTodas();
        int p=0, c=0, curso=0, f=0, cancel=0;
        for (ResumenReserva r : reservas) {
            switch (r.estado()) {
                case PENDIENTE -> p++;
                case CONFIRMADA -> c++;
                case EN_CURSO -> curso++;
                case FINALIZADA -> f++;
                case CANCELADA, NO_SHOW, RECHAZADA -> cancel++;
            }
        }
        total.setText(Integer.toString(reservas.length));
        pendientes.setText(Integer.toString(p));
        confirmadas.setText(Integer.toString(c));
        enCurso.setText(Integer.toString(curso));
        finalizadas.setText(Integer.toString(f));
        canceladas.setText(Integer.toString(cancel));
    }

    private VBox tarjeta(String titulo, Label valor) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("texto-suave");
        valor.getStyleClass().add("titulo");
        VBox tarjeta = new VBox(8, encabezado, valor);
        tarjeta.getStyleClass().add("tarjeta-apartamento");
        tarjeta.setPrefWidth(210);
        return tarjeta;
    }
}

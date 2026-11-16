package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.servicios.ServicioMapa.Ubicacion;
import co.edu.uq.sga.servicios.ServicioMapa.Pasillo;
import co.edu.uq.sga.servicios.ServicioMapa;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.servicios.ServicioApartamentos.ResumenApartamento;

import java.text.DecimalFormat;
import java.util.function.Consumer;

/** Dibuja una distribución esquemática y comunica qué nodo selecciona el usuario. */
public final class PanelMapa extends Pane {
    private static final PseudoClass EN_RUTA =
            PseudoClass.getPseudoClass("en-ruta");

    private static final PseudoClass DESTINO =
            PseudoClass.getPseudoClass("destino");

    private static final PseudoClass PREPARADO =
            PseudoClass.getPseudoClass("preparado");

    private static final PseudoClass OCUPADO =
            PseudoClass.getPseudoClass("ocupado");

    private static final PseudoClass PENDIENTE =
            PseudoClass.getPseudoClass("pendiente");

    private static final PseudoClass FUERA_SERVICIO =
            PseudoClass.getPseudoClass("fuera-servicio");

    private final ServicioMapa.Ubicacion[] ubicaciones;
    private final ServicioMapa.Pasillo[] pasillos;

    private final Button[] nodos;
    private final Line[] conexiones;
    private final Label[] distancias;

    private final int[] origenes;
    private final int[] destinos;

    public PanelMapa(
            ServicioMapa.Ubicacion[] ubicaciones,
            ServicioMapa.Pasillo[] pasillos,
            Consumer<String> alSeleccionar
    ) {
        this.ubicaciones = ubicaciones.clone();
        this.pasillos = pasillos.clone();

        nodos = new Button[ubicaciones.length];
        conexiones = new Line[pasillos.length];
        distancias = new Label[pasillos.length];

        origenes = new int[pasillos.length];
        destinos = new int[pasillos.length];

        getStyleClass().add("mapa");
        setId("mapa");

        setMinSize(690, 450);
        setPrefSize(760, 540);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int i = 0; i < pasillos.length; i++) {
            Pasillo pasillo = pasillos[i];

            origenes[i] = indiceDe(pasillo.origen());
            destinos[i] = indiceDe(pasillo.destino());

            if (origenes[i] < 0 || destinos[i] < 0) {
                throw new IllegalArgumentException(
                        "El pasillo referencia una ubicación desconocida."
                );
            }

            Line conexion = new Line();
            conexion.getStyleClass().add("pasillo");
            conexion.setMouseTransparent(true);
            conexion.setManaged(false);

            conexiones[i] = conexion;
            getChildren().add(conexion);
        }

        DecimalFormat formato = new DecimalFormat("0.########");

        for (int i = 0; i < pasillos.length; i++) {
            Label peso = new Label(
                    formato.format(pasillos[i].metros()) + " m"
            );

            peso.getStyleClass().add("peso-pasillo");
            peso.setMouseTransparent(true);
            peso.setManaged(false);

            distancias[i] = peso;
            getChildren().add(peso);
        }

        for (int i = 0; i < ubicaciones.length; i++) {
            Ubicacion ubicacion = ubicaciones[i];

            Button nodo = new Button(
                    ubicacion.id() + "\n" + ubicacion.nombre()
            );

            nodo.getStyleClass().add("nodo");
            nodo.setId("nodo-" + ubicacion.id());
            nodo.setWrapText(true);
            nodo.setManaged(false);

            nodo.setAccessibleText(
                    ubicacion.nombre() + ", calcular ruta desde recepción"
            );

            nodo.setTooltip(new Tooltip(
                    ubicacion.tipo().replace('_', ' ')
            ));

            nodo.setOnAction(
                    evento -> alSeleccionar.accept(ubicacion.id())
            );

            nodos[i] = nodo;
            getChildren().add(nodo);
        }
    }

    public void mostrarRuta(String destino, String[] ruta) {
        for (int i = 0; i < nodos.length; i++) {
            nodos[i].pseudoClassStateChanged(
                    DESTINO,
                    ubicaciones[i].id().equals(destino)
            );

            nodos[i].pseudoClassStateChanged(
                    EN_RUTA,
                    contiene(ruta, ubicaciones[i].id())
            );
        }

        for (int i = 0; i < pasillos.length; i++) {
            boolean activo = perteneceARuta(ruta, pasillos[i]);

            conexiones[i].pseudoClassStateChanged(EN_RUTA, activo);
            distancias[i].pseudoClassStateChanged(EN_RUTA, activo);
        }
    }

    @Override
    protected void layoutChildren() {
        double centroX = getWidth() / 2;
        double centroY = getHeight() / 2;

        double radioX = Math.max(0, centroX - 110);
        double radioY = Math.max(0, centroY - 65);

        for (int i = 0; i < nodos.length; i++) {
            double angulo = Math.PI + 2 * Math.PI * i / nodos.length;

            double x = centroX + radioX * Math.cos(angulo) - 82;
            double y = centroY + radioY * Math.sin(angulo) - 36;

            nodos[i].resizeRelocate(x, y, 164, 72);
        }

        for (int i = 0; i < conexiones.length; i++) {
            Button origen = nodos[origenes[i]];
            Button destino = nodos[destinos[i]];

            double x1 = origen.getLayoutX() + origen.getWidth() / 2;
            double y1 = origen.getLayoutY() + origen.getHeight() / 2;

            double x2 = destino.getLayoutX() + destino.getWidth() / 2;
            double y2 = destino.getLayoutY() + destino.getHeight() / 2;

            conexiones[i].setStartX(x1);
            conexiones[i].setStartY(y1);
            conexiones[i].setEndX(x2);
            conexiones[i].setEndY(y2);

            distancias[i].autosize();

            distancias[i].relocate(
                    (x1 + x2 - distancias[i].getWidth()) / 2,
                    (y1 + y2 - distancias[i].getHeight()) / 2
            );
        }
    }

    private int indiceDe(String codigo) {
        for (int i = 0; i < ubicaciones.length; i++) {
            if (ubicaciones[i].id().equals(codigo)) return i;
        }
        return -1;
    }

    private boolean perteneceARuta(String[] ruta, Pasillo pasillo) {
        for (int i = 1; i < ruta.length; i++) {
            boolean mismoSentido =
                    ruta[i - 1].equals(pasillo.origen())
                            && ruta[i].equals(pasillo.destino());

            boolean sentidoContrario =
                    ruta[i - 1].equals(pasillo.destino())
                            && ruta[i].equals(pasillo.origen());

            if (mismoSentido || sentidoContrario) {
                return true;
            }
        }

        return false;
    }

    private boolean contiene(String[] ruta, String codigo) {
        for (String paso : ruta) {
            if (paso.equals(codigo)) {
                return true;
            }
        }

        return false;
    }

    public void actualizarEstados(ResumenApartamento[] apartamentos) {
        for (int i = 0; i < nodos.length; i++) {
            Button nodo = nodos[i];

            nodo.pseudoClassStateChanged(PREPARADO, false);
            nodo.pseudoClassStateChanged(OCUPADO, false);
            nodo.pseudoClassStateChanged(PENDIENTE, false);
            nodo.pseudoClassStateChanged(FUERA_SERVICIO, false);

            nodo.setTooltip(new Tooltip(
                    ubicaciones[i].tipo().replace('_', ' ')
            ));

            nodo.setAccessibleText(
                    ubicaciones[i].nombre()
                            + ", calcular ruta desde recepción"
            );
        }

        for (ResumenApartamento apartamento : apartamentos) {
            int indice = indiceDe(apartamento.codigo());

            if (indice < 0
                    || !"APARTAMENTO".equals(ubicaciones[indice].tipo())) {
                continue;
            }

            Button nodo = nodos[indice];

            PseudoClass estilo = switch (apartamento.estado()) {
                case PREPARADO -> PREPARADO;
                case OCUPADO -> OCUPADO;
                case PENDIENTE_PREPARACION, EN_PREPARACION -> PENDIENTE;
                case FUERA_DE_SERVICIO -> FUERA_SERVICIO;
            };

            String estado = switch (apartamento.estado()) {
                case PREPARADO -> "Preparado";
                case OCUPADO -> "Ocupado";
                case PENDIENTE_PREPARACION -> "Pendiente de preparación";
                case EN_PREPARACION -> "En preparación";
                case FUERA_DE_SERVICIO -> "Fuera de servicio";
            };

            nodo.pseudoClassStateChanged(estilo, true);

            String detalle = apartamento.nombre() + "\nEstado: " + estado;

            if (apartamento.estado() == EstadoApartamento.FUERA_DE_SERVICIO) {
                detalle += "\nMotivo: " + apartamento.motivoFueraDeServicio();
            }

            nodo.setTooltip(new Tooltip(detalle));

            nodo.setAccessibleText(
                    apartamento.nombre() + ", " + estado
                            + ", calcular ruta desde recepción"
            );
        }
    }
}

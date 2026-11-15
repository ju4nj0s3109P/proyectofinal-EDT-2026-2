package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio;
import co.edu.uq.sga.servicios.ServicioMapa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class AplicacionSGA extends Application {

    private ServicioMapa servicio;
    private String errorCarga;

    @Override
    public void init() {
        Path archivo = Path.of("data", "mapa-edificio.txt");

        try {
            CargadorMapaEdificio cargador = new CargadorMapaEdificio();
            var mapa = cargador.cargar(archivo);

            ServicioMapa.Ubicacion[] ubicaciones =
                    new ServicioMapa.Ubicacion[mapa.ubicaciones().tamanio()];

            int indice = 0;

            for (var ubicacion : mapa.ubicaciones()) {
                ubicaciones[indice++] = new ServicioMapa.Ubicacion(
                        ubicacion.id(),
                        ubicacion.tipo(),
                        ubicacion.nombre()
                );
            }

            ServicioMapa.Pasillo[] pasillos =
                    new ServicioMapa.Pasillo[mapa.grafo().cantidadAristas()];

            indice = 0;

            for (var arista : mapa.grafo().aristas()) {
                pasillos[indice++] = new ServicioMapa.Pasillo(
                        arista.getOrigen(),
                        arista.getDestino(),
                        arista.getPeso()
                );
            }

            servicio = new ServicioMapa(
                    ubicaciones,
                    pasillos,
                    new CalculadorRutasGrafo(mapa.grafo())
            );

            servicio.consultarRecepcion();

        } catch (IOException | IllegalStateException e) {
            errorCarga = "Archivo: " + archivo.toAbsolutePath()
                    + "\n\n" + e.getMessage();
        }
    }

    @Override
    public void start(Stage escenario) {
        if (errorCarga != null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al iniciar el SGA");
            alerta.setHeaderText("No se pudo cargar el mapa del edificio");
            alerta.setContentText(errorCarga);
            alerta.showAndWait();

            Platform.exit();
            return;
        }

        VentanaPrincipal ventana = new VentanaPrincipal(servicio);
        Scene escena = new Scene(ventana, 1280, 800);

        String estilos = Objects.requireNonNull(
                AplicacionSGA.class.getResource(
                        "/co/edu/uq/sga/interfaz/estilos.css"
                ),
                "No se encontró el archivo estilos.css"
        ).toExternalForm();

        escena.getStylesheets().add(estilos);

        escenario.setTitle("SGA | Aparta-Suites UQ");
        escenario.setMinWidth(1000);
        escenario.setMinHeight(660);
        escenario.setScene(escena);
        escenario.show();
        escenario.centerOnScreen();
    }
}
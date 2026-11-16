package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio;
import co.edu.uq.sga.persistencia.CargadorApartamentos;
import co.edu.uq.sga.servicios.ServicioMapa;
import co.edu.uq.sga.servicios.ServicioApartamentos;

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
    private ServicioApartamentos servicioApartamentos;
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

            Path archivoApartamentos = Path.of("data", "apartamentos.csv");

            var catalogo = new CargadorApartamentos().cargar(archivoApartamentos);

            validarApartamentosDelMapa(catalogo, ubicaciones);

            this.servicioApartamentos = new ServicioApartamentos(catalogo);

        } catch (IOException | IllegalStateException e) {
            errorCarga = "No se pudieron cargar los datos iniciales.\n\n"
                    + e.getMessage();
        }
    }

    @Override
    public void start(Stage escenario) {
        if (errorCarga != null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al iniciar el SGA");
            alerta.setHeaderText("No se pudo iniciar el alojamiento");
            alerta.setContentText(errorCarga);
            alerta.getDialogPane().setPrefWidth(620);
            alerta.setResizable(true);
            alerta.showAndWait();

            Platform.exit();
            return;
        }

        VentanaPrincipal ventana = new VentanaPrincipal(servicio, servicioApartamentos);
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
    private void validarApartamentosDelMapa(
            CatalogoApartamentosArbol catalogo,
            ServicioMapa.Ubicacion[] ubicaciones
    ) {
        for (var apartamento : catalogo.listarPorCodigo()) {
            boolean encontrado = false;

            for (var ubicacion : ubicaciones) {
                if (apartamento.getCodigo().equals(ubicacion.id())
                        && "APARTAMENTO".equals(ubicacion.tipo())) {
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new IllegalStateException(
                        "El apartamento " + apartamento.getCodigo()
                                + " está en el CSV, pero no tiene un nodo "
                                + "de tipo APARTAMENTO en el mapa."
                );
            }
        }

        for (var ubicacion : ubicaciones) {
            if ("APARTAMENTO".equals(ubicacion.tipo())
                    && catalogo.buscarPorCodigo(ubicacion.id()).isEmpty()) {
                throw new IllegalStateException(
                        "El nodo " + ubicacion.id()
                                + " está en el mapa como apartamento, "
                                + "pero no existe en apartamentos.csv."
                );
            }
        }
    }
}
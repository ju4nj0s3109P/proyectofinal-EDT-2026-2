package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoEmpleadosArbol;
import co.edu.uq.sga.adaptadores.ColaReservasPendientesPorVencimiento;
import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.adaptadores.CatalogoReservasArbol;
import co.edu.uq.sga.adaptadores.IndiceReservasArbol;
import co.edu.uq.sga.adaptadores.RepositorioFoliosArbol;
import co.edu.uq.sga.adaptadores.ColaSolicitudesExternasEnCola;
import co.edu.uq.sga.adaptadores.RegistroConflictosEnLista;
import co.edu.uq.sga.adaptadores.PilaDeshacerFoliosArbol;
import co.edu.uq.sga.adaptadores.ReservasDelIndice;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio;
import co.edu.uq.sga.persistencia.CargadorApartamentos;
import co.edu.uq.sga.persistencia.CargadorPreparaciones;
import co.edu.uq.sga.persistencia.CargadorHuespedes;
import co.edu.uq.sga.persistencia.CargadorTemporadas;
import co.edu.uq.sga.persistencia.CargadorReservas;
import co.edu.uq.sga.persistencia.CargadorEmpleados;
import co.edu.uq.sga.persistencia.CargadorTurnosRecepcion;
import co.edu.uq.sga.persistencia.CargadorPerfilesHuespedes;
import co.edu.uq.sga.persistencia.CargadorMovimientosFolios;
import co.edu.uq.sga.servicios.ServicioTarifas;
import co.edu.uq.sga.servicios.ServicioMapa;
import co.edu.uq.sga.servicios.ServicioApartamentos;
import co.edu.uq.sga.servicios.ServicioPanelPreparacion;
import co.edu.uq.sga.servicios.ServicioReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioFolios;
import co.edu.uq.sga.servicios.ServicioDisponibilidad;
import co.edu.uq.sga.servicios.ServicioEmpleados;
import co.edu.uq.sga.servicios.ServicioTurnosRecepcion;
import co.edu.uq.sga.servicios.ServicioVencimientoReservas;
import co.edu.uq.sga.servicios.ServicioReservasPortal;
import co.edu.uq.sga.servicios.ServicioSolicitudesExternas;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDate;
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
    private ServicioPanelPreparacion servicioPreparacion;
    private CatalogoHuespedesArbol catalogoHuespedes;
    private ServicioPanelReservas servicioPanelReservas;
    private ServicioTarifas servicioTarifas;
    private ServicioReservas servicioReservas;
    private ServicioReservasPortal servicioPortal;
    private ServicioEmpleados servicioEmpleados;
    private ServicioTurnosRecepcion servicioTurnos;
    private Timeline relojVencimientos;
    private CatalogoApartamentosArbol catalogoApartamentos;

    @Override
    public void init() {
        Path archivoUbicaciones = Path.of("data", "ubicaciones.csv");
        Path archivoPasillos = Path.of("data", "pasillos.csv");
        try {
            servicioTarifas = new CargadorTemporadas().cargar(Path.of("data", "temporadas.csv"));
            var mapa = new CargadorMapaEdificio().cargar(archivoUbicaciones, archivoPasillos);
            ServicioMapa.Ubicacion[] ubicaciones = new ServicioMapa.Ubicacion[mapa.ubicaciones().tamanio()];
            int indice = 0;
            for (var ubicacion : mapa.ubicaciones()) {
                ubicaciones[indice++] = new ServicioMapa.Ubicacion(ubicacion.id(), ubicacion.tipo(), ubicacion.nombre());
            }
            ServicioMapa.Pasillo[] pasillos = new ServicioMapa.Pasillo[mapa.grafo().cantidadAristas()];
            indice = 0;
            for (var arista : mapa.grafo().aristas()) {
                pasillos[indice++] = new ServicioMapa.Pasillo(arista.getOrigen(), arista.getDestino(), arista.getPeso());
            }
            servicio = new ServicioMapa(ubicaciones, pasillos, new CalculadorRutasGrafo(mapa.grafo()));
            servicio.consultarRecepcion();

            catalogoApartamentos = new CargadorApartamentos().cargar(Path.of("data", "apartamentos.csv"));
            validarApartamentosDelMapa(catalogoApartamentos, ubicaciones);
            servicioApartamentos = new ServicioApartamentos(catalogoApartamentos);
            catalogoHuespedes = new CargadorHuespedes().cargar(Path.of("data", "huespedes.csv"));

            var indiceReservas = new IndiceReservasArbol();
            var catalogoReservas = new CatalogoReservasArbol();
            servicioReservas = new ServicioReservas(indiceReservas);
            var colaVencimientos = new ColaReservasPendientesPorVencimiento(
                    ServicioVencimientoReservas.comparadorPorVencimiento());
            var servicioVencimientos = new ServicioVencimientoReservas(servicioReservas, colaVencimientos);
            servicioPortal = new ServicioReservasPortal(servicioReservas, servicioVencimientos);
            servicioEmpleados = new ServicioEmpleados(new CatalogoEmpleadosArbol());
            servicioTurnos = new ServicioTurnosRecepcion();
            new CargadorEmpleados().cargar(Path.of("data", "empleados.csv"), servicioEmpleados);
            new CargadorTurnosRecepcion().cargar(
                    Path.of("data", "turnos-recepcion.csv"), servicioEmpleados, servicioTurnos);

            var servicioDisponibilidad = new ServicioDisponibilidad(new ReservasDelIndice(indiceReservas));
            servicioPreparacion = new ServicioPanelPreparacion(
                    catalogoApartamentos, new CalculadorRutasGrafo(mapa.grafo()), LocalDate.now());
            var servicioFolios = new ServicioFolios(new RepositorioFoliosArbol(), new PilaDeshacerFoliosArbol());

            var reservasIniciales = new CargadorReservas().cargar(
                    Path.of("data", "reservas.csv"), catalogoApartamentos, catalogoHuespedes);
            for (var cargada : reservasIniciales) {
                try {
                    catalogoReservas.registrarDirectaCargada(
                            cargada.reserva(), cargada.llegadaEstimada(), indiceReservas);
                    servicioFolios.abrir(cargada.reserva());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    throw new IOException("No se pudo incorporar la reserva "
                            + cargada.reserva().getFolio() + ": " + e.getMessage(), e);
                }
            }

            new CargadorMovimientosFolios().cargar(
                    Path.of("data", "movimientos-folios.csv"), servicioFolios);
            new CargadorPerfilesHuespedes().cargar(
                    Path.of("data", "perfiles-huespedes.csv"), catalogoHuespedes,
                    catalogoApartamentos, servicioReservas.getServicioPerfilesHuesped());

            servicioPanelReservas = new ServicioPanelReservas(
                    catalogoApartamentos, catalogoHuespedes, catalogoReservas,
                    servicioReservas, servicioPreparacion, servicioFolios,
                    servicioTarifas, servicioDisponibilidad, servicioPortal);
            var registroConflictos = new RegistroConflictosEnLista();
            var servicioSolicitudesExternas = new ServicioSolicitudesExternas(
                    servicioReservas, new ColaSolicitudesExternasEnCola(), registroConflictos);
            servicioPanelReservas.configurarSolicitudesExternas(servicioSolicitudesExternas, registroConflictos);

            var tareasIniciales = new CargadorPreparaciones().cargar(
                    Path.of("data", "preparaciones.csv"),
                    servicioPreparacion.consultarFechaOperacion(), catalogoApartamentos);
            for (var tarea : tareasIniciales) {
                servicioPreparacion.encolarPendiente(tarea.apartamento(), tarea.llegada());
            }
        } catch (IOException | IllegalStateException e) {
            errorCarga = "No se pudieron cargar los datos iniciales.\n\n" + e.getMessage();
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
        var vistaAdministracion = new PanelAdministracion(servicioEmpleados, servicioTurnos,
                servicioApartamentos, catalogoHuespedes, servicioTarifas);
        var vistaPortal = new PanelPortalHuespedes(catalogoHuespedes, catalogoApartamentos,
                servicioReservas.getServicioPerfilesHuesped(), servicioPanelReservas);
        var vistaCalendario = new PanelCalendario(servicioPanelReservas);
        var vistaEstadisticas = new PanelEstadisticas(servicioPanelReservas);
        var vistaFinanzas = new PanelGestionFinanciera(servicioPanelReservas);
        VentanaPrincipal ventana = new VentanaPrincipal(servicio, servicioApartamentos,
                servicioPreparacion, servicioPanelReservas, vistaAdministracion,
                vistaPortal, vistaCalendario, vistaEstadisticas, vistaFinanzas);
        Scene escena = new Scene(ventana, 1280, 800);
        String estilos = Objects.requireNonNull(
                AplicacionSGA.class.getResource("/co/edu/uq/sga/interfaz/estilos.css"),
                "No se encontró el archivo estilos.css").toExternalForm();
        escena.getStylesheets().add(estilos);
        escenario.setTitle("SGA | Aparta-Suites UQ");
        escenario.setMinWidth(1000);
        escenario.setMinHeight(660);
        escenario.setScene(escena);
        escenario.show();
        escenario.centerOnScreen();

        relojVencimientos = new Timeline(new KeyFrame(Duration.seconds(30), evento -> {
            try {
                int cantidad = servicioPanelReservas.procesarVencidas();
                if (cantidad > 0) ventana.actualizarReservas();
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("No se pudieron procesar los vencimientos: " + e.getMessage());
            }
        }));
        relojVencimientos.setCycleCount(Timeline.INDEFINITE);
        relojVencimientos.play();
    }

    @Override
    public void stop() {
        if (relojVencimientos != null) relojVencimientos.stop();
    }

    private void validarApartamentosDelMapa(
            CatalogoApartamentosArbol catalogo, ServicioMapa.Ubicacion[] ubicaciones) {
        for (var apartamento : catalogo.listarPorCodigo()) {
            boolean encontrado = false;
            for (var ubicacion : ubicaciones) {
                if (apartamento.getCodigo().equals(ubicacion.id())
                        && "APARTAMENTO".equals(ubicacion.tipo())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) throw new IllegalStateException(
                    "El apartamento " + apartamento.getCodigo()
                            + " está en el CSV, pero no tiene un nodo de tipo APARTAMENTO en el mapa.");
        }
        for (var ubicacion : ubicaciones) {
            if ("APARTAMENTO".equals(ubicacion.tipo())
                    && catalogo.buscarPorCodigo(ubicacion.id()).isEmpty()) {
                throw new IllegalStateException(
                        "El nodo " + ubicacion.id()
                                + " está en el mapa como apartamento, pero no existe en apartamentos.csv.");
            }
        }
    }
}
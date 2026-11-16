package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.servicios.ServicioPerfilesHuesped;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Consulta de perfil, favoritos e historial del huésped registrado. */
public final class PanelPortalHuespedes extends BorderPane {
    private final CatalogoHuespedesArbol huespedes;
    private final CatalogoApartamentosArbol apartamentos;
    private final ServicioPerfilesHuesped perfiles;
    private final TextField documento = new TextField();
    private final ListView<String> favoritos = new ListView<>();
    private final ListView<String> historial = new ListView<>();
    private final ComboBox<String> seleccionarApartamento = new ComboBox<>();
    private final Label mensaje = new Label();
    private Huesped huespedActual;

    public PanelPortalHuespedes(CatalogoHuespedesArbol huespedes,
            CatalogoApartamentosArbol apartamentos, ServicioPerfilesHuesped perfiles) {
        this.huespedes = huespedes;
        this.apartamentos = apartamentos;
        this.perfiles = perfiles;
        documento.setPromptText("Documento del huésped");
        seleccionarApartamento.setPromptText("Apartamento para favoritos");
        for (Apartamento a : apartamentos.listarPorCodigo()) {
            seleccionarApartamento.getItems().add(a.getCodigo() + " · " + a.getNombre());
        }
        Button consultar = new Button("Consultar perfil");
        Button agregar = new Button("Agregar favorito");
        Button quitar = new Button("Quitar favorito");
        consultar.setOnAction(e -> consultarPerfil());
        agregar.setOnAction(e -> agregarFavorito());
        quitar.setOnAction(e -> quitarFavorito());
        FlowPane acciones = new FlowPane(10, 10, documento, consultar);
        VBox panelFavoritos = new VBox(8, new Label("Apartamentos favoritos"),
                favoritos, new FlowPane(8, 8, seleccionarApartamento, agregar, quitar));
        VBox panelHistorial = new VBox(8, new Label("Estancias finalizadas"), historial);
        panelFavoritos.setPrefWidth(420);
        panelHistorial.setPrefWidth(420);
        FlowPane contenido = new FlowPane(18, 18, panelFavoritos, panelHistorial);
        VBox superior = new VBox(12, new Label("Portal de huéspedes"), acciones, mensaje);
        setTop(superior);
        setCenter(contenido);
        favoritos.setPrefHeight(320);
        historial.setPrefHeight(320);
    }

    private void consultarPerfil() {
        mensaje.setText("");
        if (documento.getText().isBlank()) {
            mensaje.setText("Escribe el documento del huésped.");
            return;
        }
        huespedActual = huespedes.buscarPorDocumento(documento.getText().trim()).orElse(null);
        if (huespedActual == null) {
            favoritos.getItems().clear();
            historial.getItems().clear();
            mensaje.setText("No existe un huésped con ese documento.");
            return;
        }
        favoritos.getItems().clear();
        for (Apartamento a : perfiles.consultarFavoritos(huespedActual)) {
            favoritos.getItems().add(a.getCodigo() + " · " + a.getNombre());
        }
        historial.getItems().clear();
        for (Reserva r : perfiles.consultarHistorial(huespedActual)) {
            historial.getItems().add(r.getFolio() + " · "
                    + r.getEstancia().getFechaEntrada() + " a "
                    + r.getEstancia().getFechaSalida() + " · "
                    + r.getEstado());
        }
        mensaje.setText("Perfil: " + huespedActual.getNombre());
    }

    private void agregarFavorito() {
        if (!validarPerfil()) return;
        String seleccionado = seleccionarApartamento.getValue();
        if (seleccionado == null) {
            mensaje.setText("Selecciona un apartamento.");
            return;
        }
        String codigo = seleccionado.split(" · ", 2)[0];
        Apartamento apartamento = null;
        for (Apartamento a : apartamentos.listarPorCodigo()) {
            if (a.getCodigo().equals(codigo)) { apartamento = a; break; }
        }
        if (apartamento == null) {
            mensaje.setText("El apartamento seleccionado ya no está disponible.");
            return;
        }
        perfiles.agregarFavorito(huespedActual, apartamento);
        consultarPerfil();
    }

    private void quitarFavorito() {
        if (!validarPerfil()) return;
        String seleccionado = favoritos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje.setText("Selecciona un favorito para quitarlo.");
            return;
        }
        String codigo = seleccionado.split(" · ", 2)[0];
        perfiles.quitarFavorito(huespedActual, codigo);
        consultarPerfil();
    }

    private boolean validarPerfil() {
        if (huespedActual == null) {
            mensaje.setText("Primero consulta un perfil de huésped.");
            return false;
        }
        return true;
    }
}

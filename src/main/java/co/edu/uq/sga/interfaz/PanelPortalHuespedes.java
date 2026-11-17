package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.servicios.ServicioPerfilesHuesped;
import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioFolios;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Consulta de perfil, favoritos e historial del huésped registrado. */
public final class PanelPortalHuespedes extends BorderPane {
    private final CatalogoHuespedesArbol huespedes;
    private final CatalogoApartamentosArbol apartamentos;
    private final ServicioPerfilesHuesped perfiles;
    private final ServicioPanelReservas reservas;
    private final TextField documento = new TextField();
    private final ListView<String> favoritos = new ListView<>();
    private final ListView<String> historial = new ListView<>();
    private final ListView<String> reservasActivas = new ListView<>();
    private final ComboBox<String> seleccionarApartamento = new ComboBox<>();
    private final Label mensaje = new Label();
    private Huesped huespedActual;

    public PanelPortalHuespedes(CatalogoHuespedesArbol huespedes,
            CatalogoApartamentosArbol apartamentos, ServicioPerfilesHuesped perfiles,
            ServicioPanelReservas reservas) {
        this.huespedes = huespedes;
        this.apartamentos = apartamentos;
        this.perfiles = perfiles;
        this.reservas = reservas;
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
        Button confirmarAnticipo = new Button("Pagar anticipo del 30 %");
        Button cancelarReserva = new Button("Cancelar reserva seleccionada");
        confirmarAnticipo.setOnAction(e -> confirmarAnticipo());
        cancelarReserva.setOnAction(e -> cancelarReserva());
        reservasActivas.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            String folio = extraerFolio(n);
            var reserva = folio == null ? null : buscarReserva(folio);
            confirmarAnticipo.setDisable(reserva == null || reserva.getCanal() != co.edu.uq.sga.dominio.enums.CanalReserva.PORTAL
                    || reserva.getEstado() != EstadoReserva.PENDIENTE);
            cancelarReserva.setDisable(reserva == null || (reserva.getEstado() != EstadoReserva.PENDIENTE
                    && reserva.getEstado() != EstadoReserva.CONFIRMADA));
        });
        confirmarAnticipo.setDisable(true);
        cancelarReserva.setDisable(true);
        VBox panelReservas = new VBox(8, new Label("Reservas activas y saldo de folio"), reservasActivas,
                new FlowPane(8, 8, confirmarAnticipo, cancelarReserva));
        panelFavoritos.setPrefWidth(420);
        panelHistorial.setPrefWidth(420);
        panelReservas.setPrefWidth(420);
        FlowPane contenido = new FlowPane(18, 18, panelFavoritos, panelHistorial, panelReservas);
        VBox superior = new VBox(12, new Label("Portal de huéspedes"), acciones, mensaje);
        setTop(superior);
        ScrollPane desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setPannable(true);
        setCenter(desplazamiento);
        favoritos.setPrefHeight(320);
        historial.setPrefHeight(320);
        reservasActivas.setPrefHeight(320);
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
            reservasActivas.getItems().clear();
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
        reservasActivas.getItems().clear();
        NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        for (Reserva r : reservas.consultarReservasDominio()) {
            if (!r.getTitular().getDocumento().equals(huespedActual.getDocumento())) continue;
            if (r.getEstado() == EstadoReserva.CANCELADA || r.getEstado() == EstadoReserva.RECHAZADA
                    || r.getEstado() == EstadoReserva.NO_SHOW || r.getEstado() == EstadoReserva.FINALIZADA) continue;
            var folio = reservas.consultarFolio(r.getFolio());
            reservasActivas.getItems().add(r.getFolio() + " · " + r.getEstado()
                    + " · " + r.getEstancia().getFechaEntrada() + " a " + r.getEstancia().getFechaSalida()
                    + " · Apartamentos: " + r.getAsignaciones().length
                    + " · Saldo: " + moneda.format(folio.calcularSaldo()));
        }
        if (reservasActivas.getItems().isEmpty()) reservasActivas.getItems().add("No hay reservas activas.");
        mensaje.setText("Perfil: " + huespedActual.getNombre());
    }

    private String extraerFolio(String seleccion) {
        if (seleccion == null || seleccion.isBlank() || seleccion.startsWith("No hay")) return null;
        int separador = seleccion.indexOf(" · ");
        return separador < 0 ? null : seleccion.substring(0, separador).trim();
    }

    private Reserva buscarReserva(String folio) {
        for (Reserva reserva : reservas.consultarReservasDominio()) {
            if (reserva.getFolio().equals(folio)) return reserva;
        }
        return null;
    }

    private void confirmarAnticipo() {
        String folio = extraerFolio(reservasActivas.getSelectionModel().getSelectedItem());
        if (folio == null) return;
        try {
            var cotizacion = reservas.cotizarReserva(folio);
            var anticipo = cotizacion.getTotal().multiply(new java.math.BigDecimal("0.30"));
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "Se registrará el anticipo del 30 % por "
                    + NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(anticipo)
                    + ". ¿Confirmar el pago?", ButtonType.OK, ButtonType.CANCEL);
            confirmacion.setTitle("Confirmar anticipo");
            confirmacion.setHeaderText("Reserva " + folio);
            confirmacion.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                try {
                    reservas.confirmarPortal(folio);
                    consultarPerfil();
                } catch (RuntimeException ex) {
                    mensaje.setText("No se pudo confirmar el anticipo: " + ex.getMessage());
                }
            });
        } catch (RuntimeException ex) {
            mensaje.setText("No se pudo cotizar la reserva: " + ex.getMessage());
        }
    }

    private void cancelarReserva() {
        String folio = extraerFolio(reservasActivas.getSelectionModel().getSelectedItem());
        if (folio == null) return;
        try {
            var monto = ServicioFolios.totalPagado(reservas.consultarFolio(folio));
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "La devolución estimada es "
                    + NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(monto)
                    + ". La reserva se cancelará y se registrarán los movimientos de reversión. ¿Continuar?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirmacion.setTitle("Cancelar reserva");
            confirmacion.setHeaderText("Reserva " + folio);
            confirmacion.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                try {
                    var devuelto = reservas.cancelarReserva(folio);
                    mensaje.setText("Reserva cancelada. Devolución registrada: "
                            + NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(devuelto));
                    consultarPerfil();
                } catch (RuntimeException ex) {
                    mensaje.setText("No se pudo cancelar la reserva: " + ex.getMessage());
                }
            });
        } catch (RuntimeException ex) {
            mensaje.setText("No se pudo calcular la devolución: " + ex.getMessage());
        }
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

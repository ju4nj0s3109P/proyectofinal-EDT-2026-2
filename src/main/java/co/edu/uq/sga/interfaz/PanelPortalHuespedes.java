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
import java.time.LocalDate;
import java.time.LocalTime;
import co.edu.uq.sga.servicios.ServicioPanelReservas.DatosOcupante;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ApartamentoDisponible;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
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
        Button nuevaReserva = new Button("Buscar y reservar");
        nuevaReserva.getStyleClass().add("accion-principal");
        nuevaReserva.setOnAction(e -> abrirSolicitudPortal());
        Button agregar = new Button("Agregar favorito");
        Button quitar = new Button("Quitar favorito");
        consultar.setOnAction(e -> consultarPerfil());
        agregar.setOnAction(e -> agregarFavorito());
        quitar.setOnAction(e -> quitarFavorito());
        FlowPane acciones = new FlowPane(10, 10, documento, consultar, nuevaReserva);
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

    private void abrirSolicitudPortal() {
        if (!validarPerfil()) return;
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Nueva reserva por Portal");
        dialogo.setHeaderText("Titular: " + huespedActual.getNombre());
        TextField folio = new TextField("POR-" + System.currentTimeMillis());
        DatePicker entrada = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker salida = new DatePicker(LocalDate.now().plusDays(2));
        DatePicker nacimiento = new DatePicker();
        TextField nombre = new TextField();
        TextField hora = new TextField("15:00");
        ComboBox<String> orden = new ComboBox<>();
        orden.getItems().addAll("Tarifa base", "Capacidad", "Dormitorios");
        orden.setValue("Tarifa base");
        ListView<DatosOcupante> ocupantes = new ListView<>();
        ocupantes.setPrefHeight(110);
        ocupantes.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(DatosOcupante dato, boolean vacio) {
                super.updateItem(dato, vacio);
                setText(vacio || dato == null ? null : dato.nombre() + " · " + dato.fechaNacimiento());
            }
        });
        ComboBox<ApartamentoDisponible> disponibles = new ComboBox<>();
        ListView<ApartamentoDisponible> grupoPropuesto = new ListView<>();
        grupoPropuesto.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        grupoPropuesto.setPrefHeight(95);
        grupoPropuesto.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(ApartamentoDisponible a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null : a.codigo() + " - " + a.nombre()
                        + " - capacidad " + a.capacidad() + " - tarifa " + a.tarifaBase());
            }
        });
        disponibles.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(ApartamentoDisponible a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null : a.codigo() + " · " + a.nombre()
                        + " · capacidad " + a.capacidad() + " · " + a.tarifaBase());
            }
        });
        disponibles.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(ApartamentoDisponible a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null : a.codigo() + " · " + a.nombre());
            }
        });
        disponibles.setMaxWidth(Double.MAX_VALUE);
        Label estado = new Label("Agrega ocupantes y busca disponibilidad.");
        entrada.setEditable(false); salida.setEditable(false); nacimiento.setEditable(false);
        entrada.valueProperty().addListener((o, a, n) -> { grupoPropuesto.getItems().clear(); disponibles.getItems().clear(); });
        salida.valueProperty().addListener((o, a, n) -> { grupoPropuesto.getItems().clear(); disponibles.getItems().clear(); });
        nombre.setPromptText("Nombre del ocupante");
        nacimiento.setPromptText("Fecha de nacimiento");
        Button agregar = new Button("Agregar ocupante");
        agregar.setOnAction(e -> {
            if (nombre.getText().isBlank() || nacimiento.getValue() == null) {
                estado.setText("Completa nombre y fecha de nacimiento."); return;
            }
            ocupantes.getItems().add(new DatosOcupante(nombre.getText().trim(), nacimiento.getValue()));
            grupoPropuesto.getItems().clear(); disponibles.getItems().clear();
            nombre.clear(); nacimiento.setValue(null);
            estado.setText("Ocupantes: " + ocupantes.getItems().size());
        });
        Button quitarOcupante = new Button("Quitar ocupante");
        quitarOcupante.setOnAction(e -> {
            int indice = ocupantes.getSelectionModel().getSelectedIndex();
            if (indice < 0) { estado.setText("Selecciona un ocupante para quitarlo."); return; }
            ocupantes.getItems().remove(indice); grupoPropuesto.getItems().clear(); disponibles.getItems().clear();
            estado.setText("Ocupantes: " + ocupantes.getItems().size());
        });
        Button buscar = new Button("Buscar disponibles");
        buscar.setOnAction(e -> buscarDisponiblesPortal(entrada, salida, orden, ocupantes, disponibles, grupoPropuesto, estado));
        Button proponer = new Button("Proponer varios apartamentos");
        proponer.setOnAction(e -> proponerGrupoPortal(entrada, salida, ocupantes, disponibles, grupoPropuesto, estado));
        Button cotizar = new Button("Cotizar estancia");
        cotizar.setOnAction(e -> cotizarPortal(entrada, salida, ocupantes, disponibles, grupoPropuesto, estado, dialogo));
        GridPane campos = new GridPane();
        campos.setHgap(8); campos.setVgap(8);
        campos.addRow(0, new Label("Folio"), folio);
        campos.addRow(1, new Label("Entrada"), entrada);
        campos.addRow(2, new Label("Salida"), salida);
        campos.addRow(3, new Label("Hora llegada"), hora);
        VBox contenido = new VBox(9, campos, new Label("Ocupantes"),
                new FlowPane(8, 8, nombre, nacimiento, agregar), ocupantes, quitarOcupante,
                new FlowPane(8, 8, new Label("Orden"), orden, buscar), disponibles,
                proponer, new Label("Apartamentos propuestos para el grupo"), grupoPropuesto,
                cotizar, estado);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(620);
        ButtonType enviar = new ButtonType("Enviar solicitud", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(enviar, ButtonType.CANCEL);
        Button registrar = (Button) dialogo.getDialogPane().lookupButton(enviar);
        registrar.getStyleClass().add("accion-principal");
        registrar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                validarFechasPortal(entrada, salida);
                DatosOcupante[] datos = construirDistribucionPortal(ocupantes, disponibles, grupoPropuesto);
                String[] codigos = codigosSeleccionadosPortal(disponibles, grupoPropuesto);
                if (folio.getText().isBlank()) throw new IllegalArgumentException("El folio es obligatorio.");
                reservas.registrarPortal(folio.getText().trim(), huespedActual.getDocumento(),
                        codigos, datos, entrada.getValue(), salida.getValue(),
                        LocalTime.parse(hora.getText().trim()));
                mensaje.setText("Solicitud registrada como pendiente. Registra el anticipo del 30 % antes de 24 horas.");
                consultarPerfil();
            } catch (RuntimeException ex) {
                estado.setText(ex.getMessage()); evento.consume();
            }
        });
        dialogo.showAndWait();
    }

    private void buscarDisponiblesPortal(DatePicker entrada, DatePicker salida, ComboBox<String> orden,
            ListView<DatosOcupante> ocupantes, ComboBox<ApartamentoDisponible> disponibles, ListView<ApartamentoDisponible> grupoPropuesto, Label estado) {
        try {
            validarFechasPortal(entrada, salida);
            grupoPropuesto.getItems().clear();
            if (ocupantes.getItems().isEmpty()) throw new IllegalArgumentException("Agrega al menos un ocupante.");
            var encontrados = reservas.buscarDisponibles(entrada.getValue(), salida.getValue(),
                    ocupantes.getItems().size(), orden.getValue());
            disponibles.getItems().setAll(encontrados);
            if (encontrados.length == 0) estado.setText("No hay un apartamento individual con capacidad suficiente. Prueba proponer varios.");
            else { disponibles.getSelectionModel().selectFirst(); estado.setText("Selecciona un apartamento o propone una combinaciÃ³n."); }
        } catch (RuntimeException ex) { estado.setText(ex.getMessage()); }
    }

    private void proponerGrupoPortal(DatePicker entrada, DatePicker salida, ListView<DatosOcupante> ocupantes,
            ComboBox<ApartamentoDisponible> disponibles, ListView<ApartamentoDisponible> grupo, Label estado) {
        try {
            validarFechasPortal(entrada, salida);
            if (ocupantes.getItems().isEmpty()) throw new IllegalArgumentException("Agrega al menos un ocupante.");
            var propuesta = reservas.proponerGrupo(entrada.getValue(), salida.getValue(), ocupantes.getItems().size());
            if (propuesta.length == 0) throw new IllegalArgumentException("No hay una combinaciÃ³n de apartamentos disponibles para todo el grupo.");
            grupo.getItems().setAll(propuesta);
            grupo.getSelectionModel().selectAll();
            disponibles.getSelectionModel().clearSelection();
            estado.setText("Propuesta: " + propuesta.length + " apartamento(s). Los ocupantes se distribuirÃ¡n automÃ¡ticamente por capacidad.");
        } catch (RuntimeException ex) { grupo.getItems().clear(); estado.setText(ex.getMessage()); }
    }

    private void cotizarPortal(DatePicker entrada, DatePicker salida, ListView<DatosOcupante> ocupantes,
            ComboBox<ApartamentoDisponible> disponibles, ListView<ApartamentoDisponible> grupo, Label estado, Dialog<Void> dialogo) {
        try {
            validarFechasPortal(entrada, salida);
            DatosOcupante[] datos = construirDistribucionPortal(ocupantes, disponibles, grupo);
            String[] codigos = codigosSeleccionadosPortal(disponibles, grupo);
            var c = reservas.cotizarBorrador(codigos, datos, entrada.getValue(), salida.getValue());
            StringBuilder detalle = new StringBuilder("Total: ").append(c.getTotal()).append("\n");
            for (var a : c.apartamentos()) detalle.append(a.codigo()).append(": ").append(a.detalle()).append("\n");
            Alert alerta = new Alert(Alert.AlertType.INFORMATION, detalle.toString(), ButtonType.OK);
            alerta.initOwner(dialogo.getDialogPane().getScene().getWindow());
            alerta.setTitle("CotizaciÃ³n de la estancia"); alerta.setHeaderText(c.cantidadNoches() + " noches");
            alerta.showAndWait();
            estado.setText("CotizaciÃ³n calculada. Puedes enviar la solicitud.");
        } catch (RuntimeException ex) { estado.setText(ex.getMessage()); }
    }

    private static void validarFechasPortal(DatePicker entrada, DatePicker salida) {
        if (entrada.getValue() == null || salida.getValue() == null
                || !salida.getValue().isAfter(entrada.getValue())) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
        }
    }

    private static String[] codigosSeleccionadosPortal(
            ComboBox<ApartamentoDisponible> disponible, ListView<ApartamentoDisponible> grupo) {
        if (!grupo.getItems().isEmpty()) {
            return grupo.getItems().stream().map(ApartamentoDisponible::codigo).toArray(String[]::new);
        }
        ApartamentoDisponible elegido = disponible.getValue();
        if (elegido == null) throw new IllegalArgumentException("Busca y selecciona un apartamento o propone un grupo.");
        return new String[] {elegido.codigo()};
    }

    private static DatosOcupante[] construirDistribucionPortal(
            ListView<DatosOcupante> ocupantes, ComboBox<ApartamentoDisponible> disponible,
            ListView<ApartamentoDisponible> grupo) {
        if (ocupantes.getItems().isEmpty()) throw new IllegalArgumentException("Agrega al menos un ocupante.");
        codigosSeleccionadosPortal(disponible, grupo);
        ApartamentoDisponible[] apartamentos = grupo.getItems().isEmpty()
                ? new ApartamentoDisponible[] {disponible.getValue()}
                : grupo.getItems().toArray(ApartamentoDisponible[]::new);
        return distribuirOcupantesPortal(
                ocupantes.getItems().toArray(DatosOcupante[]::new), apartamentos);
    }

    static DatosOcupante[] distribuirOcupantesPortal(
            DatosOcupante[] ocupantes, ApartamentoDisponible[] apartamentos) {
        if (ocupantes == null || ocupantes.length == 0) {
            throw new IllegalArgumentException("Agrega al menos un ocupante.");
        }
        if (apartamentos == null || apartamentos.length == 0) {
            throw new IllegalArgumentException("Selecciona al menos un apartamento.");
        }
        int capacidad = 0;
        for (ApartamentoDisponible apartamento : apartamentos) {
            if (apartamento == null || apartamento.capacidad() <= 0
                    || apartamento.codigo() == null || apartamento.codigo().isBlank()) {
                throw new IllegalArgumentException("La propuesta contiene un apartamento invÃ¡lido.");
            }
            capacidad += apartamento.capacidad();
        }
        if (capacidad < ocupantes.length) {
            throw new IllegalArgumentException("La capacidad total de los apartamentos no alcanza para todos los ocupantes.");
        }
        DatosOcupante[] resultado = new DatosOcupante[ocupantes.length];
        int indiceApartamento = 0;
        int asignadosEnApartamento = 0;
        for (int i = 0; i < resultado.length; i++) {
            while (indiceApartamento < apartamentos.length
                    && asignadosEnApartamento >= apartamentos[indiceApartamento].capacidad()) {
                indiceApartamento++;
                asignadosEnApartamento = 0;
            }
            if (indiceApartamento >= apartamentos.length) {
                throw new IllegalArgumentException("No fue posible distribuir todos los ocupantes.");
            }
            DatosOcupante dato = ocupantes[i];
            if (dato == null || dato.nombre() == null || dato.nombre().isBlank()
                    || dato.fechaNacimiento() == null) {
                throw new IllegalArgumentException("Todos los ocupantes deben tener nombre y fecha de nacimiento.");
            }
            resultado[i] = new DatosOcupante(
                    dato.nombre(), dato.fechaNacimiento(), apartamentos[indiceApartamento].codigo());
            asignadosEnApartamento++;
        }
        return resultado;
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

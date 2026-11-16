package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.adaptadores.CatalogoReservasArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.dominio.modelo.Folio;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public final class ServicioPanelReservas {

    public record DatosOcupante(
            String nombre,
            LocalDate fechaNacimiento
    ) {}

    public record ResumenReserva(
            String folio,
            String titular,
            String apartamentos,
            LocalDate entrada,
            LocalDate salida,
            LocalDateTime llegadaEstimada,
            EstadoReserva estado,
            int ocupantes
    ) {}

    private final CatalogoApartamentosArbol apartamentos;
    private final CatalogoHuespedesArbol huespedes;
    private final CatalogoReservasArbol catalogo;
    private final ServicioReservas reservas;
    private final ServicioPanelPreparacion preparacion;
    private final ServicioFolios folios;
    private final ServicioTarifas tarifas;

    public ServicioPanelReservas(
            CatalogoApartamentosArbol apartamentos,
            CatalogoHuespedesArbol huespedes,
            CatalogoReservasArbol catalogo,
            ServicioReservas reservas,
            ServicioPanelPreparacion preparacion,
            ServicioFolios folios,
            ServicioTarifas tarifas
    ) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.huespedes = Objects.requireNonNull(huespedes);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.reservas = Objects.requireNonNull(reservas);
        this.preparacion = Objects.requireNonNull(preparacion);
        this.folios = Objects.requireNonNull(folios);
        this.tarifas = Objects.requireNonNull(tarifas);
    }

    public void registrarDirecta(
            String folio,
            String documentoTitular,
            String[] codigosApartamentos,
            DatosOcupante[] datosOcupantes,
            LocalDate entrada,
            LocalDate salida,
            LocalTime horaLlegada
    ) {
        if (horaLlegada == null) {
            throw new IllegalArgumentException(
                    "La hora estimada de llegada es obligatoria."
            );
        }

        if (codigosApartamentos == null
                || codigosApartamentos.length == 0) {
            throw new IllegalArgumentException(
                    "Selecciona al menos un apartamento."
            );
        }

        if (datosOcupantes == null || datosOcupantes.length == 0) {
            throw new IllegalArgumentException(
                    "Registra al menos un ocupante."
            );
        }

        var titular = huespedes.buscarPorDocumento(documentoTitular)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el huésped titular."
                ));

        Estancia estancia = new Estancia(entrada, salida);

        AsignacionApartamento[] asignaciones =
                new AsignacionApartamento[codigosApartamentos.length];

        for (int i = 0; i < codigosApartamentos.length; i++) {
            String codigo = codigosApartamentos[i];

            var apartamento = apartamentos.buscarPorCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe el apartamento " + codigo
                    ));

            asignaciones[i] = new AsignacionApartamento(apartamento);
        }

        Ocupante[] ocupantes = new Ocupante[datosOcupantes.length];

        for (int i = 0; i < datosOcupantes.length; i++) {
            DatosOcupante datos = datosOcupantes[i];

            if (datos == null) {
                throw new IllegalArgumentException(
                        "Los ocupantes no pueden estar vacíos."
                );
            }

            ocupantes[i] = new Ocupante(
                    datos.nombre(),
                    datos.fechaNacimiento()
            );

            ocupantes[i].calcularEdadEn(entrada);
        }

        Reserva reserva = new Reserva(
                folio,
                titular,
                ocupantes,
                estancia,
                CanalReserva.DIRECTA,
                asignaciones
        );

        catalogo.registrarDirecta(
                reserva,
                entrada.atTime(horaLlegada),
                reservas
        );

        folios.abrir(reserva);
    }

    public ServicioTarifas.Cotizacion cotizarBorrador(
            String[] codigos,
            DatosOcupante[] datos,
            LocalDate entrada,
            LocalDate salida
    ) {
        if (codigos == null || codigos.length != 1) {
            throw new IllegalArgumentException(
                    "Por ahora, selecciona un solo apartamento para cotizar."
            );
        }

        String codigo = codigos[0].trim();

        var apartamento = apartamentos.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el apartamento " + codigo
                ));

        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException(
                    "Agrega los ocupantes antes de cotizar."
            );
        }

        if (datos.length > apartamento.getCapacidadMaxima()) {
            throw new IllegalArgumentException(
                    "Los ocupantes superan la capacidad del apartamento."
            );
        }

        Estancia estancia = new Estancia(entrada, salida);
        Ocupante[] ocupantes = new Ocupante[datos.length];

        for (int i = 0; i < datos.length; i++) {
            if (datos[i] == null) {
                throw new IllegalArgumentException(
                        "Hay un ocupante sin información."
                );
            }

            ocupantes[i] = new Ocupante(
                    datos[i].nombre(),
                    datos[i].fechaNacimiento()
            );

            ocupantes[i].calcularEdadEn(entrada);
        }

        return tarifas.cotizar(apartamento, estancia, ocupantes);
    }

    public ServicioTarifas.Cotizacion cotizarReserva(String codigo) {
        Reserva reserva = buscarReserva(codigo);
        var asignaciones = reserva.getAsignaciones();

        if (asignaciones.length != 1) {
            throw new IllegalStateException(
                    "Para cotizar varios apartamentos primero debemos "
                            + "distribuir los ocupantes entre ellos."
            );
        }

        return tarifas.cotizar(
                asignaciones[0].getApartamento(),
                reserva.getEstancia(),
                reserva.getOcupantes()
        );
    }

    public Folio consultarFolio(String codigo) {
        buscarReserva(codigo);
        return folios.obtener(codigo);
    }

    public void registrarCargo(
            String codigo, BigDecimal monto, String descripcion
    ) {
        buscarReserva(codigo);
        folios.registrarCargo(
                codigo, monto, descripcion, LocalDateTime.now()
        );
    }

    public void registrarPago(
            String codigo, BigDecimal monto, String descripcion
    ) {
        buscarReserva(codigo);
        folios.registrarPago(
                codigo, monto, descripcion, LocalDateTime.now()
        );
    }

    public void deshacerMovimiento(String codigo) {
        buscarReserva(codigo);
        folios.deshacerUltima(codigo, LocalDateTime.now());
    }

    public void registrarCheckIn(String folio) {
        reservas.registrarCheckIn(buscarReserva(folio));
    }

    public void registrarCheckOut(String folio) {
        Reserva reserva = buscarReserva(folio);

        reservas.registrarCheckOut(reserva);

        for (var asignacion : reserva.getAsignaciones()) {
            String codigo = asignacion.getApartamento().getCodigo();

            preparacion.encolarPendiente(
                    codigo,
                    buscarProximaLlegada(codigo)
            );
        }
    }

    private LocalDateTime buscarProximaLlegada(String codigoApartamento) {
        LocalDateTime proxima = null;
        LocalDate hoy = preparacion.consultarFechaOperacion();

        for (var registro : catalogo.listar()) {
            Reserva reserva = registro.reserva();

            if (reserva.getEstado() != EstadoReserva.CONFIRMADA
                    && reserva.getEstado() != EstadoReserva.PENDIENTE) {
                continue;
            }

            LocalDateTime llegada = registro.llegadaEstimada();

            if (llegada.toLocalDate().isBefore(hoy)) {
                continue;
            }

            for (var asignacion : reserva.getAsignaciones()) {
                if (asignacion.getApartamento().getCodigo()
                        .equals(codigoApartamento)) {

                    if (proxima == null || llegada.isBefore(proxima)) {
                        proxima = llegada;
                    }

                    break;
                }
            }
        }

        return proxima;
    }

    private Reserva buscarReserva(String folio) {
        return catalogo.buscarPorFolio(folio)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la reserva " + folio
                ))
                .reserva();
    }

    public ResumenReserva[] consultarTodas() {
        var registros = catalogo.listar();
        ResumenReserva[] resultado =
                new ResumenReserva[registros.length];

        for (int i = 0; i < registros.length; i++) {
            var registro = registros[i];
            Reserva reserva = registro.reserva();

            StringBuilder codigos = new StringBuilder();

            for (var asignacion : reserva.getAsignaciones()) {
                if (codigos.length() > 0) {
                    codigos.append(", ");
                }

                codigos.append(
                        asignacion.getApartamento().getCodigo()
                );
            }

            resultado[i] = new ResumenReserva(
                    reserva.getFolio(),
                    reserva.getTitular().getNombre(),
                    codigos.toString(),
                    reserva.getEstancia().getFechaEntrada(),
                    reserva.getEstancia().getFechaSalida(),
                    registro.llegadaEstimada(),
                    reserva.getEstado(),
                    reserva.getOcupantes().length
            );
        }

        return resultado;
    }
}
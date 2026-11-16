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
            LocalDate fechaNacimiento,
            String apartamento
    ) {
        public DatosOcupante(String nombre, LocalDate fechaNacimiento) {
            this(nombre, fechaNacimiento, null);
        }
    }

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

    public record ApartamentoDisponible(
            String codigo,
            String nombre,
            int capacidad,
            int dormitorios,
            BigDecimal tarifaBase
    ) {
        @Override
        public String toString() {
            return codigo + " · " + nombre
                    + " · Capacidad: " + capacidad
                    + " · Dormitorios: " + dormitorios
                    + " · Tarifa base/persona: $" + tarifaBase.toPlainString();
        }
    }

    public record CotizacionApartamento(
            String codigo,
            int ocupantes,
            ServicioTarifas.Cotizacion detalle
    ) {}

    public record CotizacionReserva(
            BigDecimal total,
            int noches,
            CotizacionApartamento[] apartamentos
    ) {
        public CotizacionReserva {
            apartamentos = apartamentos.clone();
        }

        @Override
        public CotizacionApartamento[] apartamentos() {
            return apartamentos.clone();
        }

        public BigDecimal getTotal() {
            return total;
        }

        public int cantidadNoches() {
            return noches;
        }
    }

    private final CatalogoApartamentosArbol apartamentos;
    private final CatalogoHuespedesArbol huespedes;
    private final CatalogoReservasArbol catalogo;
    private final ServicioReservas reservas;
    private final ServicioPanelPreparacion preparacion;
    private final ServicioFolios folios;
    private final ServicioTarifas tarifas;
    private final ServicioDisponibilidad disponibilidad;

    public ServicioPanelReservas(
            CatalogoApartamentosArbol apartamentos,
            CatalogoHuespedesArbol huespedes,
            CatalogoReservasArbol catalogo,
            ServicioReservas reservas,
            ServicioPanelPreparacion preparacion,
            ServicioFolios folios,
            ServicioTarifas tarifas,
            ServicioDisponibilidad disponibilidad
    ) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.huespedes = Objects.requireNonNull(huespedes);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.reservas = Objects.requireNonNull(reservas);
        this.preparacion = Objects.requireNonNull(preparacion);
        this.folios = Objects.requireNonNull(folios);
        this.tarifas = Objects.requireNonNull(tarifas);
        this.disponibilidad = Objects.requireNonNull(disponibilidad);
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

        AsignacionApartamento[] asignaciones = construirAsignaciones(
                codigosApartamentos,
                datosOcupantes,
                ocupantes
        );

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

    public ApartamentoDisponible[] buscarDisponibles(
            LocalDate entrada,
            LocalDate salida,
            int personas,
            String orden
    ) {
        var criterio = switch (orden) {
            case "Tarifa base" -> ComparadoresApartamento.porPrecio();
            case "Capacidad" -> ComparadoresApartamento.porCapacidad();
            case "Dormitorios" -> ComparadoresApartamento.porDormitorios();
            default -> throw new IllegalArgumentException(
                    "Selecciona un criterio de orden."
            );
        };

        var encontrados = disponibilidad.buscarDisponibles(
                apartamentos.listarPorCodigo(),
                new Estancia(entrada, salida),
                personas,
                criterio
        );

        ApartamentoDisponible[] resultado =
                new ApartamentoDisponible[encontrados.tamanio()];

        int indice = 0;

        for (var apartamento : encontrados) {
            resultado[indice++] = new ApartamentoDisponible(
                    apartamento.getCodigo(),
                    apartamento.getNombre(),
                    apartamento.getCapacidadMaxima(),
                    apartamento.getNumeroDormitorios(),
                    apartamento.getTarifaBaja()
            );
        }

        return resultado;
    }

    public ApartamentoDisponible[] proponerGrupo(
            LocalDate entrada,
            LocalDate salida,
            int personas
    ) {
        var propuesta = disponibilidad.asignarGrupo(
                apartamentos.listarPorCodigo(),
                new Estancia(entrada, salida),
                personas
        );

        ApartamentoDisponible[] resultado =
                new ApartamentoDisponible[propuesta.tamanio()];

        int indice = 0;

        for (var apartamento : propuesta) {
            resultado[indice++] = new ApartamentoDisponible(
                    apartamento.getCodigo(),
                    apartamento.getNombre(),
                    apartamento.getCapacidadMaxima(),
                    apartamento.getNumeroDormitorios(),
                    apartamento.getTarifaBaja()
            );
        }

        return resultado;
    }

    public CotizacionReserva cotizarBorrador(
            String[] codigos,
            DatosOcupante[] datos,
            LocalDate entrada,
            LocalDate salida
    ) {
        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException(
                    "Agrega los ocupantes antes de cotizar."
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

        AsignacionApartamento[] asignaciones = construirAsignaciones(
                codigos, datos, ocupantes
        );

        return cotizarAsignaciones(estancia, asignaciones);
    }

    public CotizacionReserva cotizarReserva(String codigo) {
        Reserva reserva = buscarReserva(codigo);
        AsignacionApartamento[] asignaciones = reserva.getAsignaciones();

        // Compatibilidad con reservas antiguas de un solo apartamento.
        if (asignaciones.length == 1
                && !asignaciones[0].tieneDistribucion()) {
            asignaciones[0] = new AsignacionApartamento(
                    asignaciones[0].getApartamento(),
                    reserva.getOcupantes()
            );
        }

        for (var asignacion : asignaciones) {
            if (!asignacion.tieneDistribucion()) {
                throw new IllegalStateException(
                        "Esta reserva no tiene guardada la distribución "
                                + "de ocupantes por apartamento."
                );
            }
        }

        return cotizarAsignaciones(reserva.getEstancia(), asignaciones);
    }

    private CotizacionReserva cotizarAsignaciones(
            Estancia estancia,
            AsignacionApartamento[] asignaciones
    ) {
        if (asignaciones == null || asignaciones.length == 0) {
            throw new IllegalArgumentException(
                    "Selecciona al menos un apartamento."
            );
        }

        CotizacionApartamento[] detalles =
                new CotizacionApartamento[asignaciones.length];

        BigDecimal total = BigDecimal.ZERO;
        int noches = 0;

        for (int i = 0; i < asignaciones.length; i++) {
            var asignacion = asignaciones[i];
            Ocupante[] grupo = asignacion.getOcupantes();

            var cotizacion = tarifas.cotizar(
                    asignacion.getApartamento(),
                    estancia,
                    grupo
            );

            detalles[i] = new CotizacionApartamento(
                    asignacion.getApartamento().getCodigo(),
                    grupo.length,
                    cotizacion
            );

            total = total.add(cotizacion.getTotal());
            noches = cotizacion.cantidadNoches();
        }

        return new CotizacionReserva(total, noches, detalles);
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
                if (!codigos.isEmpty()) {
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

    private AsignacionApartamento[] construirAsignaciones(
            String[] codigos,
            DatosOcupante[] datos,
            Ocupante[] ocupantes
    ) {
        if (codigos == null || codigos.length == 0) {
            throw new IllegalArgumentException(
                    "Selecciona al menos un apartamento."
            );
        }

        if (datos == null || ocupantes == null
                || datos.length == 0 || datos.length != ocupantes.length) {
            throw new IllegalArgumentException(
                    "La información de los ocupantes está incompleta."
            );
        }

        AsignacionApartamento[] resultado =
                new AsignacionApartamento[codigos.length];

        boolean[] asignados = new boolean[ocupantes.length];

        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i] == null || codigos[i].isBlank()) {
                throw new IllegalArgumentException(
                        "Hay un código de apartamento vacío."
                );
            }

            String codigo = codigos[i].trim();

            for (int j = 0; j < i; j++) {
                if (codigo.equals(codigos[j].trim())) {
                    throw new IllegalArgumentException(
                            "El apartamento " + codigo + " está repetido."
                    );
                }
            }

            var apartamento = apartamentos.buscarPorCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe el apartamento " + codigo
                    ));

            Ocupante[] temporal = new Ocupante[ocupantes.length];
            int cantidad = 0;

            for (int j = 0; j < datos.length; j++) {
                if (datos[j] == null || ocupantes[j] == null) {
                    throw new IllegalArgumentException(
                            "Hay un ocupante sin información."
                    );
                }

                String destino = datos[j].apartamento();

                // Con un solo apartamento, no hace falta distribuir manualmente.
                if (destino == null || destino.isBlank()) {
                    if (codigos.length == 1) {
                        destino = codigo;
                    } else {
                        throw new IllegalArgumentException(
                                "Asigna un apartamento a " + datos[j].nombre() + "."
                        );
                    }
                }

                if (codigo.equals(destino.trim())) {
                    temporal[cantidad++] = ocupantes[j];
                    asignados[j] = true;
                }
            }

            if (cantidad == 0) {
                throw new IllegalArgumentException(
                        "El apartamento " + codigo + " no tiene ocupantes. "
                                + "Asigna personas o retíralo de la selección."
                );
            }

            Ocupante[] grupo = new Ocupante[cantidad];
            System.arraycopy(temporal, 0, grupo, 0, cantidad);

            // El modelo valida la capacidad individual.
            resultado[i] = new AsignacionApartamento(apartamento, grupo);
        }

        for (int i = 0; i < asignados.length; i++) {
            if (!asignados[i]) {
                throw new IllegalArgumentException(
                        "El apartamento asignado a " + datos[i].nombre()
                                + " no está entre los seleccionados."
                );
            }
        }

        return resultado;
    }
}
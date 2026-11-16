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

    public ServicioPanelReservas(
            CatalogoApartamentosArbol apartamentos,
            CatalogoHuespedesArbol huespedes,
            CatalogoReservasArbol catalogo,
            ServicioReservas reservas
    ) {
        this.apartamentos = Objects.requireNonNull(apartamentos);
        this.huespedes = Objects.requireNonNull(huespedes);
        this.catalogo = Objects.requireNonNull(catalogo);
        this.reservas = Objects.requireNonNull(reservas);
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
    }

    public void registrarCheckIn(String folio) {
        reservas.registrarCheckIn(buscarReserva(folio));
    }

    public void registrarCheckOut(String folio) {
        reservas.registrarCheckOut(buscarReserva(folio));
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
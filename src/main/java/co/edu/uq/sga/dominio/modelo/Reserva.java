package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;

/** Reserva de un titular, sus ocupantes, una estancia y uno o más apartamentos. */
public class Reserva {
    private final String folio;
    private final Huesped titular;
    private final Ocupante[] ocupantes;
    private final Estancia estancia;
    private final CanalReserva canal;
    private final AsignacionApartamento[] asignaciones;
    private EstadoReserva estado;

    public Reserva(
            String folio,
            Huesped titular,
            Ocupante[] ocupantes,
            Estancia estancia,
            CanalReserva canal,
            AsignacionApartamento[] asignaciones) {
        this.folio = validarTexto(folio, "folio");
        if (titular == null) {
            throw new IllegalArgumentException("El huésped titular es obligatorio.");
        }
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia es obligatoria.");
        }
        if (canal == null) {
            throw new IllegalArgumentException("El canal de reserva es obligatorio.");
        }

        this.titular = titular;
        this.estancia = estancia;
        this.canal = canal;
        this.ocupantes = validarOcupantes(ocupantes);
        this.asignaciones = validarAsignaciones(asignaciones, this.ocupantes.length);
        this.estado = EstadoReserva.PENDIENTE;
    }

    public String getFolio() {
        return folio;
    }

    public Huesped getTitular() {
        return titular;
    }

    public Ocupante[] getOcupantes() {
        return ocupantes.clone();
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public CanalReserva getCanal() {
        return canal;
    }

    public AsignacionApartamento[] getAsignaciones() {
        return asignaciones.clone();
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void confirmar() {
        exigirEstado(EstadoReserva.PENDIENTE, "Solo una reserva pendiente puede confirmarse.");
        estado = EstadoReserva.CONFIRMADA;
    }

    public void iniciar() {
        exigirEstado(EstadoReserva.CONFIRMADA, "Solo una reserva confirmada puede iniciar.");
        estado = EstadoReserva.EN_CURSO;
    }

    public void finalizar() {
        exigirEstado(EstadoReserva.EN_CURSO, "Solo una reserva en curso puede finalizarse.");
        estado = EstadoReserva.FINALIZADA;
    }

    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar reservas pendientes o confirmadas.");
        }
        estado = EstadoReserva.CANCELADA;
    }

    public void marcarNoShow() {
        exigirEstado(
                EstadoReserva.CONFIRMADA,
                "Solo una reserva confirmada puede marcarse como no-show.");
        estado = EstadoReserva.NO_SHOW;
    }

    private void exigirEstado(EstadoReserva esperado, String mensaje) {
        if (estado != esperado) {
            throw new IllegalStateException(mensaje);
        }
    }

    private static String validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static Ocupante[] validarOcupantes(Ocupante[] ocupantes) {
        if (ocupantes == null || ocupantes.length == 0) {
            throw new IllegalArgumentException("La reserva debe tener al menos un ocupante.");
        }

        Ocupante[] copia = ocupantes.clone();
        for (Ocupante ocupante : copia) {
            if (ocupante == null) {
                throw new IllegalArgumentException("Los ocupantes no pueden ser nulos.");
            }
        }
        return copia;
    }

    private static AsignacionApartamento[] validarAsignaciones(
            AsignacionApartamento[] asignaciones, int numeroOcupantes) {
        if (asignaciones == null || asignaciones.length == 0) {
            throw new IllegalArgumentException(
                    "La reserva debe estar asignada al menos a un apartamento.");
        }

        AsignacionApartamento[] copia = asignaciones.clone();
        int capacidadTotal = 0;
        for (int i = 0; i < copia.length; i++) {
            AsignacionApartamento asignacion = copia[i];
            if (asignacion == null) {
                throw new IllegalArgumentException("Las asignaciones no pueden ser nulas.");
            }
            Apartamento apartamento = asignacion.getApartamento();
            capacidadTotal += apartamento.getCapacidadMaxima();

            for (int j = 0; j < i; j++) {
                String codigoAnterior = copia[j].getApartamento().getCodigo();
                if (codigoAnterior.equals(apartamento.getCodigo())) {
                    throw new IllegalArgumentException(
                            "Un apartamento no puede asignarse más de una vez a la reserva.");
                }
            }
        }

        if (capacidadTotal < numeroOcupantes) {
            throw new IllegalArgumentException(
                    "La capacidad asignada no alcanza para todos los ocupantes.");
        }
        return copia;
    }
}

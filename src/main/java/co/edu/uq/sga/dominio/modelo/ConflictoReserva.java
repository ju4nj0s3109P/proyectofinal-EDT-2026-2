package co.edu.uq.sga.dominio.modelo;

import java.time.LocalDateTime;

/** Registro inmutable de una solicitud externa rechazada. */
public class ConflictoReserva {
    private final String folioReserva;
    private final String[] codigosApartamentos;
    private final Estancia estancia;
    private final String motivo;
    private final LocalDateTime fechaHora;

    public ConflictoReserva(Reserva reserva, String motivo, LocalDateTime fechaHora) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva en conflicto es obligatoria.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del conflicto es obligatorio.");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora del conflicto son obligatorias.");
        }
        this.folioReserva = reserva.getFolio();
        AsignacionApartamento[] asignaciones = reserva.getAsignaciones();
        this.codigosApartamentos = new String[asignaciones.length];
        for (int i = 0; i < asignaciones.length; i++) {
            codigosApartamentos[i] = asignaciones[i].getApartamento().getCodigo();
        }
        this.estancia = reserva.getEstancia();
        this.motivo = motivo.trim();
        this.fechaHora = fechaHora;
    }

    public String getFolioReserva() { return folioReserva; }
    public String[] getCodigosApartamentos() { return codigosApartamentos.clone(); }
    public Estancia getEstancia() { return estancia; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}

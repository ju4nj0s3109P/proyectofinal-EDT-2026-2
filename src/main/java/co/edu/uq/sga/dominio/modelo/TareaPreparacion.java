package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

/** Tarea de preparación asociada a un apartamento y a su próxima llegada. */
public class TareaPreparacion {
    private final String codigo;
    private final Apartamento apartamento;
    private final LocalDateTime fechaHoraCreacion;
    private final LocalDateTime fechaHoraLlegada;
    private EstadoTareaPreparacion estado;

    public TareaPreparacion(String codigo, Apartamento apartamento,
            LocalDateTime fechaHoraCreacion, LocalDateTime fechaHoraLlegada) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la tarea es obligatorio.");
        }
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento de la tarea es obligatorio.");
        }
        if (fechaHoraCreacion == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria.");
        }
        if (fechaHoraLlegada == null) {
            throw new IllegalArgumentException("La fecha y hora de llegada son obligatorias.");
        }
        this.codigo = codigo.trim();
        this.apartamento = apartamento;
        this.fechaHoraCreacion = fechaHoraCreacion;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.estado = EstadoTareaPreparacion.ENCOLADA;
    }

    public String getCodigo() { return codigo; }
    public Apartamento getApartamento() { return apartamento; }
    public LocalDateTime getFechaHoraCreacion() { return fechaHoraCreacion; }
    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public EstadoTareaPreparacion getEstado() { return estado; }

    /**
     * Ordena primero las tareas cuya llegada está programada para la fecha
     * indicada. Entre ellas, prioriza la hora de llegada más temprana.
     * Las demás quedan después y se ordenan cronológicamente por llegada.
     */
    public static Comparator<TareaPreparacion> comparadorPorPrioridad(LocalDate fechaActual) {
        if (fechaActual == null) {
            throw new IllegalArgumentException("La fecha actual es obligatoria.");
        }
        return Comparator
                .comparing((TareaPreparacion tarea) ->
                        !tarea.fechaHoraLlegada.toLocalDate().equals(fechaActual))
                .thenComparing(TareaPreparacion::getFechaHoraLlegada)
                .thenComparing(TareaPreparacion::getFechaHoraCreacion)
                .thenComparing(TareaPreparacion::getCodigo);
    }

    public void iniciar() {
        exigirEstado(EstadoTareaPreparacion.ENCOLADA);
        estado = EstadoTareaPreparacion.EN_PROCESO;
    }

    public void completar() {
        exigirEstado(EstadoTareaPreparacion.EN_PROCESO);
        estado = EstadoTareaPreparacion.COMPLETADA;
    }

    private void exigirEstado(EstadoTareaPreparacion esperado) {
        if (estado != esperado) {
            throw new ExcepcionDominio("La tarea no está en el estado requerido: " + esperado + ".");
        }
    }
}

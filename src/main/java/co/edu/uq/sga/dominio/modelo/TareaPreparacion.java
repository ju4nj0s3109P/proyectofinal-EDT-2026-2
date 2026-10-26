package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import java.time.LocalDateTime;

/** Tarea de preparación asociada a un apartamento. */
public class TareaPreparacion {
    private final String codigo;
    private final Apartamento apartamento;
    private final LocalDateTime fechaHoraCreacion;
    private EstadoTareaPreparacion estado;

    public TareaPreparacion(String codigo, Apartamento apartamento, LocalDateTime fechaHoraCreacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la tarea es obligatorio.");
        }
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento de la tarea es obligatorio.");
        }
        if (fechaHoraCreacion == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria.");
        }
        this.codigo = codigo.trim();
        this.apartamento = apartamento;
        this.fechaHoraCreacion = fechaHoraCreacion;
        this.estado = EstadoTareaPreparacion.ENCOLADA;
    }

    public String getCodigo() { return codigo; }
    public Apartamento getApartamento() { return apartamento; }
    public LocalDateTime getFechaHoraCreacion() { return fechaHoraCreacion; }
    public EstadoTareaPreparacion getEstado() { return estado; }

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

package co.edu.uq.sga.dominio.modelo;

import java.time.LocalDate;
import java.time.Period;

/** Persona registrada como ocupante de una reserva. */
public class Ocupante {
    private final String nombre;
    private final LocalDate fechaNacimiento;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }

        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Calcula la edad cumplida en la fecha indicada.
     *
     * @param fechaReferencia fecha en la que se desea conocer la edad
     * @return edad en años cumplidos
     */
    public int calcularEdadEn(LocalDate fechaReferencia) {
        if (fechaReferencia == null) {
            throw new IllegalArgumentException("La fecha de referencia es obligatoria.");
        }
        if (fechaReferencia.isBefore(fechaNacimiento)) {
            throw new IllegalArgumentException(
                    "La fecha de referencia no puede ser anterior al nacimiento.");
        }
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    /**
     * Indica si el ocupante tiene menos de seis años en la fecha consultada.
     */
    public boolean esMenorDeSeisEn(LocalDate fechaReferencia) {
        return calcularEdadEn(fechaReferencia) < 6;
    }
}

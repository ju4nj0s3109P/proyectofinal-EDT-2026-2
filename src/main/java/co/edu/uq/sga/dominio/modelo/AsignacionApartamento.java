package co.edu.uq.sga.dominio.modelo;

import co.edu.uq.sga.dominio.excepciones.CapacidadExcedidaException;

/** Vincula una reserva con uno de los apartamentos que ocupará. */
public class AsignacionApartamento {
    private final Apartamento apartamento;
    private final Ocupante[] ocupantes;

    public AsignacionApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento asignado es obligatorio.");
        }
        this.apartamento = apartamento;
        this.ocupantes = null;
    }

    public AsignacionApartamento(
            Apartamento apartamento,
            Ocupante[] ocupantes
    ) {
        if (apartamento == null) {
            throw new IllegalArgumentException(
                    "El apartamento asignado es obligatorio."
            );
        }

        if (ocupantes == null || ocupantes.length == 0) {
            throw new IllegalArgumentException(
                    "La asignación debe contener al menos un ocupante."
            );
        }

        if (ocupantes.length > apartamento.getCapacidadMaxima()) {
            throw new CapacidadExcedidaException(
                    "Los ocupantes superan la capacidad de "
                            + apartamento.getCodigo() + "."
            );
        }

        Ocupante[] copia = ocupantes.clone();

        for (int i = 0; i < copia.length; i++) {
            if (copia[i] == null) {
                throw new IllegalArgumentException(
                        "La asignación no puede contener ocupantes nulos."
                );
            }

            for (int j = 0; j < i; j++) {
                if (copia[i] == copia[j]) {
                    throw new IllegalArgumentException(
                            "Un ocupante no puede asignarse dos veces."
                    );
                }
            }
        }

        this.apartamento = apartamento;
        this.ocupantes = copia;
    }

    public boolean tieneDistribucion() {
        return ocupantes != null;
    }

    public Ocupante[] getOcupantes() {
        if (!tieneDistribucion()) {
            throw new IllegalStateException(
                    "Esta asignación aún no tiene distribución de ocupantes."
            );
        }

        return ocupantes.clone();
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

}

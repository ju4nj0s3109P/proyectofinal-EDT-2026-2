package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.servicios.ServicioReservas;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public final class CatalogoReservasArbol {

    public record RegistroReserva(
            Reserva reserva,
            LocalDateTime llegadaEstimada
    ) {}

    private final DiccionarioArbol<String, RegistroReserva> registros =
            new DiccionarioArbol<>();

    public void registrarDirecta(
            Reserva reserva,
            LocalDateTime llegadaEstimada,
            ServicioReservas servicio
    ) {
        Objects.requireNonNull(reserva, "La reserva es obligatoria.");
        Objects.requireNonNull(servicio, "El servicio es obligatorio.");

        if (llegadaEstimada == null
                || !llegadaEstimada.toLocalDate().equals(
                reserva.getEstancia().getFechaEntrada())) {
            throw new IllegalArgumentException(
                    "La llegada estimada debe corresponder al día de entrada."
            );
        }

        if (registros.contieneClave(reserva.getFolio())) {
            throw new IllegalArgumentException(
                    "Ya existe la reserva " + reserva.getFolio()
            );
        }

        servicio.registrarDirecta(reserva);

        registros.poner(
                reserva.getFolio(),
                new RegistroReserva(reserva, llegadaEstimada)
        );
    }

    public Optional<RegistroReserva> buscarPorFolio(String folio) {
        if (folio == null || folio.isBlank()) {
            throw new IllegalArgumentException("El folio es obligatorio.");
        }

        return registros.obtener(folio.trim());
    }

    public RegistroReserva[] listar() {
        return registros.valores().aArreglo(RegistroReserva[]::new);
    }
}
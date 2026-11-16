package co.edu.uq.sga.servicios;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta periódicamente el vencimiento de reservas pendientes del portal.
 * La aplicación debe iniciar este servicio al arrancar y cerrarlo al salir.
 */
public final class VigilanciaAutomaticaVencimientos implements AutoCloseable {
    private final ServicioVencimientoReservas vencimientos;
    private final long intervaloMilisegundos;
    private ScheduledExecutorService ejecutor;
    private volatile RuntimeException ultimaFalla;

    public VigilanciaAutomaticaVencimientos(
            ServicioVencimientoReservas vencimientos, Duration intervalo) {
        this.vencimientos = Objects.requireNonNull(vencimientos, "El servicio de vencimientos es obligatorio.");
        Objects.requireNonNull(intervalo, "El intervalo es obligatorio.");
        if (intervalo.isZero() || intervalo.isNegative() || intervalo.toMillis() < 1) {
            throw new IllegalArgumentException("El intervalo debe ser de al menos un milisegundo.");
        }
        this.intervaloMilisegundos = intervalo.toMillis();
    }

    /** Inicia una única tarea periódica; puede llamarse varias veces sin duplicarla. */
    public synchronized void iniciar() {
        if (ejecutor != null && !ejecutor.isShutdown()) return;
        ejecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "sga-vencimiento-reservas");
            hilo.setDaemon(true);
            return hilo;
        });
        ejecutor.scheduleWithFixedDelay(this::procesar, 0,
                intervaloMilisegundos, TimeUnit.MILLISECONDS);
    }

    public synchronized void detener() {
        if (ejecutor != null) {
            ejecutor.shutdownNow();
            ejecutor = null;
        }
    }

    public boolean estaEnEjecucion() {
        ScheduledExecutorService actual = ejecutor;
        return actual != null && !actual.isShutdown();
    }

    /** Permite consultar un fallo de la última ejecución sin detener las siguientes revisiones. */
    public RuntimeException getUltimaFalla() {
        return ultimaFalla;
    }

    private void procesar() {
        try {
            vencimientos.procesarVencidas(LocalDateTime.now());
            ultimaFalla = null;
        } catch (RuntimeException e) {
            ultimaFalla = e;
        }
    }

    @Override
    public void close() {
        detener();
    }
}

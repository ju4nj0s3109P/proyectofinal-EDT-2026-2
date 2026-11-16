package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import co.edu.uq.sga.estructuras.lineales.ListaCircular;
import java.util.Objects;
import java.util.Optional;

/** Administra la rotación de los recepcionistas mediante la lista circular propia. */
public final class ServicioTurnosRecepcion {
    private final ListaCircular<Empleado> turnos = new ListaCircular<>();

    public void agregarRecepcionista(Empleado empleado) {
        Objects.requireNonNull(empleado, "El empleado es obligatorio.");
        if (empleado.getRol() != RolEmpleado.RECEPCIONISTA) {
            throw new IllegalArgumentException("Solo un recepcionista puede integrar la rotación.");
        }
        if (!empleado.estaActivo()) {
            throw new IllegalArgumentException("Un recepcionista inactivo no puede recibir turnos.");
        }
        if (buscar(empleado.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("El recepcionista ya está en la rotación.");
        }
        turnos.agregar(empleado);
    }

    public boolean retirarRecepcionista(String documento) {
        validarDocumento(documento);
        Optional<Empleado> empleado = buscar(documento);
        return empleado.isPresent() && turnos.eliminar(empleado.get());
    }

    /** Devuelve el recepcionista actual, retirando de la rotación los inactivos. */
    public Optional<Empleado> consultarTurnoActual() {
        depurarInactivos();
        return turnos.estaVacia() ? Optional.empty() : Optional.of(turnos.actual());
    }

    /** Avanza al siguiente recepcionista activo. */
    public Optional<Empleado> avanzarTurno() {
        depurarInactivos();
        if (turnos.estaVacia()) {
            return Optional.empty();
        }
        turnos.avanzar();
        depurarInactivos();
        return turnos.estaVacia() ? Optional.empty() : Optional.of(turnos.actual());
    }

    public Empleado[] consultarRotacion() {
        depurarInactivos();
        Empleado[] resultado = new Empleado[turnos.tamanio()];
        int indice = 0;
        for (Empleado empleado : turnos) {
            resultado[indice++] = empleado;
        }
        return resultado;
    }

    public int cantidadRecepcionistas() {
        depurarInactivos();
        return turnos.tamanio();
    }

    private Optional<Empleado> buscar(String documento) {
        for (Empleado empleado : turnos) {
            if (empleado.getDocumento().equals(documento.trim())) {
                return Optional.of(empleado);
            }
        }
        return Optional.empty();
    }

    private void depurarInactivos() {
        while (!turnos.estaVacia() && !turnos.actual().estaActivo()) {
            turnos.eliminar(turnos.actual());
        }
        if (turnos.estaVacia()) {
            return;
        }
        int restantes = turnos.tamanio();
        while (restantes-- > 0) {
            Empleado actual = turnos.actual();
            if (!actual.estaActivo()) {
                turnos.eliminar(actual);
            } else {
                turnos.avanzar();
            }
            if (turnos.estaVacia()) {
                return;
            }
        }
    }

    private static void validarDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
    }
}

package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CatalogoEmpleadosArbol;
import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import java.util.Objects;
import java.util.Optional;

/** Casos de uso administrativos para registrar y consultar el personal. */
public final class ServicioEmpleados {
    private final CatalogoEmpleadosArbol catalogo;

    public ServicioEmpleados(CatalogoEmpleadosArbol catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "El catálogo de empleados es obligatorio.");
    }

    public void registrar(Empleado empleado) {
        catalogo.agregar(Objects.requireNonNull(empleado, "El empleado es obligatorio."));
    }

    public Optional<Empleado> buscarPorDocumento(String documento) {
        return catalogo.buscarPorDocumento(documento);
    }

    public Empleado[] consultarTodos() {
        return catalogo.listarPorDocumento();
    }

    public void activar(String documento) {
        obtener(documento).activar();
    }

    public void desactivar(String documento) {
        obtener(documento).desactivar();
    }

    /** Elimina del catálogo solo cuando el empleado no está activo. */
    public boolean eliminarInactivo(String documento) {
        Empleado empleado = obtener(documento);
        if (empleado.estaActivo()) {
            throw new IllegalStateException("Desactive el empleado antes de retirarlo del catálogo.");
        }
        return catalogo.eliminar(documento);
    }

    public Empleado[] consultarPorRol(RolEmpleado rol) {
        Objects.requireNonNull(rol, "El rol es obligatorio.");
        Empleado[] todos = catalogo.listarPorDocumento();
        Empleado[] temporal = new Empleado[todos.length];
        int cantidad = 0;
        for (Empleado empleado : todos) {
            if (empleado.getRol() == rol) {
                temporal[cantidad++] = empleado;
            }
        }
        Empleado[] resultado = new Empleado[cantidad];
        System.arraycopy(temporal, 0, resultado, 0, cantidad);
        return resultado;
    }

    private Empleado obtener(String documento) {
        return buscarPorDocumento(documento)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un empleado con documento " + documento + "."));
    }
}

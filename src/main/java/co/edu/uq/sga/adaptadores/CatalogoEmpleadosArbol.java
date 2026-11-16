package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.modelo.Empleado;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import java.util.Optional;

/** Catálogo de empleados indexado por documento mediante el árbol propio. */
public final class CatalogoEmpleadosArbol {
    private final DiccionarioArbol<String, Empleado> empleados = new DiccionarioArbol<>();

    public void agregar(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        String documento = empleado.getDocumento();
        if (empleados.contieneClave(documento)) {
            throw new IllegalArgumentException("Ya existe un empleado con documento " + documento + ".");
        }
        empleados.poner(documento, empleado);
    }

    public Optional<Empleado> buscarPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        return empleados.obtener(documento.trim());
    }

    public boolean eliminar(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        return empleados.quitar(documento.trim()).isPresent();
    }

    public Empleado[] listarPorDocumento() {
        return empleados.valores().aArreglo(Empleado[]::new);
    }

    public int cantidad() { return empleados.tamanio(); }
}

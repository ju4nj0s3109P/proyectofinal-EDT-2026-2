package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.CatalogoEmpleadosArbol;
import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioEmpleadosTest {
    private ServicioEmpleados servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioEmpleados(new CatalogoEmpleadosArbol());
    }

    @Test
    void registraYBuscaEmpleadoPorDocumento() {
        Empleado empleado = new Empleado("100", "Ana", "3001234567",
                "ana@example.com", RolEmpleado.RECEPCIONISTA);
        servicio.registrar(empleado);
        assertSame(empleado, servicio.buscarPorDocumento("100").orElseThrow());
    }

    @Test
    void rechazaDocumentosDuplicados() {
        servicio.registrar(new Empleado("100", "Ana", "3001234567",
                "ana@example.com", RolEmpleado.ADMINISTRADOR));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(
                new Empleado("100", "Otra", "3007654321",
                        "otra@example.com", RolEmpleado.RECEPCIONISTA)));
    }

    @Test
    void soloPermiteEliminarEmpleadosInactivos() {
        servicio.registrar(new Empleado("100", "Ana", "3001234567",
                "ana@example.com", RolEmpleado.ADMINISTRADOR));
        assertThrows(IllegalStateException.class, () -> servicio.eliminarInactivo("100"));
        servicio.desactivar("100");
        assertTrue(servicio.eliminarInactivo("100"));
    }
}

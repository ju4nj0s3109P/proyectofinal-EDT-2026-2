package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import org.junit.jupiter.api.Test;

class ServicioTurnosRecepcionTest {
    @Test
    void rotaCircularmenteEntreRecepcionistas() {
        ServicioTurnosRecepcion turnos = new ServicioTurnosRecepcion();
        Empleado ana = empleado("1", "Ana");
        Empleado luis = empleado("2", "Luis");
        turnos.agregarRecepcionista(ana);
        turnos.agregarRecepcionista(luis);

        assertSame(ana, turnos.consultarTurnoActual().orElseThrow());
        assertSame(luis, turnos.avanzarTurno().orElseThrow());
        assertSame(ana, turnos.avanzarTurno().orElseThrow());
    }

    @Test
    void rechazaRolesDistintosDeRecepcionista() {
        ServicioTurnosRecepcion turnos = new ServicioTurnosRecepcion();
        Empleado admin = new Empleado("1", "Admin", "300", "admin@example.com",
                RolEmpleado.ADMINISTRADOR);
        assertThrows(IllegalArgumentException.class, () -> turnos.agregarRecepcionista(admin));
    }

    @Test
    void retiraEmpleadosDesactivadosDeLaRotacion() {
        ServicioTurnosRecepcion turnos = new ServicioTurnosRecepcion();
        Empleado ana = empleado("1", "Ana");
        Empleado luis = empleado("2", "Luis");
        turnos.agregarRecepcionista(ana);
        turnos.agregarRecepcionista(luis);
        ana.desactivar();
        assertSame(luis, turnos.consultarTurnoActual().orElseThrow());
        assertEquals(1, turnos.cantidadRecepcionistas());
    }

    private static Empleado empleado(String documento, String nombre) {
        return new Empleado(documento, nombre, "3001234567",
                nombre.toLowerCase() + "@example.com", RolEmpleado.RECEPCIONISTA);
    }
}

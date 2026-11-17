package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.servicios.ServicioEmpleados;
import co.edu.uq.sga.servicios.ServicioTurnosRecepcion;
import java.io.IOException;
import java.nio.file.Path;

/** Restaura la rotación circular en el orden declarado por el CSV. */
public final class CargadorTurnosRecepcion {
    public void cargar(Path archivo, ServicioEmpleados empleados, ServicioTurnosRecepcion turnos) throws IOException {
        if (empleados == null || turnos == null) throw new IllegalArgumentException("Los servicios son obligatorios.");
        var filas = new LectorCSV().leer(archivo);
        if (filas.estaVacia() || filas.primero().length != 1
                || !"documentoEmpleado".equals(filas.primero()[0].trim()))
            throw new IOException("El encabezado de turnos-recepcion.csv debe ser documentoEmpleado.");
        int registro = 0;
        for (String[] fila : filas) {
            registro++;
            if (registro == 1) continue;
            try {
                if (fila.length != 1 || fila[0].isBlank()) throw new IllegalArgumentException("Documento de empleado vacío.");
                String documento = fila[0].trim();
                var empleado = empleados.buscarPorDocumento(documento)
                        .orElseThrow(() -> new IllegalArgumentException("No existe el empleado " + documento + "."));
                turnos.agregarRecepcionista(empleado);
            } catch (RuntimeException e) {
                throw new IOException(archivo.getFileName() + ", registro " + registro + ": " + e.getMessage(), e);
            }
        }
    }
}
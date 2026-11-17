package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.dominio.enums.RolEmpleado;
import co.edu.uq.sga.dominio.modelo.Empleado;
import co.edu.uq.sga.servicios.ServicioEmpleados;
import java.io.IOException;
import java.nio.file.Path;

/** Carga el personal inicial y conserva el catálogo como dueño de los empleados. */
public final class CargadorEmpleados {
    private static final String[] ENCABEZADOS = {"documento", "nombre", "telefono", "correo", "rol", "activo"};

    public void cargar(Path archivo, ServicioEmpleados servicio) throws IOException {
        if (servicio == null) throw new IllegalArgumentException("El servicio de empleados es obligatorio.");
        var filas = new LectorCSV().leer(archivo);
        if (filas.estaVacia()) throw new IOException("El archivo de empleados está vacío.");
        String[] encabezados = filas.primero();
        if (encabezados.length != ENCABEZADOS.length) throw new IOException("Encabezado inválido en empleados.csv.");
        for (int i = 0; i < ENCABEZADOS.length; i++)
            if (!ENCABEZADOS[i].equals(encabezados[i].trim()))
                throw new IOException("En empleados.csv se esperaba la columna " + ENCABEZADOS[i] + ".");
        int registro = 0;
        for (String[] fila : filas) {
            registro++;
            if (registro == 1) continue;
            try {
                if (fila.length != ENCABEZADOS.length) throw new IllegalArgumentException("Se esperaban seis columnas.");
                Empleado empleado = new Empleado(fila[0].trim(), fila[1].trim(), fila[2].trim(),
                        fila[3].trim(), RolEmpleado.valueOf(fila[4].trim().toUpperCase()));
                String activo = fila[5].trim();
                if (!activo.equalsIgnoreCase("true") && !activo.equalsIgnoreCase("false"))
                    throw new IllegalArgumentException("El campo activo debe ser true o false.");
                if (activo.equalsIgnoreCase("false")) empleado.desactivar();
                servicio.registrar(empleado);
            } catch (RuntimeException e) {
                throw new IOException(archivo.getFileName() + ", registro " + registro + ": " + e.getMessage(), e);
            }
        }
        if (registro <= 1) throw new IOException("El CSV de empleados no contiene registros.");
    }
}
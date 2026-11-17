package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.servicios.ServicioFolios;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;

/** Carga cargos y pagos iniciales mediante el servicio dueño de los folios. */
public final class CargadorMovimientosFolios {
    private static final String[] ENCABEZADOS = {"folio", "tipo", "monto", "descripcion", "fechaHora"};

    public void cargar(Path archivo, ServicioFolios servicio) throws IOException {
        if (servicio == null) throw new IllegalArgumentException("El servicio de folios es obligatorio.");
        var filas = new LectorCSV().leer(archivo);
        if (filas.estaVacia() || filas.primero().length != ENCABEZADOS.length)
            throw new IOException("Encabezado inválido en movimientos-folios.csv.");
        for (int i = 0; i < ENCABEZADOS.length; i++)
            if (!ENCABEZADOS[i].equals(filas.primero()[i].trim()))
                throw new IOException("En movimientos-folios.csv se esperaba " + ENCABEZADOS[i] + ".");
        int registro = 0;
        for (String[] fila : filas) {
            registro++;
            if (registro == 1) continue;
            try {
                if (fila.length != ENCABEZADOS.length) throw new IllegalArgumentException("Se esperaban cinco columnas.");
                String folio = fila[0].trim();
                TipoMovimiento tipo = TipoMovimiento.valueOf(fila[1].trim().toUpperCase());
                BigDecimal monto = new BigDecimal(fila[2].trim());
                String descripcion = fila[3].trim();
                LocalDateTime fechaHora = LocalDateTime.parse(fila[4].trim());
                switch (tipo) {
                    case CARGO -> servicio.registrarCargo(folio, monto, descripcion, fechaHora);
                    case PAGO -> servicio.registrarPago(folio, monto, descripcion, fechaHora);
                    default -> throw new IllegalArgumentException("La carga inicial admite CARGO y PAGO; los reversos y devoluciones los genera el servicio.");
                }
            } catch (RuntimeException e) {
                throw new IOException(archivo.getFileName() + ", registro " + registro + ": " + e.getMessage(), e);
            }
        }
    }
}
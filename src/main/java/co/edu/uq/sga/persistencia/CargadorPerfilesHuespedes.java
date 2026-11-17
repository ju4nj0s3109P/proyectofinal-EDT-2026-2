package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.servicios.ServicioPerfilesHuesped;
import java.io.IOException;
import java.nio.file.Path;

/** Carga los apartamentos favoritos de cada perfil registrado. */
public final class CargadorPerfilesHuespedes {
    public void cargar(Path archivo, CatalogoHuespedesArbol huespedes,
            CatalogoApartamentosArbol apartamentos, ServicioPerfilesHuesped perfiles) throws IOException {
        if (huespedes == null || apartamentos == null || perfiles == null)
            throw new IllegalArgumentException("Los catálogos y el servicio de perfiles son obligatorios.");
        var filas = new LectorCSV().leer(archivo);
        if (filas.estaVacia() || filas.primero().length != 2
                || !"documentoHuesped".equals(filas.primero()[0].trim())
                || !"apartamentosFavoritos".equals(filas.primero()[1].trim()))
            throw new IOException("Encabezado inválido en perfiles-huespedes.csv.");
        int registro = 0;
        for (String[] fila : filas) {
            registro++;
            if (registro == 1) continue;
            try {
                if (fila.length != 2) throw new IllegalArgumentException("Se esperaban dos columnas.");
                String documento = fila[0].trim();
                var huesped = huespedes.buscarPorDocumento(documento)
                        .orElseThrow(() -> new IllegalArgumentException("No existe el huésped " + documento + "."));
                if (fila[1].isBlank()) continue;
                for (String codigo : fila[1].split("\\|", -1)) {
                    var apartamento = apartamentos.buscarPorCodigo(codigo.trim())
                            .orElseThrow(() -> new IllegalArgumentException("No existe el apartamento " + codigo + "."));
                    perfiles.agregarFavorito(huesped, apartamento);
                }
            } catch (RuntimeException e) {
                throw new IOException(archivo.getFileName() + ", registro " + registro + ": " + e.getMessage(), e);
            }
        }
    }
}
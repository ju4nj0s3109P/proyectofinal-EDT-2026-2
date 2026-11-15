package co.edu.uq.sga;

import co.edu.uq.sga.interfaz.VentanaPrincipal;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio;
import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.servicios.ServicioMapa;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Path archivo = Path.of("data", "mapa-edificio.txt");

        try {
            CargadorMapaEdificio cargador = new CargadorMapaEdificio();
            var mapa = cargador.cargar(archivo);

            ServicioMapa.Ubicacion[] ubicaciones =
                    new ServicioMapa.Ubicacion[mapa.ubicaciones().tamanio()];
            int indice = 0;
            for (var ubicacion : mapa.ubicaciones()) {
                ubicaciones[indice++] = new ServicioMapa.Ubicacion(
                        ubicacion.id(), ubicacion.tipo(), ubicacion.nombre());
            }
            ServicioMapa.Pasillo[] pasillos =
                    new ServicioMapa.Pasillo[mapa.grafo().cantidadAristas()];
            indice = 0;
            for (var arista : mapa.grafo().aristas()) {
                pasillos[indice++] = new ServicioMapa.Pasillo(
                        arista.getOrigen(), arista.getDestino(), arista.getPeso());
            }
            ServicioMapa servicio = new ServicioMapa(ubicaciones, pasillos,
                    new CalculadorRutasGrafo(mapa.grafo()));
            servicio.consultarRecepcion();
            SwingUtilities.invokeLater(() -> new VentanaPrincipal(servicio).setVisible(true));
        } catch (IOException | IllegalStateException e) {
            String mensaje = "No se pudo cargar el mapa.\n\n"
                    + "Archivo: " + archivo.toAbsolutePath()
                    + "\n\n" + e.getMessage();

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            mensaje,
                            "Error al iniciar el SGA",
                            JOptionPane.ERROR_MESSAGE
                    )
            );
        }
    }
}

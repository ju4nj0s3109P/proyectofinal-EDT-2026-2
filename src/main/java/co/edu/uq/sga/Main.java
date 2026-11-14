package co.edu.uq.sga;

import co.edu.uq.sga.interfaz.VentanaPrincipal;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio;
import co.edu.uq.sga.persistencia.CargadorMapaEdificio.Ubicacion;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;

/** Punto de entrada pendiente de conectar con la interfaz. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        // TODO: inicializar escenario, servicios y ventana principal.
        System.out.println("SGA: estructura inicial");
        Path archivo = Path.of("data", "mapa-edificio.txt");

        try {
            CargadorMapaEdificio cargador = new CargadorMapaEdificio();
            var mapa = cargador.cargar(archivo);

            Ubicacion[] ubicaciones =
                    mapa.ubicaciones().aArreglo(Ubicacion[]::new);

            int cantidadPasillos = mapa.grafo().cantidadAristas();

            SwingUtilities.invokeLater(() -> {
                VentanaPrincipal ventana =
                        new VentanaPrincipal(ubicaciones, cantidadPasillos);
                ventana.setVisible(true);
            });

        } catch (IOException e) {
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

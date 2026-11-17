package co.edu.uq.sga;

import co.edu.uq.sga.interfaz.AplicacionSGA;
import javafx.application.Application;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        // Evita escenas en blanco en equipos donde la aceleración gráfica
        // de JavaFX/Direct3D no es compatible con el controlador disponible.
        // Debe configurarse antes de inicializar el toolkit de JavaFX.
        System.setProperty("prism.order", "sw");
        Application.launch(AplicacionSGA.class, args);
    }
}

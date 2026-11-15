package co.edu.uq.sga;

import co.edu.uq.sga.interfaz.AplicacionSGA;
import javafx.application.Application;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Application.launch(AplicacionSGA.class, args);
    }
}

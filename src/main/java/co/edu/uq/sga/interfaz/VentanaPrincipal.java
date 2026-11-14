package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.persistencia.CargadorMapaEdificio.Ubicacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private static final Color FONDO = new Color(255, 255, 255);
    private static final Color BANNER = new Color(255, 68, 0);
    private static final Color TEXTO = new Color(0, 0, 0);

    public VentanaPrincipal(Ubicacion[] ubicaciones, int cantidadPasillos) {
        super("SGA | Aparta-Suites UQ");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(1100, 720);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(FONDO);
        contenido.add(crearEncabezado(), BorderLayout.NORTH);
        contenido.add(crearPanelUbicaciones(ubicaciones), BorderLayout.CENTER);
        contenido.add(
                crearResumen(ubicaciones.length, cantidadPasillos),
                BorderLayout.SOUTH
        );

        setContentPane(contenido);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BANNER);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 32, 25, 32));

        JLabel titulo = new JLabel("Aparta-Suites UQ");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Sistema de Gestión de Alojamiento");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(209, 226, 218));
        subtitulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(subtitulo, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelUbicaciones(Ubicacion[] ubicaciones) {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 32, 20, 32));

        JLabel titulo = new JLabel("Ubicaciones del edificio");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TEXTO);

        String[] columnas = {"Código", "Nombre", "Tipo"};

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        for (Ubicacion ubicacion : ubicaciones) {
            modelo.addRow(new Object[]{
                    ubicacion.id(),
                    ubicacion.nombre(),
                    ubicacion.tipo().replace('_', ' ')
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 15));
        tabla.setForeground(TEXTO);
        tabla.setRowHeight(44);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(220, 237, 228));
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tabla.getTableHeader().setReorderingAllowed(false);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(
                BorderFactory.createLineBorder(new Color(220, 227, 223))
        );

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(desplazamiento, BorderLayout.CENTER);

        return panel;
    }

    private JLabel crearResumen(int ubicaciones, int pasillos) {
        JLabel resumen = new JLabel(
                ubicaciones + " ubicaciones cargadas · " + pasillos + " pasillos"
        );
        resumen.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resumen.setForeground(new Color(83, 103, 93));
        resumen.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));
        return resumen;
    }
}
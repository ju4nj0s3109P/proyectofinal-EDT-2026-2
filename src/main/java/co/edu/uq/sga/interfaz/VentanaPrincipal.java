package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.servicios.ServicioMapa;
import co.edu.uq.sga.servicios.ServicioMapa.Ubicacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private static final Color FONDO = new Color(245, 247, 243);
    private static final Color TEXTO = new Color(38, 56, 47);
    private static final Color SUAVE = new Color(108, 124, 114);
    private static final Color ACENTO = new Color(193, 93, 49);

    private final ServicioMapa servicio;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel vistas = new JPanel(tarjetas);

    private final JLabel titulo =
            etiqueta("Mapa del edificio", 27, TEXTO, true);

    private final JLabel distancia =
            etiqueta("—", 40, ACENTO, true);

    private final JLabel resultado =
            etiqueta("Selecciona un destino", 13, SUAVE, false);

    private final JTextArea recorrido =
            new JTextArea("Pulsa una ubicación del mapa o elige un destino.");

    private final JComboBox<Ubicacion> destinos;
    private final PanelMapa mapa;

    private final JToggleButton botonMapa =
            new BotonNavegacion("Mapa del edificio");

    private final JToggleButton botonUbicaciones =
            new BotonNavegacion("Ubicaciones");

    public VentanaPrincipal(ServicioMapa servicio) {
        super("SGA | Aparta-Suites UQ");

        this.servicio = servicio;

        Ubicacion[] ubicaciones = servicio.consultarUbicaciones();

        destinos = new JComboBox<>(ubicaciones);
        destinos.setSelectedIndex(-1);

        mapa = new PanelMapa(
                ubicaciones,
                servicio.consultarPasillos(),
                this::seleccionarDestino
        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 660));
        setSize(1320, 820);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(crearBarraLateral(), BorderLayout.WEST);

        JPanel contenido = new JPanel(new BorderLayout(0, 22));
        contenido.setOpaque(false);
        contenido.setBorder(
                BorderFactory.createEmptyBorder(28, 28, 24, 28)
        );
        contenido.add(crearEncabezado(), BorderLayout.NORTH);

        vistas.setOpaque(false);
        vistas.add(crearVistaMapa(), "mapa");
        vistas.add(crearTabla(ubicaciones), "ubicaciones");

        contenido.add(vistas, BorderLayout.CENTER);
        raiz.add(contenido, BorderLayout.CENTER);

        setContentPane(raiz);

        botonMapa.setSelected(true);
        actualizarNavegacion();

        destinos.addActionListener(evento -> actualizarRuta());

        setLocationRelativeTo(null);
    }
    private JPanel crearBarraLateral() {
        JPanel lateral = new JPanel(new BorderLayout(0, 38));
        lateral.setBackground(new Color(31, 53, 44));
        lateral.setPreferredSize(new Dimension(210, 0));
        lateral.setBorder(BorderFactory.createEmptyBorder(32, 18, 26, 18));
        JPanel marca = new JPanel(new GridLayout(0, 1, 0, 6));
        marca.setOpaque(false);
        marca.add(etiqueta("APARTA-SUITES", 16, Color.WHITE, true));
        marca.add(etiqueta("UQ · Salento", 13, new Color(183, 203, 189), false));
        lateral.add(marca, BorderLayout.NORTH);
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        ButtonGroup grupo = new ButtonGroup();
        for (JToggleButton boton : new JToggleButton[]{botonMapa, botonUbicaciones}) {
            grupo.add(boton);
            boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setMaximumSize(new Dimension(180, 46));
            boton.setBorder(BorderFactory.createEmptyBorder(13, 14, 13, 8));
            boton.setFocusPainted(true);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            menu.add(boton);
            menu.add(Box.createVerticalStrut(10));
        }
        botonMapa.addActionListener(evento -> mostrarVista("mapa"));
        botonUbicaciones.addActionListener(evento -> mostrarVista("ubicaciones"));
        lateral.add(menu, BorderLayout.CENTER);
        lateral.add(etiqueta("Gestión de alojamiento", 12,
                new Color(183, 203, 189), false), BorderLayout.SOUTH);
        return lateral;
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(etiqueta("APARTA-SUITES UQ / EDIFICIO", 11, SUAVE, true), BorderLayout.NORTH);
        panel.add(titulo, BorderLayout.CENTER);
        String resumen = servicio.consultarUbicaciones().length + " ubicaciones   ·   "
                + servicio.consultarPasillos().length + " pasillos";
        panel.add(etiqueta(resumen, 13, SUAVE, false), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearVistaMapa() {
        JPanel panel = new JPanel(new BorderLayout(18, 0));
        panel.setOpaque(false);
        JPanel zonaMapa = new JPanel(new BorderLayout(0, 12));
        zonaMapa.setOpaque(false);
        JScrollPane scroll = new JScrollPane(mapa);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 227, 218)));
        zonaMapa.add(scroll, BorderLayout.CENTER);
        zonaMapa.add(etiqueta("Esquema de conexiones · Distancias en metros", 12, SUAVE, false),
                BorderLayout.SOUTH);
        panel.add(zonaMapa, BorderLayout.CENTER);
        panel.add(crearDetalle(), BorderLayout.EAST);
        return panel;
    }

    private JPanel crearDetalle() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(258, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 231, 222)),
                BorderFactory.createEmptyBorder(24, 20, 24, 20)));
        JPanel superior = new JPanel();
        superior.setOpaque(false);
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        superior.add(etiqueta("Tu recorrido", 20, TEXTO, true));
        superior.add(Box.createVerticalStrut(12));
        superior.add(etiqueta("Desde " + servicio.consultarRecepcion(), 13, SUAVE, false));
        superior.add(Box.createVerticalStrut(24));
        superior.add(etiqueta("Destino", 12, TEXTO, true));
        superior.add(Box.createVerticalStrut(8));
        destinos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        destinos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        destinos.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinos.getAccessibleContext().setAccessibleName("Destino de la ruta");
        superior.add(destinos);
        superior.add(Box.createVerticalStrut(25));
        distancia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        superior.add(distancia);
        superior.add(Box.createVerticalStrut(8));
        resultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        superior.add(resultado);
        panel.add(superior, BorderLayout.NORTH);
        recorrido.setEditable(false);
        recorrido.setLineWrap(true);
        recorrido.setWrapStyleWord(true);
        recorrido.setFont(new Font("SansSerif", Font.PLAIN, 14));
        recorrido.setForeground(TEXTO);
        recorrido.setBackground(Color.WHITE);
        recorrido.setMargin(new Insets(8, 0, 0, 0));
        JScrollPane texto = new JScrollPane(recorrido);
        texto.setBorder(BorderFactory.createEmptyBorder());
        panel.add(texto, BorderLayout.CENTER);
        panel.add(etiqueta("Ruta calculada con Dijkstra", 11, SUAVE, false), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearTabla(Ubicacion[] ubicaciones) {
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Tipo"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        for (Ubicacion ubicacion : ubicaciones) {
            modelo.addRow(new Object[]{ubicacion.id(), ubicacion.nombre(),
                    ubicacion.tipo().replace('_', ' ')});
        }
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.setForeground(TEXTO);
        tabla.setRowHeight(48);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(249, 235, 222));
        tabla.setSelectionForeground(TEXTO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tabla.getTableHeader().setReorderingAllowed(false);
        JButton verRuta = new JButton("Ver ruta en el mapa");
        verRuta.setFont(new Font("SansSerif", Font.BOLD, 13));
        verRuta.setEnabled(false);
        tabla.getSelectionModel().addListSelectionListener(evento ->
                verRuta.setEnabled(tabla.getSelectedRow() >= 0));
        verRuta.addActionListener(evento -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                seleccionarDestino(ubicaciones[fila].id());
                mostrarVista("mapa");
            }
        });
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(verRuta);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void seleccionarDestino(String codigo) {
        for (int i = 0; i < destinos.getItemCount(); i++) {
            if (destinos.getItemAt(i).id().equals(codigo)) {
                destinos.setSelectedIndex(i);
                return;
            }
        }
    }

    private void actualizarRuta() {
        Ubicacion destino = (Ubicacion) destinos.getSelectedItem();
        if (destino == null) return;
        var ruta = servicio.calcularDesdeRecepcion(destino.id());
        mapa.mostrarRuta(destino.id(), ruta.ubicaciones());
        if (!ruta.existe()) {
            distancia.setText("—");
            resultado.setText("Sin conexión disponible");
            recorrido.setText("No hay una ruta desde recepción hasta " + destino.nombre() + ".");
            return;
        }
        distancia.setText(new java.text.DecimalFormat("0.##").format(ruta.metros()) + " m");
        resultado.setText("Distancia total");
        recorrido.setText(String.join("\n↓\n", ruta.ubicaciones()));
        recorrido.setCaretPosition(0);
    }

    private void mostrarVista(String vista) {
        tarjetas.show(vistas, vista);
        boolean mostrandoMapa = "mapa".equals(vista);
        botonMapa.setSelected(mostrandoMapa);
        botonUbicaciones.setSelected(!mostrandoMapa);
        titulo.setText(mostrandoMapa ? "Mapa del edificio" : "Ubicaciones del edificio");
        actualizarNavegacion();
    }

    private void actualizarNavegacion() {
        for (JToggleButton boton : new JToggleButton[]{botonMapa, botonUbicaciones}) {
            boton.setBackground(boton.isSelected() ? new Color(66, 88, 72) : new Color(31, 53, 44));
            boton.setForeground(boton.isSelected() ? Color.WHITE : new Color(190, 207, 194));
        }
    }

    private static final class BotonNavegacion extends JToggleButton {
        BotonNavegacion(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (hasFocus()) {
                g.setColor(new Color(183, 203, 189));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static JLabel etiqueta(String texto, int tamano, Color color, boolean negrita) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, tamano));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

}
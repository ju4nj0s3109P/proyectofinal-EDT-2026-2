package co.edu.uq.sga.interfaz;

import co.edu.uq.sga.servicios.ServicioMapa.Pasillo;
import co.edu.uq.sga.servicios.ServicioMapa.Ubicacion;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.*;
import java.util.function.Consumer;

/** Dibuja una distribución esquemática y comunica qué nodo selecciona el usuario. */
public final class PanelMapa extends JPanel {
    private static final Color ACENTO = new Color(193, 93, 49);
    private final Ubicacion[] ubicaciones;
    private final Pasillo[] pasillos;
    private final JButton[] nodos;
    private String[] ruta = new String[0];
    private String seleccionado;

    public PanelMapa(Ubicacion[] ubicaciones, Pasillo[] pasillos,
                     Consumer<String> alSeleccionar) {
        this.ubicaciones = ubicaciones.clone();
        this.pasillos = pasillos.clone();
        this.nodos = new JButton[ubicaciones.length];
        setLayout(null);
        setBackground(new Color(250, 250, 248));
        setPreferredSize(new Dimension(760, 490));
        for (int i = 0; i < ubicaciones.length; i++) {
            Ubicacion ubicacion = ubicaciones[i];
            JButton boton = new NodoBoton(ubicacion);
            boton.addActionListener(evento -> alSeleccionar.accept(ubicacion.id()));
            nodos[i] = boton;
            add(boton);
        }
    }

    public void mostrarRuta(String destino, String[] ruta) {
        this.seleccionado = destino;
        this.ruta = ruta.clone();
        repaint();
    }

    @Override public void doLayout() {
        double centroX = getWidth() / 2.0;
        double centroY = getHeight() / 2.0;
        double radioX = Math.max(0, centroX - 105);
        double radioY = Math.max(0, centroY - 65);
        for (int i = 0; i < nodos.length; i++) {
            double angulo = Math.PI + 2 * Math.PI * i / nodos.length;
            int x = (int) (centroX + radioX * Math.cos(angulo)) - 82;
            int y = (int) (centroY + radioY * Math.sin(angulo)) - 34;
            nodos[i].setBounds(x, y, 164, 68);
        }
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(231, 234, 230));
        for (int x = 16; x < getWidth(); x += 24) {
            for (int y = 16; y < getHeight(); y += 24) {
                g.fillOval(x, y, 2, 2);
            }
        }
        for (Pasillo pasillo : pasillos) {
            dibujarPasillo(g, pasillo);
        }
        g.dispose();
    }

    private void dibujarPasillo(Graphics2D g, Pasillo pasillo) {
        int origen = indiceDe(pasillo.origen());
        int destino = indiceDe(pasillo.destino());
        if (origen < 0 || destino < 0) {
            return;
        }
        Rectangle a = nodos[origen].getBounds();
        Rectangle b = nodos[destino].getBounds();
        int x1 = (int) a.getCenterX();
        int y1 = (int) a.getCenterY();
        int x2 = (int) b.getCenterX();
        int y2 = (int) b.getCenterY();
        boolean activo = perteneceARuta(pasillo.origen(), pasillo.destino());
        g.setColor(activo ? ACENTO : new Color(195, 204, 198));
        g.setStroke(new BasicStroke(activo ? 4f : 2f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        String texto = String.format(java.util.Locale.ROOT, "%.0f m", pasillo.metros());
        // Conservar decimales cuando el peso no sea entero.
        if (pasillo.metros() != Math.rint(pasillo.metros())) {
            texto = Double.toString(pasillo.metros()) + " m";
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        int ancho = g.getFontMetrics().stringWidth(texto) + 18;
        int medioX = (x1 + x2) / 2;
        int medioY = (y1 + y2) / 2;
        g.setColor(Color.WHITE);
        g.fillRoundRect(medioX - ancho / 2, medioY - 12, ancho, 25, 12, 12);
        g.setColor(activo ? ACENTO : new Color(92, 108, 99));
        g.drawString(texto, medioX - ancho / 2 + 9, medioY + 5);
    }

    private int indiceDe(String codigo) {
        for (int i = 0; i < ubicaciones.length; i++) {
            if (ubicaciones[i].id().equals(codigo)) return i;
        }
        return -1;
    }

    private boolean perteneceARuta(String origen, String destino) {
        for (int i = 1; i < ruta.length; i++) {
            if ((ruta[i - 1].equals(origen) && ruta[i].equals(destino))
                    || (ruta[i - 1].equals(destino) && ruta[i].equals(origen))) {
                return true;
            }
        }
        return false;
    }

    private boolean nodoEnRuta(String codigo) {
        for (String paso : ruta) {
            if (paso.equals(codigo)) return true;
        }
        return false;
    }

    private final class NodoBoton extends JButton {
        private final Ubicacion ubicacion;

        NodoBoton(Ubicacion ubicacion) {
            super("<html><center><b>" + escapar(ubicacion.id()) + "</b><br>"
                    + escapar(ubicacion.nombre()) + "</center></html>");
            this.ubicacion = ubicacion;
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setForeground(new Color(39, 57, 48));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(ubicacion.nombre() + " · " + ubicacion.tipo().replace('_', ' '));
            getAccessibleContext().setAccessibleName(ubicacion.nombre());
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean activo = nodoEnRuta(ubicacion.id());
            boolean elegido = ubicacion.id().equals(seleccionado);
            g.setColor(new Color(224, 229, 224));
            g.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 6, 18, 18);
            g.setColor(activo ? new Color(255, 242, 232) : Color.WHITE);
            g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 7, 18, 18);
            g.setColor(elegido || hasFocus() ? ACENTO : new Color(209, 219, 212));
            g.setStroke(new BasicStroke(elegido || hasFocus() ? 2f : 1f));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 8, 18, 18);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

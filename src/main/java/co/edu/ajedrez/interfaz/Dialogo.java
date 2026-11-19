package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Pieza;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Ventanas emergentes con el diseño del proyecto (fondo marfil, título
 * azul y botones BotonTema): opciones, confirmación, coronación e
 * información (estructuras).
 */
public final class Dialogo {

    private Dialogo() {
    }

    /**
     * Ventana con un mensaje y varios botones (uno debajo de otro).
     * @param padre     componente sobre el que se centra.
     * @param titulo    título.
     * @param html      mensaje (admite HTML sencillo).
     * @param opciones  textos de los botones.
     * @param principal índice del botón dorado (o -1).
     * @return índice del botón elegido, o -1 si se cerró con Escape o la X.
     */
    public static int opciones(Component padre, String titulo, String html, String[] opciones, int principal) {
        JDialog d = crear(padre, titulo);
        int[] elegido = {-1};
        JPanel cuerpo = contenedor();
        cuerpo.add(encabezado(titulo, html), BorderLayout.NORTH);
        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        for (int i = 0; i < opciones.length; i++) {
            Tema.BotonTema b = new Tema.BotonTema(opciones[i], i == principal);
            int indice = i;
            b.addActionListener(e -> {
                elegido[0] = indice;
                d.dispose();
            });
            botones.add(b);
        }
        cuerpo.add(botones, BorderLayout.CENTER);
        mostrar(d, cuerpo, padre);
        return elegido[0];
    }

    /**
     * Pide confirmación.
     * @param padre   componente padre.
     * @param titulo  título.
     * @param texto   pregunta.
     * @param textoSi texto del botón que confirma.
     * @return true si el usuario confirmó.
     */
    public static boolean confirmar(Component padre, String titulo, String texto, String textoSi) {
        return opciones(padre, titulo, texto, new String[]{textoSi, "CANCELAR"}, 0) == 0;
    }

    /**
     * Muestra información (por ejemplo, el estado de las estructuras).
     * @param padre  componente padre.
     * @param titulo título.
     * @param html   contenido HTML.
     */
    public static void informacion(Component padre, String titulo, String html) {
        JDialog d = crear(padre, titulo);
        JPanel cuerpo = contenedor();
        JLabel t = Tema.etiqueta(titulo.toUpperCase(), 18, Tema.AZUL, true);
        cuerpo.add(t, BorderLayout.NORTH);
        JLabel contenido = Tema.etiqueta(html, 13, Tema.AZUL, false);
        contenido.setVerticalAlignment(SwingConstants.TOP);
        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        contenido.setOpaque(true);
        contenido.setBackground(Color.WHITE);
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        scroll.setPreferredSize(new Dimension(560, 470));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cuerpo.add(scroll, BorderLayout.CENTER);
        Tema.BotonTema cerrar = new Tema.BotonTema("CERRAR", true);
        cerrar.addActionListener(e -> d.dispose());
        cuerpo.add(cerrar, BorderLayout.SOUTH);
        mostrar(d, cuerpo, padre);
    }

    /**
     * Pregunta en qué pieza se convierte un peón que corona.
     * @param padre componente padre.
     * @param color color del peón.
     * @return 'D', 'T', 'A' o 'C', o 0 si se canceló.
     */
    public static char coronacion(Component padre, ColorPieza color) {
        JDialog d = crear(padre, "Promoción de peón");
        char[] elegido = {0};
        JPanel cuerpo = contenedor();
        cuerpo.add(encabezado("Promoción de peón", "Elige la pieza en la que se convierte el peón:"), BorderLayout.NORTH);
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        fila.setOpaque(false);
        for (char tipo : new char[]{'D', 'T', 'A', 'C'}) {
            BotonPieza b = new BotonPieza(tipo, color);
            b.addActionListener(e -> {
                elegido[0] = tipo;
                d.dispose();
            });
            fila.add(b);
        }
        cuerpo.add(fila, BorderLayout.CENTER);
        Tema.BotonTema cancelar = new Tema.BotonTema("CANCELAR", false);
        cancelar.addActionListener(e -> d.dispose());
        cuerpo.add(cancelar, BorderLayout.SOUTH);
        mostrar(d, cuerpo, padre);
        return elegido[0];
    }

    /** Botón cuadrado con la imagen de una pieza y su nombre (casilla clara del tablero). */
    private static class BotonPieza extends JButton {
        private final char tipo;
        private final ColorPieza color;

        BotonPieza(char tipo, ColorPieza color) {
            super(Pieza.nombre(tipo));
            this.tipo = tipo;
            this.color = color;
            setPreferredSize(new Dimension(92, 112));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            Tema.suavizar(g);
            boolean hover = getModel().isRollover();
            g.setColor(new Color(239, 226, 202));
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
            g.setColor(hover ? Tema.ACENTO : new Color(178, 190, 195));
            g.setStroke(new BasicStroke(hover ? 3f : 1f));
            g.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
            Piezas.dibujar(g, tipo, color, (getWidth() - 70) / 2.0, 8, 70);
            g.setColor(Tema.AZUL);
            g.setFont(Tema.fuente(12, true));
            String t = getText();
            g.drawString(t, (getWidth() - g.getFontMetrics().stringWidth(t)) / 2, getHeight() - 12);
            g.dispose();
        }
    }

    // ----- utilidades internas -----

    private static JDialog crear(Component padre, String titulo) {
        Window w = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        JDialog d = new JDialog(w, titulo, JDialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return d;
    }

    private static JPanel contenedor() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Tema.MARFIL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(5, 0, 0, 0, Tema.ACENTO),
                BorderFactory.createEmptyBorder(20, 26, 22, 26)));
        return p;
    }

    private static JPanel encabezado(String titulo, String html) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel t = Tema.etiqueta(titulo.toUpperCase(), 20, Tema.AZUL, true);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel m = Tema.etiqueta("<html><div style='text-align:center;width:360px'>" + html + "</div></html>", 13, Tema.GRIS, false);
        m.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(t, BorderLayout.NORTH);
        p.add(m, BorderLayout.CENTER);
        return p;
    }

    private static void mostrar(JDialog d, JPanel cuerpo, Component padre) {
        d.setContentPane(cuerpo);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }
}

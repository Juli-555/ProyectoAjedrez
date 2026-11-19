package co.edu.ajedrez.interfaz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

/**
 * Paleta y componentes del diseño "Ajedrez Estructurado":
 * azul petróleo, dorado y marfil. Los botones se pintan a mano (BotonTema)
 * para que el tema nativo de Windows no cambie sus colores.
 */
public final class Tema {

    // ---- Paleta del proyecto ----
    public static final Color AZUL = new Color(27, 40, 54);          // #1B2836
    public static final Color AZUL2 = new Color(37, 56, 69);         // #253845
    public static final Color ACENTO = new Color(197, 157, 93);      // #C59D5D dorado
    public static final Color MARFIL = new Color(249, 246, 238);     // #F9F6EE
    public static final Color GRIS = new Color(107, 119, 130);
    public static final Color TEXTO_CLARO = new Color(196, 205, 205);
    public static final Color BORDE = new Color(220, 225, 222);
    public static final Color FONDO_LISTA = new Color(247, 248, 247);
    public static final Color ERROR = new Color(176, 58, 46);
    public static final Color RESALTE = new Color(250, 236, 205);    // fila encontrada

    private Tema() {
    }

    /**
     * Fuente del proyecto.
     * @param tamano tamaño en puntos.
     * @param negrita true para negrita.
     * @return la fuente.
     */
    public static Font fuente(int tamano, boolean negrita) {
        return new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, tamano);
    }

    /**
     * Crea una etiqueta con el estilo del tema.
     * @param texto   texto (admite HTML).
     * @param tamano  tamaño de letra.
     * @param color   color del texto.
     * @param negrita true para negrita.
     * @return la etiqueta.
     */
    public static JLabel etiqueta(String texto, int tamano, Color color, boolean negrita) {
        JLabel l = new JLabel(texto);
        l.setForeground(color);
        l.setFont(fuente(tamano, negrita));
        return l;
    }

    /** Activa el suavizado de bordes y texto. */
    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /** @return borde de campo de texto (normal o en error). */
    public static Border bordeCampo(boolean error) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(error ? ERROR : new Color(178, 190, 195), error ? 2 : 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    /**
     * Campo de texto blanco con texto azul.
     * @param columnas ancho aproximado.
     * @return el campo.
     */
    public static JTextField campo(int columnas) {
        JTextField t = new JTextField(columnas);
        t.setFont(fuente(14, false));
        t.setForeground(AZUL);
        t.setBackground(Color.WHITE);
        t.setCaretColor(AZUL);
        t.setBorder(bordeCampo(false));
        return t;
    }

    /**
     * Botón de tema propio: evita que el Look and Feel nativo de Windows
     * sustituya los colores. Primario = dorado con texto azul; secundario =
     * azul con texto blanco; deshabilitado = gris claro con texto gris oscuro.
     */
    public static class BotonTema extends JButton {
        private static final Color HOVER_PRIMARIO = new Color(216, 180, 117);
        private static final Color HOVER_SECUNDARIO = new Color(53, 78, 93);
        private static final Color FONDO_INACTIVO = new Color(226, 232, 235);
        private static final Color TEXTO_INACTIVO = new Color(83, 97, 106);
        private static final Color BORDE_INACTIVO = new Color(178, 190, 195);
        private boolean principal;

        /**
         * @param texto     texto del botón.
         * @param principal true = dorado; false = azul.
         */
        public BotonTema(String texto, boolean principal) {
            super(texto);
            this.principal = principal;
            setFont(fuente(13, true));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /** Cambia entre estilo dorado (principal) y azul. */
        public void setPrincipal(boolean p) {
            principal = p;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                suavizar(g);
                boolean activo = isEnabled();
                boolean hover = activo && getModel().isRollover();
                boolean pulsado = activo && getModel().isPressed() && getModel().isArmed();
                Color fondo = !activo ? FONDO_INACTIVO
                        : principal ? (hover ? HOVER_PRIMARIO : ACENTO) : (hover ? HOVER_SECUNDARIO : AZUL2);
                if (pulsado) {
                    fondo = fondo.darker();
                }
                Color texto = activo ? (principal ? AZUL : Color.WHITE) : TEXTO_INACTIVO;
                Color borde = !activo ? BORDE_INACTIVO : principal ? new Color(168, 125, 65) : new Color(28, 45, 57);
                g.setColor(fondo);
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                g.setColor(isFocusOwner() && activo ? new Color(66, 135, 190) : borde);
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                g.setColor(texto);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(getText(), Math.max(4, x), y);
            } finally {
                g.dispose();
            }
        }
    }

    /**
     * Opción de un control segmentado (modo de juego, dificultad, recorrido).
     * Seleccionada = dorado; sin seleccionar = azul (sobre fondo oscuro) o
     * blanco (sobre fondo claro).
     */
    public static class BotonOpcion extends JToggleButton {
        private final boolean sobreOscuro;

        /**
         * @param texto       texto de la opción.
         * @param sobreOscuro true si va sobre el fondo azul del menú.
         */
        public BotonOpcion(String texto, boolean sobreOscuro) {
            super(texto);
            this.sobreOscuro = sobreOscuro;
            setFont(fuente(13, true));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                suavizar(g);
                boolean sel = isSelected(), hover = getModel().isRollover();
                Color fondo = sel ? ACENTO : sobreOscuro ? (hover ? new Color(53, 78, 93) : AZUL) : (hover ? FONDO_LISTA : Color.WHITE);
                Color texto = sel ? AZUL : sobreOscuro ? Color.WHITE : AZUL;
                g.setColor(fondo);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (!sel && !sobreOscuro) {
                    g.setColor(new Color(178, 190, 195));
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g.setColor(texto);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            } finally {
                g.dispose();
            }
        }
    }

    /** Panel con fondo de color, borde fino y esquinas redondeadas. */
    public static class Tarjeta extends JPanel {
        private final Color fondo;
        private Color borde;
        private float grosor = 1f;

        /**
         * @param layout organizador.
         * @param fondo  color de fondo.
         * @param borde  color del borde (o null).
         */
        public Tarjeta(LayoutManager layout, Color fondo, Color borde) {
            super(layout);
            this.fondo = fondo;
            this.borde = borde;
            setOpaque(false);
        }

        /** Cambia el borde (por ejemplo, dorado cuando es el turno del jugador). */
        public void setBorde(Color borde, float grosor) {
            this.borde = borde;
            this.grosor = grosor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            suavizar(g);
            g.setColor(fondo);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (borde != null) {
                g.setColor(borde);
                g.setStroke(new BasicStroke(grosor));
                int m = Math.round(grosor / 2);
                g.drawRoundRect(m, m, getWidth() - 1 - 2 * m, getHeight() - 1 - 2 * m, 10, 10);
            }
            g.dispose();
            super.paintComponent(gr);
        }
    }
}

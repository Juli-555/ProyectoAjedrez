package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.modelo.ColorPieza;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * Franja de un jugador encima o debajo del tablero: color, nombre, piezas
 * que ha capturado y ventaja de material. Se resalta en dorado en su turno.
 */
public class PanelJugador extends JPanel {

    private ColorPieza color = ColorPieza.BLANCO;
    private String nombre = "";
    private boolean enTurno = false;
    private String capturadas = "";   // letras de las piezas capturadas (ej. "DCPP")
    private int ventaja = 0;

    /** Crea la franja vacía. */
    public PanelJugador() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 40));
    }

    /**
     * Actualiza los datos y repinta.
     * @param color      color del jugador.
     * @param nombre     nombre a mostrar.
     * @param enTurno    true si le toca.
     * @param capturadas letras de las piezas rivales capturadas.
     * @param ventaja    puntos de material a favor (0 si no hay).
     */
    public void mostrar(ColorPieza color, String nombre, boolean enTurno, String capturadas, int ventaja) {
        this.color = color;
        this.nombre = nombre;
        this.enTurno = enTurno;
        this.capturadas = capturadas;
        this.ventaja = ventaja;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr.create();
        Tema.suavizar(g);
        int w = getWidth(), h = getHeight();
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g.setColor(enTurno ? Tema.ACENTO : Tema.BORDE);
        g.setStroke(new BasicStroke(enTurno ? 2.5f : 1f));
        g.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);
        if (enTurno) {
            g.setColor(Tema.ACENTO);
            g.fillRoundRect(1, 1, 6, h - 2, 6, 6);
        }
        // indicador de color
        g.setColor(color == ColorPieza.BLANCO ? Tema.MARFIL : Tema.AZUL);
        g.fillOval(16, h / 2 - 7, 14, 14);
        g.setColor(Tema.AZUL);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(16, h / 2 - 7, 14, 14);
        // nombre
        g.setFont(Tema.fuente(14, true));
        g.setColor(Tema.AZUL);
        g.drawString(nombre, 40, h / 2 + 5);
        // ventaja y piezas capturadas (de derecha a izquierda)
        int x = w - 14;
        if (ventaja > 0) {
            g.setFont(Tema.fuente(12, true));
            g.setColor(Tema.GRIS);
            String txt = "+" + ventaja;
            x -= g.getFontMetrics().stringWidth(txt);
            g.drawString(txt, x, h / 2 + 5);
            x -= 8;
        }
        int tam = 24;
        for (int i = capturadas.length() - 1; i >= 0; i--) {
            x -= tam - 7;
            Piezas.dibujar(g, capturadas.charAt(i), color.contrario(), x - 7, (h - tam) / 2.0, tam);
        }
        g.dispose();
    }
}

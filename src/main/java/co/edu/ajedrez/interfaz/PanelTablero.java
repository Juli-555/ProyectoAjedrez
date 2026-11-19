package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.logica.MotorAjedrez;
import co.edu.ajedrez.modelo.Movimiento;
import co.edu.ajedrez.modelo.Pieza;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Tablero Swing escalable: 64 casillas dibujadas con Java2D, piezas PNG y
 * eventos de ratón (clic o arrastre). No conoce las reglas: solo dibuja lo
 * que le indica PanelJuego.
 */
public class PanelTablero extends JPanel {

    /** Quien recibe los eventos del tablero (PanelJuego). */
    public interface Oyente {
        /**
         * Se presionó una casilla.
         * @return true si se seleccionó una pieza propia (empieza el arrastre).
         */
        boolean alPresionar(int f, int c);

        /** Se soltó una pieza arrastrada sobre (f,c). */
        void alSoltar(int f, int c);
    }

    // Colores del diseño del proyecto
    private static final Color LUZ = new Color(239, 226, 202);
    private static final Color SOMBRA = new Color(112, 140, 121);
    private static final Color MARCO = new Color(53, 66, 60);
    private static final Color ULTIMA = new Color(254, 207, 111, 92);
    private static final Color RESALTADO = new Color(245, 183, 65, 135);
    private static final Color PUNTO = new Color(26, 71, 58, 130);
    private static final Color COORD_LUZ = new Color(94, 117, 97);
    private static final Color COORD_SOMBRA = new Color(245, 230, 205);

    private final Oyente oyente;
    private MotorAjedrez tablero;
    private boolean girado = false;               // true = negras abajo
    private int[] seleccion = null;               // {f, c}
    private ListaDoble<Movimiento> destinos = new ListaDoble<>();
    private Movimiento ultima = null;
    private int[] jaque = null;                   // {f, c} del rey en jaque

    // Animación
    private Pieza piezaAnimada = null;
    private int[] animDesde, animHasta;
    private long animInicio;
    private final Timer timerAnim;

    // Arrastre
    private Pieza piezaArrastrada = null;
    private int arrastreX, arrastreY, inicioX, inicioY;
    private boolean movido = false;
    private int[] arrastreDesde;

    /**
     * @param oyente objeto que recibe los clics.
     */
    public PanelTablero(Oyente oyente) {
        this.oyente = oyente;
        setOpaque(false);
        setPreferredSize(new Dimension(615, 615));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        timerAnim = new Timer(15, e -> {
            if (System.currentTimeMillis() - animInicio > 180) {
                piezaAnimada = null;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                presionar(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                arrastrar(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                soltar(e);
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    // ----- Geometría -----

    /** @return lado del tablero en píxeles (cuadrado centrado). */
    private int lado() {
        return Math.max(160, (Math.min(getWidth(), getHeight()) - 28) / 8 * 8);
    }

    private int origenX() {
        return (getWidth() - lado()) / 2;
    }

    private int origenY() {
        return (getHeight() - lado()) / 2;
    }

    private double tam() {
        return lado() / 8.0;
    }

    /** @return posición en pantalla {x, y} de la casilla (tiene en cuenta el giro). */
    private double[] aPantalla(int f, int c) {
        int vf = girado ? 7 - f : f, vc = girado ? 7 - c : c;
        return new double[]{origenX() + vc * tam(), origenY() + vf * tam()};
    }

    /** @return casilla {f, c} bajo el punto, o null si está fuera. */
    private int[] casillaEn(int x, int y) {
        int c = (int) Math.floor((x - origenX()) / tam());
        int f = (int) Math.floor((y - origenY()) / tam());
        if (!MotorAjedrez.dentro(f, c)) {
            return null;
        }
        return girado ? new int[]{7 - f, 7 - c} : new int[]{f, c};
    }

    // ----- Ratón -----

    private void presionar(MouseEvent e) {
        int[] cas = casillaEn(e.getX(), e.getY());
        if (cas == null || tablero == null) {
            return;
        }
        if (oyente.alPresionar(cas[0], cas[1])) {
            piezaArrastrada = tablero.obtener(cas[0], cas[1]);
            arrastreDesde = cas;
            arrastreX = inicioX = e.getX();
            arrastreY = inicioY = e.getY();
            movido = false;
        }
        repaint();
    }

    private void arrastrar(MouseEvent e) {
        if (piezaArrastrada == null) {
            return;
        }
        arrastreX = e.getX();
        arrastreY = e.getY();
        if (Math.abs(arrastreX - inicioX) + Math.abs(arrastreY - inicioY) > 5) {
            movido = true;
        }
        repaint();
    }

    private void soltar(MouseEvent e) {
        if (piezaArrastrada == null) {
            return;
        }
        piezaArrastrada = null;
        int[] cas = casillaEn(e.getX(), e.getY());
        if (movido && cas != null && (cas[0] != arrastreDesde[0] || cas[1] != arrastreDesde[1])) {
            oyente.alSoltar(cas[0], cas[1]);
        }
        repaint();
    }

    // ----- Estado que dibuja -----

    /** Asigna el tablero a dibujar y limpia marcas. */
    public void usarTablero(MotorAjedrez t) {
        tablero = t;
        limpiarSeleccion();
        ultima = null;
        piezaAnimada = null;
        repaint();
    }

    /** Quita la selección y los destinos marcados. */
    public void limpiarSeleccion() {
        seleccion = null;
        destinos = new ListaDoble<>();
        repaint();
    }

    /** Marca una pieza seleccionada y sus destinos legales. */
    public void seleccionar(int f, int c, ListaDoble<Movimiento> destinos) {
        this.seleccion = new int[]{f, c};
        this.destinos = destinos;
        repaint();
    }

    /** @return la casilla seleccionada o null. */
    public int[] getSeleccion() {
        return seleccion;
    }

    /** @return los destinos de la pieza seleccionada. */
    public ListaDoble<Movimiento> getDestinos() {
        return destinos;
    }

    public void setUltima(Movimiento m) {
        ultima = m;
        repaint();
    }

    public void setJaque(int[] casillaRey) {
        jaque = casillaRey;
        repaint();
    }

    public boolean isGirado() {
        return girado;
    }

    public void setGirado(boolean g) {
        girado = g;
        repaint();
    }

    /** Anima el desplazamiento de la pieza de un movimiento. */
    public void animar(Movimiento m) {
        piezaAnimada = m.getPieza();
        animDesde = new int[]{m.getDesdeF(), m.getDesdeC()};
        animHasta = new int[]{m.getHastaF(), m.getHastaC()};
        animInicio = System.currentTimeMillis();
        timerAnim.restart();
    }

    // ----- Dibujo -----

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr.create();
        Tema.suavizar(g);
        double t = tam();
        int ox = origenX(), oy = origenY(), lado = (int) Math.round(t * 8);

        // Marco
        g.setColor(MARCO);
        g.fillRoundRect(ox - 7, oy - 7, lado + 14, lado + 14, 12, 12);

        // 1. Casillas
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                double[] p = aPantalla(f, c);
                g.setColor((f + c) % 2 == 0 ? LUZ : SOMBRA);
                g.fillRect((int) Math.floor(p[0]), (int) Math.floor(p[1]), (int) Math.ceil(t), (int) Math.ceil(t));
            }
        }
        // 2. Última jugada
        if (ultima != null) {
            g.setColor(ULTIMA);
            for (int[] cas : new int[][]{{ultima.getDesdeF(), ultima.getDesdeC()}, {ultima.getHastaF(), ultima.getHastaC()}}) {
                double[] p = aPantalla(cas[0], cas[1]);
                g.fillRect((int) p[0], (int) p[1], (int) Math.ceil(t), (int) Math.ceil(t));
            }
        }
        // 3. Selección
        if (seleccion != null) {
            double[] p = aPantalla(seleccion[0], seleccion[1]);
            g.setColor(RESALTADO);
            g.fillRect((int) p[0], (int) p[1], (int) Math.ceil(t), (int) Math.ceil(t));
        }
        // 4. Rey en jaque (resplandor rojo)
        if (jaque != null) {
            double[] p = aPantalla(jaque[0], jaque[1]);
            float r = (float) (t * 0.62);
            g.setPaint(new RadialGradientPaint((float) (p[0] + t / 2), (float) (p[1] + t / 2), r,
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{new Color(255, 40, 30, 240), new Color(230, 30, 20, 115), new Color(200, 0, 0, 0)}));
            g.fillRect((int) p[0], (int) p[1], (int) Math.ceil(t), (int) Math.ceil(t));
        }
        // 5. Coordenadas
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, (int) (t * 0.17))));
        for (int i = 0; i < 8; i++) {
            int filaAbajo = girado ? 0 : 7, colIzq = girado ? 7 : 0;
            double[] p = aPantalla(filaAbajo, i);
            g.setColor((filaAbajo + i) % 2 == 0 ? COORD_LUZ : COORD_SOMBRA);
            String letra = String.valueOf((char) ('a' + i));
            g.drawString(letra, (float) (p[0] + t - t * 0.06 - g.getFontMetrics().stringWidth(letra)), (float) (p[1] + t - t * 0.05));
            p = aPantalla(i, colIzq);
            g.setColor((i + colIzq) % 2 == 0 ? COORD_LUZ : COORD_SOMBRA);
            g.drawString(String.valueOf(8 - i), (float) (p[0] + t * 0.05), (float) (p[1] + t * 0.05 + g.getFontMetrics().getAscent()));
        }
        if (tablero == null) {
            g.dispose();
            return;
        }
        // 6. Piezas (la animada y la arrastrada se dibujan al final)
        double pad = Math.max(2, t / 17);
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Pieza pz = tablero.obtener(f, c);
                if (pz == null || pz == piezaAnimada || (pz == piezaArrastrada && movido)) {
                    continue;
                }
                double[] p = aPantalla(f, c);
                Piezas.dibujar(g, pz.getTipo(), pz.getColor(), p[0] + pad, p[1] + pad, t - 2 * pad);
            }
        }
        // 7. Destinos posibles: punto (casilla vacía) o anillo (captura)
        g.setColor(PUNTO);
        destinos.recorrerAdelante(m -> {
            double[] p = aPantalla(m.getHastaF(), m.getHastaC());
            if (m.getCapturada() != null) {
                g.setStroke(new BasicStroke((float) Math.max(2, t / 16)));
                g.draw(new Ellipse2D.Double(p[0] + 5, p[1] + 5, t - 10, t - 10));
            } else {
                g.fill(new Ellipse2D.Double(p[0] + t / 2 - t / 9, p[1] + t / 2 - t / 9, t * 2 / 9, t * 2 / 9));
            }
        });
        // 8. Pieza animada
        if (piezaAnimada != null) {
            double avance = Math.min(1, (System.currentTimeMillis() - animInicio) / 180.0);
            double suave = 1 - Math.pow(1 - avance, 3);
            double[] a = aPantalla(animDesde[0], animDesde[1]), b = aPantalla(animHasta[0], animHasta[1]);
            Piezas.dibujar(g, piezaAnimada.getTipo(), piezaAnimada.getColor(),
                    a[0] + (b[0] - a[0]) * suave + pad, a[1] + (b[1] - a[1]) * suave + pad, t - 2 * pad);
        }
        // 9. Pieza arrastrada (sigue al ratón)
        if (piezaArrastrada != null && movido) {
            double s = t * 1.08;
            Piezas.dibujar(g, piezaArrastrada.getTipo(), piezaArrastrada.getColor(), arrastreX - s / 2, arrastreY - s / 2, s);
        }
        g.dispose();
    }
}

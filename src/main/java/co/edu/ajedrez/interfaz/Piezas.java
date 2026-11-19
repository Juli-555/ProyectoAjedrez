package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.modelo.ColorPieza;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Carga una sola vez los 12 PNG de las piezas (carpeta resources/piezas) y
 * los dibuja. Nombres: b = blancas, n = negras; K rey, Q dama, R torre,
 * B alfil, N caballo, P peón (p. ej. "bK.png", "nN.png").
 * Si falta una imagen se dibuja el símbolo Unicode de la pieza.
 */
public final class Piezas {

    private static final String TIPOS_ESP = "RDTACP";        // letras usadas por el motor
    private static final String TIPOS_PNG = "KQRBNP";        // letras de los archivos PNG
    private static final String GLIFOS_B = "♔♕♖♗♘♙";
    private static final String GLIFOS_N = "♚♛♜♝♞♟";
    private static final BufferedImage[] IMAGENES = new BufferedImage[12];

    static {
        for (int i = 0; i < 12; i++) {
            String nombre = (i < 6 ? "b" : "n") + TIPOS_PNG.charAt(i % 6) + ".png";
            try (InputStream in = Piezas.class.getResourceAsStream("/piezas/" + nombre)) {
                if (in != null) {
                    IMAGENES[i] = ImageIO.read(in);
                }
            } catch (IOException e) {
                System.err.println("Recurso no disponible: " + nombre);
            }
        }
    }

    private Piezas() {
    }

    /**
     * Dibuja una pieza dentro de un cuadrado.
     * @param g     gráficos.
     * @param tipo  letra del motor ('R','D','T','A','C','P').
     * @param color color de la pieza.
     * @param x     esquina izquierda.
     * @param y     esquina superior.
     * @param tam   lado del cuadrado en píxeles.
     */
    public static void dibujar(Graphics2D g, char tipo, ColorPieza color, double x, double y, double tam) {
        int i = TIPOS_ESP.indexOf(tipo) + (color == ColorPieza.BLANCO ? 0 : 6);
        BufferedImage img = IMAGENES[i];
        if (img != null) {
            g.drawImage(img, (int) Math.round(x), (int) Math.round(y), (int) Math.round(tam), (int) Math.round(tam), null);
            return;
        }
        String glifo = String.valueOf((color == ColorPieza.BLANCO ? GLIFOS_B : GLIFOS_N).charAt(i % 6));
        g.setFont(new Font("Serif", Font.PLAIN, (int) (tam * 0.82)));
        g.setColor(color == ColorPieza.BLANCO ? Color.WHITE : new Color(32, 37, 36));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(glifo, (float) (x + (tam - fm.stringWidth(glifo)) / 2), (float) (y + (tam + fm.getAscent()) / 2 - fm.getDescent() / 2.0));
    }

    /**
     * Ícono de una pieza (coronación, piezas capturadas, reglas).
     * @param tipo  letra del motor.
     * @param color color.
     * @param tam   tamaño en píxeles.
     * @return el ícono.
     */
    public static ImageIcon icono(char tipo, ColorPieza color, int tam) {
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.suavizar(g);
        dibujar(g, tipo, color, 0, 0, tam);
        g.dispose();
        return new ImageIcon(img);
    }
}

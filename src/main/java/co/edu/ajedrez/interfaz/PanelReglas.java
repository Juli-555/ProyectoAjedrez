package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.modelo.ColorPieza;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Pantalla "Reglas y controles": cómo se mueve cada pieza, jugadas
 * especiales, cómo se gana/pierde/empata y controles del juego.
 */
public final class PanelReglas {

    private static final int ANCHO = 860;

    private PanelReglas() {
    }

    /** @return el panel de reglas listo para agregar a la ventana. */
    public static JPanel crear() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(20, 26, 24, 26)));

        agregar(c, Tema.etiqueta("REGLAS Y CONTROLES", 18, Tema.AZUL, true));
        agregar(c, parrafo("<span style='color:#6B7782'>Objetivo: dar <b>jaque mate</b> al rey contrario, es decir, atacarlo sin que tenga "
                + "ninguna jugada para escapar. Juegan primero las blancas y luego se alternan los turnos.</span>"));
        agregar(c, subtitulo("CÓMO SE MUEVE CADA PIEZA"));
        JPanel piezas = new JPanel(new GridLayout(3, 2, 10, 8));
        piezas.setOpaque(false);
        piezas.add(pieza('R', "<b>Rey:</b> una casilla en cualquier dirección. Nunca puede quedar en jaque."));
        piezas.add(pieza('D', "<b>Dama:</b> cualquier número de casillas en línea recta o diagonal."));
        piezas.add(pieza('T', "<b>Torre:</b> cualquier número de casillas en línea recta."));
        piezas.add(pieza('A', "<b>Alfil:</b> cualquier número de casillas en diagonal."));
        piezas.add(pieza('C', "<b>Caballo:</b> salta en forma de L (2 + 1). Es la única pieza que salta sobre otras."));
        piezas.add(pieza('P', "<b>Peón:</b> avanza una casilla (dos en su primera jugada) y captura en diagonal."));
        agregar(c, piezas);
        agregar(c, subtitulo("JUGADAS ESPECIALES"));
        agregar(c, parrafo("• <b>Enroque:</b> el rey se mueve dos casillas hacia una torre y la torre salta al otro lado. Solo si ninguno se ha "
                + "movido, no hay piezas en medio y el rey no está, no pasa ni cae en jaque. Para enrocar, mueve el rey dos casillas.<br>"
                + "• <b>Captura al paso:</b> si un peón rival avanza dos casillas y queda al lado de un peón tuyo, puedes capturarlo como si "
                + "hubiera avanzado una, solo en la jugada inmediata.<br>"
                + "• <b>Promoción:</b> el peón que llega a la última fila se convierte en dama, torre, alfil o caballo (el juego pregunta cuál)."));
        agregar(c, subtitulo("CÓMO SE GANA, SE PIERDE O SE EMPATA"));
        agregar(c, parrafo("• <b>Gana</b> quien da jaque mate, o si el rival se rinde. <b>Pierde</b> quien recibe jaque mate o se rinde.<br>"
                + "• <b>Tablas automáticas:</b> rey ahogado, material insuficiente, 75 jugadas de cada bando sin capturas ni movimientos de "
                + "peón, o la misma posición 5 veces.<br>"
                + "• <b>Tablas que se reclaman</b> (botón RECLAMAR TABLAS): la misma posición 3 veces o 50 jugadas de cada bando sin capturas "
                + "ni movimientos de peón."));
        agregar(c, subtitulo("CONTROLES"));
        agregar(c, parrafo("• <b>Mover:</b> clic en una pieza de tu turno y luego en una casilla marcada (punto = casilla libre, anillo = captura). "
                + "También puedes arrastrar la pieza.<br>"
                + "• <b>DESHACER / REHACER</b> (Ctrl+Z / Ctrl+Y) usan pilas LIFO. Contra la computadora se deshace tu jugada y la de ella.<br>"
                + "• <b>REPRODUCIR PARTIDA</b> usa la cola FIFO: la partida se repite sola desde el inicio. PAUSA la detiene y PASO avanza "
                + "una jugada.<br>"
                + "• <b>◀ ATRÁS / ADELANTE ▶</b> recorren el historial con la lista doble (también con las flechas del teclado o haciendo "
                + "clic en una jugada del registro). <b>SALIR REPR.</b> vuelve a la partida real sin cambios.<br>"
                + "• <b>ARCHIVO BST</b>: cada partida terminada se guarda sola en el árbol (y en un archivo, así que se conserva al cerrar)."));

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        c.setMaximumSize(new Dimension(ANCHO, Integer.MAX_VALUE));
        JPanel centrador = new JPanel();
        centrador.setLayout(new BoxLayout(centrador, BoxLayout.X_AXIS));
        centrador.setOpaque(false);
        centrador.add(Box.createHorizontalGlue());
        centrador.add(c);
        centrador.add(Box.createHorizontalGlue());
        JPanel relleno = new JPanel(new BorderLayout());
        relleno.setBackground(Tema.MARFIL);
        relleno.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        relleno.add(centrador, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(relleno);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        envoltura.add(scroll);
        return envoltura;
    }

    private static void agregar(JPanel c, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(comp);
        c.add(Box.createVerticalStrut(6));
    }

    private static JLabel subtitulo(String t) {
        JLabel l = Tema.etiqueta(t, 13, new Color(150, 110, 50), true);
        l.setBorder(BorderFactory.createEmptyBorder(12, 0, 2, 0));
        return l;
    }

    private static JLabel parrafo(String html) {
        return Tema.etiqueta("<html><div style='width:" + (ANCHO - 70) + "px'>" + html + "</div></html>", 13, Tema.AZUL, false);
    }

    private static JPanel pieza(char tipo, String html) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Tema.FONDO_LISTA);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JLabel icono = new JLabel(Piezas.icono(tipo, ColorPieza.BLANCO, 40));
        icono.setOpaque(true);
        icono.setBackground(new Color(112, 140, 121));
        p.add(icono, BorderLayout.WEST);
        p.add(Tema.etiqueta("<html><div style='width:310px'>" + html + "</div></html>", 13, Tema.AZUL, false), BorderLayout.CENTER);
        return p;
    }
}

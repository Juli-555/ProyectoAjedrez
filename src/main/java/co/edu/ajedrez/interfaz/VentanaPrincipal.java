package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.servicios.RegistroPartidas;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Ventana principal. Un CardLayout cambia entre las pantallas:
 * menú (azul), juego, archivo BST de partidas y reglas. Las tres últimas
 * comparten el encabezado azul con MENÚ, REGLAS, ARCHIVO BST y NUEVA PARTIDA.
 */
public final class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel pantalla = new JPanel(tarjetas);
    private final RegistroPartidas registros = new RegistroPartidas();
    private final PanelMenu panelMenu;
    private final PanelJuego panelJuego;
    private final PanelPartidas panelPartidas;
    private String pantallaActual = "inicio";

    // Aviso flotante (pantallas que no tienen barra de mensajes)
    private String textoAviso = null;
    private boolean avisoError = false;
    private final Timer timerAviso;

    /** Construye la interfaz y muestra el menú. */
    public VentanaPrincipal() {
        super("Ajedrez Estructurado | Proyecto Final - Estructuras de Datos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1180, 800);
        setIconImage(Piezas.icono('C', co.edu.ajedrez.modelo.ColorPieza.BLANCO, 64).getImage());

        panelMenu = new PanelMenu(this);
        panelJuego = new PanelJuego(this);
        panelPartidas = new PanelPartidas(this);
        pantalla.add(panelMenu, "inicio");
        pantalla.add(conEncabezado(panelJuego, false), "juego");
        pantalla.add(conEncabezado(panelPartidas, true), "partidas");
        pantalla.add(conEncabezado(PanelReglas.crear(), true), "reglas");
        setContentPane(pantalla);
        tarjetas.show(pantalla, "inicio");

        JComponent capa = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                pintarAviso((Graphics2D) g, getWidth(), getHeight());
            }
        };
        setGlassPane(capa);
        timerAviso = new Timer(2600, e -> {
            textoAviso = null;
            getGlassPane().setVisible(false);
        });
        timerAviso.setRepeats(false);
        setLocationRelativeTo(null);
    }

    /**
     * Envuelve una pantalla con el encabezado azul del proyecto.
     * @param contenido    pantalla.
     * @param botonPartida true para mostrar el botón PARTIDA (volver al juego).
     */
    private JPanel conEncabezado(JPanel contenido, boolean botonPartida) {
        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(Tema.MARFIL);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Tema.AZUL);
        header.setBorder(BorderFactory.createEmptyBorder(13, 22, 13, 22));
        header.add(Tema.etiqueta("♞   AJEDREZ ESTRUCTURADO", 19, Color.WHITE, true), BorderLayout.WEST);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        Tema.BotonTema home = new Tema.BotonTema("MENÚ", false);
        Tema.BotonTema reglas = new Tema.BotonTema("REGLAS", false);
        Tema.BotonTema archivo = new Tema.BotonTema("ARCHIVO BST", false);
        Tema.BotonTema nueva = new Tema.BotonTema("NUEVA PARTIDA", true);
        home.addActionListener(e -> irAlMenu());
        reglas.addActionListener(e -> mostrar("reglas"));
        archivo.addActionListener(e -> mostrar("partidas"));
        nueva.addActionListener(e -> panelJuego.nuevaConMismosJugadores());
        acciones.add(home);
        if (botonPartida) {
            Tema.BotonTema volver = new Tema.BotonTema("PARTIDA", false);
            volver.setToolTipText("Volver a la partida actual");
            volver.addActionListener(e -> panelJuego.reanudar());
            acciones.add(volver);
        }
        acciones.add(reglas);
        acciones.add(archivo);
        acciones.add(nueva);
        header.add(acciones, BorderLayout.EAST);
        fondo.add(header, BorderLayout.NORTH);
        fondo.add(contenido, BorderLayout.CENTER);
        return fondo;
    }

    /** Vuelve al menú (pide confirmación si hay una partida a medias). */
    public void irAlMenu() {
        if (pantallaActual.equals("juego") && !panelJuego.confirmarAbandono()) {
            return;
        }
        mostrar("inicio");
    }

    /**
     * Cambia de pantalla.
     * @param nombre "inicio", "juego", "partidas" o "reglas".
     */
    public void mostrar(String nombre) {
        if (!nombre.equals("juego")) {
            panelJuego.detenerTodo();
        }
        if (nombre.equals("partidas")) {
            panelPartidas.actualizar();
        }
        pantallaActual = nombre;
        tarjetas.show(pantalla, nombre);
    }

    /** @return nombre de la pantalla visible. */
    public String getPantallaActual() {
        return pantallaActual;
    }

    /**
     * Muestra un mensaje al usuario. En la pantalla de juego va en la barra
     * inferior (como en el diseño original); en las demás, como aviso flotante.
     * @param texto   mensaje.
     * @param esError true si es un error (se muestra en rojo).
     */
    public void aviso(String texto, boolean esError) {
        if (pantallaActual.equals("juego")) {
            panelJuego.mensaje(texto, esError);
            return;
        }
        textoAviso = texto;
        avisoError = esError;
        getGlassPane().setVisible(true);
        getGlassPane().repaint();
        timerAviso.restart();
    }

    private void pintarAviso(Graphics2D g, int ancho, int alto) {
        if (textoAviso == null) {
            return;
        }
        Tema.suavizar(g);
        g.setFont(Tema.fuente(14, true));
        int w = g.getFontMetrics().stringWidth(textoAviso) + 44, h = 44;
        int x = (ancho - w) / 2, y = alto - h - 28;
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect(x + 2, y + 4, w, h, 10, 10);
        g.setColor(Tema.AZUL);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(avisoError ? new Color(215, 96, 79) : Tema.ACENTO);
        g.fillRoundRect(x, y, 6, h, 6, 6);
        g.setColor(Color.WHITE);
        g.drawString(textoAviso, x + 24, y + 27);
    }

    public RegistroPartidas getRegistros() {
        return registros;
    }

    public PanelJuego getPanelJuego() {
        return panelJuego;
    }

    public PanelPartidas getPanelPartidas() {
        return panelPartidas;
    }

    public PanelMenu getPanelMenu() {
        return panelMenu;
    }
}

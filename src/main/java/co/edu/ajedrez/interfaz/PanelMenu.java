package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.servicios.GestorPartida;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Portada azul del proyecto con la configuración de la partida: modo de
 * juego, nombres y dificultad. Valida los nombres antes de empezar: si hay
 * un dato inválido muestra el mensaje y el juego NO se cierra.
 */
public class PanelMenu extends JPanel {

    private static final Color AMARILLO_ERROR = new Color(255, 190, 170);

    private final VentanaPrincipal ventana;
    private final Tema.BotonOpcion modoDos = new Tema.BotonOpcion("DOS JUGADORES", true);
    private final Tema.BotonOpcion modoIA = new Tema.BotonOpcion("CONTRA LA COMPUTADORA", true);
    private final Tema.BotonOpcion[] dificultad = {
        new Tema.BotonOpcion("FÁCIL", true), new Tema.BotonOpcion("NORMAL", true), new Tema.BotonOpcion("DIFÍCIL", true)};
    private final JTextField nombreBlancas = Tema.campo(12);
    private final JTextField nombreNegras = Tema.campo(12);
    private final JLabel error = Tema.etiqueta(" ", 13, AMARILLO_ERROR, true);

    /** @param ventana ventana principal. */
    public PanelMenu(VentanaPrincipal ventana) {
        super(new BorderLayout());
        this.ventana = ventana;
        setBackground(Tema.AZUL);

        // Barra superior
        JPanel barra = caja(26, 42, 10, 42);
        barra.setLayout(new BorderLayout());
        barra.add(Tema.etiqueta("♟  ESTRUCTURAS DE DATOS  /  PROYECTO FINAL", 13, Tema.ACENTO, true), BorderLayout.WEST);
        barra.add(Tema.etiqueta("JAVA 17 · SWING · NETBEANS", 12, new Color(192, 202, 206), false), BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        // Centro
        JPanel centro = caja(10, 50, 10, 50);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(Box.createVerticalGlue());
        centrado(centro, Tema.etiqueta("♞", 80, Tema.ACENTO, true));
        centrado(centro, Tema.etiqueta("AJEDREZ ESTRUCTURADO", 36, Color.WHITE, true));
        centro.add(Box.createVerticalStrut(10));
        centrado(centro, Tema.etiqueta("Cinco estructuras de datos. Una partida completa.", 17, Tema.TEXTO_CLARO, false));
        centro.add(Box.createVerticalStrut(20));

        // Configuración de la partida
        Tema.Tarjeta config = new Tema.Tarjeta(null, Tema.AZUL2, new Color(53, 78, 93));
        config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
        config.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));
        izquierda(config, etiquetaCampo("MODO DE JUEGO"));
        izquierda(config, segmentado(new Tema.BotonOpcion[]{modoDos, modoIA}, 0));
        config.add(Box.createVerticalStrut(10));
        JPanel fila = new JPanel(new GridLayout(1, 2, 12, 0));
        fila.setOpaque(false);
        fila.add(campo("JUGADOR CON BLANCAS", nombreBlancas));
        JPanel derecha = new JPanel(new CardLayout());
        derecha.setOpaque(false);
        derecha.add(campo("JUGADOR CON NEGRAS", nombreNegras), "negras");
        JPanel campoDif = new JPanel(new BorderLayout(0, 0));
        campoDif.setOpaque(false);
        campoDif.add(etiquetaCampo("DIFICULTAD"), BorderLayout.NORTH);
        campoDif.add(segmentado(dificultad, 1), BorderLayout.CENTER);
        derecha.add(campoDif, "dificultad");
        fila.add(derecha);
        izquierda(config, fila);
        config.add(Box.createVerticalStrut(6));
        izquierda(config, error);
        modoDos.addActionListener(e -> ((CardLayout) derecha.getLayout()).show(derecha, "negras"));
        modoIA.addActionListener(e -> ((CardLayout) derecha.getLayout()).show(derecha, "dificultad"));
        config.setMaximumSize(new Dimension(520, 190));
        config.setPreferredSize(new Dimension(520, 190));
        config.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(config);
        centro.add(Box.createVerticalStrut(16));

        // Botones (mismo tamaño y orden del diseño original)
        Tema.BotonTema empezar = new Tema.BotonTema("NUEVA PARTIDA", true);
        Tema.BotonTema reglas = new Tema.BotonTema("REGLAS Y CONTROLES", false);
        Tema.BotonTema archivo = new Tema.BotonTema("ARCHIVO BST", false);
        empezar.addActionListener(e -> iniciar());
        reglas.addActionListener(e -> ventana.mostrar("reglas"));
        archivo.addActionListener(e -> ventana.mostrar("partidas"));
        for (Tema.BotonTema b : new Tema.BotonTema[]{empezar, reglas, archivo}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(250, 45));
            centro.add(b);
            centro.add(Box.createVerticalStrut(10));
        }
        centro.add(Box.createVerticalStrut(10));
        centrado(centro, Tema.etiqueta("Proyecto académico · Lizeth Juliana Chavez Lopez,Aide Zureli Samboni Zuñiga y Yhair Camilo Medina Tenorio", 13, Tema.TEXTO_CLARO, false));
        centro.add(Box.createVerticalGlue());
        add(centro, BorderLayout.CENTER);

        JPanel pie = caja(8, 32, 20, 32);
        pie.setLayout(new FlowLayout(FlowLayout.CENTER));
        pie.add(Tema.etiqueta("Pila  ·  Cola  ·  Lista circular  ·  Lista doble  ·  BST", 13, Tema.ACENTO, false));
        add(pie, BorderLayout.SOUTH);

        nombreBlancas.addActionListener(e -> iniciar());
        nombreNegras.addActionListener(e -> iniciar());
        DocumentListener limpiar = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { limpiarError(); }
            public void removeUpdate(DocumentEvent e) { limpiarError(); }
            public void changedUpdate(DocumentEvent e) { limpiarError(); }
        };
        nombreBlancas.getDocument().addDocumentListener(limpiar);
        nombreNegras.getDocument().addDocumentListener(limpiar);
    }

    // ----- utilidades de diseño -----

    private static JPanel caja(int arriba, int izq, int abajo, int der) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(arriba, izq, abajo, der));
        return p;
    }

    private static void centrado(JPanel p, JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(c);
    }

    private static void izquierda(JPanel p, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
    }

    private static JLabel etiquetaCampo(String texto) {
        JLabel l = Tema.etiqueta(texto, 11, Tema.ACENTO, true);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    private static JPanel campo(String titulo, JTextField t) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(etiquetaCampo(titulo), BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private static JPanel segmentado(Tema.BotonOpcion[] opciones, int seleccionada) {
        JPanel p = new JPanel(new GridLayout(1, opciones.length, 6, 0));
        p.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < opciones.length; i++) {
            grupo.add(opciones[i]);
            p.add(opciones[i]);
            opciones[i].setSelected(i == seleccionada);
        }
        return p;
    }

    private void limpiarError() {
        error.setText(" ");
        nombreBlancas.setBorder(Tema.bordeCampo(false));
        nombreNegras.setBorder(Tema.bordeCampo(false));
    }

    /**
     * Valida un nombre escrito por el usuario.
     * @param texto      lo que escribió.
     * @param porDefecto nombre si lo dejó vacío.
     * @return {null, nombre válido} o {mensaje de error, null}.
     */
    public static String[] validarNombre(String texto, String porDefecto) {
        String limpio = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            return new String[]{null, porDefecto};
        }
        if (limpio.length() > 20) {
            return new String[]{"El nombre puede tener máximo 20 caracteres.", null};
        }
        if (!limpio.matches("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 ._-]+")) {
            return new String[]{"Solo se permiten letras, números, espacios, punto y guiones.", null};
        }
        return new String[]{null, limpio};
    }

    /** Lee el formulario, valida y empieza la partida. */
    private void iniciar() {
        boolean contraIA = modoIA.isSelected();
        String[] b = validarNombre(nombreBlancas.getText(), "Blancas");
        if (b[0] != null) {
            mostrarError("Blancas: " + b[0], nombreBlancas);
            return;
        }
        String negras = "Computadora";
        if (!contraIA) {
            String[] n = validarNombre(nombreNegras.getText(), "Negras");
            if (n[0] != null) {
                mostrarError("Negras: " + n[0], nombreNegras);
                return;
            }
            if (n[1].equalsIgnoreCase(b[1])) {
                mostrarError("Los dos jugadores deben tener nombres diferentes.", nombreNegras);
                return;
            }
            negras = n[1];
        }
        int dif = dificultad[0].isSelected() ? 1 : dificultad[2].isSelected() ? 3 : 2;
        ventana.getPanelJuego().nuevaPartida(contraIA ? GestorPartida.Modo.CONTRA_IA : GestorPartida.Modo.DOS_JUGADORES, b[1], negras, dif);
    }

    private void mostrarError(String texto, JTextField campo) {
        error.setText(texto);
        campo.setBorder(Tema.bordeCampo(true));
        campo.requestFocusInWindow();
    }

    // Accesos para la demostración y las capturas
    JTextField getNombreBlancas() { return nombreBlancas; }
    JTextField getNombreNegras() { return nombreNegras; }
    Tema.BotonOpcion getModoIA() { return modoIA; }
    Tema.BotonOpcion getModoDos() { return modoDos; }
    void pulsarJugar() { iniciar(); }
}

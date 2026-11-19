package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.estructuras.Cola;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.estructuras.NodoDoble;
import co.edu.ajedrez.logica.IA;
import co.edu.ajedrez.logica.MotorAjedrez;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Jugador;
import co.edu.ajedrez.modelo.Movimiento;
import co.edu.ajedrez.modelo.PartidaGuardada;
import co.edu.ajedrez.modelo.Pieza;
import co.edu.ajedrez.servicios.GestorPartida;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Pantalla de juego. Conecta el tablero y los botones con GestorPartida.
 *
 * Modos:
 *  - JUEGO      : partida normal.
 *  - REPETICION : reproduce la partida desencolando una COLA (FIFO).
 *  - VISOR      : recorre la partida con un cursor sobre los nodos de la
 *                 LISTA DOBLE de jugadas (◀ atrás / adelante ▶).
 */
public class PanelJuego extends JPanel implements PanelTablero.Oyente {

    private enum Modo { JUEGO, REPETICION, VISOR }

    private static final int PASO_MS = 800;   // tiempo entre jugadas en la reproducción automática

    private final VentanaPrincipal ventana;
    private final PanelTablero vistaTablero = new PanelTablero(this);
    private final PanelJugador tarjetaArriba = new PanelJugador();
    private final PanelJugador tarjetaAbajo = new PanelJugador();
    private final JLabel turno = Tema.etiqueta("TURNO DE BLANCAS", 18, Tema.AZUL, true);
    private final JLabel tip = Tema.etiqueta("Selecciona una pieza y luego una casilla resaltada", 12, Tema.GRIS, false);
    private final JLabel estado = Tema.etiqueta("Turno de Blancas", 15, Tema.AZUL, true);
    private final JLabel totalJugadas = Tema.etiqueta("0 medias jugadas", 12, Tema.GRIS, false);
    private final JLabel ultimoAviso = Tema.etiqueta("Proyecto final · Estructuras de Datos", 12, Tema.GRIS, false);
    private final ModeloJugadas modeloJugadas = new ModeloJugadas();
    private final JList<String> listaJugadas = new JList<>(modeloJugadas);

    private final Tema.BotonTema btnDeshacer = new Tema.BotonTema("↶  DESHACER", false);
    private final Tema.BotonTema btnRehacer = new Tema.BotonTema("↷  REHACER", false);
    private final Tema.BotonTema btnReproducir = new Tema.BotonTema("▶  REPRODUCIR PARTIDA", true);
    private final Tema.BotonTema btnPausa = new Tema.BotonTema("PAUSA", false);
    private final Tema.BotonTema btnPaso = new Tema.BotonTema("PASO  ▶", false);
    private final Tema.BotonTema btnAtras = new Tema.BotonTema("◀  ATRÁS", false);
    private final Tema.BotonTema btnAdelante = new Tema.BotonTema("ADELANTE  ▶", false);
    private final Tema.BotonTema btnSalir = new Tema.BotonTema("SALIR REPR.", false);
    private final Tema.BotonTema btnGirar = new Tema.BotonTema("⟲  GIRAR", false);
    private final Tema.BotonTema btnEstructuras = new Tema.BotonTema("ESTRUCTURAS", false);
    private final Tema.BotonTema btnRendirse = new Tema.BotonTema("RENDIRSE", false);
    private final Tema.BotonTema btnReclamar = new Tema.BotonTema("RECLAMAR TABLAS", true);

    private GestorPartida partida;            // partida en juego (o la última terminada)
    private GestorPartida.Modo modoPartida;
    private String nombreB, nombreN;
    private int dificultad;
    private IA ia;
    private int nodosIA = 0;
    private boolean iaPensando = false;
    private int tokenIA = 0;                  // invalida respuestas viejas de la IA
    private PartidaGuardada registroGuardado;
    private Modo modo = Modo.JUEGO;

    // Reproducción (Cola)
    private GestorPartida partidaRep;
    private Cola<String> colaRep;
    private Supplier<Cola<String>> crearCola;
    private int totalRep;
    private boolean pausada, repTerminada;
    private String textoFinalRep;
    private boolean origenGuardada;
    private final Timer timerRep;

    // Visor (Lista doble)
    private GestorPartida partidaVisor;
    private NodoDoble<Movimiento> cursor;     // null = posición inicial
    private int indiceVisor;

    /** @param ventana ventana principal. */
    public PanelJuego(VentanaPrincipal ventana) {
        super(new BorderLayout());
        this.ventana = ventana;
        setBackground(Tema.MARFIL);

        JPanel zona = new JPanel(new BorderLayout(16, 0));
        zona.setBackground(Tema.MARFIL);
        zona.setBorder(BorderFactory.createEmptyBorder(16, 22, 12, 22));

        // ---- Izquierda: título, tablero y estado ----
        JPanel izquierda = new JPanel(new BorderLayout(0, 6));
        izquierda.setOpaque(false);
        JPanel alto = new JPanel(new BorderLayout());
        alto.setOpaque(false);
        alto.add(turno, BorderLayout.NORTH);
        alto.add(tip, BorderLayout.SOUTH);
        izquierda.add(alto, BorderLayout.NORTH);
        // Franja de arriba, tablero y franja de abajo con el MISMO ancho del tablero
        JPanel columna = new JPanel(null) {
            @Override
            public void doLayout() {
                int franja = 40, sep = 6;
                // S = lado del tablero (múltiplo de 8); el marco visible mide S + 14
                int s = Math.max(160, Math.min(getWidth() - 14, getHeight() - 2 * (franja + sep) - 14) / 8 * 8);
                int ancho = s + 14;
                int x = (getWidth() - ancho) / 2;
                int y = (getHeight() - (ancho + 2 * (franja + sep))) / 2;
                tarjetaArriba.setBounds(x, y, ancho, franja);
                // el panel del tablero deja 7 px alrededor del marco
                vistaTablero.setBounds(x - 7, y + franja + sep - 7, s + 28, s + 28);
                tarjetaAbajo.setBounds(x, y + franja + 2 * sep + ancho, ancho, franja);
            }
        };
        columna.setOpaque(false);
        columna.add(tarjetaArriba);
        columna.add(vistaTablero);
        columna.add(tarjetaAbajo);
        izquierda.add(columna, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 10));
        pie.add(estado, BorderLayout.WEST);
        pie.add(totalJugadas, BorderLayout.EAST);
        izquierda.add(pie, BorderLayout.SOUTH);
        zona.add(izquierda, BorderLayout.CENTER);

        // ---- Derecha: registro de jugadas y acciones ----
        JPanel lateral = new JPanel(new BorderLayout(0, 12));
        lateral.setBackground(Color.WHITE);
        lateral.setPreferredSize(new Dimension(300, 500));
        lateral.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JPanel sup = new JPanel();
        sup.setOpaque(false);
        sup.setLayout(new BoxLayout(sup, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("REGISTRO DE JUGADAS", 15, Tema.AZUL, true);
        JLabel mini = Tema.etiqueta("Lista doblemente enlazada", 12, Tema.GRIS, false);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        mini.setAlignmentX(Component.LEFT_ALIGNMENT);
        sup.add(titulo);
        sup.add(Box.createVerticalStrut(4));
        sup.add(mini);
        lateral.add(sup, BorderLayout.NORTH);

        listaJugadas.setFont(new Font("Monospaced", Font.PLAIN, 13));
        listaJugadas.setForeground(Tema.AZUL);
        listaJugadas.setBackground(Tema.FONDO_LISTA);
        listaJugadas.setSelectionBackground(Tema.ACENTO);
        listaJugadas.setSelectionForeground(Tema.AZUL);
        listaJugadas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaJugadas.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        listaJugadas.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, v, i, sel, false);
                c.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return c;
            }
        });
        listaJugadas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = listaJugadas.locationToIndex(e.getPoint());
                GestorPartida p = modo == Modo.VISOR ? partidaVisor : partida;
                if (fila < 0 || p == null || p.totalMovimientos() == 0 || modo == Modo.REPETICION) {
                    return;
                }
                boolean negras = e.getX() > listaJugadas.getWidth() * 0.55;
                int indice = Math.min(fila * 2 + (negras ? 1 : 0), p.totalMovimientos() - 1);
                if (modo == Modo.JUEGO) {
                    iniciarVisor(partida, false);
                }
                visorIr(indice);
            }
        });
        JScrollPane scroll = new JScrollPane(listaJugadas);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        lateral.add(scroll, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new GridLayout(0, 1, 0, 8));
        abajo.setOpaque(false);
        abajo.add(par(btnDeshacer, btnRehacer));
        abajo.add(btnReproducir);
        abajo.add(par(btnPausa, btnPaso));
        abajo.add(par(btnAtras, btnAdelante));
        abajo.add(par(btnSalir, btnGirar));
        abajo.add(par(btnEstructuras, btnRendirse));
        abajo.add(btnReclamar);
        lateral.add(abajo, BorderLayout.SOUTH);
        zona.add(lateral, BorderLayout.EAST);
        add(zona, BorderLayout.CENTER);

        // ---- Barra inferior de mensajes ----
        JPanel mensaje = new JPanel(new BorderLayout());
        mensaje.setOpaque(false);
        mensaje.setBorder(BorderFactory.createEmptyBorder(0, 24, 12, 24));
        mensaje.add(ultimoAviso, BorderLayout.CENTER);
        add(mensaje, BorderLayout.SOUTH);

        // ---- Eventos ----
        btnDeshacer.addActionListener(e -> deshacer());
        btnRehacer.addActionListener(e -> rehacer());
        btnReproducir.addActionListener(e -> reproducirActual());
        btnPausa.addActionListener(e -> pausarRepeticion());
        btnPaso.addActionListener(e -> pasoManual());
        btnAtras.addActionListener(e -> atras());
        btnAdelante.addActionListener(e -> visorAdelante(true));
        btnSalir.addActionListener(e -> salirReproductor());
        btnGirar.addActionListener(e -> girar());
        btnEstructuras.addActionListener(e -> mostrarEstructuras());
        btnRendirse.addActionListener(e -> rendirse());
        btnReclamar.addActionListener(e -> reclamarTablas());
        timerRep = new Timer(PASO_MS, e -> pasoRepeticion());
        timerRep.setRepeats(false);
        atajo("ctrl Z", this::deshacer);
        atajo("ctrl Y", this::rehacer);
        atajo("LEFT", this::atras);
        atajo("RIGHT", () -> visorAdelante(true));
        atajo("HOME", () -> visorIr(-1));
        atajo("END", () -> visorIr(Integer.MAX_VALUE));
        atajo("SPACE", this::pausarRepeticion);
        atajo("ESCAPE", this::salirReproductor);
    }

    private static JPanel par(Component a, Component b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return p;
    }

    /** Registra un atajo de teclado activo mientras se ve la pantalla de juego. */
    private void atajo(String tecla, Runnable accion) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), tecla);
        getActionMap().put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ventana.getPantallaActual().equals("juego")) {
                    accion.run();
                }
            }
        });
    }

    /**
     * Muestra un mensaje en la barra inferior.
     * @param texto   mensaje.
     * @param esError true para mostrarlo en rojo.
     */
    public void mensaje(String texto, boolean esError) {
        ultimoAviso.setText(texto == null ? "" : texto);
        ultimoAviso.setForeground(esError ? Tema.ERROR : Tema.GRIS);
        ultimoAviso.setFont(Tema.fuente(12, esError));
    }

    // =========================================================
    //  Inicio de partida
    // =========================================================

    /**
     * Crea una partida nueva y muestra el tablero.
     * @param modoP      modo de juego.
     * @param blancas    nombre de blancas.
     * @param negras     nombre de negras.
     * @param dificultad 1, 2 o 3.
     */
    public void nuevaPartida(GestorPartida.Modo modoP, String blancas, String negras, int dificultad) {
        detenerTodo();
        this.modoPartida = modoP;
        this.nombreB = blancas;
        this.nombreN = negras;
        this.dificultad = dificultad;
        partida = new GestorPartida(modoP, blancas, negras, dificultad);
        ia = modoP == GestorPartida.Modo.CONTRA_IA ? new IA(dificultad) : null;
        nodosIA = 0;
        registroGuardado = null;
        vistaTablero.setGirado(false);
        ventana.mostrar("juego");
        entrarModoJuego();
        mensaje("¡Comenzó una nueva partida! " + (ia != null ? "Juegas con blancas contra la computadora." : "Empiezan las blancas."), false);
    }

    /** Botón NUEVA PARTIDA del encabezado: repite la configuración anterior. */
    public void nuevaConMismosJugadores() {
        if (partida == null) {
            ventana.mostrar("inicio");
            return;
        }
        if (ventana.getPantallaActual().equals("juego") && !confirmarAbandono()) {
            return;
        }
        nuevaPartida(modoPartida, nombreB, nombreN, dificultad);
    }

    /**
     * Pide confirmación si hay una partida a medias.
     * @return true si se puede abandonar la pantalla.
     */
    public boolean confirmarAbandono() {
        if (partida == null || partida.estaTerminada() || partida.totalMovimientos() == 0 || modo != Modo.JUEGO) {
            return true;
        }
        return Dialogo.confirmar(this, "¿Abandonar la partida?",
                "La partida en curso se perderá. Solo se guardan en el árbol BST las partidas terminadas.", "SÍ, ABANDONAR");
    }

    /** Pone la pantalla en modo de juego normal con la partida actual. */
    private void entrarModoJuego() {
        modo = Modo.JUEGO;
        btnPausa.setText("PAUSA");
        vistaTablero.usarTablero(partida.getTablero());
        vistaTablero.setUltima(partida.getJugadas().ultimo());
        actualizar();
        if (!partida.estaTerminada() && partida.jugadorActual().esIA()) {
            programarIA();
        }
    }

    /** Detiene la IA y la reproducción (al salir de la pantalla o empezar otra partida). */
    public void detenerTodo() {
        tokenIA++;
        iaPensando = false;
        timerRep.stop();
    }

    // =========================================================
    //  Interacción con el tablero
    // =========================================================

    private boolean puedeInteractuar() {
        return modo == Modo.JUEGO && partida != null && !partida.estaTerminada()
                && !iaPensando && !partida.jugadorActual().esIA();
    }

    @Override
    public boolean alPresionar(int f, int c) {
        if (partida == null) {
            return false;
        }
        if (modo != Modo.JUEGO) {
            mensaje("Estás viendo una " + (modo == Modo.VISOR ? "revisión" : "reproducción") + ". Pulsa SALIR REPR. para volver a jugar.", false);
            return false;
        }
        if (partida.estaTerminada()) {
            mensaje("Esta partida ha terminado. Pulsa NUEVA PARTIDA para jugar otra.", false);
            return false;
        }
        if (iaPensando) {
            mensaje("Espera: la computadora está pensando…", false);
            return false;
        }
        if (!puedeInteractuar()) {
            return false;
        }
        int[] sel = vistaTablero.getSeleccion();
        if (sel != null && destinosA(f, c).tamano() > 0) {   // destino de la pieza seleccionada
            intentarMover(f, c);
            return false;
        }
        Pieza p = partida.getTablero().obtener(f, c);
        ColorPieza color = partida.colorActual();
        if (p != null && p.getColor() == color) {
            if (sel != null && sel[0] == f && sel[1] == c) {   // segundo clic: deseleccionar
                vistaTablero.limpiarSeleccion();
                return false;
            }
            ListaDoble<Movimiento> destinos = partida.legalesDesde(f, c);
            vistaTablero.seleccionar(f, c, destinos);
            if (destinos.estaVacia()) {
                mensaje("Esa pieza no tiene jugadas legales en este momento.", false);
            }
            return true;
        }
        if (p != null) {
            mensaje("Es el turno de " + color.getNombre() + ".", true);
        } else if (sel != null) {
            mensaje("Movimiento ilegal; elige una casilla marcada.", true);
        }
        vistaTablero.limpiarSeleccion();
        return false;
    }

    @Override
    public void alSoltar(int f, int c) {
        if (!puedeInteractuar() || vistaTablero.getSeleccion() == null) {
            return;
        }
        if (destinosA(f, c).tamano() > 0) {
            intentarMover(f, c);
        } else {
            mensaje("Movimiento ilegal; elige una casilla marcada.", true);
        }
    }

    /** Jugadas de la pieza seleccionada que llegan a (f,c). */
    private ListaDoble<Movimiento> destinosA(int f, int c) {
        ListaDoble<Movimiento> lista = new ListaDoble<>();
        vistaTablero.getDestinos().recorrerAdelante(m -> {
            if (m.getHastaF() == f && m.getHastaC() == c) {
                lista.insertarFinal(m);
            }
        });
        return lista;
    }

    /** Juega hacia (f,c); si es coronación pregunta la pieza. */
    private void intentarMover(int f, int c) {
        ListaDoble<Movimiento> opciones = destinosA(f, c);
        if (opciones.tamano() == 1) {
            realizarJugada(opciones.primero());
            return;
        }
        char tipo = Dialogo.coronacion(this, partida.colorActual());   // varias = coronación
        if (tipo != 0) {
            realizarJugada(opciones.buscar(m -> m.getPromocion() == tipo));
        }
    }

    /** Juega un movimiento legal y actualiza todo. */
    private void realizarJugada(Movimiento mov) {
        Movimiento jugada = partida.jugar(mov);
        if (jugada == null) {
            mensaje("Movimiento no permitido.", true);
            return;
        }
        vistaTablero.limpiarSeleccion();
        vistaTablero.setUltima(jugada);
        vistaTablero.animar(jugada);
        int n = partida.totalMovimientos();
        mensaje(((n + 1) / 2) + (n % 2 == 1 ? ". " : "... ") + jugada.getSan()
                + "  ·  apilada en la pila de deshacer e insertada al final de la lista doble.", false);
        actualizar();
        if (partida.estaTerminada()) {
            finalizarLuego();
        } else if (partida.jugadorActual().esIA()) {
            programarIA();
        }
    }

    /**
     * Pide su jugada a la IA en otro hilo (SwingWorker) para que la ventana
     * no se congele. La IA piensa sobre una COPIA del tablero.
     */
    private void programarIA() {
        int token = ++tokenIA;
        iaPensando = true;
        actualizar();
        ColorPieza color = partida.colorActual();
        String fen = partida.getTablero().aFEN(color);
        new SwingWorker<Movimiento, Void>() {
            @Override
            protected Movimiento doInBackground() throws Exception {
                Thread.sleep(600);                       // pausa para que se vea "pensando"
                MotorAjedrez copia = new MotorAjedrez();
                copia.cargarFEN(fen);
                return ia.elegirJugada(copia, color);
            }

            @Override
            protected void done() {
                if (token != tokenIA || modo != Modo.JUEGO || partida.estaTerminada()) {
                    return;
                }
                iaPensando = false;
                try {
                    Movimiento m = get();
                    nodosIA = ia.getUltimosNodos();
                    if (m != null) {
                        realizarJugada(partida.buscarLegal(m.getDesdeF(), m.getDesdeC(), m.getHastaF(), m.getHastaC(), m.getPromocion()));
                        return;
                    }
                } catch (Exception ex) {
                    mensaje("La computadora no pudo jugar: " + ex.getMessage(), true);
                }
                actualizar();
            }
        }.execute();
    }

    /** Deshace (contra la IA deshace también su respuesta). */
    private void deshacer() {
        if (modo != Modo.JUEGO || iaPensando || partida == null || !partida.puedeDeshacer()) {
            return;
        }
        partida.deshacer();
        while (partida.jugadorActual().esIA() && partida.puedeDeshacer()) {
            partida.deshacer();
        }
        vistaTablero.limpiarSeleccion();
        vistaTablero.setUltima(partida.getJugadas().ultimo());
        mensaje("Última jugada deshecha con la pila LIFO (pasó a la pila de rehacer).", false);
        actualizar();
        if (partida.jugadorActual().esIA()) {
            programarIA();
        }
    }

    /** Rehace (contra la IA rehace también su respuesta). */
    private void rehacer() {
        if (modo != Modo.JUEGO || iaPensando || partida == null || !partida.puedeRehacer()) {
            return;
        }
        Movimiento mov = partida.rehacer();
        if (partida.jugadorActual().esIA() && partida.puedeRehacer()) {
            mov = partida.rehacer();
        }
        vistaTablero.limpiarSeleccion();
        vistaTablero.setUltima(mov);
        vistaTablero.animar(mov);
        mensaje("Jugada rehecha con la pila LIFO.", false);
        actualizar();
        if (partida.estaTerminada()) {
            finalizarLuego();
        } else if (partida.jugadorActual().esIA()) {
            programarIA();
        }
    }

    private void girar() {
        vistaTablero.setGirado(!vistaTablero.isGirado());
        actualizar();
    }

    private void rendirse() {
        if (modo != Modo.JUEGO || partida == null || partida.estaTerminada()) {
            return;
        }
        ColorPieza color = partida.getModo() == GestorPartida.Modo.CONTRA_IA ? ColorPieza.BLANCO : partida.colorActual();
        Jugador j = partida.jugador(color);
        if (Dialogo.confirmar(this, "¿Rendirse?", j.getNombre() + " (" + color.getNombre()
                + ") abandonará la partida y ganará el rival.", "SÍ, RENDIRSE")) {
            tokenIA++;
            iaPensando = false;
            partida.rendirse(color);
            finalizarPartida();
        }
    }

    private void reclamarTablas() {
        if (modo != Modo.JUEGO || partida == null) {
            return;
        }
        String r = partida.reclamarTablas();
        mensaje(r, !partida.estaTerminada());
        if (partida.estaTerminada()) {
            tokenIA++;
            iaPensando = false;
            finalizarPartida();
        }
    }

    // =========================================================
    //  Fin de partida
    // =========================================================

    /** Muestra el final tras una pausa corta, solo si se sigue viendo ESA partida. */
    private void finalizarLuego() {
        GestorPartida esta = partida;
        Timer t = new Timer(500, e -> {
            if (partida == esta && modo == Modo.JUEGO) {
                finalizarPartida();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    /** Guarda la partida en el árbol BST y muestra el resultado. */
    private void finalizarPartida() {
        if (partida == null || !partida.estaTerminada()) {
            return;
        }
        if (!partida.isGuardada()) {
            registroGuardado = ventana.getRegistros().guardarPartida(partida);
            partida.setGuardada(true);
        }
        mensaje(partida.textoResultado() + " · Partida guardada en el árbol BST (clave " + partida.totalMovimientos() + ").", false);
        actualizar();
        mostrarDialogoFin();
    }

    /** Ventana con el resultado y las opciones al terminar. */
    private void mostrarDialogoFin() {
        if (partida == null || !partida.estaTerminada()) {
            return;
        }
        String titulo = partida.getGanador() == null ? "¡Tablas!"
                : "Jaque mate".equals(partida.getMotivo()) ? "¡Jaque mate!" : "Partida terminada";
        if (partida.getModo() == GestorPartida.Modo.CONTRA_IA && partida.getGanador() == ColorPieza.NEGRO) {
            titulo = "Gana la computadora";
        }
        int clave = registroGuardado != null ? registroGuardado.getCantidad() : partida.totalMovimientos();
        String html = "<b style='color:#1B2836'>" + partida.textoResultado() + "</b><br><br>"
                + partida.totalMovimientos() + " medias jugadas · guardada en el árbol BST con clave <b>" + clave + "</b>";
        int r = Dialogo.opciones(this, titulo, html, new String[]{
            "▶  REPRODUCIR PARTIDA (COLA)", "◀ ▶  REVISAR JUGADA A JUGADA (LISTA DOBLE)",
            "NUEVA PARTIDA CON LOS MISMOS JUGADORES", "VER LA POSICIÓN FINAL", "MENÚ PRINCIPAL"}, 0);
        switch (r) {
            case 0:
                reproducirActual();
                break;
            case 1:
                iniciarVisor(partida, false);
                break;
            case 2:
                nuevaPartida(modoPartida, nombreB, nombreN, dificultad);
                break;
            case 4:
                ventana.mostrar("inicio");
                break;
            default:
                break;
        }
    }

    // =========================================================
    //  Reproducción automática (COLA)
    // =========================================================

    /** Reproduce la partida actual desde el inicio (se puede usar en cualquier momento). */
    private void reproducirActual() {
        if (partida == null || partida.totalMovimientos() == 0 || modo != Modo.JUEGO) {
            return;
        }
        GestorPartida p = partida;
        iniciarRepeticion(p::construirColaRepeticion, nombreB, nombreN, false, p.estaTerminada() ? p.textoResultado() : "Partida en curso");
    }

    /**
     * Reproduce una partida desencolando sus jugadas una a una.
     * @param crear      función que arma la cola de códigos.
     * @param blancas    nombre de blancas.
     * @param negras     nombre de negras.
     * @param guardada   true si viene del archivo BST.
     * @param textoFinal resultado a mostrar al final.
     */
    public void iniciarRepeticion(Supplier<Cola<String>> crear, String blancas, String negras, boolean guardada, String textoFinal) {
        detenerTodo();
        crearCola = crear;
        colaRep = crear.get();
        totalRep = colaRep.tamano();
        partidaRep = new GestorPartida(blancas, negras);
        pausada = false;
        repTerminada = false;
        origenGuardada = guardada;
        textoFinalRep = textoFinal;
        modo = Modo.REPETICION;
        vistaTablero.usarTablero(partidaRep.getTablero());
        btnPausa.setText("PAUSA");
        ventana.mostrar("juego");
        mensaje("Reproducción FIFO: las " + totalRep + " jugadas se encolaron en orden y se desencolan una por una.", false);
        actualizar();
        timerRep.setInitialDelay(700);
        timerRep.restart();
    }

    /** Desencola la siguiente jugada y la muestra. */
    private void pasoRepeticion() {
        if (modo != Modo.REPETICION) {
            return;
        }
        if (colaRep.estaVacia()) {
            repTerminada = true;
            btnPausa.setText("VER OTRA VEZ");
            mensaje("Fin de la reproducción · " + textoFinalRep, false);
            actualizar();
            return;
        }
        String codigo = colaRep.desencolar();
        Movimiento mov = partidaRep.jugarCodigo(codigo);
        if (mov == null) {
            mensaje("La partida guardada tiene una jugada inválida (" + codigo + "). Se detiene.", true);
            colaRep.vaciar();
        } else {
            vistaTablero.setUltima(mov);
            vistaTablero.animar(mov);
            mensaje("Reproduciendo " + mov.getSan() + " · desencolada del frente de la cola.", false);
        }
        actualizar();
        if (!pausada) {
            timerRep.setInitialDelay(PASO_MS);
            timerRep.restart();
        }
    }

    /** Pausa / continúa (o reinicia si ya terminó). */
    private void pausarRepeticion() {
        if (modo != Modo.REPETICION) {
            return;
        }
        if (repTerminada) {
            iniciarRepeticion(crearCola, partidaRep.jugador(ColorPieza.BLANCO).getNombre(),
                    partidaRep.jugador(ColorPieza.NEGRO).getNombre(), origenGuardada, textoFinalRep);
            return;
        }
        pausada = !pausada;
        btnPausa.setText(pausada ? "CONTINUAR" : "PAUSA");
        timerRep.stop();
        if (!pausada) {
            pasoRepeticion();
        }
        actualizar();
    }

    /** PASO: deja la reproducción en pausa y avanza una sola jugada. */
    private void pasoManual() {
        if (modo != Modo.REPETICION || repTerminada) {
            return;
        }
        pausada = true;
        btnPausa.setText("CONTINUAR");
        timerRep.stop();
        pasoRepeticion();
    }

    /** Sale de la reproducción o del visor. */
    private void salirReproductor() {
        if (modo == Modo.JUEGO) {
            return;
        }
        boolean guardada = origenGuardada;
        timerRep.stop();
        if (modo == Modo.VISOR) {
            visorIr(Integer.MAX_VALUE);   // deja la posición final
        }
        if (!guardada && partida != null) {
            entrarModoJuego();
            mensaje("Partida real recuperada sin cambios.", false);
        } else {
            modo = Modo.JUEGO;
            if (partida != null) {
                vistaTablero.usarTablero(partida.getTablero());
                vistaTablero.setUltima(partida.getJugadas().ultimo());
            }
            ventana.mostrar("partidas");
        }
    }

    /** @return true si hay una partida creada (para el botón PARTIDA del encabezado). */
    public boolean hayPartida() {
        return partida != null;
    }

    /** Vuelve a la pantalla de juego con la partida actual (botón PARTIDA del encabezado). */
    public void reanudar() {
        if (partida == null) {
            ventana.mostrar("inicio");
            return;
        }
        ventana.mostrar("juego");
        if (modo == Modo.JUEGO) {
            entrarModoJuego();
        }
    }

    // =========================================================
    //  Visor adelante / atrás (LISTA DOBLE)
    // =========================================================

    /** Botón ◀ ATRÁS: si se está jugando, abre el visor en la última jugada y retrocede. */
    private void atras() {
        if (modo == Modo.JUEGO) {
            if (partida == null || partida.totalMovimientos() == 0) {
                return;
            }
            iniciarVisor(partida, false);
        }
        visorAtras(true);
    }

    /**
     * Abre el visor sobre una partida cuyo tablero está en la posición final.
     * El cursor empieza en el último nodo de la lista doble de jugadas.
     * @param p        partida.
     * @param guardada true si viene del archivo BST.
     */
    public void iniciarVisor(GestorPartida p, boolean guardada) {
        detenerTodo();
        partidaVisor = p;
        cursor = p.getJugadas().getCola();
        indiceVisor = p.totalMovimientos() - 1;
        origenGuardada = guardada;
        modo = Modo.VISOR;
        vistaTablero.usarTablero(p.getTablero());
        vistaTablero.setUltima(cursor == null ? null : cursor.getDato());
        ventana.mostrar("juego");
        mensaje("Revisión con la lista doble: ◀ va al nodo anterior y ▶ al siguiente. SALIR REPR. vuelve a la partida.", false);
        actualizar();
    }

    /** Retrocede: deshace el nodo actual y pasa al ANTERIOR. O(1). */
    private boolean visorAtras(boolean redibujar) {
        if (modo != Modo.VISOR || cursor == null) {
            return false;
        }
        partidaVisor.getTablero().deshacer(cursor.getDato());
        cursor = cursor.getAnterior();
        indiceVisor--;
        if (redibujar) {
            vistaTablero.setUltima(cursor == null ? null : cursor.getDato());
            actualizar();
        }
        return true;
    }

    /** Avanza: pasa al nodo SIGUIENTE y lo aplica. O(1). */
    private boolean visorAdelante(boolean redibujar) {
        if (modo != Modo.VISOR) {
            return false;
        }
        NodoDoble<Movimiento> siguiente = cursor == null ? partidaVisor.getJugadas().getCabeza() : cursor.getSiguiente();
        if (siguiente == null) {
            return false;
        }
        partidaVisor.getTablero().aplicar(siguiente.getDato());
        cursor = siguiente;
        indiceVisor++;
        if (redibujar) {
            vistaTablero.setUltima(siguiente.getDato());
            vistaTablero.animar(siguiente.getDato());
            actualizar();
        }
        return true;
    }

    /** Lleva el visor a la jugada "indice" (-1 = posición inicial). */
    void visorIr(int indice) {
        if (modo != Modo.VISOR) {
            return;
        }
        while (indiceVisor > indice && visorAtras(false)) {
            // retrocede nodo por nodo
        }
        while (indiceVisor < indice && visorAdelante(false)) {
            // avanza nodo por nodo
        }
        vistaTablero.setUltima(cursor == null ? null : cursor.getDato());
        actualizar();
    }

    // =========================================================
    //  Actualización de la pantalla
    // =========================================================

    private GestorPartida partidaMostrada() {
        if (modo == Modo.REPETICION) {
            return partidaRep;
        }
        if (modo == Modo.VISOR) {
            return partidaVisor;
        }
        return partida;
    }

    private ColorPieza colorEnPantalla() {
        if (modo == Modo.VISOR) {
            return cursor == null ? ColorPieza.BLANCO : cursor.getDato().getPieza().getColor().contrario();
        }
        return partidaMostrada().colorActual();
    }

    private int indiceVisible() {
        return modo == Modo.VISOR ? indiceVisor : partidaMostrada().totalMovimientos() - 1;
    }

    /** Sincroniza tablero, textos, lista de jugadas y botones. */
    public void actualizar() {
        GestorPartida p = partidaMostrada();
        if (p == null) {
            return;
        }
        ColorPieza color = colorEnPantalla();
        MotorAjedrez t = p.getTablero();
        Pieza rey = t.rey(color);
        vistaTablero.setJaque(rey != null && t.enJaque(color) ? new int[]{rey.getFila(), rey.getCol()} : null);
        actualizarTarjetas(p, color);
        actualizarTextos(p, color);
        int visibles = modo == Modo.REPETICION ? indiceVisible() + 1 : p.totalMovimientos();
        modeloJugadas.mostrar(p.getJugadas(), visibles);
        int idx = indiceVisible();
        if (idx >= 0) {
            listaJugadas.setSelectedIndex(idx / 2);
            listaJugadas.ensureIndexIsVisible(idx / 2);
        } else {
            listaJugadas.clearSelection();
        }
        totalJugadas.setText(p.totalMovimientos() + " medias jugadas");

        boolean enJuego = modo == Modo.JUEGO && partida != null;
        boolean libre = enJuego && !iaPensando;
        btnDeshacer.setEnabled(libre && partida.puedeDeshacer());
        btnRehacer.setEnabled(libre && partida.puedeRehacer());
        btnReproducir.setEnabled(libre && partida.totalMovimientos() > 0);
        btnPausa.setEnabled(modo == Modo.REPETICION);
        btnPaso.setEnabled(modo == Modo.REPETICION && !repTerminada && !colaRep.estaVacia());
        btnAtras.setEnabled((libre && partida.totalMovimientos() > 0) || (modo == Modo.VISOR && cursor != null));
        btnAdelante.setEnabled(modo == Modo.VISOR && indiceVisor < partidaVisor.totalMovimientos() - 1);
        btnSalir.setEnabled(modo != Modo.JUEGO);
        btnRendirse.setEnabled(libre && !partida.estaTerminada());
        btnReclamar.setEnabled(libre && partida.puedeReclamarTablas());
        vistaTablero.repaint();
    }

    /** Franjas de los jugadores con capturas (recorre la lista doble de jugadas). */
    private void actualizarTarjetas(GestorPartida p, ColorPieza colorTurno) {
        ColorPieza abajo = vistaTablero.isGirado() ? ColorPieza.NEGRO : ColorPieza.BLANCO;
        int limite = indiceVisible();
        StringBuilder capB = new StringBuilder(), capN = new StringBuilder();
        int[] puntos = {0, 0};   // {blancas, negras}
        p.getJugadas().recorrerAdelante((m, i) -> {
            if (i > limite) {
                return false;
            }
            if (m.getCapturada() != null) {
                boolean blanca = m.getPieza().getColor() == ColorPieza.BLANCO;
                (blanca ? capB : capN).append(m.getCapturada().getTipo());
                puntos[blanca ? 0 : 1] += Math.round(Pieza.valor(m.getCapturada().getTipo()) / 100f);
            }
            return true;
        });
        boolean terminada = modo == Modo.JUEGO && p.estaTerminada();
        for (ColorPieza c : new ColorPieza[]{ColorPieza.BLANCO, ColorPieza.NEGRO}) {
            PanelJugador tarjeta = c == abajo ? tarjetaAbajo : tarjetaArriba;
            Jugador j = p.jugador(c);
            boolean blanca = c == ColorPieza.BLANCO;
            int dif = blanca ? puntos[0] - puntos[1] : puntos[1] - puntos[0];
            tarjeta.mostrar(c, j.getNombre() + (j.esIA() && !j.getNombre().equalsIgnoreCase("Computadora") ? "  (computadora)" : ""), !terminada && c == colorTurno,
                    ordenar(blanca ? capB.toString() : capN.toString()), Math.max(0, dif));
        }
    }

    /** Ordena las letras capturadas: D, T, A, C, P. */
    private static String ordenar(String letras) {
        StringBuilder sb = new StringBuilder();
        for (char tipo : "DTACP".toCharArray()) {
            for (char ch : letras.toCharArray()) {
                if (ch == tipo) {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

    /** Título superior y estado inferior (mismo formato del diseño original). */
    private void actualizarTextos(GestorPartida p, ColorPieza color) {
        Color colorEstado = Tema.AZUL;
        String s;
        if (modo == Modo.REPETICION) {
            turno.setText("REPRODUCCIÓN DE PARTIDA");
            tip.setText("Cola FIFO: la primera jugada que entró es la primera que se muestra");
            int hechas = totalRep - colaRep.tamano();
            s = repTerminada ? "Reproducción terminada · " + textoFinalRep
                    : (pausada ? "En pausa · " : "") + "Reproducción FIFO · jugada " + hechas + " de " + totalRep + " · quedan " + colaRep.tamano();
        } else if (modo == Modo.VISOR) {
            turno.setText("REVISIÓN DE PARTIDA");
            tip.setText("Lista doble: ◀ nodo anterior · ▶ nodo siguiente (también con las flechas del teclado)");
            int n = partidaVisor.totalMovimientos();
            s = cursor == null ? "Posición inicial (0 de " + n + ")" : "Jugada " + (indiceVisor + 1) + " de " + n + ": " + cursor.getDato().getSan();
        } else if (p.estaTerminada()) {
            turno.setText("PARTIDA TERMINADA");
            tip.setText("Puedes reproducirla, revisarla jugada a jugada o empezar una nueva");
            s = p.textoResultado();
        } else {
            Jugador j = p.jugador(color);
            turno.setText("TURNO DE " + color.getNombre().toUpperCase() + "  ·  " + j.getNombre().toUpperCase());
            tip.setText(iaPensando ? "La computadora está pensando su jugada…"
                    : "Selecciona una pieza y luego una casilla resaltada (también puedes arrastrarla)");
            s = "Turno de " + color.getNombre();
            if (p.getTablero().enJaque(color)) {
                s += " · ¡Jaque!";
                colorEstado = Tema.ERROR;
            }
            if (p.repeticiones() >= 3) {
                s += " · Puede reclamar tablas por repetición";
            } else if (p.getTablero().getMediosMovimientos() >= 100) {
                s += " · Puede reclamar tablas por 50 movimientos";
            }
        }
        estado.setText(s);
        estado.setForeground(colorEstado);
    }

    /** Ventana ESTRUCTURAS: estado real de cada estructura en este momento. */
    private void mostrarEstructuras() {
        GestorPartida p = partidaMostrada();
        if (p == null) {
            return;
        }
        StringBuilder h = new StringBuilder("<html><body style='width:430px'>");
        h.append("<p style='color:#6B7782'>Todas están programadas desde cero con nodos y referencias (paquete <b>estructuras</b>). ")
                .append("No se usa ArrayList, Stack, Queue ni LinkedList.</p><table cellpadding='5' width='430'>");
        fila(h, "1. LISTA CIRCULAR", "Turnos. El último nodo apunta al primero.", p.cicloTurnos()
                + "<br>Turno actual: <b>" + colorEnPantalla().getNombre() + "</b>");
        fila(h, "2. PILA (LIFO)", "Deshacer y rehacer.", "Deshacer: " + p.getPilaDeshacer().tamano() + " · tope <b>"
                + tope(p.getPilaDeshacer().verTope()) + "</b><br>Rehacer: " + p.getPilaRehacer().tamano() + " · tope <b>"
                + tope(p.getPilaRehacer().verTope()) + "</b>");
        fila(h, "3. COLA (FIFO)", "Reproducción de la partida.", modo == Modo.REPETICION
                ? colaRep.tamano() + " jugadas pendientes · frente <b>" + (colaRep.estaVacia() ? "—" : colaRep.verFrente()) + "</b>"
                : "Se llena al pulsar REPRODUCIR PARTIDA.");
        fila(h, "4. LISTA DOBLE", "Historial adelante/atrás y piezas vivas.", p.totalMovimientos() + " medias jugadas.<br>Vista inversa: "
                + p.historialInverso() + "<br>Piezas vivas · Blancas: <b>" + p.getTablero().piezasVivas(ColorPieza.BLANCO).tamano()
                + "</b> · Negras: <b>" + p.getTablero().piezasVivas(ColorPieza.NEGRO).tamano() + "</b>");
        fila(h, "5. ÁRBOL BST", "Archivo de partidas (clave = n.º de jugadas).", ventana.getRegistros().getArbol().tamano()
                + " partidas · altura " + ventana.getRegistros().getArbol().altura());
        fila(h, "ÁRBOL DE JUGADAS", "IA minimax.", ia != null ? String.format("%,d nodos en la última decisión (profundidad %d)",
                nodosIA, ia.getProfundidad()) : "Solo en el modo contra la computadora.");
        h.append("</table></body></html>");
        Dialogo.informacion(this, "Estructuras de datos en vivo", h.toString());
    }

    private static String tope(Movimiento m) {
        return m == null ? "—" : m.getSan();
    }

    private static void fila(StringBuilder h, String nombre, String uso, String valor) {
        h.append("<tr><td valign='top' width='150'><font color='#C59D5D'><b>").append(nombre).append("</b></font><br><font color='#6B7782'>")
                .append(uso).append("</font></td><td valign='top'><font color='#1B2836'>").append(valor).append("</font></td></tr>");
    }

    /**
     * Modelo de la lista de jugadas: lee directamente la ListaDoble de la
     * partida (no copia los datos a otra colección). Cada fila es un par
     * "1.  e4      e5".
     */
    private static class ModeloJugadas extends AbstractListModel<String> {
        private ListaDoble<Movimiento> lista = new ListaDoble<>();
        private int visibles = 0;

        void mostrar(ListaDoble<Movimiento> lista, int visibles) {
            this.lista = lista;
            this.visibles = Math.min(visibles, lista.tamano());
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }

        @Override
        public int getSize() {
            return Math.max(1, (visibles + 1) / 2);
        }

        @Override
        public String getElementAt(int i) {
            if (visibles == 0) {
                return "Aún no hay movimientos.";
            }
            Movimiento b = lista.obtener(2 * i);
            Movimiento n = 2 * i + 1 < visibles ? lista.obtener(2 * i + 1) : null;
            return String.format("%3d.  %-9s %s", i + 1, b == null ? "" : b.getSan(), n == null ? "" : n.getSan());
        }
    }

    // Accesos usados por la demostración y las capturas
    GestorPartida getPartida() { return partida; }
    PanelTablero getVistaTablero() { return vistaTablero; }
    void jugarCodigo(String c) {
        int[] d = Movimiento.leerCodigo(c);
        realizarJugada(partida.buscarLegal(d[0], d[1], d[2], d[3], (char) d[4]));
    }
    void mostrarFin() { mostrarDialogoFin(); }
    void repetirActual() { reproducirActual(); }
    void pausar() { pausarRepeticion(); }
    void abrirEstructuras() { mostrarEstructuras(); }
    void rendirseSinPreguntar() {
        partida.rendirse(partida.colorActual());
        finalizarSinDialogo();
    }
    void finalizarSinDialogo() {
        if (!partida.isGuardada() && partida.estaTerminada()) {
            registroGuardado = ventana.getRegistros().guardarPartida(partida);
            partida.setGuardada(true);
        }
        actualizar();
    }
}

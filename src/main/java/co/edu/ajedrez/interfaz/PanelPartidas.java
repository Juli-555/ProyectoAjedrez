package co.edu.ajedrez.interfaz;

import co.edu.ajedrez.estructuras.ArbolBST;
import co.edu.ajedrez.estructuras.Cola;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.estructuras.NodoArbol;
import co.edu.ajedrez.modelo.PartidaGuardada;
import co.edu.ajedrez.servicios.GestorPartida;
import co.edu.ajedrez.servicios.RegistroPartidas;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * ARCHIVO BST: partidas terminadas guardadas en el árbol binario de
 * búsqueda (clave = número de medias jugadas). Muestra los tres
 * recorridos, busca por número de jugadas, elimina, reproduce (Cola) o
 * revisa (Lista doble) y dibuja la forma del árbol.
 */
public class PanelPartidas extends JPanel {

    private final VentanaPrincipal ventana;
    private final RegistroPartidas registros;
    private final Tema.BotonOpcion[] recorridos = {
        new Tema.BotonOpcion("INORDEN", false), new Tema.BotonOpcion("PREORDEN", false), new Tema.BotonOpcion("POSORDEN", false)};
    private final JTextField buscar = Tema.campo(6);
    private final JLabel error = Tema.etiqueta(" ", 12, Tema.ERROR, true);
    private final JLabel estadisticas = Tema.etiqueta(" ", 12, Tema.GRIS, false);
    private final ModeloTabla modelo = new ModeloTabla();
    private final JTable tabla = new JTable(modelo);
    private final DibujoArbol dibujo = new DibujoArbol();
    private ListaDoble<Integer> resaltadas = new ListaDoble<>();   // ids encontrados en la búsqueda

    /** @param ventana ventana principal. */
    public PanelPartidas(VentanaPrincipal ventana) {
        super(new BorderLayout(0, 12));
        this.ventana = ventana;
        this.registros = ventana.getRegistros();
        setBackground(Tema.MARFIL);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Encabezado de la pantalla
        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.etiqueta("ARCHIVO DE PARTIDAS · ÁRBOL BINARIO DE BÚSQUEDA", 18, Tema.AZUL, true);
        JLabel sub = Tema.etiqueta("Cada partida terminada se guarda sola en el BST con clave = número de medias jugadas. "
                + "El recorrido inorden las ordena de la más corta a la más larga.", 12, Tema.GRIS, false);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        arriba.add(titulo);
        arriba.add(Box.createVerticalStrut(4));
        arriba.add(sub);
        arriba.add(Box.createVerticalStrut(10));

        JPanel herramientas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        herramientas.setOpaque(false);
        herramientas.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel seg = new JPanel(new GridLayout(1, 3, 6, 0));
        seg.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < 3; i++) {
            grupo.add(recorridos[i]);
            seg.add(recorridos[i]);
            recorridos[i].addActionListener(e -> actualizar());
        }
        recorridos[0].setSelected(true);
        herramientas.add(seg);
        herramientas.add(Box.createHorizontalStrut(18));
        herramientas.add(Tema.etiqueta("N.º DE JUGADAS:", 12, Tema.AZUL, true));
        herramientas.add(buscar);
        Tema.BotonTema btnBuscar = new Tema.BotonTema("BUSCAR", true);
        Tema.BotonTema btnLimpiar = new Tema.BotonTema("LIMPIAR", false);
        herramientas.add(btnBuscar);
        herramientas.add(btnLimpiar);
        herramientas.add(error);
        btnBuscar.addActionListener(e -> buscarPartidas());
        buscar.addActionListener(e -> buscarPartidas());
        btnLimpiar.addActionListener(e -> {
            buscar.setText("");
            error.setText(" ");
            resaltadas = new ListaDoble<>();
            actualizar();
        });
        arriba.add(herramientas);
        add(arriba, BorderLayout.NORTH);

        // Tabla
        tabla.setRowHeight(30);
        tabla.setFont(Tema.fuente(13, false));
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(Tema.AZUL);
        tabla.setGridColor(Tema.BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setDefaultRenderer(new Encabezado());
        tabla.setDefaultRenderer(Object.class, new Pintor());
        int[] anchos = {36, 44, 56, 140, 90, 90, 240};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));

        JPanel izquierda = new JPanel(new BorderLayout(0, 10));
        izquierda.setOpaque(false);
        izquierda.add(scroll, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new GridLayout(1, 3, 8, 0));
        acciones.setOpaque(false);
        Tema.BotonTema repetir = new Tema.BotonTema("▶  REPRODUCIR (COLA)", true);
        Tema.BotonTema revisar = new Tema.BotonTema("◀ ▶  REVISAR (LISTA DOBLE)", false);
        Tema.BotonTema eliminar = new Tema.BotonTema("ELIMINAR DEL BST", false);
        acciones.add(repetir);
        acciones.add(revisar);
        acciones.add(eliminar);
        repetir.addActionListener(e -> accion("repetir"));
        revisar.addActionListener(e -> accion("visor"));
        eliminar.addActionListener(e -> accion("eliminar"));
        izquierda.add(acciones, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.setBackground(Color.WHITE);
        derecha.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        JPanel tituloArbol = new JPanel();
        tituloArbol.setOpaque(false);
        tituloArbol.setLayout(new BoxLayout(tituloArbol, BoxLayout.Y_AXIS));
        tituloArbol.add(Tema.etiqueta("FORMA DEL ÁRBOL", 15, Tema.AZUL, true));
        tituloArbol.add(Tema.etiqueta("Claro = ganan blancas · azul = ganan negras · gris = tablas", 11, Tema.GRIS, false));
        derecha.add(tituloArbol, BorderLayout.NORTH);
        derecha.add(dibujo, BorderLayout.CENTER);
        derecha.add(estadisticas, BorderLayout.SOUTH);
        derecha.setPreferredSize(new Dimension(390, 400));

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(izquierda, BorderLayout.CENTER);
        centro.add(derecha, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
    }

    /** Vuelve a leer el árbol con el recorrido elegido y repinta. */
    public void actualizar() {
        String tipo = recorridos[1].isSelected() ? "preorden" : recorridos[2].isSelected() ? "posorden" : "inorden";
        modelo.mostrar(registros.recorrido(tipo));
        ArbolBST<PartidaGuardada> a = registros.getArbol();
        estadisticas.setText(a.estaVacio() ? "<html>El árbol está vacío. Termina una partida y aparecerá aquí.</html>"
                : "<html>" + a.tamano() + " nodos · altura " + a.altura() + " · más corta: " + a.minimo()
                + " · más larga: " + a.maximo() + " medias jugadas</html>");
        dibujo.repaint();
    }

    /** Busca partidas por número de jugadas (valida lo escrito). */
    void buscarPartidas() {
        String texto = buscar.getText().trim();
        resaltadas = new ListaDoble<>();
        if (!texto.matches("\\d{1,4}")) {
            error.setText("Escribe un número entero (por ejemplo: 24).");
            buscar.setBorder(Tema.bordeCampo(true));
            actualizar();
            return;
        }
        error.setText(" ");
        buscar.setBorder(Tema.bordeCampo(false));
        ListaDoble<PartidaGuardada> encontradas = registros.buscar(Integer.parseInt(texto));
        encontradas.recorrerAdelante(r -> resaltadas.insertarFinal(r.getId()));
        ventana.aviso(encontradas.estaVacia() ? "No hay partidas con " + texto + " medias jugadas."
                : "Se encontraron " + encontradas.tamano() + " partida(s) con " + texto + " medias jugadas (resaltadas).", false);
        actualizar();
    }

    private boolean estaResaltada(int id) {
        return resaltadas.buscar(x -> x == id) != null;
    }

    /** Reproducir, revisar o eliminar la partida seleccionada en la tabla. */
    private void accion(String que) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            ventana.aviso("Primero selecciona una partida de la tabla.", true);
            return;
        }
        ArbolBST.Entrada<PartidaGuardada> e = modelo.lista.obtener(fila);
        PartidaGuardada r = e.dato;
        PanelJuego juego = ventana.getPanelJuego();
        switch (que) {
            case "repetir":
                juego.iniciarRepeticion(() -> colaDesdeTexto(r.getJugadas()), r.getBlancas(), r.getNegras(), true, r.textoResultado());
                break;
            case "visor":
                GestorPartida p = new GestorPartida(r.getBlancas(), r.getNegras());
                colaDesdeTexto(r.getJugadas()).recorrer(p::jugarCodigo);
                juego.iniciarVisor(p, true);
                break;
            default:
                if (Dialogo.confirmar(this, "¿Eliminar partida?", "Se eliminará del árbol BST la partida #" + r.getId() + " "
                        + r.getBlancas() + " vs " + r.getNegras() + " (clave " + e.clave + ").", "ELIMINAR")) {
                    if (registros.eliminar(e.clave, r.getId())) {
                        ventana.aviso("Partida #" + r.getId() + " eliminada del árbol BST.", false);
                    }
                    actualizar();
                }
                break;
        }
    }

    /**
     * Convierte el texto guardado ("e2e4 e7e5 ...") en una Cola de códigos.
     * @param texto jugadas separadas por espacio.
     * @return la cola.
     */
    public static Cola<String> colaDesdeTexto(String texto) {
        Cola<String> cola = new Cola<>();
        StringBuilder actual = new StringBuilder();
        for (char ch : (texto + " ").toCharArray()) {
            if (ch == ' ') {
                if (actual.length() > 0) {
                    cola.encolar(actual.toString());
                }
                actual.setLength(0);
            } else {
                actual.append(ch);
            }
        }
        return cola;
    }

    /** Modelo de la tabla: lee directamente la ListaDoble del recorrido. */
    private static class ModeloTabla extends AbstractTableModel {
        private final String[] columnas = {"N.º", "ID", "CLAVE", "FECHA", "BLANCAS", "NEGRAS", "RESULTADO"};
        ListaDoble<ArbolBST.Entrada<PartidaGuardada>> lista = new ListaDoble<>();

        void mostrar(ListaDoble<ArbolBST.Entrada<PartidaGuardada>> l) {
            lista = l;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return lista.tamano();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int c) {
            return columnas[c];
        }

        @Override
        public Object getValueAt(int fila, int col) {
            ArbolBST.Entrada<PartidaGuardada> e = lista.obtener(fila);
            PartidaGuardada r = e.dato;
            switch (col) {
                case 0: return fila + 1;
                case 1: return "#" + r.getId();
                case 2: return e.clave;
                case 3: return r.getFecha();
                case 4: return r.getBlancas();
                case 5: return r.getNegras();
                default: return r.textoResultado();
            }
        }
    }

    /** Encabezado de la tabla en azul del proyecto. */
    private static class Encabezado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, false, false, fila, col);
            setBackground(Tema.AZUL);
            setForeground(Color.WHITE);
            setFont(Tema.fuente(11, true));
            setHorizontalAlignment(col <= 2 ? CENTER : LEFT);
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            return this;
        }
    }

    /** Pinta las filas resaltadas por la búsqueda y la columna de la clave. */
    private class Pintor extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, fila, col);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            boolean resaltada = estaResaltada(modelo.lista.obtener(fila).dato.getId());
            setBackground(sel ? new Color(226, 232, 235) : resaltada ? Tema.RESALTE : (fila % 2 == 0 ? Color.WHITE : Tema.FONDO_LISTA));
            setForeground(col == 2 ? new Color(150, 110, 50) : Tema.AZUL);
            setFont(Tema.fuente(13, col == 2 || resaltada));
            setHorizontalAlignment(col <= 2 ? CENTER : LEFT);
            return this;
        }
    }

    /** Dibuja el árbol BST: x = posición en inorden, y = nivel. */
    private class DibujoArbol extends JPanel {
        private int indice;
        private ListaDoble<Object[]> circulos = new ListaDoble<>();

        DibujoArbol() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics gr) {
            NodoArbol<PartidaGuardada> raiz = registros.getArbol().getRaiz();
            if (raiz == null) {
                return;
            }
            Graphics2D g = (Graphics2D) gr.create();
            Tema.suavizar(g);
            int n = registros.getArbol().tamano(), h = registros.getArbol().altura();
            double sepX = Math.min(50, (getWidth() - 10) / (double) n);
            double sepY = Math.min(62, (getHeight() - 30) / (double) Math.max(1, h));
            double r = Math.max(9, Math.min(17, sepX * 0.42));
            double x0 = (getWidth() - sepX * n) / 2;
            indice = 0;
            circulos = new ListaDoble<>();
            dibujar(g, raiz, 0, x0, sepX, sepY, r);
            circulos.recorrerAdelante(info -> circulo(g, info, r));
            g.dispose();
        }

        /**
         * Recorrido inorden: calcula la posición de cada nodo, dibuja las
         * líneas a sus hijos y guarda el nodo en una ListaDoble para dibujar
         * los círculos encima en una segunda pasada.
         */
        private double[] dibujar(Graphics2D g, NodoArbol<PartidaGuardada> nodo, int nivel, double x0, double sepX, double sepY, double r) {
            if (nodo == null) {
                return null;
            }
            double[] izq = dibujar(g, nodo.getIzquierdo(), nivel + 1, x0, sepX, sepY, r);
            double[] yo = {x0 + indice++ * sepX + sepX / 2, nivel * sepY + r + 6};
            double[] der = dibujar(g, nodo.getDerecho(), nivel + 1, x0, sepX, sepY, r);
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(178, 190, 195));
            for (double[] hijo : new double[][]{izq, der}) {
                if (hijo != null) {
                    g.drawLine((int) yo[0], (int) yo[1], (int) hijo[0], (int) hijo[1]);
                }
            }
            circulos.insertarFinal(new Object[]{nodo, yo});
            return yo;
        }

        /** Dibuja el círculo de un nodo con su clave. */
        @SuppressWarnings("unchecked")
        private void circulo(Graphics2D g, Object[] info, double r) {
            NodoArbol<PartidaGuardada> nodo = (NodoArbol<PartidaGuardada>) info[0];
            double x = ((double[]) info[1])[0], y = ((double[]) info[1])[1];
            PartidaGuardada d = nodo.getDato();
            boolean blancas = "BLANCO".equals(d.getGanador()), negras = "NEGRO".equals(d.getGanador());
            g.setColor(blancas ? new Color(239, 226, 202) : negras ? Tema.AZUL : new Color(178, 190, 195));
            g.fillOval((int) (x - r), (int) (y - r), (int) (2 * r), (int) (2 * r));
            boolean res = estaResaltada(d.getId());
            g.setStroke(new BasicStroke(res ? 4f : 1.5f));
            g.setColor(res ? Tema.ACENTO : Tema.AZUL);
            g.drawOval((int) (x - r), (int) (y - r), (int) (2 * r), (int) (2 * r));
            g.setFont(Tema.fuente((int) Math.max(10, r * 0.8), true));
            String txt = String.valueOf(nodo.getClave());
            g.setColor(negras ? Color.WHITE : Tema.AZUL);
            g.drawString(txt, (float) (x - g.getFontMetrics().stringWidth(txt) / 2.0), (float) (y + g.getFontMetrics().getAscent() / 2.0 - 2));
        }
    }

    // Accesos para la demostración/capturas
    JTextField getCampoBuscar() { return buscar; }
    void elegirRecorrido(int i) { recorridos[i].setSelected(true); actualizar(); }
    JTable getTabla() { return tabla; }
}

package co.edu.ajedrez.servicios;

import co.edu.ajedrez.estructuras.Cola;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.estructuras.Pila;
import co.edu.ajedrez.logica.MotorAjedrez;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Jugador;
import co.edu.ajedrez.modelo.Movimiento;
import co.edu.ajedrez.modelo.Pieza;

/**
 * GestorPartida: coordina una partida completa. Aquí se juntan las estructuras:
 *  - ListaCircular (GestorTurnos) -> a quién le toca (Blancas / Negras)
 *  - Pila          pilaDeshacer -> deshacer jugadas
 *  - Pila          pilaRehacer  -> rehacer jugadas deshechas
 *  - ListaDoble    jugadas      -> lista de jugadas / visor adelante-atrás
 *  - ListaDoble    posiciones   -> posiciones vistas (triple repetición)
 *  - Cola          (al final)   -> repetición automática de la partida
 *  - ListaDoble    piezasVivas  -> dentro del Tablero
 */
public class GestorPartida {

    /** Modo de juego. */
    public enum Modo { DOS_JUGADORES, CONTRA_IA }

    private final Modo modo;
    private final int dificultad;
    private final MotorAjedrez tablero = new MotorAjedrez();
    private final GestorTurnos turnos = new GestorTurnos();
    private final Pila<Movimiento> pilaDeshacer = new Pila<>();
    private final Pila<Movimiento> pilaRehacer = new Pila<>();
    private final ListaDoble<Movimiento> jugadas = new ListaDoble<>();
    private final ListaDoble<String> posiciones = new ListaDoble<>();

    private boolean terminada = false;
    private ColorPieza ganador = null;     // null = tablas
    private String motivo = null;          // "Jaque mate", "Rendición", ...
    private ListaDoble<Movimiento> cacheLegales = null;
    private boolean guardada = false;

    /**
     * Crea una partida en la posición inicial.
     * @param modo          modo de juego.
     * @param nombreBlancas nombre de quien juega con blancas.
     * @param nombreNegras  nombre de quien juega con negras.
     * @param dificultad    1, 2 o 3 (profundidad de la IA).
     */
    public GestorPartida(Modo modo, String nombreBlancas, String nombreNegras, int dificultad) {
        this(modo, nombreBlancas, nombreNegras, dificultad, null);
    }

    /**
     * Crea una partida desde una posición FEN (se usa en las pruebas).
     * @param fen posición inicial o null para la normal.
     */
    public GestorPartida(Modo modo, String nombreBlancas, String nombreNegras, int dificultad, String fen) {
        this.modo = modo;
        this.dificultad = (dificultad >= 1 && dificultad <= 3) ? dificultad : 2;
        ColorPieza primero = ColorPieza.BLANCO;
        if (fen != null) {
            primero = tablero.cargarFEN(fen);
        } else {
            tablero.posicionInicial();
        }
        turnos.agregar(new Jugador(ColorPieza.BLANCO, nombreBlancas, false));
        turnos.agregar(new Jugador(ColorPieza.NEGRO, nombreNegras, modo == Modo.CONTRA_IA));
        if (primero == ColorPieza.NEGRO) {
            turnos.avanzar();
        }
        posiciones.insertarFinal(tablero.clavePosicion(primero));
    }

    /** Partida rápida de dos jugadores (útil para pruebas y repeticiones). */
    public GestorPartida(String nombreBlancas, String nombreNegras) {
        this(Modo.DOS_JUGADORES, nombreBlancas, nombreNegras, 2);
    }

    /** @return el jugador al que le toca (nodo actual de la lista circular). O(1). */
    public Jugador jugadorActual() {
        return turnos.actual();
    }

    /** @return el color al que le toca. O(1). */
    public ColorPieza colorActual() {
        return turnos.actual().getColor();
    }

    /**
     * Busca un jugador por su color en la lista circular. O(n), n = 2.
     * @param color color.
     * @return el jugador.
     */
    public Jugador jugador(ColorPieza color) {
        return turnos.buscar(color);
    }

    /** @return jugadas legales del jugador actual (se calculan una vez por turno). */
    public ListaDoble<Movimiento> legales() {
        if (cacheLegales == null) {
            cacheLegales = tablero.generarLegales(colorActual());
        }
        return cacheLegales;
    }

    /**
     * @param f fila.
     * @param c columna.
     * @return jugadas legales que salen de (f,c) (vacía si no hay pieza propia).
     */
    public ListaDoble<Movimiento> legalesDesde(int f, int c) {
        ListaDoble<Movimiento> resultado = new ListaDoble<>();
        if (terminada) {
            return resultado;
        }
        legales().recorrerAdelante(m -> {
            if (m.getDesdeF() == f && m.getDesdeC() == c) {
                resultado.insertarFinal(m);
            }
        });
        return resultado;
    }

    /**
     * Busca la jugada legal de (df,dc) a (hf,hc).
     * @param promocion pieza de coronación o 0.
     * @return el movimiento o null si no es legal.
     */
    public Movimiento buscarLegal(int df, int dc, int hf, int hc, char promocion) {
        return legales().buscar(m -> m.getDesdeF() == df && m.getDesdeC() == dc && m.getHastaF() == hf
                && m.getHastaC() == hc && m.getPromocion() == promocion);
    }

    /**
     * Juega un movimiento legal.
     * @param mov movimiento obtenido de legales().
     * @return el movimiento jugado, o null si no es legal o la partida terminó.
     */
    public Movimiento jugar(Movimiento mov) {
        if (terminada || mov == null || legales().buscar(m -> m == mov) == null) {
            return null;
        }
        mov.setSan(notacion(mov));
        ejecutar(mov);
        pilaRehacer.vaciar();          // una jugada nueva invalida el "rehacer"
        return mov;
    }

    /**
     * Juega a partir de un código ("e2e4", "e7e8D").
     * @param codigo texto de la jugada.
     * @return el movimiento o null si es inválido o ilegal.
     */
    public Movimiento jugarCodigo(String codigo) {
        int[] d = Movimiento.leerCodigo(codigo);
        if (d == null) {
            return null;
        }
        return jugar(buscarLegal(d[0], d[1], d[2], d[3], (char) d[4]));
    }

    /** Aplica la jugada y actualiza todas las estructuras. */
    private void ejecutar(Movimiento mov) {
        tablero.aplicar(mov);
        pilaDeshacer.apilar(mov);
        jugadas.insertarFinal(mov);
        turnos.avanzar();
        posiciones.insertarFinal(tablero.clavePosicion(colorActual()));
        cacheLegales = null;
        evaluarFin();
        String san = mov.getSan();
        if (!san.endsWith("+") && !san.endsWith("#")) {
            if (terminada && "Jaque mate".equals(motivo)) {
                mov.setSan(san + "#");
            } else if (tablero.enJaque(colorActual())) {
                mov.setSan(san + "+");
            }
        }
    }

    /** @return true si se puede deshacer. */
    public boolean puedeDeshacer() {
        return !terminada && !pilaDeshacer.estaVacia();
    }

    /** @return true si se puede rehacer. */
    public boolean puedeRehacer() {
        return !terminada && !pilaRehacer.estaVacia();
    }

    /**
     * Deshace la última jugada: la saca de la pila de deshacer, la revierte,
     * la pasa a la pila de rehacer y devuelve el turno. O(1).
     * @return el movimiento deshecho o null.
     */
    public Movimiento deshacer() {
        if (!puedeDeshacer()) {
            return null;
        }
        Movimiento mov = pilaDeshacer.desapilar();
        tablero.deshacer(mov);
        pilaRehacer.apilar(mov);
        jugadas.eliminarFinal();
        posiciones.eliminarFinal();
        turnos.retroceder();
        cacheLegales = null;
        return mov;
    }

    /**
     * Rehace la última jugada deshecha. O(1).
     * @return el movimiento rehecho o null.
     */
    public Movimiento rehacer() {
        if (!puedeRehacer()) {
            return null;
        }
        Movimiento mov = pilaRehacer.desapilar();
        ejecutar(mov);
        return mov;
    }

    /**
     * El jugador del color dado abandona la partida.
     * @param color color que se rinde.
     */
    public void rendirse(ColorPieza color) {
        if (terminada) {
            return;
        }
        terminar(color.contrario(), "Rendición");
    }

    private void terminar(ColorPieza ganador, String motivo) {
        this.terminada = true;
        this.ganador = ganador;
        this.motivo = motivo;
    }

    /**
     * Revisa si la partida terminó sola: jaque mate, ahogado, material
     * insuficiente, regla de 75 movimientos o repetición quíntuple.
     * (Las tablas por triple repetición y 50 movimientos se RECLAMAN.)
     */
    public void evaluarFin() {
        ColorPieza color = colorActual();
        if (legales().estaVacia()) {
            if (tablero.enJaque(color)) {
                terminar(color.contrario(), "Jaque mate");
            } else {
                terminar(null, "Rey ahogado");
            }
        } else if (tablero.materialInsuficiente()) {
            terminar(null, "Material insuficiente");
        } else if (tablero.getMediosMovimientos() >= 150) {
            terminar(null, "Regla de los 75 movimientos");
        } else if (repeticiones() >= 5) {
            terminar(null, "Repetición quíntuple");
        }
    }

    /**
     * Cuenta cuántas veces apareció la posición actual recorriendo la
     * ListaDoble de posiciones. O(n).
     * @return número de veces (1 = solo la actual).
     */
    public int repeticiones() {
        String clave = posiciones.ultimo();
        return posiciones.contar(clave::equals);
    }

    /**
     * ¿Se pueden reclamar tablas? (misma posición 3 veces o 50 jugadas de cada
     * bando sin capturas ni movimientos de peón).
     * @return true si el jugador puede reclamarlas.
     */
    public boolean puedeReclamarTablas() {
        return !terminada && (repeticiones() >= 3 || tablero.getMediosMovimientos() >= 100);
    }

    /**
     * Reclama tablas si las reglas lo permiten.
     * @return mensaje con el resultado.
     */
    public String reclamarTablas() {
        if (!puedeReclamarTablas()) {
            return "Todavía no se pueden reclamar tablas.";
        }
        terminar(null, repeticiones() >= 3 ? "Triple repetición" : "Regla de los 50 movimientos");
        return "Tablas reclamadas por " + motivo.toLowerCase() + ".";
    }

    /**
     * Notación algebraica en español de una jugada ANTES de aplicarla.
     * Ej.: e4, Cf3, Axb5, O-O, exd6, e8=D, Tad1.
     */
    private String notacion(Movimiento mov) {
        if ("corto".equals(mov.getEnroque())) {
            return "O-O";
        }
        if ("largo".equals(mov.getEnroque())) {
            return "O-O-O";
        }
        Pieza p = mov.getPieza();
        String destino = Movimiento.casilla(mov.getHastaF(), mov.getHastaC());
        if (p.getTipo() == 'P') {
            String col = String.valueOf((char) ('a' + mov.getDesdeC()));
            return (mov.getCapturada() != null ? col + "x" : "") + destino + (mov.getPromocion() != 0 ? "=" + mov.getPromocion() : "");
        }
        // ¿Otra pieza igual puede ir a la misma casilla? -> desambiguar
        int[] info = {0, 0, 0};   // {otras, mismaColumna, mismaFila}
        legales().recorrerAdelante(m -> {
            if (m.getPieza() != p && m.getPieza().getTipo() == p.getTipo() && m.getHastaF() == mov.getHastaF() && m.getHastaC() == mov.getHastaC()) {
                info[0]++;
                if (m.getDesdeC() == mov.getDesdeC()) info[1] = 1;
                if (m.getDesdeF() == mov.getDesdeF()) info[2] = 1;
            }
        });
        String desamb = "";
        if (info[0] > 0) {
            String origen = Movimiento.casilla(mov.getDesdeF(), mov.getDesdeC());
            desamb = info[1] == 0 ? origen.substring(0, 1) : (info[2] == 0 ? origen.substring(1) : origen);
        }
        return p.getTipo() + desamb + (mov.getCapturada() != null ? "x" : "") + destino;
    }

    /**
     * Construye la COLA de repetición con todas las jugadas en orden.
     * La pila de deshacer tiene la última jugada arriba; al pasarlas a una
     * pila auxiliar el orden se invierte (la primera queda arriba). Luego se
     * desapila y se encola: la cola queda en orden cronológico (FIFO). O(n).
     * @return Cola de códigos ("e2e4", ...).
     */
    public Cola<String> construirColaRepeticion() {
        Pila<String> auxiliar = new Pila<>();
        pilaDeshacer.recorrer(mov -> auxiliar.apilar(mov.codigo()));
        Cola<String> cola = new Cola<>();
        while (!auxiliar.estaVacia()) {
            cola.encolar(auxiliar.desapilar());
        }
        return cola;
    }

    /** @return todas las jugadas en código separadas por espacio (para guardar). O(n). */
    public String codigosJugadas() {
        StringBuilder sb = new StringBuilder();
        jugadas.recorrerAdelante(m -> {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(m.codigo());
        });
        return sb.toString();
    }

    /** @return frase con el resultado ("Ganan Blancas (Ana) por jaque mate"), o "" si sigue. */
    public String textoResultado() {
        if (!terminada) {
            return "";
        }
        if (ganador == null) {
            return "Tablas — " + motivo;
        }
        return "Ganan " + ganador.getNombre() + " (" + jugador(ganador).getNombre() + ") por " + motivo.toLowerCase();
    }

    public Modo getModo() { return modo; }
    public int getDificultad() { return dificultad; }
    public MotorAjedrez getTablero() { return tablero; }
    public GestorTurnos getTurnos() { return turnos; }
    /** @return cantidad de medias jugadas (nodos de la lista doble). */
    public int totalMovimientos() { return jugadas.tamano(); }
    /** @return ciclo de turnos de la lista circular, p. ej. "Blancas → Negras → Blancas". */
    public String cicloTurnos() { return turnos.demostrarRecorrido(); }

    /** @return las jugadas de la última a la primera (recorre la lista doble hacia atrás). */
    public String historialInverso() {
        StringBuilder b = new StringBuilder();
        jugadas.recorrerAtras(m -> b.append(b.length() == 0 ? "" : ", ").append(m.getSan()));
        return b.length() == 0 ? "Sin movimientos." : b.toString();
    }
    public Pila<Movimiento> getPilaDeshacer() { return pilaDeshacer; }
    public Pila<Movimiento> getPilaRehacer() { return pilaRehacer; }
    public ListaDoble<Movimiento> getJugadas() { return jugadas; }
    public boolean estaTerminada() { return terminada; }
    public ColorPieza getGanador() { return ganador; }
    public String getMotivo() { return motivo; }
    public boolean isGuardada() { return guardada; }
    public void setGuardada(boolean guardada) { this.guardada = guardada; }
}

package co.edu.ajedrez.logica;

import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Movimiento;
import co.edu.ajedrez.modelo.Pieza;

import co.edu.ajedrez.estructuras.ListaDoble;

/**
 * MotorAjedrez: estado del tablero y reglas del ajedrez.
 *
 * - casillas: matriz 8x8 que dice qué pieza hay en cada casilla. Es solo la
 *   cuadrícula del tablero, NO es una de las estructuras evaluadas.
 * - piezasVivas: una ListaDoble por color con las piezas en juego. Generar
 *   jugadas recorre esta lista (máx. 16 nodos) en vez de las 64 casillas.
 *
 * Reglas: movimientos de todas las piezas, capturas, jaque, jaque mate,
 * ahogado, enroque corto y largo, captura al paso, coronación, material
 * insuficiente, regla de 50 movimientos (y triple repetición en Partida).
 */
public class MotorAjedrez {

    // Tablas de desplazamientos (constantes)
    private static final int[][] SALTOS_CABALLO = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    private static final int[][] DIRECCIONES_REY = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
    private static final int[][] RECTAS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int[][] DIAGONALES = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final char[] CORONACIONES = {'D', 'T', 'A', 'C'};

    // Índices del arreglo de derechos de enroque
    private static final int B_CORTO = 0, B_LARGO = 1, N_CORTO = 2, N_LARGO = 3;

    private Pieza[][] casillas;
    private ListaDoble<Pieza> vivasBlancas;
    private ListaDoble<Pieza> vivasNegras;
    private Pieza reyBlanco, reyNegro;
    private boolean[] enroque = new boolean[4];   // derechos de enroque
    private int[] alPaso;                          // {fila, col} o null
    private int mediosMovimientos;                 // para la regla de 50

    /** Crea un tablero vacío. */
    public MotorAjedrez() {
        limpiar();
    }

    /** Deja el tablero sin piezas. */
    private void limpiar() {
        casillas = new Pieza[8][8];
        vivasBlancas = new ListaDoble<>();
        vivasNegras = new ListaDoble<>();
        reyBlanco = reyNegro = null;
        enroque = new boolean[4];
        alPaso = null;
        mediosMovimientos = 0;
    }

    /**
     * @param color color.
     * @return la ListaDoble de piezas vivas de ese color.
     */
    public ListaDoble<Pieza> piezasVivas(ColorPieza color) {
        return color == ColorPieza.BLANCO ? vivasBlancas : vivasNegras;
    }

    /**
     * @param color color.
     * @return el rey de ese color.
     */
    public Pieza rey(ColorPieza color) {
        return color == ColorPieza.BLANCO ? reyBlanco : reyNegro;
    }

    /**
     * Pone una pieza nueva en el tablero y la agrega a piezasVivas. O(1).
     * @return la pieza creada.
     */
    private Pieza colocarPieza(char tipo, ColorPieza color, int f, int c) {
        Pieza p = new Pieza(tipo, color, f, c);
        casillas[f][c] = p;
        piezasVivas(color).insertarFinal(p);
        if (tipo == 'R') {
            if (color == ColorPieza.BLANCO) {
                reyBlanco = p;
            } else {
                reyNegro = p;
            }
        }
        return p;
    }

    /** Deja el tablero en la posición inicial. */
    public void posicionInicial() {
        cargarFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    /**
     * Carga una posición en notación FEN (estándar internacional).
     * Se usa para la posición inicial y para las pruebas.
     * @param fen texto FEN.
     * @return el color al que le toca jugar.
     */
    public ColorPieza cargarFEN(String fen) {
        limpiar();
        String[] partes = fen.trim().split("\\s+");
        String[] filas = partes[0].split("/");
        String letrasFen = "kqrbnp";
        String letrasEsp = "RDTACP";
        for (int f = 0; f < 8; f++) {
            int c = 0;
            for (char ch : filas[f].toCharArray()) {
                if (Character.isDigit(ch)) {
                    c += ch - '0';
                    continue;
                }
                ColorPieza color = Character.isUpperCase(ch) ? ColorPieza.BLANCO : ColorPieza.NEGRO;
                char tipo = letrasEsp.charAt(letrasFen.indexOf(Character.toLowerCase(ch)));
                colocarPieza(tipo, color, f, c);
                c++;
            }
        }
        String derechos = partes.length > 2 ? partes[2] : "-";
        enroque[B_CORTO] = derechos.contains("K");
        enroque[B_LARGO] = derechos.contains("Q");
        enroque[N_CORTO] = derechos.contains("k");
        enroque[N_LARGO] = derechos.contains("q");
        String ep = partes.length > 3 ? partes[3] : "-";
        alPaso = ep.equals("-") ? null : new int[]{8 - (ep.charAt(1) - '0'), ep.charAt(0) - 'a'};
        mediosMovimientos = partes.length > 4 ? Integer.parseInt(partes[4]) : 0;
        return partes.length > 1 && partes[1].equals("b") ? ColorPieza.NEGRO : ColorPieza.BLANCO;
    }

    /** @return true si (f,c) está dentro del tablero. */
    public static boolean dentro(int f, int c) {
        return f >= 0 && f < 8 && c >= 0 && c < 8;
    }

    /** @return la pieza en (f,c) o null (también null si está fuera). */
    public Pieza obtener(int f, int c) {
        return dentro(f, c) ? casillas[f][c] : null;
    }

    /**
     * ¿La casilla (f,c) está atacada por alguna pieza del color "por"?
     * Se mira hacia afuera desde la casilla. O(1) (máx. 8 direcciones x 7).
     * @return true si está atacada.
     */
    public boolean estaAtacada(int f, int c, ColorPieza por) {
        int filaPeon = por == ColorPieza.BLANCO ? f + 1 : f - 1;   // el peón blanco ataca hacia arriba
        for (int dc = -1; dc <= 1; dc += 2) {
            Pieza p = obtener(filaPeon, c + dc);
            if (p != null && p.getColor() == por && p.getTipo() == 'P') {
                return true;
            }
        }
        for (int[] d : SALTOS_CABALLO) {
            Pieza p = obtener(f + d[0], c + d[1]);
            if (p != null && p.getColor() == por && p.getTipo() == 'C') {
                return true;
            }
        }
        for (int[] d : DIRECCIONES_REY) {
            Pieza p = obtener(f + d[0], c + d[1]);
            if (p != null && p.getColor() == por && p.getTipo() == 'R') {
                return true;
            }
        }
        return atacaEnLinea(f, c, por, RECTAS, 'T') || atacaEnLinea(f, c, por, DIAGONALES, 'A');
    }

    /** Busca en cada dirección la primera pieza: ¿es torre/alfil o dama del atacante? */
    private boolean atacaEnLinea(int f, int c, ColorPieza por, int[][] direcciones, char tipoLinea) {
        for (int[] d : direcciones) {
            int nf = f + d[0], nc = c + d[1];
            while (dentro(nf, nc)) {
                Pieza p = casillas[nf][nc];
                if (p != null) {
                    if (p.getColor() == por && (p.getTipo() == tipoLinea || p.getTipo() == 'D')) {
                        return true;
                    }
                    break;
                }
                nf += d[0];
                nc += d[1];
            }
        }
        return false;
    }

    /**
     * @param color color del rey.
     * @return true si ese rey está en jaque.
     */
    public boolean enJaque(ColorPieza color) {
        Pieza r = rey(color);
        return r != null && estaAtacada(r.getFila(), r.getCol(), color.contrario());
    }

    /**
     * Agrega a "lista" las jugadas pseudolegales de una pieza (siguen las
     * reglas de movimiento pero aún no se revisa si dejan al rey en jaque).
     * @param p     pieza.
     * @param lista donde se agregan los movimientos.
     */
    public void generarPseudoDePieza(Pieza p, ListaDoble<Movimiento> lista) {
        int f = p.getFila(), c = p.getCol();
        ColorPieza enemigo = p.getColor().contrario();
        switch (p.getTipo()) {
            case 'P': {
                int dir = p.getColor() == ColorPieza.BLANCO ? -1 : 1;
                int filaInicial = p.getColor() == ColorPieza.BLANCO ? 6 : 1;
                if (dentro(f + dir, c) && casillas[f + dir][c] == null) {          // avance simple
                    agregarPeon(p, f + dir, c, null, lista);
                    if (f == filaInicial && casillas[f + 2 * dir][c] == null) {   // avance doble
                        lista.insertarFinal(new Movimiento(p, f + 2 * dir, c));
                    }
                }
                for (int dc = -1; dc <= 1; dc += 2) {                              // capturas
                    int nf = f + dir, nc = c + dc;
                    if (!dentro(nf, nc)) {
                        continue;
                    }
                    Pieza objetivo = casillas[nf][nc];
                    if (objetivo != null && objetivo.getColor() == enemigo) {
                        agregarPeon(p, nf, nc, objetivo, lista);
                    } else if (objetivo == null && alPaso != null && alPaso[0] == nf && alPaso[1] == nc) {
                        Pieza capturado = casillas[f][nc];
                        if (capturado != null && capturado.getTipo() == 'P' && capturado.getColor() == enemigo) {
                            lista.insertarFinal(new Movimiento(p, nf, nc, capturado, true, null, (char) 0));
                        }
                    }
                }
                break;
            }
            case 'C':
                agregarSaltos(p, SALTOS_CABALLO, lista);
                break;
            case 'R':
                agregarSaltos(p, DIRECCIONES_REY, lista);
                agregarEnroques(p, lista);
                break;
            case 'A':
                agregarDeslizamientos(p, DIAGONALES, lista);
                break;
            case 'T':
                agregarDeslizamientos(p, RECTAS, lista);
                break;
            case 'D':
                agregarDeslizamientos(p, DIAGONALES, lista);
                agregarDeslizamientos(p, RECTAS, lista);
                break;
            default:
                break;
        }
    }

    /** Agrega un movimiento de peón; si llega a la última fila crea las 4 coronaciones. */
    private void agregarPeon(Pieza p, int nf, int nc, Pieza capturada, ListaDoble<Movimiento> lista) {
        if (nf == 0 || nf == 7) {
            for (char tipo : CORONACIONES) {
                lista.insertarFinal(new Movimiento(p, nf, nc, capturada, false, null, tipo));
            }
        } else {
            lista.insertarFinal(new Movimiento(p, nf, nc, capturada, false, null, (char) 0));
        }
    }

    /** Movimientos de un solo salto (caballo y rey). */
    private void agregarSaltos(Pieza p, int[][] saltos, ListaDoble<Movimiento> lista) {
        for (int[] d : saltos) {
            int nf = p.getFila() + d[0], nc = p.getCol() + d[1];
            if (!dentro(nf, nc)) {
                continue;
            }
            Pieza destino = casillas[nf][nc];
            if (destino == null) {
                lista.insertarFinal(new Movimiento(p, nf, nc));
            } else if (destino.getColor() != p.getColor()) {
                lista.insertarFinal(new Movimiento(p, nf, nc, destino, false, null, (char) 0));
            }
        }
    }

    /** Movimientos en línea hasta chocar (alfil, torre, dama). */
    private void agregarDeslizamientos(Pieza p, int[][] direcciones, ListaDoble<Movimiento> lista) {
        for (int[] d : direcciones) {
            int nf = p.getFila() + d[0], nc = p.getCol() + d[1];
            while (dentro(nf, nc)) {
                Pieza destino = casillas[nf][nc];
                if (destino == null) {
                    lista.insertarFinal(new Movimiento(p, nf, nc));
                } else {
                    if (destino.getColor() != p.getColor()) {
                        lista.insertarFinal(new Movimiento(p, nf, nc, destino, false, null, (char) 0));
                    }
                    break;
                }
                nf += d[0];
                nc += d[1];
            }
        }
    }

    /**
     * Enroques: derechos vigentes, casillas intermedias vacías, el rey no
     * está en jaque y no pasa ni cae en una casilla atacada.
     */
    private void agregarEnroques(Pieza rey, ListaDoble<Movimiento> lista) {
        boolean blanco = rey.getColor() == ColorPieza.BLANCO;
        int fila = blanco ? 7 : 0;
        if (rey.getFila() != fila || rey.getCol() != 4) {
            return;
        }
        boolean corto = enroque[blanco ? B_CORTO : N_CORTO];
        boolean largo = enroque[blanco ? B_LARGO : N_LARGO];
        ColorPieza enemigo = rey.getColor().contrario();
        if ((!corto && !largo) || estaAtacada(fila, 4, enemigo)) {
            return;
        }
        if (corto && esTorrePropia(fila, 7, rey.getColor()) && casillas[fila][5] == null && casillas[fila][6] == null
                && !estaAtacada(fila, 5, enemigo) && !estaAtacada(fila, 6, enemigo)) {
            lista.insertarFinal(new Movimiento(rey, fila, 6, null, false, "corto", (char) 0));
        }
        if (largo && esTorrePropia(fila, 0, rey.getColor()) && casillas[fila][1] == null && casillas[fila][2] == null
                && casillas[fila][3] == null && !estaAtacada(fila, 3, enemigo) && !estaAtacada(fila, 2, enemigo)) {
            lista.insertarFinal(new Movimiento(rey, fila, 2, null, false, "largo", (char) 0));
        }
    }

    private boolean esTorrePropia(int f, int c, ColorPieza color) {
        Pieza t = casillas[f][c];
        return t != null && t.getTipo() == 'T' && t.getColor() == color;
    }

    /**
     * Genera TODAS las jugadas legales de un color:
     * 1) recorre la ListaDoble de piezas vivas y genera las pseudolegales;
     * 2) prueba cada una (aplica, revisa jaque al propio rey, deshace).
     * @param color color que juega.
     * @return ListaDoble de movimientos legales.
     */
    public ListaDoble<Movimiento> generarLegales(ColorPieza color) {
        ListaDoble<Movimiento> pseudo = new ListaDoble<>();
        piezasVivas(color).recorrerAdelante(p -> generarPseudoDePieza(p, pseudo));
        return filtrarLegales(pseudo, color);
    }

    /**
     * Jugadas legales de UNA pieza.
     * @param pieza pieza.
     * @return ListaDoble de movimientos.
     */
    public ListaDoble<Movimiento> legalesDePieza(Pieza pieza) {
        ListaDoble<Movimiento> pseudo = new ListaDoble<>();
        generarPseudoDePieza(pieza, pseudo);
        return filtrarLegales(pseudo, pieza.getColor());
    }

    /** Deja solo las jugadas que no dejan al propio rey en jaque. */
    private ListaDoble<Movimiento> filtrarLegales(ListaDoble<Movimiento> pseudo, ColorPieza color) {
        ListaDoble<Movimiento> legales = new ListaDoble<>();
        pseudo.recorrerAdelante(m -> {
            aplicar(m);
            if (!enJaque(color)) {
                legales.insertarFinal(m);
            }
            deshacer(m);
        });
        return legales;
    }

    /**
     * Aplica un movimiento y guarda en él lo necesario para deshacerlo.
     * O(1) (+ O(16) si hay captura, por la lista de piezas vivas).
     * @param m movimiento.
     */
    public void aplicar(Movimiento m) {
        Pieza p = m.getPieza();
        boolean eraPeon = p.getTipo() == 'P';
        m.guardarEstadoPrevio(enroque.clone(), alPaso, mediosMovimientos);

        // 1. Quitar la capturada (en captura al paso está al lado, no en el destino)
        if (m.getCapturada() != null) {
            int cf = m.esAlPaso() ? m.getDesdeF() : m.getHastaF();
            casillas[cf][m.getHastaC()] = null;
            piezasVivas(m.getCapturada().getColor()).eliminarDato(m.getCapturada());
        }
        // 2. Mover la pieza
        casillas[m.getDesdeF()][m.getDesdeC()] = null;
        casillas[m.getHastaF()][m.getHastaC()] = p;
        p.mover(m.getHastaF(), m.getHastaC());
        // 3. Coronación
        if (m.getPromocion() != 0) {
            p.setTipo(m.getPromocion());
        }
        // 4. Enroque: mover también la torre
        if (m.getEnroque() != null) {
            int desdeT = m.getEnroque().equals("corto") ? 7 : 0;
            int hastaT = m.getEnroque().equals("corto") ? 5 : 3;
            Pieza torre = casillas[m.getDesdeF()][desdeT];
            casillas[m.getDesdeF()][desdeT] = null;
            casillas[m.getDesdeF()][hastaT] = torre;
            torre.mover(m.getDesdeF(), hastaT);
        }
        // 5. Casilla de captura al paso (solo tras un avance doble)
        alPaso = (eraPeon && Math.abs(m.getHastaF() - m.getDesdeF()) == 2) ? new int[]{(m.getDesdeF() + m.getHastaF()) / 2, m.getDesdeC()} : null;
        // 6. Derechos de enroque
        if (p.getTipo() == 'R') {
            if (p.getColor() == ColorPieza.BLANCO) {
                enroque[B_CORTO] = enroque[B_LARGO] = false;
            } else {
                enroque[N_CORTO] = enroque[N_LARGO] = false;
            }
        }
        quitarDerechoEsquina(m.getDesdeF(), m.getDesdeC());
        quitarDerechoEsquina(m.getHastaF(), m.getHastaC());
        // 7. Regla de 50 movimientos
        mediosMovimientos = (eraPeon || m.getCapturada() != null) ? 0 : mediosMovimientos + 1;
    }

    /** Si algo sale o llega a una esquina, esa torre ya no puede enrocar. */
    private void quitarDerechoEsquina(int f, int c) {
        if (f == 7 && c == 0) enroque[B_LARGO] = false;
        if (f == 7 && c == 7) enroque[B_CORTO] = false;
        if (f == 0 && c == 0) enroque[N_LARGO] = false;
        if (f == 0 && c == 7) enroque[N_CORTO] = false;
    }

    /**
     * Deshace un movimiento aplicado antes (operación inversa de aplicar). O(1).
     * @param m movimiento.
     */
    public void deshacer(Movimiento m) {
        Pieza p = m.getPieza();
        if (m.getEnroque() != null) {
            int desdeT = m.getEnroque().equals("corto") ? 7 : 0;
            int hastaT = m.getEnroque().equals("corto") ? 5 : 3;
            Pieza torre = casillas[m.getDesdeF()][hastaT];
            casillas[m.getDesdeF()][hastaT] = null;
            casillas[m.getDesdeF()][desdeT] = torre;
            torre.mover(m.getDesdeF(), desdeT);
        }
        if (m.getPromocion() != 0) {
            p.setTipo('P');
        }
        casillas[m.getHastaF()][m.getHastaC()] = null;
        casillas[m.getDesdeF()][m.getDesdeC()] = p;
        p.mover(m.getDesdeF(), m.getDesdeC());
        if (m.getCapturada() != null) {
            int cf = m.esAlPaso() ? m.getDesdeF() : m.getHastaF();
            casillas[cf][m.getHastaC()] = m.getCapturada();
            m.getCapturada().mover(cf, m.getHastaC());
            piezasVivas(m.getCapturada().getColor()).insertarFinal(m.getCapturada());
        }
        enroque = m.getEnroquePrevio().clone();
        alPaso = m.getAlPasoPrevio();
        mediosMovimientos = m.getMediosPrevios();
    }

    /**
     * ¿Ningún bando puede dar mate? (rey solo, rey + alfil o caballo, o
     * un alfil por bando en casillas del mismo color). O(n).
     * @return true si es material insuficiente.
     */
    public boolean materialInsuficiente() {
        if (vivasBlancas.tamano() + vivasNegras.tamano() > 4) {
            return false;
        }
        int[] rb = resumenMaterial(vivasBlancas), rn = resumenMaterial(vivasNegras);
        if (rb[1] > 0 || rn[1] > 0) {
            return false;
        }
        if (rb[0] + rn[0] <= 1) {
            return true;
        }
        return rb[0] == 1 && rn[0] == 1 && rb[2] != -1 && rb[2] == rn[2];
    }

    /** @return {piezas menores, otras piezas, color de casilla del alfil o -1}. */
    private int[] resumenMaterial(ListaDoble<Pieza> lista) {
        int[] r = {0, 0, -1};
        lista.recorrerAdelante(p -> {
            if (p.getTipo() == 'R') {
                return;
            }
            if (p.getTipo() == 'A' || p.getTipo() == 'C') {
                r[0]++;
                if (p.getTipo() == 'A') {
                    r[2] = (p.getFila() + p.getCol()) % 2;
                }
            } else {
                r[1]++;
            }
        });
        return r;
    }

    /**
     * Texto que identifica la posición (para la triple repetición). O(64).
     * @param turno color al que le toca.
     * @return la clave.
     */
    public String clavePosicion(ColorPieza turno) {
        StringBuilder s = new StringBuilder(80);
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = casillas[f][c];
                s.append(p == null ? '.' : (p.getColor() == ColorPieza.BLANCO ? p.getTipo() : Character.toLowerCase(p.getTipo())));
            }
        }
        s.append(turno == ColorPieza.BLANCO ? 'w' : 'b');
        for (boolean e : enroque) {
            s.append(e ? '1' : '0');
        }
        if (alPaso != null) {
            s.append(alPaso[0]).append(alPaso[1]);
        }
        return s.toString();
    }

    /**
     * Escribe la posición en notación FEN. La IA la usa para pensar sobre
     * una COPIA del tablero (en otro hilo) sin tocar el que se dibuja.
     * @param turno color al que le toca.
     * @return texto FEN.
     */
    public String aFEN(ColorPieza turno) {
        String letrasEsp = "RDTACP", letrasFen = "kqrbnp";
        StringBuilder s = new StringBuilder();
        for (int f = 0; f < 8; f++) {
            int vacias = 0;
            for (int c = 0; c < 8; c++) {
                Pieza p = casillas[f][c];
                if (p == null) {
                    vacias++;
                    continue;
                }
                if (vacias > 0) {
                    s.append(vacias);
                    vacias = 0;
                }
                char ch = letrasFen.charAt(letrasEsp.indexOf(p.getTipo()));
                s.append(p.getColor() == ColorPieza.BLANCO ? Character.toUpperCase(ch) : ch);
            }
            if (vacias > 0) {
                s.append(vacias);
            }
            if (f < 7) {
                s.append('/');
            }
        }
        s.append(turno == ColorPieza.BLANCO ? " w " : " b ");
        String der = (enroque[B_CORTO] ? "K" : "") + (enroque[B_LARGO] ? "Q" : "") + (enroque[N_CORTO] ? "k" : "") + (enroque[N_LARGO] ? "q" : "");
        s.append(der.isEmpty() ? "-" : der).append(' ');
        s.append(alPaso == null ? "-" : Movimiento.casilla(alPaso[0], alPaso[1])).append(' ').append(mediosMovimientos).append(" 1");
        return s.toString();
    }

    /** @return número de medias jugadas sin captura ni peón. */
    public int getMediosMovimientos() {
        return mediosMovimientos;
    }
}

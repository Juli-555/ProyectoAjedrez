package co.edu.ajedrez.modelo;

/**
 * Una jugada y la información necesaria para deshacerla.
 * Además del origen y el destino guarda lo que cambia en el tablero
 * (pieza capturada, derechos de enroque, casilla de captura al paso...).
 * Gracias a eso la Pila de deshacer puede revertir cualquier jugada.
 */
public class Movimiento {

    private final Pieza pieza;
    private final int desdeF, desdeC, hastaF, hastaC;
    private final Pieza capturada;       // pieza capturada o null
    private final boolean alPaso;        // captura al paso
    private final String enroque;        // "corto", "largo" o null
    private final char promocion;        // 'D','T','A','C' o 0 si no corona

    // --- Datos para deshacer (los llena MotorAjedrez.aplicar) ---
    private boolean[] enroquePrevio;
    private int[] alPasoPrevio;
    private int mediosPrevios;

    // --- Notación algebraica (la llena GestorPartida) ---
    private String san = "";

    /**
     * Crea un movimiento simple (sin captura).
     * @param pieza  pieza que se mueve.
     * @param hastaF fila destino.
     * @param hastaC columna destino.
     */
    public Movimiento(Pieza pieza, int hastaF, int hastaC) {
        this(pieza, hastaF, hastaC, null, false, null, (char) 0);
    }

    /**
     * Crea un movimiento completo.
     * @param pieza     pieza que se mueve.
     * @param hastaF    fila destino.
     * @param hastaC    columna destino.
     * @param capturada pieza capturada (o null).
     * @param alPaso    true si es captura al paso.
     * @param enroque   "corto", "largo" o null.
     * @param promocion pieza a la que corona o 0.
     */
    public Movimiento(Pieza pieza, int hastaF, int hastaC, Pieza capturada,
                      boolean alPaso, String enroque, char promocion) {
        this.pieza = pieza;
        this.desdeF = pieza.getFila();
        this.desdeC = pieza.getCol();
        this.hastaF = hastaF;
        this.hastaC = hastaC;
        this.capturada = capturada;
        this.alPaso = alPaso;
        this.enroque = enroque;
        this.promocion = promocion;
    }

    /**
     * Convierte fila/columna a texto, p. ej. (7,4) -> "e1".
     * @param f fila 0..7
     * @param c columna 0..7
     * @return texto de 2 caracteres.
     */
    public static String casilla(int f, int c) {
        return "" + (char) ('a' + c) + (8 - f);
    }

    /**
     * Código corto "origen+destino[+coronación]" ("e2e4", "e7e8D").
     * Se usa para guardar partidas y repetirlas.
     * @return el código.
     */
    public String codigo() {
        return casilla(desdeF, desdeC) + casilla(hastaF, hastaC) + (promocion != 0 ? String.valueOf(promocion) : "");
    }

    /**
     * Interpreta un código "e2e4" / "e7e8D".
     * @param texto código.
     * @return {desdeF, desdeC, hastaF, hastaC, promocion} o null si es inválido.
     */
    public static int[] leerCodigo(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim();
        if (!t.matches("[a-h][1-8][a-h][1-8][DTAC]?")) {
            return null;
        }
        return new int[]{
            8 - (t.charAt(1) - '0'), t.charAt(0) - 'a',
            8 - (t.charAt(3) - '0'), t.charAt(2) - 'a',
            t.length() == 5 ? t.charAt(4) : 0
        };
    }

    /**
     * Guarda el estado del tablero ANTES de aplicar la jugada (lo usa MotorAjedrez).
     * @param enroque      copia de los derechos de enroque.
     * @param alPaso       casilla de captura al paso o null.
     * @param medios       contador de la regla de 50 movimientos.
     */
    public void guardarEstadoPrevio(boolean[] enroque, int[] alPaso, int medios) {
        this.enroquePrevio = enroque;
        this.alPasoPrevio = alPaso;
        this.mediosPrevios = medios;
    }

    public Pieza getPieza() { return pieza; }
    public int getDesdeF() { return desdeF; }
    public int getDesdeC() { return desdeC; }
    public int getHastaF() { return hastaF; }
    public int getHastaC() { return hastaC; }
    public Pieza getCapturada() { return capturada; }
    public boolean esAlPaso() { return alPaso; }
    public String getEnroque() { return enroque; }
    public char getPromocion() { return promocion; }
    public boolean[] getEnroquePrevio() { return enroquePrevio; }
    public int[] getAlPasoPrevio() { return alPasoPrevio; }
    public int getMediosPrevios() { return mediosPrevios; }
    public String getSan() { return san; }

    /** @param san notación algebraica de la jugada. */
    public void setSan(String san) {
        this.san = san;
    }

    @Override
    public String toString() {
        return san.isEmpty() ? codigo() : san;
    }
}

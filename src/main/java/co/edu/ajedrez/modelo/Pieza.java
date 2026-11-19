package co.edu.ajedrez.modelo;

/**
 * Una pieza de ajedrez sobre el tablero.
 *
 * Tipos (letra en español): 'R' Rey, 'D' Dama, 'T' Torre, 'A' Alfil,
 * 'C' Caballo, 'P' Peón.
 * Coordenadas: fila 0 es la fila 8 del tablero (arriba) y fila 7 la fila 1;
 * columna 0 es la columna "a".
 */
public class Pieza {

    private static int siguienteId = 1;

    private char tipo;
    private final ColorPieza color;
    private int fila;
    private int col;
    private final int id;

    /**
     * Crea una pieza.
     * @param tipo  'R','D','T','A','C' o 'P'.
     * @param color color de la pieza.
     * @param fila  0..7
     * @param col   0..7
     */
    public Pieza(char tipo, ColorPieza color, int fila, int col) {
        this.tipo = tipo;
        this.color = color;
        this.fila = fila;
        this.col = col;
        this.id = siguienteId++;
    }

    /**
     * Valor material de un tipo de pieza en "centipeones" (lo usa la IA).
     * @param tipo letra de la pieza.
     * @return 100 peón, 320 caballo, 330 alfil, 500 torre, 900 dama, 0 rey.
     */
    public static int valor(char tipo) {
        switch (tipo) {
            case 'P': return 100;
            case 'C': return 320;
            case 'A': return 330;
            case 'T': return 500;
            case 'D': return 900;
            default: return 0;
        }
    }

    /**
     * @param tipo letra de la pieza.
     * @return nombre en español ("Dama", "Caballo"...).
     */
    public static String nombre(char tipo) {
        switch (tipo) {
            case 'P': return "Peón";
            case 'C': return "Caballo";
            case 'A': return "Alfil";
            case 'T': return "Torre";
            case 'D': return "Dama";
            default: return "Rey";
        }
    }

    /** @return valor material de esta pieza. */
    public int valor() {
        return valor(tipo);
    }

    public char getTipo() {
        return tipo;
    }

    /** Cambia el tipo (solo lo usa MotorAjedrez al coronar y al deshacer la coronación). */
    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public ColorPieza getColor() {
        return color;
    }

    public int getFila() {
        return fila;
    }

    public int getCol() {
        return col;
    }

    /** Actualiza la posición de la pieza (solo la llama MotorAjedrez). */
    public void mover(int fila, int col) {
        this.fila = fila;
        this.col = col;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return (color == ColorPieza.BLANCO ? "" : "n") + tipo + Movimiento.casilla(fila, col);
    }
}

package co.edu.ajedrez.modelo;

/**
 * Color de una pieza o de un jugador.
 */
public enum ColorPieza {
    BLANCO("Blancas"),
    NEGRO("Negras");

    private final String nombre;

    ColorPieza(String nombre) {
        this.nombre = nombre;
    }

    /** @return el color contrario. */
    public ColorPieza contrario() {
        return this == BLANCO ? NEGRO : BLANCO;
    }

    /** @return "Blancas" o "Negras". */
    public String getNombre() {
        return nombre;
    }
}

package co.edu.ajedrez.modelo;

/**
 * Datos de un jugador: su color, su nombre y si es la computadora.
 * Cada jugador es un nodo de la ListaCircular de turnos.
 */
public class Jugador {

    private final ColorPieza color;
    private final String nombre;
    private final boolean esIA;

    /**
     * @param color  color con el que juega.
     * @param nombre nombre a mostrar.
     * @param esIA   true si lo controla la computadora.
     */
    public Jugador(ColorPieza color, String nombre, boolean esIA) {
        this.color = color;
        this.nombre = nombre;
        this.esIA = esIA;
    }

    public ColorPieza getColor() {
        return color;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esIA() {
        return esIA;
    }

    @Override
    public String toString() {
        return color.getNombre() + " (" + nombre + ")";
    }
}

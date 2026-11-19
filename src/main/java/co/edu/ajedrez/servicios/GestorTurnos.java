package co.edu.ajedrez.servicios;

import co.edu.ajedrez.estructuras.ListaCircular;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Jugador;
import java.util.function.Consumer;

/**
 * Gestiona los turnos con una LISTA CIRCULAR propia: un nodo por jugador
 * (Blancas -> Negras -> Blancas...). El cursor "turno" de la lista indica a
 * quién le toca; al llegar al último nodo, avanzar vuelve solo al primero
 * porque el último apunta al primero.
 */
public final class GestorTurnos {

    private final ListaCircular<Jugador> jugadores = new ListaCircular<>();

    /**
     * Agrega un jugador al final del ciclo de turnos. O(1).
     * @param j jugador.
     */
    public void agregar(Jugador j) {
        jugadores.insertarFinal(j);
    }

    /** @return el jugador al que le toca (nodo apuntado por el cursor). O(1). */
    public Jugador actual() {
        return jugadores.obtenerTurno();
    }

    /** @return el nombre del bando actual ("Blancas" o "Negras"). */
    public String jugadorActual() {
        return actual().getColor().getNombre();
    }

    /** Pasa el turno al siguiente nodo tras una jugada legal. O(1). */
    public void avanzar() {
        jugadores.avanzarTurno();
    }

    /** Devuelve el turno al nodo anterior (al deshacer). O(n), n = 2. */
    public void retroceder() {
        jugadores.retrocederTurno();
    }

    /**
     * Busca un jugador por color recorriendo la lista. O(n).
     * @param color color buscado.
     * @return el jugador.
     */
    public Jugador buscar(ColorPieza color) {
        return jugadores.buscar(j -> j.getColor() == color);
    }

    /**
     * Recorre una vuelta completa de la lista.
     * @param accion operación aplicada a cada jugador.
     */
    public void recorrer(Consumer<Jugador> accion) {
        jugadores.recorrer(accion);
    }

    /** @return recorrido demostrativo, p. ej. "Blancas → Negras → Blancas". */
    public String demostrarRecorrido() {
        StringBuilder b = new StringBuilder();
        jugadores.recorrer(j -> {
            if (b.length() > 0) {
                b.append(" → ");
            }
            b.append(j.getColor().getNombre());
        });
        return b.append(" → ").append(jugadores.siguienteDelUltimo().getColor().getNombre()).toString();
    }

    /** @return la lista circular (para mostrarla en el panel de estructuras). */
    public ListaCircular<Jugador> getLista() {
        return jugadores;
    }
}

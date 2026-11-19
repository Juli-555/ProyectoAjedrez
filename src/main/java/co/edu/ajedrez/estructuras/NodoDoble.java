package co.edu.ajedrez.estructuras;

/**
 * Nodo con dos enlaces: al anterior y al siguiente.
 * Lo usa: ListaDoble.
 *
 * @param <T> tipo del dato.
 */
public class NodoDoble<T> {

    T dato;
    NodoDoble<T> anterior;    // referencia al nodo anterior
    NodoDoble<T> siguiente;   // referencia al nodo siguiente

    /**
     * Crea un nodo doble sin enlaces.
     * @param dato valor que guarda el nodo.
     */
    public NodoDoble(T dato) {
        this.dato = dato;
    }

    /** @return el dato guardado. */
    public T getDato() {
        return dato;
    }

    /** @return el nodo anterior (o null si es la cabeza). */
    public NodoDoble<T> getAnterior() {
        return anterior;
    }

    /** @return el nodo siguiente (o null si es la cola). */
    public NodoDoble<T> getSiguiente() {
        return siguiente;
    }
}

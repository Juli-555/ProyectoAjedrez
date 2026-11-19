package co.edu.ajedrez.estructuras;

/**
 * Nodo con un solo enlace hacia el siguiente nodo.
 * Lo usan: Pila, Cola y ListaCircular.
 *
 * @param <T> tipo del dato que guarda el nodo.
 */
public class NodoSimple<T> {

    T dato;                    // información guardada
    NodoSimple<T> siguiente;   // referencia (puntero) al siguiente nodo

    /**
     * Crea un nodo con siguiente = null.
     * @param dato valor que guarda el nodo.
     */
    public NodoSimple(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    /** @return el dato guardado. */
    public T getDato() {
        return dato;
    }

    /** @return el nodo siguiente (o null). */
    public NodoSimple<T> getSiguiente() {
        return siguiente;
    }
}

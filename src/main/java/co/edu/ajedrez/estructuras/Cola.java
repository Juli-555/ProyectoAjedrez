package co.edu.ajedrez.estructuras;

import java.util.function.Consumer;

/**
 * Cola (FIFO: el primero en entrar es el primero en salir), implementada
 * como lista enlazada simple con referencias al frente y al final.
 *
 * USO EN EL JUEGO:
 *  - Repetición de la partida: las jugadas se encolan en orden y la
 *    repetición automática desencola una por paso y la muestra.
 *  - También se usa para repetir partidas guardadas en el árbol.
 *
 * @param <T> tipo de los elementos.
 */
public class Cola<T> {

    private NodoSimple<T> frente;   // primer nodo (sale primero)
    private NodoSimple<T> fin;      // último nodo (entró de último)
    private int cantidad;

    /** Crea una cola vacía. */
    public Cola() {
        frente = null;
        fin = null;
        cantidad = 0;
    }

    /**
     * Encola (enqueue): agrega un dato al final. Complejidad O(1).
     * @param dato elemento a agregar.
     */
    public void encolar(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        cantidad++;
    }

    /**
     * Desencola (dequeue): quita y devuelve el dato del frente. Complejidad O(1).
     * @return el dato del frente, o null si la cola está vacía.
     */
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;   // quedó vacía
        }
        cantidad--;
        return dato;
    }

    /**
     * Consulta el primer dato sin quitarlo. Complejidad O(1).
     * @return el dato del frente, o null si está vacía.
     */
    public T verFrente() {
        return estaVacia() ? null : frente.dato;
    }

    /** @return true si la cola está vacía. O(1). */
    public boolean estaVacia() {
        return frente == null;
    }

    /** @return número de elementos. O(1). */
    public int tamano() {
        return cantidad;
    }

    /** Igual que vaciar(): nombre usado en la primera versión del proyecto. O(1). */
    public void limpiar() {
        vaciar();
    }

    /** Vacía la cola. O(1). */
    public void vaciar() {
        frente = null;
        fin = null;
        cantidad = 0;
    }

    /**
     * Recorre del frente al final sin modificar la cola. O(n).
     * @param accion operación aplicada a cada dato.
     */
    public void recorrer(Consumer<T> accion) {
        NodoSimple<T> actual = frente;
        while (actual != null) {
            accion.accept(actual.dato);
            actual = actual.siguiente;
        }
    }
}

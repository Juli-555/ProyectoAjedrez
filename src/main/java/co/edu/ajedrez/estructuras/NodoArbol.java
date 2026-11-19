package co.edu.ajedrez.estructuras;

/**
 * Nodo de árbol binario: una clave para ordenar, un dato y dos hijos.
 * Lo usa: ArbolBST.
 *
 * @param <T> tipo del dato.
 */
public class NodoArbol<T> {

    int clave;                  // valor con el que se ordena el árbol
    T dato;                     // información asociada
    NodoArbol<T> izquierdo;     // claves menores
    NodoArbol<T> derecho;       // claves mayores o iguales

    /**
     * Crea un nodo hoja.
     * @param clave clave numérica.
     * @param dato  información asociada.
     */
    public NodoArbol(int clave, T dato) {
        this.clave = clave;
        this.dato = dato;
    }

    /** @return la clave del nodo. */
    public int getClave() {
        return clave;
    }

    /** @return el dato del nodo. */
    public T getDato() {
        return dato;
    }

    /** @return el hijo izquierdo (o null). */
    public NodoArbol<T> getIzquierdo() {
        return izquierdo;
    }

    /** @return el hijo derecho (o null). */
    public NodoArbol<T> getDerecho() {
        return derecho;
    }
}

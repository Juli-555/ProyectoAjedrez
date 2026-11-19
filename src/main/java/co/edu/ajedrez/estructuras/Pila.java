package co.edu.ajedrez.estructuras;

import java.util.function.Consumer;

/**
 * Pila (LIFO: el último en entrar es el primero en salir), implementada
 * como lista enlazada simple donde el tope es la cabeza.
 *
 * USO EN EL JUEGO:
 *  - pilaDeshacer: cada jugada hecha se apila; "Deshacer" saca la del tope.
 *  - pilaRehacer: la jugada deshecha se apila aquí; "Rehacer" la saca.
 *    Una jugada nueva vacía esta pila.
 *  - Al terminar, una pila auxiliar invierte el orden de las jugadas para
 *    llenar la Cola de repetición.
 *
 * @param <T> tipo de los elementos.
 */
public class Pila<T> {

    private NodoSimple<T> tope;   // nodo de arriba
    private int cantidad;         // número de elementos

    /** Crea una pila vacía. */
    public Pila() {
        tope = null;
        cantidad = 0;
    }

    /**
     * Apila (push): pone un dato encima de todos. Complejidad O(1).
     * @param dato elemento a guardar.
     */
    public void apilar(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        nuevo.siguiente = tope;   // el nuevo apunta al antiguo tope
        tope = nuevo;             // y pasa a ser el tope
        cantidad++;
    }

    /**
     * Desapila (pop): quita y devuelve el dato del tope. Complejidad O(1).
     * @return el dato del tope, o null si la pila está vacía.
     */
    //metodo o cperacion
    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T dato = tope.dato;
        tope = tope.siguiente;
        cantidad--;
        return dato;
    }

    /**
     * Consulta el dato del tope sin quitarlo. Complejidad O(1).
     * @return el dato del tope, o null si está vacía.
     */
    public T verTope() {
        return estaVacia() ? null : tope.dato;
    }

    /**
     * Complejidad O(1).
     * @return true si la pila no tiene elementos.
     */
    public boolean estaVacia() {
        return tope == null;
    }

    /**
     * Complejidad O(1).
     * @return número de elementos.
     */
    public int tamano() {
        return cantidad;
    }

    /** Igual que vaciar(): nombre usado en la primera versión del proyecto. O(1). */
    public void limpiar() {
        vaciar();
    }

    /** Elimina todos los elementos. Complejidad O(1). */
    public void vaciar() {
        tope = null;
        cantidad = 0;
    }

    /**
     * Recorre la pila del tope al fondo sin modificarla. Complejidad O(n).
     * @param accion operación que se aplica a cada dato.
     */
    public void recorrer(Consumer<T> accion) {
        NodoSimple<T> actual = tope;
        while (actual != null) {
            accion.accept(actual.dato);
            actual = actual.siguiente;
        }
    }
}

package co.edu.ajedrez.estructuras;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Lista circular simplemente enlazada: el último nodo NO apunta a null,
 * apunta de nuevo al primero. Se guarda solo la referencia al ÚLTIMO;
 * el primero es ultimo.siguiente (O(1)).
 *
 * USO EN EL JUEGO:
 *  - Control de TURNOS: un nodo por jugador (Blancas -> Negras -> ...).
 *    La referencia "turno" indica a quién le toca; tras cada jugada se
 *    llama a avanzarTurno() y al deshacer a retrocederTurno(). Para un
 *    torneo con más jugadores bastaría con insertar más nodos.
 *
 * @param <T> tipo de los elementos.
 */
public class ListaCircular<T> {

    private NodoSimple<T> ultimo;   // ultimo.siguiente es el primero
    private NodoSimple<T> turno;    // cursor: nodo al que le toca
    private int cantidad;

    /** Crea una lista circular vacía. */
    public ListaCircular() {
        ultimo = null;
        turno = null;
        cantidad = 0;
    }

    /** @return el primer nodo, o null si está vacía. O(1). */
    private NodoSimple<T> primero() {
        return ultimo == null ? null : ultimo.siguiente;
    }

    /**
     * Inserta un dato al inicio. O(1).
     * @param dato elemento a insertar.
     */
    public void insertarInicio(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (ultimo == null) {
            nuevo.siguiente = nuevo;            // un solo nodo se apunta a sí mismo
            ultimo = nuevo;
        } else {
            nuevo.siguiente = ultimo.siguiente; // apunta al antiguo primero
            ultimo.siguiente = nuevo;           // el último apunta al nuevo primero
        }
        if (turno == null) {
            turno = nuevo;
        }
        cantidad++;
    }

    /**
     * Inserta un dato al final. O(1).
     * @param dato elemento a insertar.
     */
    public void insertarFinal(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (ultimo == null) {
            nuevo.siguiente = nuevo;
        } else {
            nuevo.siguiente = ultimo.siguiente; // el nuevo cierra el círculo
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        if (turno == null) {
            turno = nuevo;
        }
        cantidad++;
    }

    /**
     * Inserta un dato en una posición (0 = inicio). O(n).
     * @param posicion entero entre 0 y tamano().
     * @param dato     elemento a insertar.
     * @return true si se insertó, false si la posición no es válida.
     */
    public boolean insertarEnPosicion(int posicion, T dato) {
        if (posicion < 0 || posicion > cantidad) {
            return false;
        }
        if (posicion == 0) {
            insertarInicio(dato);
            return true;
        }
        if (posicion == cantidad) {
            insertarFinal(dato);
            return true;
        }
        NodoSimple<T> anterior = primero();
        for (int i = 0; i < posicion - 1; i++) {
            anterior = anterior.siguiente;
        }
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        nuevo.siguiente = anterior.siguiente;
        anterior.siguiente = nuevo;
        cantidad++;
        return true;
    }

    /**
     * Elimina el primer elemento que cumpla el criterio. O(n).
     * @param criterio condición que debe cumplir el dato.
     * @return el dato eliminado, o null si no se encontró.
     */
    public T eliminar(Predicate<T> criterio) {
        if (ultimo == null) {
            return null;
        }
        NodoSimple<T> anterior = ultimo;
        NodoSimple<T> actual = ultimo.siguiente;
        for (int i = 0; i < cantidad; i++) {
            if (criterio.test(actual.dato)) {
                quitarNodo(anterior, actual);
                return actual.dato;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Elimina el elemento de una posición. O(n).
     * @param posicion entero entre 0 y tamano()-1.
     * @return el dato eliminado, o null si la posición no es válida.
     */
    public T eliminarEnPosicion(int posicion) {
        if (posicion < 0 || posicion >= cantidad) {
            return null;
        }
        NodoSimple<T> anterior = ultimo;
        NodoSimple<T> actual = ultimo.siguiente;
        for (int i = 0; i < posicion; i++) {
            anterior = actual;
            actual = actual.siguiente;
        }
        quitarNodo(anterior, actual);
        return actual.dato;
    }

    /** Desconecta "nodo" conociendo su anterior; ajusta ultimo y turno. O(1). */
    private void quitarNodo(NodoSimple<T> anterior, NodoSimple<T> nodo) {
        if (cantidad == 1) {
            ultimo = null;
            turno = null;
        } else {
            anterior.siguiente = nodo.siguiente;
            if (nodo == ultimo) {
                ultimo = anterior;
            }
            if (nodo == turno) {
                turno = nodo.siguiente;
            }
        }
        cantidad--;
    }

    /**
     * Busca el primer elemento que cumpla el criterio. O(n).
     * @param criterio condición de búsqueda.
     * @return el dato encontrado o null.
     */
    public T buscar(Predicate<T> criterio) {
        NodoSimple<T> actual = primero();
        for (int i = 0; i < cantidad; i++) {
            if (criterio.test(actual.dato)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Busca la posición del primer elemento que cumpla el criterio. O(n).
     * @param criterio condición de búsqueda.
     * @return índice 0..n-1, o -1 si no está.
     */
    public int buscarPosicion(Predicate<T> criterio) {
        NodoSimple<T> actual = primero();
        for (int i = 0; i < cantidad; i++) {
            if (criterio.test(actual.dato)) {
                return i;
            }
            actual = actual.siguiente;
        }
        return -1;
    }

    /**
     * Recorre UNA vuelta completa, del primero al último. O(n).
     * @param accion operación aplicada a cada dato.
     */
    public void recorrer(Consumer<T> accion) {
        NodoSimple<T> actual = primero();
        for (int i = 0; i < cantidad; i++) {
            accion.accept(actual.dato);
            actual = actual.siguiente;
        }
    }

    /**
     * Inserta en una posición (alias de insertarEnPosicion).
     * @param posicion índice 0..tamano().
     * @param dato     elemento.
     * @return true si se insertó.
     */
    public boolean insertar(int posicion, T dato) {
        return insertarEnPosicion(posicion, dato);
    }

    /**
     * Elimina la posición indicada (alias de eliminarEnPosicion). O(n).
     * @param posicion índice.
     * @return el dato eliminado o null.
     */
    public T eliminarEn(int posicion) {
        return eliminarEnPosicion(posicion);
    }

    /**
     * Devuelve el dato de una posición recorriendo desde el primero. O(n).
     * @param posicion índice 0..n-1.
     * @return el dato, o null si la posición no es válida.
     */
    public T obtener(int posicion) {
        if (posicion < 0 || posicion >= cantidad) {
            return null;
        }
        NodoSimple<T> actual = primero();
        for (int i = 0; i < posicion; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** @return el dato del turno actual, o null si está vacía. O(1). */
    public T obtenerTurno() {
        return turno == null ? null : turno.dato;
    }

    /**
     * Pasa el turno al siguiente nodo (da la vuelta sola). O(1).
     * @return el dato del nuevo turno.
     */
    public T avanzarTurno() {
        if (turno == null) {
            return null;
        }
        turno = turno.siguiente;
        return turno.dato;
    }

    /**
     * Devuelve el turno al nodo anterior (se usa al deshacer). Como la lista
     * es simple, se da la vuelta buscando el nodo cuyo siguiente es el turno. O(n).
     * @return el dato del nuevo turno.
     */
    public T retrocederTurno() {
        if (turno == null) {
            return null;
        }
        NodoSimple<T> actual = turno;
        while (actual.siguiente != turno) {
            actual = actual.siguiente;
        }
        turno = actual;
        return turno.dato;
    }

    /** Pone el turno en el primer nodo. O(1). */
    public void reiniciarTurno() {
        turno = primero();
    }

    /** @return el dato al que apunta el último nodo (debe ser el primero). O(1). */
    public T siguienteDelUltimo() {
        return ultimo == null ? null : ultimo.siguiente.dato;
    }

    /** @return true si está vacía. O(1). */
    public boolean estaVacia() {
        return cantidad == 0;
    }

    /** @return número de elementos. O(1). */
    public int tamano() {
        return cantidad;
    }

    /** Igual que vaciar(): nombre usado en la primera versión del proyecto. O(1). */
    public void limpiar() {
        vaciar();
    }

    /** Elimina todos los elementos. O(1). */
    public void vaciar() {
        ultimo = null;
        turno = null;
        cantidad = 0;
    }
}

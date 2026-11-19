package co.edu.ajedrez.estructuras;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Lista doblemente enlazada: cada nodo conoce a su anterior y a su
 * siguiente, así se recorre en AMBOS sentidos y un nodo conocido se quita en O(1).
 *
 * USO EN EL JUEGO:
 *  1. PIEZAS VIVAS: una lista por jugador con sus piezas en el tablero.
 *     Para generar jugadas solo se recorren esas piezas (máx. 16).
 *     Al capturar se elimina la pieza; al deshacer, se vuelve a insertar.
 *  2. LISTA DE JUGADAS / VISOR: el visor usa un cursor sobre los nodos:
 *     "Adelante" = cursor.siguiente, "Atrás" = cursor.anterior.
 *  3. Resultados temporales: jugadas legales, recorridos del árbol, etc.
 *
 * @param <T> tipo de los elementos.
 */
public class ListaDoble<T> {

    private NodoDoble<T> cabeza;   // primer nodo
    private NodoDoble<T> cola;     // último nodo
    private int cantidad;

    /** Interfaz para recorridos que pueden detenerse (devolviendo false). */
    public interface Visitante<T> {
        /**
         * @param dato     dato del nodo visitado.
         * @param posicion índice del nodo.
         * @return false para detener el recorrido.
         */
        boolean visitar(T dato, int posicion);
    }

    /** Crea una lista vacía. */
    public ListaDoble() {
        cabeza = null;
        cola = null;
        cantidad = 0;
    }

    /**
     * Inserta al inicio. O(1).
     * @param dato elemento.
     * @return el nodo creado.
     */
    public NodoDoble<T> insertarInicio(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (cabeza == null) {
            cabeza = cola = nuevo;
        } else {
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
            cabeza = nuevo;
        }
        cantidad++;
        return nuevo;
    }

    /**
     * Inserta al final. O(1).
     * @param dato elemento.
     * @return el nodo creado.
     */
    public NodoDoble<T> insertarFinal(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (cola == null) {
            cabeza = cola = nuevo;
        } else {
            nuevo.anterior = cola;
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        cantidad++;
        return nuevo;
    }

    /**
     * Inserta en una posición (0 = inicio, tamano() = final). O(n).
     * @param posicion índice donde quedará el dato.
     * @param dato     elemento.
     * @return el nodo creado, o null si la posición no es válida.
     */
    public NodoDoble<T> insertarEnPosicion(int posicion, T dato) {
        if (posicion < 0 || posicion > cantidad) {
            return null;
        }
        if (posicion == 0) {
            return insertarInicio(dato);
        }
        if (posicion == cantidad) {
            return insertarFinal(dato);
        }
        NodoDoble<T> siguiente = obtenerNodo(posicion);
        NodoDoble<T> anterior = siguiente.anterior;
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        nuevo.anterior = anterior;
        nuevo.siguiente = siguiente;
        anterior.siguiente = nuevo;
        siguiente.anterior = nuevo;
        cantidad++;
        return nuevo;
    }

    /**
     * Devuelve el NODO de una posición; si está en la segunda mitad,
     * recorre desde la cola (aprovecha los dos sentidos). O(n/2).
     * @param posicion índice.
     * @return el nodo o null.
     */
    public NodoDoble<T> obtenerNodo(int posicion) {
        if (posicion < 0 || posicion >= cantidad) {
            return null;
        }
        NodoDoble<T> actual;
        if (posicion < cantidad / 2) {
            actual = cabeza;
            for (int i = 0; i < posicion; i++) {
                actual = actual.siguiente;
            }
        } else {
            actual = cola;
            for (int i = cantidad - 1; i > posicion; i--) {
                actual = actual.anterior;
            }
        }
        return actual;
    }

    /**
     * @param posicion índice.
     * @return el dato de esa posición o null. O(n).
     */
    public T obtener(int posicion) {
        NodoDoble<T> nodo = obtenerNodo(posicion);
        return nodo == null ? null : nodo.dato;
    }

    /**
     * Desconecta un nodo que pertenece a esta lista. O(1).
     * @param nodo nodo a quitar.
     * @return su dato.
     */
    public T eliminarNodo(NodoDoble<T> nodo) {
        if (nodo.anterior != null) {
            nodo.anterior.siguiente = nodo.siguiente;
        } else {
            cabeza = nodo.siguiente;
        }
        if (nodo.siguiente != null) {
            nodo.siguiente.anterior = nodo.anterior;
        } else {
            cola = nodo.anterior;
        }
        nodo.anterior = null;
        nodo.siguiente = null;
        cantidad--;
        return nodo.dato;
    }

    /**
     * Elimina el primer elemento que cumpla el criterio. O(n).
     * @param criterio condición.
     * @return el dato eliminado o null.
     */
    public T eliminar(Predicate<T> criterio) {
        NodoDoble<T> actual = cabeza;
        while (actual != null) {
            if (criterio.test(actual.dato)) {
                return eliminarNodo(actual);
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Elimina un objeto concreto (compara referencias con ==). O(n).
     * @param dato objeto a quitar.
     * @return el dato eliminado o null.
     */
    public T eliminarDato(T dato) {
        return eliminar(d -> d == dato);
    }

    /**
     * Elimina el elemento de una posición. O(n).
     * @param posicion índice.
     * @return el dato eliminado o null.
     */
    public T eliminarEnPosicion(int posicion) {
        NodoDoble<T> nodo = obtenerNodo(posicion);
        return nodo == null ? null : eliminarNodo(nodo);
    }

    /** @return el último dato eliminado de la lista, o null si está vacía. O(1). */
    public T eliminarFinal() {
        return cola == null ? null : eliminarNodo(cola);
    }

    /**
     * Busca el primer elemento que cumpla el criterio. O(n).
     * @param criterio condición.
     * @return el dato o null.
     */
    public T buscar(Predicate<T> criterio) {
        NodoDoble<T> actual = cabeza;
        while (actual != null) {
            if (criterio.test(actual.dato)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Cuenta cuántos elementos cumplen el criterio. O(n).
     * @param criterio condición.
     * @return cantidad.
     */
    public int contar(Predicate<T> criterio) {
        int total = 0;
        NodoDoble<T> actual = cabeza;
        while (actual != null) {
            if (criterio.test(actual.dato)) {
                total++;
            }
            actual = actual.siguiente;
        }
        return total;
    }

    /**
     * Recorre de la CABEZA a la COLA. O(n).
     * @param accion operación aplicada a cada dato.
     */
    public void recorrerAdelante(Consumer<T> accion) {
        NodoDoble<T> actual = cabeza;
        while (actual != null) {
            accion.accept(actual.dato);
            actual = actual.siguiente;
        }
    }

    /**
     * Recorre de la cabeza a la cola con índice; se detiene si el visitante devuelve false. O(n).
     * @param visitante recibe (dato, posición).
     */
    public void recorrerAdelante(Visitante<T> visitante) {
        NodoDoble<T> actual = cabeza;
        int i = 0;
        while (actual != null) {
            if (!visitante.visitar(actual.dato, i++)) {
                return;
            }
            actual = actual.siguiente;
        }
    }

    /**
     * Recorre de la COLA a la CABEZA (sentido inverso). O(n).
     * @param accion operación aplicada a cada dato.
     */
    public void recorrerAtras(Consumer<T> accion) {
        NodoDoble<T> actual = cola;
        while (actual != null) {
            accion.accept(actual.dato);
            actual = actual.anterior;
        }
    }

    /**
     * Inserta en una posición (alias de insertarEnPosicion). O(n).
     * @param posicion índice 0..tamano().
     * @param dato     elemento.
     * @return el nodo creado o null.
     */
    public NodoDoble<T> insertar(int posicion, T dato) {
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

    /** @return el último dato eliminado (alias de eliminarFinal). O(1). */
    public T eliminarUltimo() {
        return eliminarFinal();
    }

    /** @return el primer nodo (para el cursor del visor). */
    public NodoDoble<T> getCabeza() {
        return cabeza;
    }

    /** @return el último nodo (para el cursor del visor). */
    public NodoDoble<T> getCola() {
        return cola;
    }

    /** @return el primer dato o null. O(1). */
    public T primero() {
        return cabeza == null ? null : cabeza.dato;
    }

    /** @return el último dato o null. O(1). */
    public T ultimo() {
        return cola == null ? null : cola.dato;
    }

    /**
     * Texto con los datos separados por ", " (para mostrar y para pruebas). O(n).
     * @return por ejemplo "[1, 2, 3]".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        NodoDoble<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append(", ");
            }
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
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
        cabeza = null;
        cola = null;
        cantidad = 0;
    }
}

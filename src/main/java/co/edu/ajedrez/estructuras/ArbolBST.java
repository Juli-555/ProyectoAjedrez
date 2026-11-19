package co.edu.ajedrez.estructuras;

import java.util.function.Predicate;

/**
 * Árbol Binario de Búsqueda (BST). Para cada nodo, las claves del subárbol
 * izquierdo son menores y las del derecho mayores o iguales (los repetidos
 * van a la derecha).
 *
 * USO EN EL JUEGO:
 *  - "Partidas guardadas": cada partida terminada se inserta con
 *    CLAVE = número de jugadas. El INORDEN las muestra de la más corta a
 *    la más larga; se puede buscar por número de jugadas y eliminar.
 *  - Se guarda en archivo en PREORDEN: al reinsertar en ese orden se
 *    reconstruye exactamente el mismo árbol.
 *
 * @param <T> tipo del dato asociado a cada clave.
 */
public class ArbolBST<T> {

    /** Par clave-dato que devuelven los recorridos. */
    public static class Entrada<T> {
        public final int clave;
        public final T dato;

        /**
         * @param clave clave del nodo.
         * @param dato  dato del nodo.
         */
        public Entrada(int clave, T dato) {
            this.clave = clave;
            this.dato = dato;
        }

        @Override
        public String toString() {
            return String.valueOf(clave);
        }
    }

    private NodoArbol<T> raiz;
    private int cantidad;

    /** Crea un árbol vacío. */
    public ArbolBST() {
        raiz = null;
        cantidad = 0;
    }

    /**
     * Inserta un dato con su clave (versión iterativa).
     * Complejidad O(h): O(log n) promedio, O(n) peor caso.
     * @param clave valor por el que se ordena.
     * @param dato  información a guardar.
     */
    public void insertar(int clave, T dato) {
        NodoArbol<T> nuevo = new NodoArbol<>(clave, dato);
        cantidad++;
        if (raiz == null) {
            raiz = nuevo;
            return;
        }
        NodoArbol<T> actual = raiz;
        while (true) {
            if (clave < actual.clave) {
                if (actual.izquierdo == null) {
                    actual.izquierdo = nuevo;
                    return;
                }
                actual = actual.izquierdo;
            } else {                          // mayor o igual -> derecha
                if (actual.derecho == null) {
                    actual.derecho = nuevo;
                    return;
                }
                actual = actual.derecho;
            }
        }
    }

    /**
     * Busca el primer dato con la clave dada. O(h).
     * @param clave clave buscada.
     * @return el dato o null.
     */
    public T buscar(int clave) {
        NodoArbol<T> actual = raiz;
        while (actual != null) {
            if (clave == actual.clave) {
                return actual.dato;
            }
            actual = clave < actual.clave ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    /**
     * Busca TODOS los datos con la clave (puede haber repetidos). O(h + k).
     * @param clave clave buscada.
     * @return ListaDoble con los datos encontrados (puede estar vacía).
     */
    public ListaDoble<T> buscarTodos(int clave) {
        ListaDoble<T> resultado = new ListaDoble<>();
        NodoArbol<T> actual = raiz;
        while (actual != null) {
            if (clave == actual.clave) {
                resultado.insertarFinal(actual.dato);
            }
            actual = clave < actual.clave ? actual.izquierdo : actual.derecho;
        }
        return resultado;
    }

    /**
     * Elimina el primer nodo con la clave. O(h).
     * @param clave clave a eliminar.
     * @return true si eliminó.
     */
    public boolean eliminar(int clave) {
        return eliminar(clave, null);
    }

    /**
     * Elimina un nodo con la clave que además cumpla el criterio
     * (sirve para escoger entre repetidos, por ejemplo por id). O(h).
     * @param clave    clave a eliminar.
     * @param criterio condición sobre el dato (null = cualquiera).
     * @return true si eliminó.
     */
    public boolean eliminar(int clave, Predicate<T> criterio) {
        int antes = cantidad;
        raiz = eliminarRec(raiz, clave, criterio);
        return cantidad < antes;
    }

    /**
     * Elimina en el subárbol "nodo" y devuelve su nueva raíz. Casos:
     * 1) hoja -> null; 2) un hijo -> ese hijo; 3) dos hijos -> se copia el
     * SUCESOR inorden (mínimo del subárbol derecho) y se elimina ese sucesor.
     */
    private NodoArbol<T> eliminarRec(NodoArbol<T> nodo, int clave, Predicate<T> criterio) {
        if (nodo == null) {
            return null;
        }
        if (clave < nodo.clave) {
            nodo.izquierdo = eliminarRec(nodo.izquierdo, clave, criterio);
            return nodo;
        }
        if (clave > nodo.clave || (criterio != null && !criterio.test(nodo.dato))) {
            // los repetidos están a la derecha
            nodo.derecho = eliminarRec(nodo.derecho, clave, criterio);
            return nodo;
        }
        cantidad--;                                   // encontrado
        if (nodo.izquierdo == null) {
            return nodo.derecho;                      // casos 1 y 2
        }
        if (nodo.derecho == null) {
            return nodo.izquierdo;                    // caso 2
        }
        NodoArbol<T> sucesor = nodo.derecho;          // caso 3
        while (sucesor.izquierdo != null) {
            sucesor = sucesor.izquierdo;
        }
        nodo.clave = sucesor.clave;
        nodo.dato = sucesor.dato;
        nodo.derecho = eliminarMinimo(nodo.derecho);
        return nodo;
    }

    /** Quita el nodo mínimo (el más a la izquierda) de un subárbol. O(h). */
    private NodoArbol<T> eliminarMinimo(NodoArbol<T> nodo) {
        if (nodo.izquierdo == null) {
            return nodo.derecho;
        }
        nodo.izquierdo = eliminarMinimo(nodo.izquierdo);
        return nodo;
    }

    /**
     * Recorrido INORDEN (izquierdo, raíz, derecho): claves ordenadas. O(n).
     * @return ListaDoble de entradas.
     */
    public ListaDoble<Entrada<T>> inorden() {
        ListaDoble<Entrada<T>> lista = new ListaDoble<>();
        inordenRec(raiz, lista);
        return lista;
    }

    private void inordenRec(NodoArbol<T> nodo, ListaDoble<Entrada<T>> lista) {
        if (nodo == null) {
            return;
        }
        inordenRec(nodo.izquierdo, lista);
        lista.insertarFinal(new Entrada<>(nodo.clave, nodo.dato));
        inordenRec(nodo.derecho, lista);
    }

    /**
     * Recorrido PREORDEN (raíz, izquierdo, derecho). Se usa para guardar. O(n).
     * @return ListaDoble de entradas.
     */
    public ListaDoble<Entrada<T>> preorden() {
        ListaDoble<Entrada<T>> lista = new ListaDoble<>();
        preordenRec(raiz, lista);
        return lista;
    }

    private void preordenRec(NodoArbol<T> nodo, ListaDoble<Entrada<T>> lista) {
        if (nodo == null) {
            return;
        }
        lista.insertarFinal(new Entrada<>(nodo.clave, nodo.dato));
        preordenRec(nodo.izquierdo, lista);
        preordenRec(nodo.derecho, lista);
    }

    /**
     * Recorrido POSORDEN (izquierdo, derecho, raíz). O(n).
     * @return ListaDoble de entradas.
     */
    public ListaDoble<Entrada<T>> posorden() {
        ListaDoble<Entrada<T>> lista = new ListaDoble<>();
        posordenRec(raiz, lista);
        return lista;
    }

    private void posordenRec(NodoArbol<T> nodo, ListaDoble<Entrada<T>> lista) {
        if (nodo == null) {
            return;
        }
        posordenRec(nodo.izquierdo, lista);
        posordenRec(nodo.derecho, lista);
        lista.insertarFinal(new Entrada<>(nodo.clave, nodo.dato));
    }

    /** @return número de niveles (vacío = 0). O(n). */
    public int altura() {
        return alturaRec(raiz);
    }

    private int alturaRec(NodoArbol<T> nodo) {
        return nodo == null ? 0 : 1 + Math.max(alturaRec(nodo.izquierdo), alturaRec(nodo.derecho));
    }

    /** @return la clave mínima o -1 si está vacío. O(h). */
    public int minimo() {
        if (raiz == null) {
            return -1;
        }
        NodoArbol<T> n = raiz;
        while (n.izquierdo != null) {
            n = n.izquierdo;
        }
        return n.clave;
    }

    /** @return la clave máxima o -1 si está vacío. O(h). */
    public int maximo() {
        if (raiz == null) {
            return -1;
        }
        NodoArbol<T> n = raiz;
        while (n.derecho != null) {
            n = n.derecho;
        }
        return n.clave;
    }

    /** @return la raíz (para dibujar el árbol en pantalla). */
    public NodoArbol<T> getRaiz() {
        return raiz;
    }

    /** @return true si está vacío. O(1). */
    public boolean estaVacio() {
        return raiz == null;
    }

    /** @return número de nodos. O(1). */
    public int tamano() {
        return cantidad;
    }

    /** Igual que vaciar(). O(1). */
    public void limpiar() {
        vaciar();
    }

    /** Elimina todos los nodos. O(1). */
    public void vaciar() {
        raiz = null;
        cantidad = 0;
    }
}

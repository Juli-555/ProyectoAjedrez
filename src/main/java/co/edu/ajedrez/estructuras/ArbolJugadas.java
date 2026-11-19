package co.edu.ajedrez.estructuras;

import java.util.function.Consumer;

/**
 * Árbol general (n-ario) de jugadas posibles para la IA.
 * Cada nodo guarda solo dos referencias (sin arreglos):
 *   - primerHijo: su primer hijo
 *   - siguienteHermano: el siguiente hijo de su mismo padre
 * (representación "hijo izquierdo - hermano derecho").
 *
 * USO EN EL JUEGO: la IA construye el árbol con profundidad 1, 2 o 3.
 * Las hojas se evalúan y los valores suben con MINIMAX.
 *
 * @param <M> tipo de la jugada que guarda cada nodo.
 */
public class ArbolJugadas<M> {

    /** Nodo del árbol de jugadas. */
    public static class NodoJugada<M> {
        private final M movimiento;          // jugada que lleva a este nodo (null en la raíz)
        public int valor;                    // puntaje minimax
        private NodoJugada<M> primerHijo;
        private NodoJugada<M> ultimoHijo;    // permite agregar hijos en O(1)
        private NodoJugada<M> siguienteHermano;

        /** @param movimiento jugada del nodo. */
        public NodoJugada(M movimiento) {
            this.movimiento = movimiento;
        }

        /** @return la jugada del nodo. */
        public M getMovimiento() {
            return movimiento;
        }

        /** @return el primer hijo o null. */
        public NodoJugada<M> getPrimerHijo() {
            return primerHijo;
        }

        /** @return el siguiente hermano o null. */
        public NodoJugada<M> getSiguienteHermano() {
            return siguienteHermano;
        }
    }

    private final NodoJugada<M> raiz;
    private int cantidad;

    /** Crea un árbol con una raíz (la posición actual). */
    public ArbolJugadas() {
        raiz = new NodoJugada<>(null);
        cantidad = 1;
    }

    /**
     * Agrega un hijo al final de los hijos de "padre". O(1).
     * @param padre      nodo padre.
     * @param movimiento jugada del hijo.
     * @return el nodo hijo creado.
     */
    public NodoJugada<M> agregarHijo(NodoJugada<M> padre, M movimiento) {
        NodoJugada<M> hijo = new NodoJugada<>(movimiento);
        if (padre.primerHijo == null) {
            padre.primerHijo = hijo;
        } else {
            padre.ultimoHijo.siguienteHermano = hijo;
        }
        padre.ultimoHijo = hijo;
        cantidad++;
        return hijo;
    }

    /**
     * Recorre los hijos directos de un nodo. O(k).
     * @param padre  nodo.
     * @param accion operación aplicada a cada hijo.
     */
    public void recorrerHijos(NodoJugada<M> padre, Consumer<NodoJugada<M>> accion) {
        NodoJugada<M> hijo = padre.primerHijo;
        while (hijo != null) {
            accion.accept(hijo);
            hijo = hijo.siguienteHermano;
        }
    }

    /**
     * Recorrido en preorden de todo el árbol. O(n).
     * @param accion operación aplicada a cada nodo.
     */
    public void preorden(Consumer<NodoJugada<M>> accion) {
        preordenRec(raiz, accion);
    }

    private void preordenRec(NodoJugada<M> nodo, Consumer<NodoJugada<M>> accion) {
        accion.accept(nodo);
        NodoJugada<M> hijo = nodo.primerHijo;
        while (hijo != null) {
            preordenRec(hijo, accion);
            hijo = hijo.siguienteHermano;
        }
    }

    /** @return la raíz. */
    public NodoJugada<M> getRaiz() {
        return raiz;
    }

    /** @return número total de nodos. O(1). */
    public int tamano() {
        return cantidad;
    }
}

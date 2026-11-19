package co.edu.ajedrez.logica;

import co.edu.ajedrez.estructuras.ArbolJugadas;
import co.edu.ajedrez.estructuras.ArbolJugadas.NodoJugada;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Movimiento;
import java.util.Random;

/**
 * Inteligencia artificial sencilla con MINIMAX.
 *
 * Para elegir su jugada construye un ArbolJugadas:
 *   nivel 0: posición actual (raíz)
 *   nivel 1: cada jugada posible de la IA
 *   nivel 2: cada respuesta del rival
 *   nivel 3: cada respuesta de la IA (solo en "Difícil")
 * Las hojas se evalúan y los valores suben: en niveles de la IA se toma
 * el MÁXIMO y en los del rival el MÍNIMO.
 */
public class IA {

    private static final int MATE = 100000;
    private final int profundidad;
    private final Random azar = new Random();
    private int ultimosNodos = 0;

    /** @param profundidad 1 (fácil), 2 (normal) o 3 (difícil). */
    public IA(int profundidad) {
        this.profundidad = profundidad;
    }

    /**
     * Elige la mejor jugada. El tablero se modifica temporalmente y queda igual.
     * Complejidad O(b^d), b ≈ 30 jugadas por posición, d = profundidad.
     * @param tablero motor con la posición actual.
     * @param colorIA color con el que juega la IA.
     * @return la jugada elegida (creada por el tablero) o null si no hay.
     */
    public Movimiento elegirJugada(MotorAjedrez tablero, ColorPieza colorIA) {
        ArbolJugadas<Movimiento> arbol = new ArbolJugadas<>();
        NodoJugada<Movimiento> raiz = arbol.getRaiz();
        expandir(arbol, raiz, tablero, colorIA, colorIA, profundidad, 0);
        ultimosNodos = arbol.tamano();
        if (raiz.getPrimerHijo() == null) {
            return null;
        }
        // Entre los hijos con el mejor valor (margen de 8) se elige uno al azar
        ListaDoble<NodoJugada<Movimiento>> candidatos = new ListaDoble<>();
        arbol.recorrerHijos(raiz, hijo -> {
            if (hijo.valor >= raiz.valor - 8) {
                candidatos.insertarFinal(hijo);
            }
        });
        return candidatos.obtener(azar.nextInt(candidatos.tamano())).getMovimiento();
    }

    /**
     * Crea los hijos de "nodo" y calcula su valor minimax (recursivo).
     * @return el valor del nodo.
     */
    private int expandir(ArbolJugadas<Movimiento> arbol, NodoJugada<Movimiento> nodo, MotorAjedrez tablero,
                         ColorPieza turno, ColorPieza colorIA, int prof, int ply) {
        if (prof == 0) {
            nodo.valor = evaluar(tablero, colorIA);
            return nodo.valor;
        }
        ListaDoble<Movimiento> jugadas = tablero.generarLegales(turno);
        if (jugadas.estaVacia()) {
            if (tablero.enJaque(turno)) {
                nodo.valor = turno == colorIA ? -MATE + ply : MATE - ply;   // jaque mate
            } else {
                nodo.valor = 0;                                            // ahogado
            }
            return nodo.valor;
        }
        boolean maximiza = turno == colorIA;
        int[] mejor = {maximiza ? Integer.MIN_VALUE : Integer.MAX_VALUE};
        jugadas.recorrerAdelante(mov -> {
            NodoJugada<Movimiento> hijo = arbol.agregarHijo(nodo, mov);
            tablero.aplicar(mov);
            int v = expandir(arbol, hijo, tablero, turno.contrario(), colorIA, prof - 1, ply + 1);
            tablero.deshacer(mov);
            if (maximiza ? v > mejor[0] : v < mejor[0]) {
                mejor[0] = v;
            }
        });
        nodo.valor = mejor[0];
        return nodo.valor;
    }

    /**
     * Función de evaluación: material + bonificaciones de posición.
     * Positivo = bueno para la IA. O(32).
     * @param tablero tablero.
     * @param colorIA color de la IA.
     * @return puntaje.
     */
    public int evaluar(MotorAjedrez tablero, ColorPieza colorIA) {
        return sumar(tablero, colorIA) - sumar(tablero, colorIA.contrario());
    }

    private int sumar(MotorAjedrez tablero, ColorPieza color) {
        int[] total = {0};
        int filaInicial = color == ColorPieza.BLANCO ? 7 : 0;
        int piezasRival = tablero.piezasVivas(color.contrario()).tamano();
        tablero.piezasVivas(color).recorrerAdelante(p -> {
            double v = p.valor();
            double centro = 3.5 - Math.max(Math.abs(p.getFila() - 3.5), Math.abs(p.getCol() - 3.5)); // 0..3
            switch (p.getTipo()) {
                case 'P': {
                    int avance = color == ColorPieza.BLANCO ? 6 - p.getFila() : p.getFila() - 1;   // 0..5
                    boolean central = p.getCol() >= 2 && p.getCol() <= 5;
                    v += central ? avance * 6 + centro * 6 : avance * 2;
                    if (avance >= 4) v += avance * 8;                                           // cerca de coronar
                    break;
                }
                case 'C':
                case 'A':
                    v += centro * 10;
                    if (p.getFila() == filaInicial) v -= 12;                                    // sin desarrollar
                    break;
                case 'D':
                    v += centro * 3;
                    break;
                case 'R':
                    if (p.getFila() == filaInicial && (p.getCol() == 6 || p.getCol() == 2)) v += 30;   // enrocado
                    else if (p.getFila() != filaInicial && piezasRival > 6) v -= 25;                    // expuesto
                    break;
                default:
                    break;
            }
            total[0] += (int) Math.round(v);
        });
        return total[0];
    }

    /** @return nodos del árbol creado en la última decisión. */
    public int getUltimosNodos() {
        return ultimosNodos;
    }

    /** @return la profundidad de búsqueda. */
    public int getProfundidad() {
        return profundidad;
    }
}

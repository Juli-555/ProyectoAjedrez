package co.edu.ajedrez;

import co.edu.ajedrez.estructuras.ArbolBST;
import co.edu.ajedrez.estructuras.Cola;
import co.edu.ajedrez.estructuras.ListaCircular;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.estructuras.Pila;
import co.edu.ajedrez.logica.IA;
import co.edu.ajedrez.logica.MotorAjedrez;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.Movimiento;
import co.edu.ajedrez.modelo.PartidaGuardada;
import co.edu.ajedrez.servicios.GestorPartida;
import co.edu.ajedrez.servicios.RegistroPartidas;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Pruebas automáticas del proyecto (sin librerías externas).
 *
 * Cómo ejecutarlas en NetBeans: clic derecho sobre este archivo -> "Run File",
 * o "Clean and Build" (Maven las ejecuta en la fase test).
 *
 * Verifica:
 *  1. Cada operación de las 5 estructuras.
 *  2. El motor con "perft": contar todas las jugadas legales hasta cierta
 *     profundidad y comparar con los valores oficiales conocidos.
 *  3. Situaciones del juego: mate, ahogado, enroque, al paso, coronación,
 *     deshacer/rehacer, repetición, IA y guardado en archivo.
 */
public final class PruebasProyecto {

    private static int correctas = 0, total = 0;
    private static String grupoActual = "";
    private static final StringBuilder csv = new StringBuilder();

    /**
     * Registra y muestra una prueba.
     * @param grupo       estructura o módulo.
     * @param descripcion qué se prueba.
     * @param esperado    valor esperado.
     * @param obtenido    valor obtenido.
     */
    private static void prueba(String grupo, String descripcion, Object esperado, Object obtenido) {
        boolean ok = Objects.equals(String.valueOf(esperado), String.valueOf(obtenido));
        total++;
        if (ok) {
            correctas++;
        }
        if (!grupo.equals(grupoActual)) {
            grupoActual = grupo;
            System.out.println("\n== " + grupo + " ==");
        }
        System.out.println((ok ? "  OK     " : "  FALLA  ") + descripcion
                + (ok ? "" : "  (esperado " + esperado + ", obtenido " + obtenido + ")"));
        csv.append(grupo).append('\t').append(descripcion).append('\t').append(esperado).append('\t')
                .append(obtenido).append('\t').append(ok ? "OK" : "FALLA").append('\n');
    }

    /** Cuenta las posiciones alcanzables a "prof" jugadas (perft). */
    private static long perft(MotorAjedrez t, ColorPieza color, int prof) {
        if (prof == 0) {
            return 1;
        }
        long[] suma = {0};
        t.generarLegales(color).recorrerAdelante(m -> {
            t.aplicar(m);
            suma[0] += perft(t, color.contrario(), prof - 1);
            t.deshacer(m);
        });
        return suma[0];
    }

    private static long perftFEN(String fen, int prof) {
        MotorAjedrez t = new MotorAjedrez();
        ColorPieza c = t.cargarFEN(fen);
        return perft(t, c, prof);
    }

    private static GestorPartida partida(String... jugadas) {
        GestorPartida p = new GestorPartida("Ana", "Luis");
        for (String j : jugadas) {
            p.jugarCodigo(j);
        }
        return p;
    }

    private static GestorPartida desdeFEN(String fen) {
        return new GestorPartida(GestorPartida.Modo.DOS_JUGADORES, "A", "B", 2, fen);
    }

    /**
     * Ejecuta todas las pruebas.
     * @param args opcional: ruta de un archivo .tsv donde guardar los resultados.
     * @throws IOException si no se puede escribir el archivo de resultados.
     */
    public static void main(String[] args) throws IOException {
        long inicio = System.currentTimeMillis();

        // ---------- PILA ----------
        Pila<String> pila = new Pila<>();
        prueba("Pila", "Pila nueva está vacía", true, pila.estaVacia());
        pila.apilar("e4");
        pila.apilar("e5");
        pila.apilar("Cf3");
        prueba("Pila", "Tamaño después de apilar 3", 3, pila.tamano());
        prueba("Pila", "verTope devuelve el último apilado", "Cf3", pila.verTope());
        prueba("Pila", "desapilar sale en orden LIFO", "Cf3 e5 e4", pila.desapilar() + " " + pila.desapilar() + " " + pila.desapilar());
        prueba("Pila", "desapilar en pila vacía devuelve null", null, pila.desapilar());

        // ---------- COLA ----------
        Cola<String> cola = new Cola<>();
        prueba("Cola", "Cola nueva está vacía", true, cola.estaVacia());
        cola.encolar("e2e4");
        cola.encolar("e7e5");
        cola.encolar("g1f3");
        prueba("Cola", "Tamaño después de encolar 3", 3, cola.tamano());
        prueba("Cola", "verFrente devuelve el primero", "e2e4", cola.verFrente());
        prueba("Cola", "desencolar sale en orden FIFO", "e2e4 e7e5 g1f3", cola.desencolar() + " " + cola.desencolar() + " " + cola.desencolar());
        prueba("Cola", "desencolar en cola vacía devuelve null", null, cola.desencolar());
        cola.encolar("x");
        prueba("Cola", "Se puede reutilizar tras vaciarse", "x", cola.verFrente());

        // ---------- LISTA CIRCULAR ----------
        ListaCircular<String> lc = new ListaCircular<>();
        lc.insertarFinal("Blancas");
        lc.insertarFinal("Negras");
        prueba("Lista circular", "Turno inicial", "Blancas", lc.obtenerTurno());
        prueba("Lista circular", "Avanzar 3 veces da la vuelta", "Negras Blancas Negras",
                lc.avanzarTurno() + " " + lc.avanzarTurno() + " " + lc.avanzarTurno());
        prueba("Lista circular", "Retroceder turno", "Blancas", lc.retrocederTurno());
        lc.insertarInicio("Árbitro");
        lc.insertarEnPosicion(2, "Jugador3");
        StringBuilder orden = new StringBuilder();
        lc.recorrer(d -> orden.append(d).append(' '));
        prueba("Lista circular", "Insertar inicio y en posición", "Árbitro Blancas Jugador3 Negras", orden.toString().trim());
        prueba("Lista circular", "El último apunta al primero", "Árbitro", lc.siguienteDelUltimo());
        prueba("Lista circular", "Buscar posición", 2, lc.buscarPosicion("Jugador3"::equals));
        prueba("Lista circular", "Buscar inexistente", null, lc.buscar("Nadie"::equals));
        prueba("Lista circular", "Eliminar devuelve el dato", "Jugador3", lc.eliminar("Jugador3"::equals));
        prueba("Lista circular", "Eliminar en posición 0", "Árbitro", lc.eliminarEnPosicion(0));
        prueba("Lista circular", "Tamaño final", 2, lc.tamano());
        prueba("Lista circular", "Posición inválida no se inserta", false, lc.insertarEnPosicion(9, "x"));
        prueba("Lista circular", "obtener(1) recorre los nodos", "Negras", lc.obtener(1));

        // ---------- LISTA DOBLE ----------
        ListaDoble<Integer> ld = new ListaDoble<>();
        ld.insertarFinal(2);
        ld.insertarFinal(3);
        ld.insertarInicio(1);
        ld.insertarEnPosicion(3, 4);
        ld.insertarEnPosicion(1, 10);
        prueba("Lista doble", "Insertar inicio/final/posición", "[1, 10, 2, 3, 4]", ld.toString());
        StringBuilder atras = new StringBuilder();
        ld.recorrerAtras(d -> atras.append(d).append(' '));
        prueba("Lista doble", "Recorrer hacia atrás", "4 3 2 10 1", atras.toString().trim());
        prueba("Lista doble", "Buscar con condición", 3, ld.buscar(x -> x > 2 && x < 10));
        prueba("Lista doble", "Eliminar por valor", 10, ld.eliminar(x -> x == 10));
        prueba("Lista doble", "Eliminar en posición final", 4, ld.eliminarEnPosicion(3));
        prueba("Lista doble", "Eliminar inexistente devuelve null", null, ld.eliminar(x -> x == 99));
        prueba("Lista doble", "Enlaces anteriores correctos", 1, ld.getCola().getAnterior().getAnterior().getDato());
        prueba("Lista doble", "Obtener por posición", 2, ld.obtener(1));
        prueba("Lista doble", "eliminarUltimo quita la cola", 3, ld.eliminarUltimo());

        // ---------- ÁRBOL BST ----------
        ArbolBST<String> a = new ArbolBST<>();
        for (int k : new int[]{50, 30, 70, 20, 40, 60, 80, 40}) {
            a.insertar(k, "p" + k);
        }
        prueba("Árbol BST", "Inorden queda ordenado", "[20, 30, 40, 40, 50, 60, 70, 80]", a.inorden().toString());
        prueba("Árbol BST", "Preorden", "[50, 30, 20, 40, 40, 70, 60, 80]", a.preorden().toString());
        prueba("Árbol BST", "Posorden", "[20, 40, 40, 30, 60, 80, 70, 50]", a.posorden().toString());
        prueba("Árbol BST", "Buscar existente", "p60", a.buscar(60));
        prueba("Árbol BST", "Buscar inexistente", null, a.buscar(65));
        prueba("Árbol BST", "Buscar todos los repetidos", 2, a.buscarTodos(40).tamano());
        prueba("Árbol BST", "Eliminar hoja (20)", true, a.eliminar(20));
        prueba("Árbol BST", "Eliminar nodo con 2 hijos (raíz 50)", true, a.eliminar(50));
        prueba("Árbol BST", "Raíz pasa a ser el sucesor (60)", 60, a.getRaiz().getClave());
        prueba("Árbol BST", "Eliminar inexistente", false, a.eliminar(999));
        prueba("Árbol BST", "Inorden tras eliminar", "[30, 40, 40, 60, 70, 80]", a.inorden().toString());
        prueba("Árbol BST", "Altura (60-30-40-40)", 4, a.altura());

        // ---------- MOTOR: PERFT ----------
        String inicial = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String kiwi = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        prueba("Motor (perft)", "Posición inicial, profundidad 1", 20, perftFEN(inicial, 1));
        prueba("Motor (perft)", "Posición inicial, profundidad 2", 400, perftFEN(inicial, 2));
        prueba("Motor (perft)", "Posición inicial, profundidad 3", 8902, perftFEN(inicial, 3));
        prueba("Motor (perft)", "Posición inicial, profundidad 4", 197281, perftFEN(inicial, 4));
        prueba("Motor (perft)", "\"Kiwipete\" (enroques, al paso, coronación) prof. 2", 2039, perftFEN(kiwi, 2));
        prueba("Motor (perft)", "\"Kiwipete\" prof. 3", 97862, perftFEN(kiwi, 3));
        prueba("Motor (perft)", "Posición 3 (al paso con clavada) prof. 4", 43238, perftFEN("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 4));
        prueba("Motor (perft)", "Posición 5 (coronaciones) prof. 3", 62379, perftFEN("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3));
        MotorAjedrez copia = new MotorAjedrez();
        copia.cargarFEN(kiwi);
        prueba("Motor (perft)", "Copiar el tablero (FEN) para la IA conserva la posición", kiwi, copia.aFEN(ColorPieza.BLANCO));

        // ---------- SITUACIONES DE JUEGO ----------
        GestorPartida p = partida("f2f3", "e7e5", "g2g4", "d8h4");
        prueba("Juego", "Mate del loco", "Jaque mate", p.getMotivo());
        prueba("Juego", "Gana Negras", ColorPieza.NEGRO, p.getGanador());
        prueba("Juego", "Notación de la última jugada", "Dh4#", p.getJugadas().ultimo().getSan());
        prueba("Juego", "No se puede jugar después del mate", null, p.jugarCodigo("a2a3"));

        p = desdeFEN("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1");
        p.evaluarFin();
        prueba("Juego", "Rey ahogado = tablas", "Rey ahogado", p.getMotivo());

        p = partida();
        prueba("Juego", "Jugada ilegal es rechazada", null, p.jugarCodigo("e2e5"));
        prueba("Juego", "Texto inválido es rechazado", null, p.jugarCodigo("hola"));
        prueba("Juego", "Turno no cambia tras jugada ilegal", ColorPieza.BLANCO, p.colorActual());
        for (String j : new String[]{"e2e4", "d7d5", "e4d5", "c7c5", "d5c6"}) {
            p.jugarCodigo(j);
        }
        prueba("Juego", "Captura al paso quita el peón", null, p.getTablero().obtener(3, 2));
        prueba("Juego", "Notación captura al paso", "dxc6", p.getJugadas().ultimo().getSan());
        prueba("Juego", "Piezas vivas negras tras 2 capturas", 14, p.getTablero().piezasVivas(ColorPieza.NEGRO).tamano());
        p.deshacer();
        p.deshacer();
        prueba("Juego", "Deshacer 2 veces devuelve el turno a Negras", ColorPieza.NEGRO, p.colorActual());
        prueba("Juego", "Deshacer restaura piezas vivas", 15, p.getTablero().piezasVivas(ColorPieza.NEGRO).tamano());
        prueba("Juego", "Pila de rehacer tiene 2", 2, p.getPilaRehacer().tamano());
        p.rehacer();
        prueba("Juego", "Rehacer vuelve a jugar c5", "c5", p.getJugadas().ultimo().getSan());
        p.jugarCodigo("g1f3");
        prueba("Juego", "Jugada nueva vacía la pila de rehacer", 0, p.getPilaRehacer().tamano());

        p = partida("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6", "e1g1");
        prueba("Juego", "Enroque corto mueve la torre a f1", 'T', p.getTablero().obtener(7, 5).getTipo());
        prueba("Juego", "Notación del enroque", "O-O", p.getJugadas().ultimo().getSan());
        p.deshacer();
        prueba("Juego", "Deshacer enroque devuelve la torre a h1", 'T', p.getTablero().obtener(7, 7).getTipo());

        p = desdeFEN("8/P6k/8/8/8/8/8/K7 w - - 0 1");
        p.jugarCodigo("a7a8T");
        prueba("Juego", "Coronación a torre", 'T', p.getTablero().obtener(0, 0).getTipo());
        prueba("Juego", "Notación de coronación", "a8=T", p.getJugadas().ultimo().getSan());
        p.deshacer();
        prueba("Juego", "Deshacer coronación vuelve a peón", 'P', p.getTablero().obtener(1, 0).getTipo());

        p = partida("g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8");
        prueba("Juego", "Triple repetición: la partida sigue hasta que se reclame", false, p.estaTerminada());
        prueba("Juego", "Triple repetición: se pueden reclamar tablas", true, p.puedeReclamarTablas());
        p.reclamarTablas();
        prueba("Juego", "Reclamar tablas termina la partida", "Triple repetición", p.getMotivo());
        p = partida("g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8",
                "g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8");
        prueba("Juego", "Repetición quíntuple = tablas automáticas", "Repetición quíntuple", p.getMotivo());
        p = partida();
        prueba("Juego", "Reclamar tablas sin motivo no termina la partida", "Todavía no se pueden reclamar tablas.", p.reclamarTablas());

        p = desdeFEN("8/8/8/8/8/2k5/8/K1b5 w - - 0 1");
        p.evaluarFin();
        prueba("Juego", "Rey vs rey y alfil = material insuficiente", "Material insuficiente", p.getMotivo());

        p = partida("e2e4", "e7e5", "g1f3");
        Cola<String> rep = p.construirColaRepeticion();
        prueba("Juego", "Cola de repetición en orden cronológico", "e2e4 e7e5 g1f3",
                rep.desencolar() + " " + rep.desencolar() + " " + rep.desencolar());

        p = partida();
        p.rendirse(ColorPieza.BLANCO);
        prueba("Juego", "Rendición: gana el rival", ColorPieza.NEGRO, p.getGanador());
        p = partida("e2e4", "e7e5");
        prueba("Juego", "GestorTurnos recorre la lista circular", "Blancas → Negras → Blancas", p.cicloTurnos());
        prueba("Juego", "Historial inverso recorre la lista doble desde la cola", "e5, e4", p.historialInverso());

        // ---------- IA ----------
        MotorAjedrez t = new MotorAjedrez();
        t.cargarFEN("6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1");
        IA ia = new IA(2);
        Movimiento m = ia.elegirJugada(t, ColorPieza.BLANCO);
        prueba("IA", "Encuentra el mate en 1 (Td8#)", "d1d8", m.codigo());
        prueba("IA", "El tablero queda igual después de pensar", 'T', t.obtener(7, 3).getTipo());
        t.cargarFEN("4k3/8/8/3q4/8/8/8/3QK3 w - - 0 1");
        prueba("IA", "Captura la dama que está colgada", "d1d5", new IA(1).elegirJugada(t, ColorPieza.BLANCO).codigo());
        t.posicionInicial();
        ia.elegirJugada(t, ColorPieza.NEGRO);
        prueba("IA", "Árbol de jugadas prof. 2 desde el inicio tiene 1+20+400 nodos", 421, ia.getUltimosNodos());

        // ---------- ALMACÉN (BST de partidas en archivo) ----------
        File archivo = File.createTempFile("ajedrez_prueba", ".txt");
        archivo.delete();
        RegistroPartidas alm = new RegistroPartidas(archivo);
        String[][] juegos = {{"e2e4", "e7e5", "g1f3", "b8c6"}, {"d2d4"}, {"c2c4", "e7e5", "b1c3"}};
        PartidaGuardada tercera = null;
        for (String[] js : juegos) {
            GestorPartida q = partida(js);
            q.rendirse(ColorPieza.BLANCO);
            tercera = alm.guardarPartida(q);
        }
        RegistroPartidas alm2 = new RegistroPartidas(archivo);
        prueba("Archivo BST", "Se guardan y se recargan 3 partidas", 3, alm2.getArbol().tamano());
        prueba("Archivo BST", "Inorden por número de jugadas", "[1, 3, 4]", alm2.recorrido("inorden").toString());
        prueba("Archivo BST", "Buscar partidas de 3 jugadas", 1, alm2.buscar(3).tamano());
        prueba("Archivo BST", "Eliminar una partida", true, alm2.eliminar(3, tercera.getId()));
        prueba("Archivo BST", "Quedan 2 partidas en el archivo", 2, new RegistroPartidas(archivo).getArbol().tamano());
        prueba("Archivo BST", "Una partida sin terminar NO se guarda", null, alm2.guardarPartida(partida()));
        archivo.delete();

        double segundos = (System.currentTimeMillis() - inicio) / 1000.0;
        System.out.printf("%n%d de %d pruebas correctas (%.1f s).%n", correctas, total, segundos);
        if (args.length > 0) {
            try (PrintWriter w = new PrintWriter(args[0], StandardCharsets.UTF_8)) {
                w.print(csv);
            }
        }
        if (correctas != total) {
            throw new IllegalStateException((total - correctas) + " prueba(s) fallaron.");
        }
    }
}

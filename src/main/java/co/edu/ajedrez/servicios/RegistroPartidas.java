package co.edu.ajedrez.servicios;

import co.edu.ajedrez.estructuras.ArbolBST;
import co.edu.ajedrez.estructuras.ListaDoble;
import co.edu.ajedrez.modelo.ColorPieza;
import co.edu.ajedrez.modelo.PartidaGuardada;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RegistroPartidas: archivo de partidas guardadas en un Árbol BST con CLAVE = número de jugadas.
 *  - INORDEN  -> de la partida más corta a la más larga.
 *  - buscar(n) -> partidas que duraron exactamente n jugadas.
 *  - eliminar  -> borra una partida (por clave e id).
 * El árbol se guarda en un archivo de texto en PREORDEN; al volver a abrir
 * el juego se reinserta en ese orden y se obtiene el mismo árbol.
 */
public class RegistroPartidas {

    private final ArbolBST<PartidaGuardada> arbol = new ArbolBST<>();
    private final File archivo;
    private int siguienteId = 1;

    /** Crea el almacén usando el archivo "ajedrez_estructurado_partidas.txt" en la carpeta del usuario. */
    public RegistroPartidas() {
        this(new File(System.getProperty("user.home"), "ajedrez_estructurado_partidas.txt"));
    }

    /**
     * Crea el almacén con un archivo específico (se usa en las pruebas).
     * @param archivo archivo donde se guardan las partidas.
     */
    public RegistroPartidas(File archivo) {
        this.archivo = archivo;
        cargar();
    }

    /**
     * Lee el archivo y reconstruye el árbol. Las líneas dañadas se ignoran;
     * si el archivo no existe o no se puede leer, se empieza vacío.
     */
    public final void cargar() {
        arbol.vaciar();
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader lector = Files.newBufferedReader(archivo.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {     // las líneas vienen en preorden
                PartidaGuardada r = PartidaGuardada.desdeLinea(linea);
                if (r != null) {
                    arbol.insertar(r.getCantidad(), r);
                    siguienteId = Math.max(siguienteId, r.getId() + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudieron leer las partidas: " + e.getMessage());
            arbol.vaciar();
        }
    }

    /**
     * Guarda el árbol en el archivo, recorriéndolo en preorden.
     * @return true si se pudo guardar.
     */
    public boolean persistir() {
        try (BufferedWriter escritor = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            ListaDoble<ArbolBST.Entrada<PartidaGuardada>> lista = arbol.preorden();
            IOException[] error = {null};
            lista.recorrerAdelante(e -> {
                try {
                    escritor.write(e.dato.aLinea());
                    escritor.newLine();
                } catch (IOException ex) {
                    error[0] = ex;
                }
            });
            if (error[0] != null) {
                throw error[0];
            }
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo guardar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda una partida TERMINADA en el árbol y en el archivo.
     * @param partida partida.
     * @return el registro guardado, o null si la partida no ha terminado.
     */
    public PartidaGuardada guardarPartida(GestorPartida partida) {
        if (!partida.estaTerminada()) {
            return null;
        }
        ColorPieza g = partida.getGanador();
        PartidaGuardada r = new PartidaGuardada(siguienteId++,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                partida.jugador(ColorPieza.BLANCO).getNombre(), partida.jugador(ColorPieza.NEGRO).getNombre(),
                g == null ? "TABLAS" : g.name(), partida.getMotivo(),
                partida.getJugadas().tamano(), partida.codigosJugadas());
        arbol.insertar(r.getCantidad(), r);
        persistir();
        return r;
    }

    /**
     * Elimina una partida.
     * @param clave número de jugadas de la partida.
     * @param id    identificador de la partida.
     * @return true si se eliminó.
     */
    public boolean eliminar(int clave, int id) {
        boolean ok = arbol.eliminar(clave, r -> r.getId() == id);
        if (ok) {
            persistir();
        }
        return ok;
    }

    /**
     * @param cantidad número de jugadas.
     * @return ListaDoble con las partidas de exactamente esa cantidad.
     */
    public ListaDoble<PartidaGuardada> buscar(int cantidad) {
        return arbol.buscarTodos(cantidad);
    }

    /**
     * @param tipo "inorden", "preorden" o "posorden".
     * @return ListaDoble con las partidas en ese recorrido.
     */
    public ListaDoble<ArbolBST.Entrada<PartidaGuardada>> recorrido(String tipo) {
        switch (tipo) {
            case "preorden": return arbol.preorden();
            case "posorden": return arbol.posorden();
            default: return arbol.inorden();
        }
    }

    /** @return el árbol (para dibujarlo y mostrar estadísticas). */
    public ArbolBST<PartidaGuardada> getArbol() {
        return arbol;
    }

    /** @return ruta del archivo donde se guardan las partidas. */
    public File getArchivo() {
        return archivo;
    }
}

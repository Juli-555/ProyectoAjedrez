package co.edu.ajedrez.modelo;

/**
 * Datos de una partida terminada que se guarda en el árbol BST (RegistroPartidas).
 */
public class PartidaGuardada {

    private final int id;
    private final String fecha;       // "17/11/2026 19:06"
    private final String blancas;
    private final String negras;
    private final String ganador;     // "BLANCO", "NEGRO" o "TABLAS"
    private final String motivo;
    private final int cantidad;       // número de jugadas = clave en el árbol
    private final String jugadas;     // "e2e4 e7e5 ..."

    /**
     * Crea un registro.
     * @param id       identificador único.
     * @param fecha    fecha y hora en texto.
     * @param blancas  nombre de blancas.
     * @param negras   nombre de negras.
     * @param ganador  "BLANCO", "NEGRO" o "TABLAS".
     * @param motivo   motivo del final.
     * @param cantidad número de jugadas.
     * @param jugadas  jugadas en código separadas por espacio.
     */
    public PartidaGuardada(int id, String fecha, String blancas, String negras, String ganador,
                           String motivo, int cantidad, String jugadas) {
        this.id = id;
        this.fecha = fecha;
        this.blancas = blancas;
        this.negras = negras;
        this.ganador = ganador;
        this.motivo = motivo;
        this.cantidad = cantidad;
        this.jugadas = jugadas;
    }

    /**
     * Convierte el registro a una línea de texto separada por "|".
     * @return la línea para el archivo.
     */
    public String aLinea() {
        return String.join("|", String.valueOf(cantidad), String.valueOf(id), fecha, blancas, negras,
                ganador, motivo, jugadas);
    }

    /**
     * Lee un registro desde una línea del archivo.
     * @param linea texto con 8 campos separados por "|".
     * @return el registro, o null si la línea está dañada.
     */
    public static PartidaGuardada desdeLinea(String linea) {
        try {
            String[] c = linea.split("\\|", -1);
            if (c.length != 8) {
                return null;
            }
            return new PartidaGuardada(Integer.parseInt(c[1]), c[2], c[3], c[4], c[5], c[6],
                    Integer.parseInt(c[0]), c[7]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** @return texto del resultado ("Ganan Blancas por jaque mate", "Tablas — ..."). */
    public String textoResultado() {
        if ("TABLAS".equals(ganador)) {
            return "Tablas — " + motivo;
        }
        return "Ganan " + ("BLANCO".equals(ganador) ? "Blancas" : "Negras") + " por " + motivo.toLowerCase();
    }

    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getBlancas() { return blancas; }
    public String getNegras() { return negras; }
    public String getGanador() { return ganador; }
    public String getMotivo() { return motivo; }
    public int getCantidad() { return cantidad; }
    public String getJugadas() { return jugadas; }
}

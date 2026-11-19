package co.edu.ajedrez;

import co.edu.ajedrez.interfaz.VentanaPrincipal;
import java.awt.Window;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del juego; inicia toda la interfaz en el hilo EDT de Swing.
 * Captura cualquier error inesperado para que el juego NO se cierre:
 * solo muestra un mensaje y la ventana sigue funcionando.
 */
public final class Main {

    private Main() {
    }

    /**
     * Ejecuta la aplicación.
     * @param args parámetros opcionales (sin uso).
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                for (Window w : Window.getWindows()) {
                    if (w instanceof VentanaPrincipal && w.isVisible()) {
                        ((VentanaPrincipal) w).aviso("Ocurrió un error inesperado, pero el juego sigue funcionando.", true);
                        return;
                    }
                }
                JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado: " + error.getMessage());
            });
        });
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Apariencia por defecto si no hay soporte.
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}

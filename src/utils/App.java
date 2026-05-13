package utils;

import view.MainFrame;
import controller.GameController;
import javax.swing.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            GameController controller = new GameController(frame, frame.getGamePanel());
            frame.setGameController(controller);
        });
    }

    /**
     * Shows a Swing dialog for choosing a card colour, replacing the old
     * console-based Scanner input used by wild / +4 cards.
     */
    public static int scegliColore() {
        String[] colori = {"Rosso", "Verde", "Blu", "Giallo"};
        String scelta = (String) JOptionPane.showInputDialog(
                null,
                "Scegli il colore:",
                "Colore Jolly",
                JOptionPane.QUESTION_MESSAGE,
                null,
                colori,
                colori[0]
        );
        if (scelta != null) {
            switch (scelta) {
                case "Rosso":  return 0;
                case "Verde":  return 1;
                case "Blu":    return 2;
                case "Giallo": return 3;
            }
        }
        return 0; // default
    }
}
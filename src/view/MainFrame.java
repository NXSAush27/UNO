package view;

import controller.GameController; // Assicurati che ci sia questo import
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private MenuPanel menuPanel;
    private ConfigPanel configPanel;
    private GamePanel gamePanel;
    private GameController gameController; // Questo deve essere presente

    public MainFrame() {
        super("UNO - Progetto MDP");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. Inizializza prima le schermate grafiche
        menuPanel = new MenuPanel(this);
        configPanel = new ConfigPanel(this);
        gamePanel = new GamePanel(this);

        // 2. PEZZO MANCANTE FONDAMENTALE: Crea il controller e passalo ai pannelli
        gameController = new GameController(this, gamePanel);
        configPanel.setController(gameController); // Ora il controller nel ConfigPanel NON è più null!
        gamePanel.setController(gameController);   // Lo passiamo anche al tavolo da gioco

        // 3. Aggiungi i pannelli al contenitore principale
        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(configPanel, "CONFIG");
        mainPanel.add(gamePanel, "GAME");

        add(mainPanel);
        setVisible(true);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
    public GameController getGameController() {
        return gameController;
    }
    /** Imposta la modalità simulazione per il pannello di configurazione. */
    public void setSimulationMode(boolean mode) {
        configPanel.setModalitaSimulazione(mode);
    }
}
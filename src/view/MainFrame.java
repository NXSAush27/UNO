package view;

import controller.GameController;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Pannelli specifici
    private MenuPanel menuPanel;
    private ConfigPanel configPanel;
    private GamePanel gamePanel;

    private GameController gameController; // set by App after construction

    public MainFrame() {
        super("UNO - Progetto MDP");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Inizializza le schermate
        menuPanel = new MenuPanel(this);
        configPanel = new ConfigPanel(this);
        gamePanel = new GamePanel(this);

        // Aggiungi le schermate al CardLayout con un nome univoco
        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(configPanel, "CONFIG");
        mainPanel.add(gamePanel, "GAME");

        add(mainPanel);
        setVisible(true);
    }

    /** Metodo per cambiare schermata */
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    /** Called by App to inject the GameController into all panels that need it. */
    public void setGameController(GameController controller) {
        this.gameController = controller;
        configPanel.setController(controller);
        gamePanel.setController(controller);
    }

    public GameController getGameController() {
        return gameController;
    }

    /** Imposta la modalità simulazione per il pannello di configurazione. */
    public void setSimulationMode(boolean mode) {
        configPanel.setModalitaSimulazione(mode);
    }
}
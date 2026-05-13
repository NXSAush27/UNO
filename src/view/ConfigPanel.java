package view;

import controller.GameController;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class ConfigPanel extends JPanel {
    private MainFrame mainFrame;
    private GameController controller;

    // Esempi di componenti richiesti dal bando
    private JComboBox<String> comboModalita;
    private JSpinner spinnerSogliaPunti;
    private JCheckBox checkStacking;
    private JCheckBox checkNumberRush;

    public ConfigPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new JLabel("Configurazione Partita"));

        // Numero giocatori
        SpinnerNumberModel numModel = new SpinnerNumberModel(2, 2, 6, 1);
        JSpinner spinnerGiocatori = new JSpinner(numModel);
        add(new JLabel("Numero giocatori (2-6):"));
        add(spinnerGiocatori);

        // Modalità Singola o A Punti [cite: 142]
        comboModalita = new JComboBox<>(new String[]{"Partita Singola", "Partita a Punti"});
        add(comboModalita);

        // Soglia punti
        SpinnerNumberModel sogliaModel = new SpinnerNumberModel(500, 100, 9999, 50);
        spinnerSogliaPunti = new JSpinner(sogliaModel);
        add(new JLabel("Soglia punti:"));
        add(spinnerSogliaPunti);

        // Regole alternative [cite: 271-272]
        checkStacking = new JCheckBox("Attiva Stacking (+2 su +2)");
        checkNumberRush = new JCheckBox("Attiva Number Rush");
        add(checkStacking);
        add(checkNumberRush);

        // Bottone per iniziare
        JButton btnStart = new JButton("Avvia Partita");
        btnStart.addActionListener((ActionEvent e) -> {
            int numGiocatori = (Integer) spinnerGiocatori.getValue();
            int sogliaPunti = (Integer) spinnerSogliaPunti.getValue();
            if (controller != null) {
                controller.avviaNuovaPartita(numGiocatori, sogliaPunti);
            }
        });
        add(btnStart);

        JButton btnBack = new JButton("Indietro");
        btnBack.addActionListener(e -> mainFrame.showPanel("MENU"));
        add(btnBack);
    }

    /** Called by MainFrame to inject the GameController. */
    public void setController(GameController controller) {
        this.controller = controller;
    }
}
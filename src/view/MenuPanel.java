package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPanel extends JPanel {
    private MainFrame mainFrame;

    public MenuPanel(MainFrame frame) {
        this.mainFrame = frame;
        
        // Sfondo (puoi personalizzarlo con un'immagine)
        setBackground(new Color(200, 0, 0)); // Rosso UNO
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Spazio tra i bottoni

        // --- TITOLO ---
        JLabel titolo = new JLabel("UNO - Progetto MDP");
        titolo.setFont(new Font("Arial", Font.BOLD, 48));
        titolo.setForeground(Color.WHITE);
        gbc.gridy = 0;
        add(titolo, gbc);

        // --- BOTTONE: NUOVA PARTITA ---
        JButton btnNuova = creaBottoneMenu("Nuova Partita");
        btnNuova.addActionListener(e -> {
            mainFrame.setSimulationMode(false);
            mainFrame.showPanel("CONFIG");
        });
        gbc.gridy = 1;
        add(btnNuova, gbc);

        // --- BOTTONE: CARICA PARTITA  ---
        JButton btnCarica = creaBottoneMenu("Carica Partita");
        btnCarica.addActionListener(e -> {
            // Qui chiamerai il metodo per caricare il file .ser o .json
            mainFrame.getGameController().caricaPartita();
        });
        gbc.gridy = 2;
        add(btnCarica, gbc);

        // --- BOTTONE: SIMULAZIONE BOT [cite: 246] ---
        JButton btnSimulazione = creaBottoneMenu("Modalità Simulazione");
        btnSimulazione.addActionListener(e -> {
            mainFrame.setSimulationMode(true);
            mainFrame.showPanel("CONFIG");
        });
        gbc.gridy = 3;
        add(btnSimulazione, gbc);

        // --- BOTTONE: ESCI ---
        JButton btnEsci = creaBottoneMenu("Esci");
        btnEsci.addActionListener(e -> System.exit(0));
        gbc.gridy = 4;
        add(btnEsci, gbc);
    }

    // Metodo utility per creare bottoni uniformi
    private JButton creaBottoneMenu(String testo) {
        JButton b = new JButton(testo);
        b.setPreferredSize(new Dimension(250, 50));
        b.setFont(new Font("Arial", Font.PLAIN, 18));
        b.setFocusPainted(false);
        return b;
    }
}
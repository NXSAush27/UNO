package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPanel extends JPanel {
    private MainFrame mainFrame;

    public MenuPanel(MainFrame frame) {
        this.mainFrame = frame;
        
        // Sfondo (puoi personalizzarlo con un'immagine)
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
    // --- FEATURE: Sfondo Sfumato Radiale (Bianco Rossastro -> Rosso Bordeaux) ---
    @Override
    protected void paintComponent(Graphics g) {
        // Disegna i componenti di base
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Attiva l'anti-aliasing per rendere la transizione fluida
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        // Manteniamo il raggio ampio per non fare un cerchio netto
        float radius = Math.max(width, height);
        if (radius <= 0) radius = 1; 
        
        // 1. I TUOI NUOVI COLORI: Un bianco rossastro al centro, bordeaux scuro ai lati
        Color biancoRossastro = new Color(255, 85, 80); 
        Color rossoBordeaux = new Color(255, 35, 35);
        
        // 2. LA MAGIA DELLA COMPRESSIONE: 
        // Impostando 0.5f (o 0.6f), il gradiente finisce molto prima.
        // Risultato: il bordeaux domina molto di più i bordi dello schermo.
        float[] posizioni = {0.0f, 0.5f}; 
        Color[] colori = {biancoRossastro, rossoBordeaux};
        
        RadialGradientPaint gradient = new RadialGradientPaint(centerX, centerY, radius, posizioni, colori);
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        g2d.dispose();
    }
}
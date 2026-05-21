package view;

import controller.GameController;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import model.Giocatore;
import model.GiocatoreUmano;
import model.GiocatoreBot;

public class ConfigPanel extends JPanel {
    private MainFrame mainFrame;
    private GameController controller;

    // Componenti
    private JSpinner spinnerGiocatori;
    private JSpinner spinnerUmani;
    private JComboBox<String> comboModalita;
    private JSpinner spinnerSogliaPunti;
    private JCheckBox checkStacking;
    private JCheckBox checkNumberRush;

    // Per gestire la modalità simulazione
    private boolean modalitaSimulazione = false;
    private JLabel labelUmani;
    private JPanel panelGiocatori;

    public ConfigPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));

        // --- Panel principale con GridBagLayout ---
        JPanel mainGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Titolo
        JLabel titolo = new JLabel("Configurazione Partita");
        titolo.setFont(new Font("SansSerif", Font.BOLD, 16));
        mainGrid.add(titolo, gbc);
        gbc.gridy++;

        // --- Sezione Impostazioni Partita ---
        JPanel panelImpostazioni = new JPanel(new GridBagLayout());
        panelImpostazioni.setBorder(BorderFactory.createTitledBorder("Impostazioni Partita"));
        GridBagConstraints gbcImp = new GridBagConstraints();
        gbcImp.insets = new Insets(3, 3, 3, 3);
        gbcImp.fill = GridBagConstraints.HORIZONTAL;
        gbcImp.gridx = 0;
        gbcImp.gridy = 0;

        // Modalità
        gbcImp.gridy++;
        gbcImp.anchor = GridBagConstraints.WEST;
        panelImpostazioni.add(new JLabel("Modalità:"), gbcImp);
        comboModalita = new JComboBox<>(new String[]{"Partita Singola", "Partita a Punti"});
        gbcImp.gridx++;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;
        gbcImp.weightx = 1.0;
        panelImpostazioni.add(comboModalita, gbcImp);
        gbcImp.gridx = 0;
        gbcImp.weightx = 0;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;

        // Soglia punti
        gbcImp.gridy++;
        panelImpostazioni.add(new JLabel("Soglia punti:"), gbcImp);
        SpinnerNumberModel sogliaModel = new SpinnerNumberModel(500, 100, 9999, 50);
        spinnerSogliaPunti = new JSpinner(sogliaModel);
        gbcImp.gridx++;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;
        gbcImp.weightx = 1.0;
        panelImpostazioni.add(spinnerSogliaPunti, gbcImp);
        gbcImp.gridx = 0;
        gbcImp.weightx = 0;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;

        // Regole alternative
        gbcImp.gridy++;
        checkStacking = new JCheckBox("Attiva Stacking (+2 su +2)");
        panelImpostazioni.add(checkStacking, gbcImp);
        gbcImp.gridy++;
        checkNumberRush = new JCheckBox("Attiva Number Rush");
        panelImpostazioni.add(checkNumberRush, gbcImp);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        mainGrid.add(panelImpostazioni, gbc);
        gbc.weightx = 0;
        gbc.weighty = 0;

        // --- Sezione Giocatori ---
        panelGiocatori = new JPanel(new GridBagLayout());
        panelGiocatori.setBorder(BorderFactory.createTitledBorder("Giocatori"));
        GridBagConstraints gbcGioc = new GridBagConstraints();
        gbcGioc.insets = new Insets(3, 3, 3, 3);
        gbcGioc.fill = GridBagConstraints.HORIZONTAL;
        gbcGioc.gridx = 0;
        gbcGioc.gridy = 0;

        // Totale giocatori
        gbcGioc.gridy++;
        gbcGioc.anchor = GridBagConstraints.WEST;
        panelGiocatori.add(new JLabel("Numero giocatori (2-6):"), gbcGioc);
        SpinnerNumberModel numModel = new SpinnerNumberModel(2, 2, 6, 1);
        spinnerGiocatori = new JSpinner(numModel);
        gbcGioc.gridx++;
        gbcGioc.fill = GridBagConstraints.HORIZONTAL;
        gbcGioc.weightx = 1.0;
        panelGiocatori.add(spinnerGiocatori, gbcGioc);
        gbcGioc.gridx = 0;
        gbcGioc.weightx = 0;
        gbcGioc.fill = GridBagConstraints.HORIZONTAL;

        // Giocatori umani
        gbcGioc.gridy++;
        gbcGioc.anchor = GridBagConstraints.WEST;
        labelUmani = new JLabel("Giocatori umani (0-6):");
        panelGiocatori.add(labelUmani, gbcGioc);
        SpinnerNumberModel umaniModel = new SpinnerNumberModel(1, 0, 6, 1);
        spinnerUmani = new JSpinner(umaniModel);
        // ChangeListener to cap at total players
        spinnerUmani.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int total = (Integer) spinnerGiocatori.getValue();
                int umani = (Integer) spinnerUmani.getValue();
                if (umani > total) {
                    spinnerUmani.setValue(total);
                }
            }
        });
        gbcGioc.gridx++;
        gbcGioc.fill = GridBagConstraints.HORIZONTAL;
        gbcGioc.weightx = 1.0;
        panelGiocatori.add(spinnerUmani, gbcGioc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        mainGrid.add(panelGiocatori, gbc);
        gbc.weightx = 0;
        gbc.weighty = 0;

        // --- Pulsanti ---
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelBottoni = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnStart = new JButton("Avvia Partita");
        btnStart.addActionListener((ActionEvent e) -> {
            int numGiocatori = (Integer) spinnerGiocatori.getValue();
            int sogliaPunti = (Integer) spinnerSogliaPunti.getValue();
            Giocatore[] giocatori = new Giocatore[numGiocatori];
            if (modalitaSimulazione) {
                // Tutti bot
                for (int i = 0; i < numGiocatori; i++) {
                    giocatori[i] = new GiocatoreBot("Bot " + i);
                }
            } else {
                int numUmani = (Integer) spinnerUmani.getValue();
                for (int i = 0; i < numGiocatori; i++) {
                    giocatori[i] = (i < numUmani)
                        ? new GiocatoreUmano("Giocatore " + (i+1))
                        : new GiocatoreBot("Bot " + i);
                }
            }
            if (controller != null) {
                controller.avviaNuovaPartita(giocatori, sogliaPunti);
            }
        });
        JButton btnBack = new JButton("Indietro");
        btnBack.addActionListener(e -> mainFrame.showPanel("MENU"));
        panelBottoni.add(btnStart);
        panelBottoni.add(btnBack);
        mainGrid.add(panelBottoni, gbc);

        add(mainGrid, BorderLayout.CENTER);
    }

    /** Called by MainFrame to inject the GameController. */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /** Called by MainFrame to switch between normal game and bot simulation. */
    public void setModalitaSimulazione(boolean simulazione) {
        this.modalitaSimulazione = simulazione;
        if (simulazione) {
            labelUmani.setVisible(false);
            spinnerUmani.setVisible(false);
            panelGiocatori.setBorder(BorderFactory.createTitledBorder("Configurazione Simulazione"));
        } else {
            labelUmani.setVisible(true);
            spinnerUmani.setVisible(true);
            panelGiocatori.setBorder(BorderFactory.createTitledBorder("Giocatori"));
        }
        revalidate();
        repaint();
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
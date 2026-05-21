package view;

import controller.GameController;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        mainGrid.setOpaque(false); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Titolo
        JLabel titolo = new JLabel("CONFIGURAZIONE PARTITA", SwingConstants.CENTER);
        titolo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titolo.setForeground(Color.WHITE); 
        mainGrid.add(titolo, gbc);
        gbc.gridy++;

        // --- Sezione Impostazioni Partita ---
        JPanel panelImpostazioni = new JPanel(new GridBagLayout());
        panelImpostazioni.setOpaque(false); 
        panelImpostazioni.setBorder(createStyledTitledBorder("Impostazioni Partita"));
        
        GridBagConstraints gbcImp = new GridBagConstraints();
        gbcImp.insets = new Insets(6, 6, 6, 6);
        gbcImp.fill = GridBagConstraints.HORIZONTAL;
        gbcImp.gridx = 0;
        gbcImp.gridy = 0;

        // Modalità
        gbcImp.gridy++;
        gbcImp.anchor = GridBagConstraints.WEST;
        panelImpostazioni.add(createStyledLabel("Modalità:"), gbcImp);
        
        comboModalita = new JComboBox<>(new String[]{"Partita Singola", "Partita a Punti"});
        styleComboBox(comboModalita); // APPLICA LO STILE AL MENU A TENDINA
        
        gbcImp.gridx++;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;
        gbcImp.weightx = 1.0;
        panelImpostazioni.add(comboModalita, gbcImp);
        gbcImp.gridx = 0;
        gbcImp.weightx = 0;
        gbcImp.fill = GridBagConstraints.HORIZONTAL;

        // Soglia punti
        gbcImp.gridy++;
        panelImpostazioni.add(createStyledLabel("Soglia punti:"), gbcImp);
        SpinnerNumberModel sogliaModel = new SpinnerNumberModel(500, 100, 9999, 50);
        spinnerSogliaPunti = new JSpinner(sogliaModel);
        styleSpinner(spinnerSogliaPunti); // APPLICA LO STILE AL SELETTORE
        
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
        styleCheckBox(checkStacking);
        panelImpostazioni.add(checkStacking, gbcImp);
        
        gbcImp.gridy++;
        checkNumberRush = new JCheckBox("Attiva Number Rush");
        styleCheckBox(checkNumberRush);
        panelImpostazioni.add(checkNumberRush, gbcImp);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        mainGrid.add(panelImpostazioni, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;

        // --- Sezione Giocatori ---
        panelGiocatori = new JPanel(new GridBagLayout());
        panelGiocatori.setOpaque(false); 
        panelGiocatori.setBorder(createStyledTitledBorder("Giocatori"));
        
        GridBagConstraints gbcGioc = new GridBagConstraints();
        gbcGioc.insets = new Insets(6, 6, 6, 6);
        gbcGioc.fill = GridBagConstraints.HORIZONTAL;
        gbcGioc.gridx = 0;
        gbcGioc.gridy = 0;

        // Totale giocatori
        gbcGioc.gridy++;
        gbcGioc.anchor = GridBagConstraints.WEST;
        panelGiocatori.add(createStyledLabel("Numero giocatori (2-6):"), gbcGioc);
        SpinnerNumberModel numModel = new SpinnerNumberModel(2, 2, 6, 1);
        spinnerGiocatori = new JSpinner(numModel);
        styleSpinner(spinnerGiocatori); // APPLICA LO STILE
        
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
        labelUmani = createStyledLabel("Giocatori umani (0-6):");
        panelGiocatori.add(labelUmani, gbcGioc);
        SpinnerNumberModel umaniModel = new SpinnerNumberModel(1, 0, 6, 1);
        spinnerUmani = new JSpinner(umaniModel);
        styleSpinner(spinnerUmani); // APPLICA LO STILE
        
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
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;

        // --- Pulsanti Customizzati ---
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel panelBottoni = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBottoni.setOpaque(false); 
        
        JButton btnStart = new JButton("Avvia Partita");
        styleButton(btnStart, new Color(40, 140, 60), new Color(60, 180, 80)); 
        btnStart.addActionListener((ActionEvent e) -> {
            int numGiocatori = (Integer) spinnerGiocatori.getValue();
            int sogliaPunti = (Integer) spinnerSogliaPunti.getValue();
            Giocatore[] giocatori = new Giocatore[numGiocatori];
            if (modalitaSimulazione) {
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
        styleButton(btnBack, new Color(50, 50, 50), new Color(80, 80, 80)); 
        btnBack.addActionListener(e -> mainFrame.showPanel("MENU"));
        
        panelBottoni.add(btnStart);
        panelBottoni.add(btnBack);
        mainGrid.add(panelBottoni, gbc);

        add(mainGrid, BorderLayout.CENTER);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void setModalitaSimulazione(boolean simulazione) {
        this.modalitaSimulazione = simulazione;
        if (simulazione) {
            labelUmani.setVisible(false);
            spinnerUmani.setVisible(false);
            panelGiocatori.setBorder(createStyledTitledBorder("Configurazione Simulazione"));
        } else {
            labelUmani.setVisible(true);
            spinnerUmani.setVisible(true);
            panelGiocatori.setBorder(createStyledTitledBorder("Giocatori"));
        }
        revalidate();
        repaint();
    }
    
    // --- METODI DI STYLING UI ---

    private JLabel createStyledLabel(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        return lbl;
    }
    
    private void styleCheckBox(JCheckBox chk) {
        chk.setOpaque(false);
        chk.setForeground(Color.WHITE);
        chk.setFont(new Font("SansSerif", Font.BOLD, 15));
        chk.setFocusPainted(false);
    }
    
    private TitledBorder createStyledTitledBorder(String titolo) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2, true), 
            titolo,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 16),
            Color.WHITE
        );
    }

    // NUOVO METODO: Trasforma il JComboBox in un componente dark elegante
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.BOLD, 14));
        combo.setBackground(new Color(45, 45, 45));
        combo.setForeground(Color.WHITE);
        
        // Crea un bordo composto per distanziare il testo
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        // RENDERER PERSONALIZZATO: Cambia l'aspetto delle righe quando apri il menu
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("SansSerif", Font.BOLD, 14));
                label.setOpaque(true);
                
                // Fondo scuro per la lista a comparsa
                list.setBackground(new Color(35, 35, 35));
                
                if (isSelected) {
                    label.setBackground(new Color(180, 25, 35)); // Evidenziazione bordeaux in linea col tema
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(new Color(45, 45, 45));
                    label.setForeground(Color.LIGHT_GRAY);
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return label;
            }
        });
    }

    // NUOVO METODO: Scurisce ed uniforma i selettori numerici (JSpinner)
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.BOLD, 14));
        spinner.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        
        // Stilizza l'editor di testo dentro lo spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBackground(new Color(45, 45, 45));
            textField.setForeground(Color.WHITE);
            textField.setCaretColor(Color.WHITE);
            textField.setFont(new Font("SansSerif", Font.BOLD, 14));
            textField.setHorizontalAlignment(JTextField.CENTER);
        }
    }
    
    private void styleButton(JButton btn, Color bgColor, Color hoverColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
    }

    // --- FEATURE: Sfondo Sfumato Radiale (Bianco Rossastro -> Rosso Bordeaux) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        float centerX = width / 2f;
        float centerY = height / 2f;
        
        float radius = Math.max(width, height);
        if (radius <= 0) radius = 1; 
        
Color biancoRossastro = new Color(255, 85, 80); 
        Color rossoBordeaux = new Color(255, 35, 35);
        
        float[] posizioni = {0.0f, 0.5f}; 
        Color[] colori = {biancoRossastro, rossoBordeaux};
        
        RadialGradientPaint gradient = new RadialGradientPaint(centerX, centerY, radius, posizioni, colori);
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
    }
}
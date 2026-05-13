package view;

import controller.GameController;
import model.Carta;
import model.Giocatore;
import model.GiocatoreBot;
import model.Mano;
import model.Partita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GamePanel extends JPanel {
    private MainFrame mainFrame;
    private GameController controller;
    private JTextArea areaStorico;
    private JPanel pannelloMano;
    private JLabel labelScarti;
    private JLabel labelTurno;
    private Partita partitaCorrente;

    // Panel per avversari
    private JPanel panelNorth;
    private JPanel panelWest;
    private JPanel panelEast;

    // Overlay per transizione hotseat
    private JPanel overlayPassaggio;

    // Cached card back image
    private static ImageIcon cardBackIcon;

    // Constants for image directory and card dimensions
    private static final String IMG_PATH_BASE = "/UnoCards/";
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    
    // Color mapping from Carta.colore to folder names
    // Carta.colore: 0=rosso, 1=verde, 2=blu, 3=giallo, 4=jolly
    private static final String[] COLORE_NOMI = {"red", "green", "blue", "yellow", "wild"};

    static {
        java.net.URL imgURL = GamePanel.class.getResource("/UnoCards/card back/card_back.png");
        if (imgURL != null) {
            Image img = new ImageIcon(imgURL).getImage();
            Image scaled = img.getScaledInstance(40, 60, Image.SCALE_SMOOTH);
            cardBackIcon = new ImageIcon(scaled);
        } else {
            cardBackIcon = null;
        }
    }

    public GamePanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // --- ZONA CENTRALE: Mazzo e Scarti ---
        JPanel centro = new JPanel();
        labelScarti = new JLabel("CARTA IN CIMA: [Nessuna]");
        centro.add(labelScarti);

        JButton btnPesca = new JButton("Pesca Carta");
        btnPesca.addActionListener(this::azionePesca);
        centro.add(btnPesca);
        add(centro, BorderLayout.CENTER);

        // --- ZONA IN BASSO: Mano del Giocatore ---
        pannelloMano = new JPanel(new FlowLayout());
        add(pannelloMano, BorderLayout.SOUTH);

        // --- ZONA NORD: Avversari ---
        panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        add(panelNorth, BorderLayout.NORTH);

        // --- ZONA OVEST: Avversari ---
        panelWest = new JPanel();
        panelWest.setLayout(new BoxLayout(panelWest, BoxLayout.Y_AXIS));
        add(panelWest, BorderLayout.WEST);

        // --- ZONA EST: Avversari ---
        panelEast = new JPanel();
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        add(panelEast, BorderLayout.EAST);

        // --- ZONA DESTRA: Storico, turno e Azioni Extra ---
        JPanel pannelloDestro = new JPanel(new BorderLayout());

        // Turno corrente
        labelTurno = new JLabel("Turno: --");
        pannelloDestro.add(labelTurno, BorderLayout.NORTH);

        // Lo storico mosse
        areaStorico = new JTextArea(15, 20);
        areaStorico.setEditable(false);
        areaStorico.setText("Partita Iniziata...\n");
        JScrollPane scrollStorico = new JScrollPane(areaStorico);
        pannelloDestro.add(scrollStorico, BorderLayout.CENTER);

        // Bottoni per Salvare e dichiarare UNO
        JPanel bottoniDestra = new JPanel(new GridLayout(2, 1));
        JButton btnUno = new JButton("Dichiara UNO!");
        btnUno.addActionListener(this::azioneDichiaraUno);
        JButton btnSalva = new JButton("Salva Partita");
        btnSalva.addActionListener(this::azioneSalva);
        bottoniDestra.add(btnUno);
        bottoniDestra.add(btnSalva);

        pannelloDestro.add(bottoniDestra, BorderLayout.SOUTH);
        add(pannelloDestro, BorderLayout.EAST);

        // --- OVERLAY per transizione hotseat ---
        overlayPassaggio = new JPanel(new BorderLayout());
        overlayPassaggio.setBackground(new Color(0, 0, 0, 200));
        overlayPassaggio.setVisible(false);
        JLabel labelOverlay = new JLabel("Passa il dispositivo");
        labelOverlay.setForeground(Color.WHITE);
        labelOverlay.setFont(new Font("SansSerif", Font.BOLD, 24));
        labelOverlay.setHorizontalAlignment(SwingConstants.CENTER);
        overlayPassaggio.add(labelOverlay, BorderLayout.CENTER);
        overlayPassaggio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (controller != null) {
                    controller.confermaPassaggio();
                }
            }
        });

        // Overlay is added to the frame's layered pane by mostraTransizionePassaggio()
        // to ensure it covers the entire window and stays on top.
    }

    /** Called by MainFrame to inject the GameController. */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    // -------------------------------------------------------------------
    //  Hotseat overlay methods
    // -------------------------------------------------------------------

    public void mostraTransizionePassaggio(String nomeGiocatore) {
        for (var comp : overlayPassaggio.getComponents()) {
            if (comp instanceof JLabel label) {
                label.setText("Passa il dispositivo a " + nomeGiocatore + " — Clicca per rivelare le carte");
                label.revalidate();
            }
        }
        // Add overlay to frame's layered pane (on top)
        mainFrame.getLayeredPane().add(overlayPassaggio, JLayeredPane.PALETTE_LAYER);
        overlayPassaggio.setBounds(0, 0, mainFrame.getWidth(), mainFrame.getHeight());
        overlayPassaggio.setVisible(true);
        overlayPassaggio.revalidate();
        overlayPassaggio.repaint();
    }

    public void nascondiTransizione() {
        overlayPassaggio.setVisible(false);
        mainFrame.getLayeredPane().remove(overlayPassaggio);
        mainFrame.getLayeredPane().revalidate();
        mainFrame.getLayeredPane().repaint();
    }

    // -------------------------------------------------------------------
    //  Opponent panel update
    // -------------------------------------------------------------------

    public void aggiornaOpponenti(Partita partita) {
        panelNorth.removeAll();
        panelWest.removeAll();
        panelEast.removeAll();

        Giocatore[] giocatori = partita.getGiocatori();
        Giocatore corrente = partita.getGiocatoreCorrente();
        int numGiocatori = giocatori.length;
        int numOpponenti = numGiocatori - 1;

        if (numOpponenti <= 0) {
            panelNorth.revalidate();
            panelNorth.repaint();
            panelWest.revalidate();
            panelWest.repaint();
            panelEast.revalidate();
            panelEast.repaint();
            return;
        }

        int oppos[] = new int[numOpponenti];
        int idx = 0;
        for (int i = 0; i < numGiocatori; i++) {
            if (giocatori[i] != corrente) {
                oppos[idx++] = i;
            }
        }

        // Positioning: 1opp=N, 2opp=N*2, 3opp=N+E+W, 4opp=N*2+E+W, 5opp=N*2+E*2+W*2
        // panels[0] = NORTH, [1] = EAST, [2] = WEST
        int alloc[] = new int[3];
        alloc[0] = alloc[1] = alloc[2] = 0;

        switch (numOpponenti) {
            case 1 -> alloc[0] = 1;
            case 2 -> alloc[0] = 2;
            case 3 -> { alloc[0] = 1; alloc[1] = 1; alloc[2] = 1; }
            case 4 -> { alloc[0] = 2; alloc[1] = 1; alloc[2] = 1; }
            case 5 -> { alloc[0] = 2; alloc[1] = 2; alloc[2] = 1; } // WEST gets 1
            // Alternative: alloc[2] = 2 would be balanced but plan says WEST*2 for 6 players
        }

        // Distribute opponents into panels according to allocation
        int pos = 0;
        // NORTH panel
        for (int i = 0; i < alloc[0]; i++) {
            int giocIndex = oppos[pos++];
            panelNorth.add(creaPanelAvversario(giocatori[giocIndex]));
        }
        // EAST panel
        for (int i = 0; i < alloc[1]; i++) {
            int giocIndex = oppos[pos++];
            panelEast.add(creaPanelAvversario(giocatori[giocIndex]));
        }
        // WEST panel
        for (int i = 0; i < alloc[2]; i++) {
            int giocIndex = oppos[pos++];
            panelWest.add(creaPanelAvversario(giocatori[giocIndex]));
        }

        panelNorth.revalidate();
        panelNorth.repaint();
        panelWest.revalidate();
        panelWest.repaint();
        panelEast.revalidate();
        panelEast.repaint();
    }

    private JPanel creaPanelAvversario(Giocatore g) {
        JPanel p = new JPanel(new BorderLayout(5, 5));

        JLabel nomeLabel = new JLabel(g.getNome() + (g instanceof GiocatoreBot ? " (BOT)" : ""));
        nomeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        p.add(nomeLabel, BorderLayout.NORTH);

        // Card count display
        JPanel cardPanel = new JPanel();
        int cardCount = g.getMano().getCarte().size();
        if (cardBackIcon != null) {
            int displayCount = Math.min(cardCount, 5);
            cardPanel.setLayout(new GridLayout(1, displayCount, 2, 0));
            for (int i = 0; i < displayCount; i++) {
                cardPanel.add(new JLabel(cardBackIcon));
            }
            if (cardCount > 5) {
                JLabel more = new JLabel("+" + (cardCount - 5));
                more.setHorizontalAlignment(SwingConstants.CENTER);
                cardPanel.add(more);
            }
        } else {
            JLabel fallback = new JLabel("x " + cardCount);
            fallback.setHorizontalAlignment(SwingConstants.CENTER);
            cardPanel.add(fallback);
        }
        p.add(cardPanel, BorderLayout.CENTER);

        JLabel countLabel = new JLabel("x " + cardCount);
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(countLabel, BorderLayout.SOUTH);

        return p;
    }

    // -------------------------------------------------------------------
    //  Action listeners wired to GameController
    // -------------------------------------------------------------------

    private void azionePesca(ActionEvent e) {
        if (controller != null) {
            controller.gestisciClickPesca();
        }
    }

    private void azioneSalva(ActionEvent e) {
        if (controller != null) {
            controller.salvaPartita();
        }
    }

    private void azioneDichiaraUno(ActionEvent e) {
        if (controller != null) {
            controller.dichiaraUno();
        }
    }

    // -------------------------------------------------------------------
    //  Public API called by GameController
    // -------------------------------------------------------------------

    public void aggiungiLog(String messaggio) {
        areaStorico.append(messaggio + "\n");
    }

    public void aggiornaTavolo(Partita partita) {
        this.partitaCorrente = partita;

        // Update discard pile with card image
        if (partita.getCartaInGioco() != null) {
            Carta cartaInGioco = partita.getCartaInGioco();
            ImageIcon icon = getIconForCarta(cartaInGioco);
            if (icon != null) {
                labelScarti.setIcon(icon);
                labelScarti.setText("");
            } else {
                labelScarti.setIcon(null);
                labelScarti.setText("CARTA IN CIMA: " + cartaInGioco.toString());
            }
        } else {
            labelScarti.setIcon(null);
            labelScarti.setText("CARTA IN CIMA: [Nessuna]");
        }

        // Aggiorna l'etichetta del turno
        Giocatore corrente = partita.getGiocatoreCorrente();
        labelTurno.setText("Turno: " + corrente.getNome()
                + (corrente instanceof model.GiocatoreBot ? " (BOT)" : ""));

        // Aggiorna gli avversari
        aggiornaOpponenti(partita);

        // Ricostruisce la mano del giocatore corrente con bottoni cliccabili
        pannelloMano.removeAll();
        Mano mano = corrente.getMano();
        List<Carta> carte = mano.getCarte();

        for (int i = 0; i < carte.size(); i++) {
            Carta c = carte.get(i);
            ImageIcon icon = getIconForCarta(c);
            JButton btn;
            
            if (icon != null) {
                btn = new JButton(icon);
            } else {
                // Fallback to text if image fails to load
                btn = new JButton(c.toString());
            }
            
            final int indice = i;
            // Solo le carte giocabili (valide) sono cliccabili
            boolean valida = partita.isMossaValida(c);
            btn.setEnabled(valida);
            
            // Style button to look like a clean card
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            
            if (valida) {
                btn.addActionListener(ev -> giocaCarta(indice));
            }
            pannelloMano.add(btn);
        }

        pannelloMano.revalidate();
        pannelloMano.repaint();
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Delegates a card-play to the controller.
     */
    private void giocaCarta(int indiceCarta) {
        if (controller != null) {
            controller.gestisciClickCarta(indiceCarta);
        }
    }

    /**
     * Returns a scaled ImageIcon for the given card.
     * If the image cannot be loaded, returns null.
     */
    private ImageIcon getIconForCarta(Carta c) {
        if (c == null) return null;

        String colorName = "";
        String fileName = "";
        
        try {
            // Use getters to access card properties
            int colore = c.getColore();
            int tipo = c.getTipo();
            int numero = c.getNumero();
            
            // Determine color folder
            if (colore < 0 || colore >= COLORE_NOMI.length) {
                System.err.println("Invalid colore: " + colore + " for card " + c);
                return null;
            }
            colorName = COLORE_NOMI[colore];
            
            // Determine filename based on card type
            if (colore == 4) {
                // Wild cards (colore = 4 = jolly)
                if (tipo == 5) {
                    // +4 card
                    fileName = "4_plus.png";
                } else {
                    // Wild card (tipo = 4)
                    fileName = "wild_card.png";
                }
            } else {
                // Colored cards
                if (tipo == 0) {
                    // Normal number card
                    fileName = numero + "_" + colorName + ".png";
                } else if (tipo == 1) {
                    // +2 card
                    fileName = "2plus_" + colorName + ".png";
                } else if (tipo == 2) {
                    // Reverse card
                    fileName = "inverse_" + colorName + ".png";
                } else if (tipo == 3) {
                    // Skip/Block card
                    fileName = "block_" + colorName + ".png";
                } else {
                    // Unknown type - fallback
                    System.err.println("Unknown tipo: " + tipo + " for card " + c);
                    return null;
                }
            }
            
            String resourcePath = IMG_PATH_BASE + colorName + "/" + fileName;
            java.net.URL imgURL = GamePanel.class.getResource(resourcePath);
            if (imgURL == null) {
                System.err.println("Image NOT FOUND: " + resourcePath + " (colore=" + colore + ", tipo=" + tipo + ", numero=" + numero + ")");
                // Try to find where we are looking
                java.net.URL baseURL = GamePanel.class.getResource("/UnoCards/");
                System.err.println("  Base URL check: /UnoCards/ exists? " + (baseURL != null ? baseURL : "NO"));
                return null;
            }
            
            Image img = new ImageIcon(imgURL).getImage();
            Image scaled = img.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
            System.out.println("Loaded image: " + resourcePath);
            return new ImageIcon(scaled);
            
        } catch (Exception e) {
            System.err.println("Error loading image for card: " + c + " path: " + IMG_PATH_BASE + colorName + "/" + fileName);
            e.printStackTrace();
            return null;
        }
    }
}
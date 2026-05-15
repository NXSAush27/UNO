package view;

import controller.GameController;
import model.Carta;
import model.Giocatore;
import model.GiocatoreBot;
import model.GiocatoreUmano;
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

    // Cached card back image (used to hide bot's hand from the human player)
    private static ImageIcon cardBackIcon;

    // Constants for image paths and card dimensions
    private static final String IMG_PATH_BASE = "/UnoCards/";
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;

    // Carta.colore → folder name mapping
    // 0=rosso=red, 1=verde=green, 2=blu=blue, 3=giallo=yellow, 4=jolly=wild
    private static final String[] COLORE_NOMI = {"red", "green", "blue", "yellow", "wild"};

    static {
        java.net.URL imgURL = GamePanel.class.getResource("/UnoCards/card back/card_back.png");
        if (imgURL != null) {
            Image img = new ImageIcon(imgURL).getImage();
            Image scaled = img.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
            cardBackIcon = new ImageIcon(scaled);
        } else {
            cardBackIcon = null;
        }
    }

    // ────────────────────────────────────────────
    // Constructor / setup
    // ────────────────────────────────────────────

    public GamePanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // ── ZONA CENTRALE: discard pile + draw button ──
        JPanel centro = new JPanel();
        labelScarti = new JLabel("CARTA IN CIMA: [Nessuna]");
        centro.add(labelScarti);

        JButton btnPesca = new JButton("Pesca Carta");
        btnPesca.addActionListener(this::azionePesca);
        centro.add(btnPesca);
        add(centro, BorderLayout.CENTER);

        // ── ZONA IN BASSO: current player's hand ──
        pannelloMano = new JPanel(new FlowLayout());
        add(pannelloMano, BorderLayout.SOUTH);

        // ── ZONA NORD, OVEST, EST: opponent panels ──
        panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        add(panelNorth, BorderLayout.NORTH);

        panelWest = new JPanel();
        panelWest.setLayout(new BoxLayout(panelWest, BoxLayout.Y_AXIS));
        add(panelWest, BorderLayout.WEST);

        panelEast = new JPanel();
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        add(panelEast, BorderLayout.EAST);

        // ── ZONA DESTRA: log + turn display + extra buttons ──
        JPanel pannelloDestro = new JPanel(new BorderLayout());

        labelTurno = new JLabel("Turno: --");
        pannelloDestro.add(labelTurno, BorderLayout.NORTH);

        areaStorico = new JTextArea(15, 20);
        areaStorico.setEditable(false);
        areaStorico.setText("Partita Iniziata...\n");
        JScrollPane scrollStorico = new JScrollPane(areaStorico);
        pannelloDestro.add(scrollStorico, BorderLayout.CENTER);

        JPanel bottoniDestra = new JPanel(new GridLayout(2, 1));
        JButton btnUno = new JButton("Dichiara UNO!");
        btnUno.addActionListener(this::azioneDichiaraUno);
        JButton btnSalva = new JButton("Salva Partita");
        btnSalva.addActionListener(this::azioneSalva);
        bottoniDestra.add(btnUno);
        bottoniDestra.add(btnSalva);
        pannelloDestro.add(bottoniDestra, BorderLayout.SOUTH);
        add(pannelloDestro, BorderLayout.EAST);

        // ── OVERLAY per hotseat passaggio ──
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
    }

    /** Called by MainFrame to inject the GameController. */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    // ────────────────────────────────────────────
    //  Hotseat overlay
    // ────────────────────────────────────────────

    public void mostraTransizionePassaggio(String nomeGiocatore) {
        for (var comp : overlayPassaggio.getComponents()) {
            if (comp instanceof JLabel label) {
                label.setText("Passa il dispositivo a " + nomeGiocatore
                        + " \u2014 Clicca per rivelare le carte");
                label.revalidate();
            }
        }
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

    // ────────────────────────────────────────────
    //  Opponent panels
    // ────────────────────────────────────────────

    public void aggiornaOpponenti(Partita partita) {
        panelNorth.removeAll();
        panelWest.removeAll();
        panelEast.removeAll();

        Giocatore[] giocatori = partita.getGiocatori();
        Giocatore corrente = partita.getGiocatoreCorrente();
        int numGiocatori = giocatori.length;
        int numOpponenti = numGiocatori - 1;

        if (numOpponenti <= 0) {
            panelNorth.revalidate(); panelNorth.repaint();
            panelWest.revalidate();  panelWest.repaint();
            panelEast.revalidate();  panelEast.repaint();
            return;
        }

        int oppos[] = new int[numOpponenti];
        int idx = 0;
        for (int i = 0; i < numGiocatori; i++) {
            if (giocatori[i] != corrente) {
                oppos[idx++] = i;
            }
        }

        // panels[0]=NORTH  panels[1]=EAST  panels[2]=WEST
        int alloc[] = new int[3];
        alloc[0] = alloc[1] = alloc[2] = 0;
        switch (numOpponenti) {
            case 1 -> alloc[0] = 1;
            case 2 -> alloc[0] = 2;
            case 3 -> { alloc[0] = 1; alloc[1] = 1; alloc[2] = 1; }
            case 4 -> { alloc[0] = 2; alloc[1] = 1; alloc[2] = 1; }
            case 5 -> { alloc[0] = 2; alloc[1] = 2; alloc[2] = 1; }
        }

        int pos = 0;
        for (int i = 0; i < alloc[0]; i++) panelNorth.add(creaPanelAvversario(giocatori[oppos[pos++]]));
        for (int i = 0; i < alloc[1]; i++) panelEast.  add(creaPanelAvversario(giocatori[oppos[pos++]]));
        for (int i = 0; i < alloc[2]; i++) panelWest.  add(creaPanelAvversario(giocatori[oppos[pos++]]));

        panelNorth.revalidate(); panelNorth.repaint();
        panelWest.revalidate();  panelWest.repaint();
        panelEast.revalidate();  panelEast.repaint();
    }

    private JPanel creaPanelAvversario(Giocatore g) {
        JPanel p = new JPanel(new BorderLayout(5, 5));

        JLabel nomeLabel = new JLabel(g.getNome() + (g instanceof GiocatoreBot ? " (BOT)" : ""));
        nomeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        p.add(nomeLabel, BorderLayout.NORTH);

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

    // ────────────────────────────────────────────
    //  Action listeners
    // ────────────────────────────────────────────

    private void azionePesca(ActionEvent e) {
        if (controller != null) controller.gestisciClickPesca();
    }

    private void azioneSalva(ActionEvent e) {
        if (controller != null) controller.salvaPartita();
    }

    private void azioneDichiaraUno(ActionEvent e) {
        if (controller != null) controller.dichiaraUno();
    }

    // ────────────────────────────────────────────
    //  Public API called by GameController
    // ────────────────────────────────────────────

    public void aggiungiLog(String messaggio) {
        areaStorico.append(messaggio + "\n");
    }

    public void aggiornaTavolo(Partita partita) {
        this.partitaCorrente = partita;
        Giocatore corrente = partita.getGiocatoreCorrente();
        Mano manoCorrente    = corrente.getMano();
        boolean isHuman      = corrente instanceof GiocatoreUmano;

        // ── Discard pile ────────────────────────────────────────────────────────
        if (partita.getCartaInGioco() != null) {
            ImageIcon scartiIcon = getIconForCarta(partita.getCartaInGioco());
            if (scartiIcon != null) {
                labelScarti.setIcon(scartiIcon);
                labelScarti.setText("");
            } else {
                labelScarti.setIcon(null);
                labelScarti.setText("CARTA IN CIMA: " + partita.getCartaInGioco());
            }
        } else {
            labelScarti.setIcon(null);
            labelScarti.setText("CARTA IN CIMA: [Nessuna]");
        }

        // ── Turn label ──
        labelTurno.setText("Turno: " + corrente.getNome()
                + (corrente instanceof GiocatoreBot ? " (BOT)" : ""));

        // ── Opponent panels ──
        aggiornaOpponenti(partita);

        // ── Hand panel ──
        pannelloMano.removeAll();
        List<Carta> carte = manoCorrente.getCarte();

        for (int i = 0; i < carte.size(); i++) {
            Carta c = carte.get(i);
            JButton btn;

            if (isHuman) {
                // Human sees actual card images; only playable cards get a listener
                ImageIcon icon = getIconForCarta(c);
                if (icon != null) {
                    btn = new JButton(icon);
                } else {
                    btn = new JButton(c.toString());
                }
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);

                boolean valida = partita.isMossaValida(c);
                btn.setEnabled(valida);

                if (valida) {
                    final int indice = i;
                    btn.addActionListener(ev -> giocaCarta(indice));
                }
            } else {
                // Bot's turn → show card back, never clickable
                if (cardBackIcon != null) {
                    btn = new JButton(cardBackIcon);
                } else {
                    btn = new JButton("?");
                }
                btn.setEnabled(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
            }

            pannelloMano.add(btn);
        }

        pannelloMano.revalidate();
        pannelloMano.repaint();
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    // ────────────────────────────────────────────
    //  Delegates
    // ────────────────────────────────────────────

    private void giocaCarta(int indiceCarta) {
        if (controller != null) {
            controller.gestisciClickCarta(indiceCarta);
        }
    }
    private void pescaCarta() {
        if (controller != null) {
            controller.gestisciClickPesca();
        }
    }

    // ────────────────────────────────────────────
    //  Image helper
    // ────────────────────────────────────────────

    /**
     * Maps a {@link Carta} to the correct image file inside {@code /UnoCards/}.
     *
     * <p>Mapping rules (based on {@code Carta.tipo} — highest priority):
     * <ul>
     *   <li>tipo 4 → &quot;wild_card.png&quot; in the wild/ folder</li>
     *   <li>tipo 5 → &quot;4_plus.png&quot; in the wild/ folder</li>
     *   <li>tipo 0 → &quot;{numero}_{colore}.png&quot; in the colour folder</li>
     *   <li>tipo 1 → &quot;2plus_{colore}.png&quot;</li>
     *   <li>tipo 2 → &quot;inverse_{colore}.png&quot;</li>
     *   <li>tipo 3 → &quot;block_{colore}.png&quot;</li>
     * </ul>
     * All images are scaled to {@link #CARD_WIDTH}×{@link #CARD_HEIGHT}.
      * Returns {@code null} if the image cannot be found (caller should show text fallback).
      */
    private ImageIcon getIconForCarta(Carta c) {
        if (c == null) return null;

        try {
            int colore = c.getColore();
            int tipo   = c.getTipo();
            int numero = c.getNumero();

            String fileName;

            if (tipo == 4) {
                // Wild: use colour-specific image if a colour has already been
                // chosen (colore 0-3), otherwise use the base image.
                if (colore >= 0 && colore <= 3) {
                    fileName = "wild_card_" + COLORE_NOMI[colore] + ".png";
                } else {
                    fileName = "wild_card.png";
                }
            } else if (tipo == 5) {
                // +4: same logic
                if (colore >= 0 && colore <= 3) {
                    fileName = "4_plus_" + COLORE_NOMI[colore] + ".png";
                } else {
                    fileName = "4_plus.png";
                }
            } else if (colore >= 0 && colore < COLORE_NOMI.length) {
                String col = COLORE_NOMI[colore];
                switch (tipo) {
                    case 0 -> fileName = numero + "_" + col + ".png";
                    case 1 -> fileName = "2plus_"   + col + ".png";
                    case 2 -> fileName = "inverse_" + col + ".png";
                    case 3 -> fileName = "block_"   + col + ".png";
                    default -> { return null; }
                }
            } else {
                return null;
            }

            String resourcePath = IMG_PATH_BASE
                    + (tipo >= 4 ? "wild" : COLORE_NOMI[colore])
                    + "/" + fileName;
            java.net.URL imgURL = GamePanel.class.getResource(resourcePath);
            if (imgURL == null) {
                System.err.println("Image NOT FOUND: " + resourcePath
                        + "  [tipo=" + tipo + " colore=" + colore + " numero=" + numero + "]");
                return null;
            }

            Image img = new ImageIcon(imgURL).getImage();
            Image scaled = img.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception e) {
            System.err.println("Error loading card image:");
            e.printStackTrace();
            return null;
        }
    }
}
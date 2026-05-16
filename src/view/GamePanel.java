package view;

import controller.GameController;
import model.Carta;
import model.Giocatore;
import model.Partita;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class GamePanel extends JPanel {
    private MainFrame mainFrame;
    private GameController controller;
    private JTextArea areaStorico;
    private JPanel pannelloMano;
    private JPanel pannelloAvversari;
    private JLabel labelScarti;
    private JButton btnPesca;
    private JLabel labelTurno;
    private Partita partitaCorrente;
    private JPanel overlay;
    private JLabel labelTransizione;

    // Dimensioni costanti per le carte
    private final int CARD_WIDTH = 70;
    private final int CARD_HEIGHT = 100;
    private final int SMALL_CARD_WIDTH = 35;
    private final int SMALL_CARD_HEIGHT = 50;

    public GamePanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());
        setBackground(new Color(34, 139, 34)); // Sfondo verde tavolo da gioco

        // --- ZONA NORD: Avversari (Carte coperte) ---
        pannelloAvversari = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        pannelloAvversari.setOpaque(false);
        add(pannelloAvversari, BorderLayout.NORTH);

        // --- ZONA CENTRALE: Mazzo e Scarti ---
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 50));
        centro.setOpaque(false);
        
        // Pila scarti
        labelScarti = new JLabel();
        labelScarti.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        centro.add(labelScarti);

        // Mazzo di pesca
        btnPesca = new JButton();
        ImageIcon backIcon = getCardBackIcon(CARD_WIDTH, CARD_HEIGHT);
        if (backIcon != null) {
            btnPesca.setIcon(backIcon);
            pulisciBottone(btnPesca);
        } else {
            btnPesca.setText("Pesca");
        }
        btnPesca.addActionListener(this::azionePesca);
        centro.add(btnPesca);
        add(centro, BorderLayout.CENTER);

        // --- ZONA IN BASSO: Mano del Giocatore Corrente ---
        JPanel sudPanel = new JPanel(new BorderLayout());
        sudPanel.setOpaque(false);
        
        labelTurno = new JLabel("Turno: --", SwingConstants.CENTER);
        labelTurno.setFont(new Font("Arial", Font.BOLD, 20));
        labelTurno.setForeground(Color.WHITE);
        sudPanel.add(labelTurno, BorderLayout.NORTH);

        pannelloMano = new JPanel(new FlowLayout(FlowLayout.CENTER, -10, 10)); // Gap negativo per sovrapporre leggermente le carte
        pannelloMano.setOpaque(false);
        sudPanel.add(pannelloMano, BorderLayout.CENTER);
        add(sudPanel, BorderLayout.SOUTH);

        // --- ZONA DESTRA: Storico e Azioni ---
        JPanel pannelloDestro = new JPanel(new BorderLayout());
        pannelloDestro.setBorder(new EmptyBorder(10, 10, 10, 10));
        pannelloDestro.setOpaque(false);

        areaStorico = new JTextArea(15, 20);
        areaStorico.setEditable(false);
        areaStorico.setText("Partita Iniziata...\n");
        JScrollPane scrollStorico = new JScrollPane(areaStorico);
        pannelloDestro.add(scrollStorico, BorderLayout.CENTER);

        JPanel bottoniDestra = new JPanel(new GridLayout(2, 1, 0, 5));
        bottoniDestra.setOpaque(false);
        JButton btnUno = new JButton("Dichiara UNO!");
        btnUno.addActionListener(this::azioneDichiaraUno);
        JButton btnSalva = new JButton("Salva Partita");
        btnSalva.addActionListener(this::azioneSalva);
        bottoniDestra.add(btnUno);
        bottoniDestra.add(btnSalva);

        pannelloDestro.add(bottoniDestra, BorderLayout.SOUTH);
        add(pannelloDestro, BorderLayout.EAST);

        overlay = new JPanel(new GridBagLayout());
        overlay.setBackground(new Color(0, 0, 0, 200));
        overlay.setVisible(false);
        overlay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (controller != null) controller.confermaPassaggio();
            }
        });

        labelTransizione = new JLabel();
        labelTransizione.setFont(new Font("Arial", Font.BOLD, 24));
        labelTransizione.setForeground(Color.WHITE);
        overlay.add(labelTransizione);
        add(overlay, BorderLayout.CENTER);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    private void azionePesca(ActionEvent e) {
        if (controller != null) controller.gestisciClickPesca();
    }

    private void azioneSalva(ActionEvent e) {
        if (controller != null) controller.salvaPartita();
    }

    private void azioneDichiaraUno(ActionEvent e) {
        if (controller != null) controller.dichiaraUno();
    }

    public void aggiungiLog(String messaggio) {
        areaStorico.append(messaggio + "\n");
        areaStorico.setCaretPosition(areaStorico.getDocument().getLength()); // Scrolla giù
    }

    public void aggiornaTavolo(Partita partita) {
        this.partitaCorrente = partita;
        Giocatore corrente = partita.getGiocatoreCorrente();

        // 1. Aggiorna l'etichetta del turno
        labelTurno.setText("Turno di: " + corrente.getNome() + (corrente instanceof model.GiocatoreBot ? " (BOT)" : ""));

        // 2. Aggiorna Carta in Gioco (Scarti)
        if (partita.getCartaInGioco() != null) {
            ImageIcon iconaScarto = getIconForCarta(partita.getCartaInGioco(), CARD_WIDTH, CARD_HEIGHT);
            if (iconaScarto != null) {
                labelScarti.setIcon(iconaScarto);
                labelScarti.setText("");
            } else {
                labelScarti.setText(partita.getCartaInGioco().toString());
            }
        }

        // 3. Aggiorna Mano del Giocatore Corrente
        pannelloMano.removeAll();
        java.util.List<Carta> carte = corrente.getMano().getCarte();
        for (int i = 0; i < carte.size(); i++) {
            Carta c = carte.get(i);
            JButton btn = new JButton();
            ImageIcon iconaCarta = getIconForCarta(c, CARD_WIDTH, CARD_HEIGHT);
            
            if (iconaCarta != null) {
                btn.setIcon(iconaCarta);
                pulisciBottone(btn);
            } else {
                btn.setText(c.toString()); // Fallback testuale
            }

            final int indice = i;
            boolean valida = partita.isMossaValida(c);
            btn.setEnabled(valida);
            if (valida) {
                btn.addActionListener(ev -> giocaCarta(indice));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            pannelloMano.add(btn);
        }

        // 4. Aggiorna Avversari (Carte Coperte)
        pannelloAvversari.removeAll();
        for (Giocatore g : partita.getGiocatori()) {
            if (g != corrente) {
                JPanel oppPanel = new JPanel(new BorderLayout());
                oppPanel.setOpaque(false);
                
                JLabel nameLabel = new JLabel(g.getNome() + " (" + g.getMano().getCarte().size() + ")");
                nameLabel.setForeground(Color.WHITE);
                nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                // Disegna i piccoli dorsi sovrapposti
                JPanel miniCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -15, 0));
                miniCardsPanel.setOpaque(false);
                ImageIcon miniBack = getCardBackIcon(SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT);
                
                for (int i = 0; i < g.getMano().getCarte().size(); i++) {
                    JLabel miniCard = new JLabel(miniBack != null ? miniBack : new ImageIcon());
                    if (miniBack == null) miniCard.setText("[X]");
                    miniCardsPanel.add(miniCard);
                }
                
                oppPanel.add(nameLabel, BorderLayout.NORTH);
                oppPanel.add(miniCardsPanel, BorderLayout.CENTER);
                pannelloAvversari.add(oppPanel);
            }
        }

        revalidate();
        repaint();
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void giocaCarta(int indiceCarta) {
        if (controller != null) controller.gestisciClickCarta(indiceCarta);
    }

    // --- METODI UTILITY PER LE IMMAGINI ---

    private void pulisciBottone(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
    }

    private ImageIcon getIconForCarta(Carta c, int w, int h) {
        String colorFolder = "";
        String fileName = "";

        // Mappatura colore
        switch (c.getColore()) {
            case 0: colorFolder = "red"; break;
            case 1: colorFolder = "yellow"; break;
            case 2: colorFolder = "green"; break;
            case 3: colorFolder = "blue"; break;
            default: colorFolder = "wild"; break;
        }

        // Mappatura Tipo (0=Normale, 1=+2, 2=Reverse, 3=Skip, 4=Wild, 5=+4)
        if (c.getTipo() == 0) {
            fileName = c.getNumero() + "_" + colorFolder + ".png";
        } else if (c.getTipo() == 1) {
            fileName = "2plus_" + colorFolder + ".png";
        } else if (c.getTipo() == 2) {
            fileName = "inverse_" + colorFolder + ".png";
        } else if (c.getTipo() == 3) {
            fileName = "block_" + colorFolder + ".png";
        } else if (c.getTipo() == 4) {
            if (c.getColore() == 4) { // Wild colorato
                colorFolder = "wild";
                fileName = "wild_card_" + c.getColore() + ".png";
            } else { // Wild normale
                colorFolder = "wild";
                fileName = "wild_card.png";
            }
        } else if (c.getTipo() == 5) {
            if (c.getColore() == 4) { // +4 colorato
                colorFolder = "wild";
                fileName = "4_plus_" + c.getColore() + ".png";
            } else { // +4 normale
                colorFolder = "wild";
                fileName = "4_plus_" + c.getColore() + ".png";
            }
        }

        String path = "src/UnoCards/" + colorFolder + "/" + fileName;
        return loadAndScaleImage(path, w, h);
    }

    private ImageIcon getCardBackIcon(int w, int h) {
        String path = "src/UnoCards/card back/card_back.png";
        return loadAndScaleImage(path, w, h);
    }

    private ImageIcon loadAndScaleImage(String path, int w, int h) {
        File file = new File(path);
        if (!file.exists()) {
            return null; // Ritorna null se il file non esiste per usare il testo di fallback
        }
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public void mostraTransizionePassaggio(String nome) {
        setComponentZOrder(overlay, getComponentCount() - 1);
        overlay.setVisible(true);
        labelTransizione.setText(nome + "'s turn - Click to continue");
        revalidate();
        repaint();
    }

    public void nascondiTransizione() {
        overlay.setVisible(false);
        revalidate();
        repaint();
    }
}
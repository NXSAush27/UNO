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

    // Dimensioni costanti per le carte
    private final int CARD_WIDTH = 75;
    private final int CARD_HEIGHT = 110;
    private final int SMALL_CARD_WIDTH = 40;
    private final int SMALL_CARD_HEIGHT = 60;

    public GamePanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());
        setBackground(new Color(20, 110, 40)); // Verde tavolo da gioco profondo

        // --- ZONA NORD: Avversari (Carte coperte) ---
        // Torniamo al FlowLayout perché l'OpponentHandPanel (in fondo) gestirà l'overlap matematicamente
        pannelloAvversari = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        pannelloAvversari.setOpaque(false);
        add(pannelloAvversari, BorderLayout.NORTH);

        // --- ZONA CENTRALE: Tavolo da Gioco (Mazzo e Scarti) ---
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 40));
        centro.setOpaque(false);
        
        labelScarti = new JLabel("", SwingConstants.CENTER);
        labelScarti.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        labelScarti.setOpaque(true);
        labelScarti.setBackground(Color.WHITE);
        labelScarti.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        labelScarti.setFont(new Font("Arial", Font.BOLD, 12));
        centro.add(labelScarti);

        btnPesca = new JButton();
        btnPesca.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        ImageIcon backIcon = getCardBackIcon(CARD_WIDTH, CARD_HEIGHT);
        if (backIcon != null) {
            btnPesca.setIcon(backIcon);
            pulisciBottone(btnPesca);
        } else {
            btnPesca.setText("PESCA");
            btnPesca.setBackground(Color.DARK_GRAY);
            btnPesca.setForeground(Color.WHITE);
            btnPesca.setFont(new Font("Arial", Font.BOLD, 12));
        }
        btnPesca.addActionListener(this::azionePesca);
        centro.add(btnPesca);
        add(centro, BorderLayout.CENTER);

        // --- ZONA IN BASSO: Mano del Giocatore Corrente ---
        JPanel sudPanel = new JPanel(new BorderLayout());
        sudPanel.setOpaque(false);
        
        labelTurno = new JLabel("Turno: --", SwingConstants.CENTER);
        labelTurno.setFont(new Font("Arial", Font.BOLD, 22));
        labelTurno.setForeground(Color.WHITE);
        sudPanel.add(labelTurno, BorderLayout.NORTH);

        pannelloMano = new JPanel(new FlowLayout(FlowLayout.CENTER, -15, 15)); // Ventaglio leggero
        pannelloMano.setOpaque(false);
        sudPanel.add(pannelloMano, BorderLayout.CENTER);
        add(sudPanel, BorderLayout.SOUTH);

       // --- ZONA DESTRA: Storico e Azioni ---
        JPanel pannelloDestro = new JPanel(new BorderLayout());
        pannelloDestro.setBorder(new EmptyBorder(10, 10, 10, 10));
        pannelloDestro.setOpaque(false);

        areaStorico = new JTextArea(20, 30);
        areaStorico.setEditable(false);
        areaStorico.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Aumentato a 4 righe per fare spazio al nuovo bottone
        JPanel bottoniDestra = new JPanel(new GridLayout(4, 1, 0, 8)); 
        bottoniDestra.setOpaque(false);
        
        JButton btnUno = new JButton("Dichiara UNO!");
        btnUno.addActionListener(e -> { if (controller != null) controller.dichiaraUno(); });
        
        // NUOVO PULSANTE PENALITÀ
        JButton btnPenalizza = new JButton("Penalizza (No UNO)");
        btnPenalizza.setBackground(new Color(200, 50, 50)); // Rosso scuro per evidenziarlo
        btnPenalizza.setForeground(Color.WHITE);
        btnPenalizza.addActionListener(e -> { if (controller != null) controller.penalizza(); });

        JButton btnSalva = new JButton("Salva Partita");
        btnSalva.addActionListener(e -> { if (controller != null) controller.salvaPartita(); });
        
        JButton btnMostraLog = new JButton("Vedi Storico");
        btnMostraLog.addActionListener(e -> {
            JDialog dialogLog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Storico Partita", false);
            dialogLog.add(new JScrollPane(areaStorico));
            dialogLog.setSize(400, 400);
            dialogLog.setLocationRelativeTo(this);
            dialogLog.setVisible(true);
        });

        bottoniDestra.add(btnUno);
        bottoniDestra.add(btnPenalizza); // Inserito qui
        bottoniDestra.add(btnSalva);
        bottoniDestra.add(btnMostraLog);

        pannelloDestro.add(bottoniDestra, BorderLayout.SOUTH);
        add(pannelloDestro, BorderLayout.EAST);
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
        areaStorico.setCaretPosition(areaStorico.getDocument().getLength());
    }

    public void aggiornaTavolo(Partita partita) {
        this.partitaCorrente = partita;
        Giocatore corrente = partita.getGiocatoreCorrente();

        labelTurno.setText("Turno di: " + corrente.getNome() + (corrente instanceof model.GiocatoreBot ? " (BOT)" : ""));

        // 1. Aggiorna Pila Scarti
        if (partita.getCartaInGioco() != null) {
            ImageIcon iconaScarto = getIconForCarta(partita.getCartaInGioco(), CARD_WIDTH, CARD_HEIGHT);
            if (iconaScarto != null) {
                labelScarti.setIcon(iconaScarto);
                labelScarti.setText("");
                labelScarti.setBorder(null);
            } else {
                labelScarti.setIcon(null);
                labelScarti.setText("<html><center>" + partita.getCartaInGioco().toString().replace(" ", "<br>") + "</center></html>");
                labelScarti.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }
        }

       // 2. Aggiorna Mano Giocatore (Nasconde le carte se è il turno del Bot)
        pannelloMano.removeAll();
        java.util.List<Carta> carte = corrente.getMano().getCarte();
        boolean isBotTurn = corrente instanceof model.GiocatoreBot;

        for (int i = 0; i < carte.size(); i++) {
            Carta c = carte.get(i);
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            
            if (isBotTurn) {
                // Durante il turno del bot, mostra il dorso delle carte in basso
                ImageIcon iconaDorso = getCardBackIcon(CARD_WIDTH, CARD_HEIGHT);
                if (iconaDorso != null) {
                    btn.setIcon(iconaDorso);
                    pulisciBottone(btn);
                } else {
                    btn.setText("UNO");
                    btn.setBackground(Color.DARK_GRAY);
                }
                btn.setEnabled(false); // Blocca i click dell'umano!
            } else {
                // Durante il turno umano, mostra le carte vere
                ImageIcon iconaCarta = getIconForCarta(c, CARD_WIDTH, CARD_HEIGHT);
                if (iconaCarta != null) {
                    btn.setIcon(iconaCarta);
                    pulisciBottone(btn);
                } else {
                    btn.setText(c.toString());
                    btn.setBackground(Color.WHITE);
                }

                final int indice = i;
                boolean valida = partita.isMossaValida(c);
                btn.setEnabled(valida); // Solo le carte giocabili sono abilitate
                if (valida) {
                    btn.addActionListener(ev -> giocaCarta(indice));
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    // Evidenzia le carte giocabili con un bordo verde
                    btn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                } else {
                    // Le carte non giocabili mostrano un messaggio se si clicca
                    btn.addActionListener(ev -> {
                        // Non fare nulla, l'utente deve pescare
                    });
                    btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                }
            }
            pannelloMano.add(btn);
        }
        
        // Blocca anche il bottone "PESCA" se tocca al Bot
        btnPesca.setEnabled(!isBotTurn);

        // 3. Aggiorna Avversari (Esclude i bot dalla renderizzazione grafica delle carte)
        pannelloAvversari.removeAll();
        for (Giocatore g : partita.getGiocatori()) {
            if (g != corrente) {
                if (g instanceof model.GiocatoreBot) {
                    // Se è un BOT, mostriamo solo una scritta semplice senza mazzo grafico di carte coperte
                    JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    botPanel.setOpaque(false);
                    JLabel labelBot = new JLabel(g.getNome() + " (BOT) - Carte: " + g.getMano().getCarte().size());
                    labelBot.setForeground(Color.LIGHT_GRAY);
                    labelBot.setFont(new Font("Arial", Font.ITALIC, 13));
                    botPanel.add(labelBot);
                    pannelloAvversari.add(botPanel);
                } else {
                    // Se è un umano reale avversario, usiamo il pannello matematico anti-clipping
                    OpponentHandPanel oppPanel = new OpponentHandPanel(
                        g.getNome(), 
                        g.getMano().getCarte().size(), 
                        getCardBackIcon(SMALL_CARD_WIDTH, SMALL_CARD_HEIGHT)
                    );
                    pannelloAvversari.add(oppPanel);
                }
            }
        }
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void giocaCarta(int indiceCarta) {
        if (controller != null) controller.gestisciClickCarta(indiceCarta);
    }

    private void pulisciBottone(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
    }

    // --- LOGICA OVERLAY (GlassPane) A PROVA DI CRASH ---

    public void mostraTransizionePassaggio(String nome) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (topFrame == null) return;

        JPanel glassPane = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 220));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        glassPane.setOpaque(false);

        JLabel labelTransizione = new JLabel("Turno di " + nome + " - Clicca per continuare");
        labelTransizione.setFont(new Font("Arial", Font.BOLD, 30));
        labelTransizione.setForeground(Color.WHITE);
        glassPane.add(labelTransizione);

        glassPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // LA VERA SOLUZIONE AL BLOCCO: 
                // Invece di nascondere solo la grafica, avvisiamo il controller!
                if (controller != null) {
                    controller.confermaPassaggio(); // Questo rimette inTransizione = false e riattiva le carte!
                } else {
                    nascondiTransizione();
                }
            }
        });

        topFrame.setGlassPane(glassPane);
        glassPane.setVisible(true);
    }

    public void nascondiTransizione() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (topFrame != null) {
            Component glass = topFrame.getGlassPane();
            glass.setVisible(false);
            
            // LA VERA FIX: Sostituiamo il glass pane con uno nuovo, vuoto e senza listener.
            // Questo garantisce che il mouse venga "liberato" e i bottoni tornino cliccabili.
            JPanel emptyGlass = new JPanel();
            emptyGlass.setOpaque(false);
            topFrame.setGlassPane(emptyGlass);
        }
    }

    // --- LOGICA IMMAGINI ---

    private ImageIcon getIconForCarta(Carta c, int w, int h) {
        String colorFolder = "";
        String fileName = "";

        switch (c.getColore()) {
            case 0: colorFolder = "red"; break;
            case 1: colorFolder = "yellow"; break;
            case 2: colorFolder = "green"; break;
            case 3: colorFolder = "blue"; break;
            default: colorFolder = "wild"; break;
        }

        if (c.getTipo() == 0) {
            fileName = c.getNumero() + "_" + colorFolder + ".png";
        } else if (c.getTipo() == 1) {
            fileName = "2plus_" + colorFolder + ".png";
        } else if (c.getTipo() == 2) {
            fileName = "inverse_" + colorFolder + ".png";
        } else if (c.getTipo() == 3) {
            fileName = "block_" + colorFolder + ".png";
        } else if (c.getTipo() == 4) {
            colorFolder = "wild";
            String suffix = "red"; 
            if (c.getColore() == 1) suffix = "yellow";
            else if (c.getColore() == 2) suffix = "green";
            else if (c.getColore() == 3) suffix = "blue";
            fileName = (c.getColore() >= 4 || c.getColore() < 0) ? "wild_card.png" : "wild_card_" + suffix + ".png";
        } else if (c.getTipo() == 5) {
            colorFolder = "wild";
            String suffix = "red";
            if (c.getColore() == 1) suffix = "yellow";
            else if (c.getColore() == 2) suffix = "green";
            else if (c.getColore() == 3) suffix = "blue";
            fileName = (c.getColore() >= 4 || c.getColore() < 0) ? "4_plus.png" : "4_plus_" + suffix + ".png";
        }

        return loadAndScaleImage(colorFolder + "/" + fileName, w, h);
    }

    private ImageIcon getCardBackIcon(int w, int h) {
        return loadAndScaleImage("card back/card_back.png", w, h);
    }

    private ImageIcon loadAndScaleImage(String relativePath, int w, int h) {
        java.net.URL imgURL = getClass().getResource("/UnoCards/" + relativePath);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        }

        String[] pathsToTest = {
            "src/UnoCards/" + relativePath,
            "UnoCards/" + relativePath,
            "../src/UnoCards/" + relativePath
        };

        for (String p : pathsToTest) {
            File file = new File(p);
            if (file.exists()) {
                return new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        }
        System.out.println("⚠️ IMMAGINE MANCANTE: " + relativePath);
        return null;
    }

    // --- CLASSE INTERNA ANTI-CLIPPING PER GLI AVVERSARI ---
    class OpponentHandPanel extends JPanel {
        private String name;
        private int numCards;
        private ImageIcon cardBack;
        private final int cardW = SMALL_CARD_WIDTH;
        private final int cardH = SMALL_CARD_HEIGHT;

        public OpponentHandPanel(String name, int numCards, ImageIcon cardBack) {
            this.name = name;
            this.numCards = numCards;
            this.cardBack = cardBack;
            setOpaque(false);
            // La Bounding Box perfetta: larga 180px, alta il necessario per testo e carte.
            setPreferredSize(new Dimension(180, cardH + 35)); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Disegna il Nome e il Numero di carte in alto e centrato
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String text = name + " (" + numCards + ")";
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            g2d.drawString(text, textX, 15);

            // 2. Disegna le carte con l'algoritmo di spacing dinamico
            if (cardBack != null && numCards > 0) {
                int startY = 25;
                
                // Lasciamo un piccolo margine laterale (10px in totale)
                int maxAvailableWidth = getWidth() - 10; 
                // Spazio che divide una carta dall'altra
                int stepX = maxAvailableWidth - cardW;
                
                if (numCards > 1) {
                    stepX = (maxAvailableWidth - cardW) / (numCards - 1);
                }
                
                // Mettiamo un tetto massimo alla distanza: se ha poche carte non devono stare troppo larghe
                if (stepX > 25) stepX = 25; 
                
                // Calcoliamo esattamente quanto spazio prenderà l'intero ventaglio
                int totalWidthUsed = cardW + (numCards - 1) * stepX;
                
                // E lo posizioniamo al centro esatto della Bounding Box
                int startX = (getWidth() - totalWidthUsed) / 2;

                for (int i = 0; i < numCards; i++) {
                    g2d.drawImage(cardBack.getImage(), startX + (i * stepX), startY, cardW, cardH, null);
                }
            }
        }
    }
}
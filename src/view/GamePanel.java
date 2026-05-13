package view;

import controller.GameController;
import model.Carta;
import model.Giocatore;
import model.Mano;
import model.Partita;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GamePanel extends JPanel {
    private MainFrame mainFrame;
    private GameController controller;
    private JTextArea areaStorico;
    private JPanel pannelloMano;
    private JLabel labelScarti;
    private JLabel labelTurno;
    private Partita partitaCorrente; // cached reference for card-action listeners

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

        // --- ZONA DESTRA: Storico, turno e Azioni Extra ---
        JPanel pannelloDestro = new JPanel(new BorderLayout());

        // Turno corrente
        labelTurno = new JLabel("Turno: --");
        pannelloDestro.add(labelTurno, BorderLayout.NORTH);

        // Lo storico mosse [cite: 250-255]
        areaStorico = new JTextArea(15, 20);
        areaStorico.setEditable(false);
        areaStorico.setText("Partita Iniziata...\n");
        JScrollPane scrollStorico = new JScrollPane(areaStorico);
        pannelloDestro.add(scrollStorico, BorderLayout.CENTER);

        // Bottoni per Salvare e dichiarare UNO [cite: 154, 248]
        JPanel bottoniDestra = new JPanel(new GridLayout(2, 1));
        JButton btnUno = new JButton("Dichiara UNO!");
        btnUno.addActionListener(this::azioneDichiaraUno);
        JButton btnSalva = new JButton("Salva Partita");
        btnSalva.addActionListener(this::azioneSalva);
        bottoniDestra.add(btnUno);
        bottoniDestra.add(btnSalva);

        pannelloDestro.add(bottoniDestra, BorderLayout.SOUTH);
        add(pannelloDestro, BorderLayout.EAST);
    }

    /** Called by MainFrame to inject the GameController. */
    public void setController(GameController controller) {
        this.controller = controller;
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

        if (partita.getCartaInGioco() != null) {
            labelScarti.setText("CARTA IN CIMA: " + partita.getCartaInGioco());
        }

        // Aggiorna l'etichetta del turno
        Giocatore corrente = partita.getGiocatoreCorrente();
        labelTurno.setText("Turno: " + corrente.getNome()
                + (corrente instanceof model.GiocatoreBot ? " (BOT)" : ""));

        // Ricostruisce la mano del giocatore corrente con bottoni cliccabili
        pannelloMano.removeAll();
        Mano mano = corrente.getMano();
        List<Carta> carte = mano.getCarte();

        for (int i = 0; i < carte.size(); i++) {
            Carta c = carte.get(i);
            JButton btn = new JButton(c.toString());
            final int indice = i;
            // Solo le carte giocabili (valide) sono cliccabili
            boolean valida = partita.isMossaValida(c);
            btn.setEnabled(valida);
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
}
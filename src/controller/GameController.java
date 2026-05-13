package controller;

import model.*;
import view.*;
import javax.swing.JOptionPane;

public class GameController {
    private Partita partita;
    private GamePanel gamePanel;
    private MainFrame mainFrame;

    public GameController(MainFrame frame, GamePanel panel) {
        this.mainFrame = frame;
        this.gamePanel = panel;
    }

    /**
     * Called by ConfigPanel "Avvia Partita" button.
     * Creates the Partita model, distributes cards, and switches to GAME view.
     */
    public void avviaNuovaPartita(int numGiocatori, int sogliaPunti) {
        // 1. Crea il model
        partita = new Partita(numGiocatori, sogliaPunti);
        partita.distribuisciCarteIniziali();

        // 2. Aggiorna la grafica per il primo turno
        gamePanel.aggiornaTavolo(partita);
        gamePanel.aggiungiLog("Partita iniziata! Turno di: "
                + partita.getGiocatoreCorrente().getNome());

        mainFrame.showPanel("GAME");

        // Se il primo giocatore è un bot, esegui il suo turno
        controllaTurnoBot();
    }

    /**
     * Called by GamePanel when the user clicks a card in hand.
     */
    public void gestisciClickCarta(int indiceCarta) {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();
        // Guarda se l'indice è ancora valido (la mano potrebbe essere cambiata)
        if (indiceCarta < 0 || indiceCarta >= corrente.getMano().getCarte().size()) return;

        Carta cartaScelta = corrente.getMano().get(indiceCarta);

        if (partita.isMossaValida(cartaScelta)) {
            // Eseguiamo la mossa nel Model
            partita.giocaCarta(corrente, cartaScelta);

            gamePanel.aggiungiLog(corrente.getNome() + " ha giocato: " + cartaScelta);

            // Controlla vittoria
            if (partita.verificaVittoria(corrente)) {
                gamePanel.aggiungiLog("🎉 " + corrente.getNome() + " ha vinto la partita!");
                JOptionPane.showMessageDialog(gamePanel,
                        corrente.getNome() + " ha vinto la partita!",
                        "Partita Terminata",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Passa al prossimo turno
            partita.passaTurno();
            gamePanel.aggiornaTavolo(partita);

            controllaTurnoBot();

        } else {
            gamePanel.mostraErrore("Mossa non valida! Devi rispondere al colore o al numero.");
        }
    }

    /**
     * Called by GamePanel "Pesca Carta" button.
     */
    public void gestisciClickPesca() {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();
        partita.pescaCarta(corrente);
        gamePanel.aggiungiLog(corrente.getNome() + " ha pescato una carta.");

        partita.passaTurno();
        gamePanel.aggiornaTavolo(partita);
        controllaTurnoBot();
    }

    /**
     * Called by GamePanel "Salva Partita" button.
     */
    public void salvaPartita() {
        if (partita == null) {
            JOptionPane.showMessageDialog(gamePanel,
                    "Nessuna partita in corso da salvare!",
                    "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }
        partita.salvaPartita();
        gamePanel.aggiungiLog("Partita salvata in savegame.dat");
    }

    /**
     * Called by GamePanel "Dichiara UNO!" button.
     */
    public void dichiaraUno() {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();
        partita.SegnalaUno(corrente);
        gamePanel.aggiungiLog(corrente.getNome() + " ha dichiarato UNO!");
    }

    /**
     * If the current player is a bot, executes its turn after a short delay.
     */
    private void controllaTurnoBot() {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();

        if (corrente instanceof GiocatoreBot) {
            javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
                Carta mossa = ((GiocatoreBot) corrente).decidiMossa(partita);
                if (mossa != null && partita.isMossaValida(mossa)) {
                    partita.giocaCarta(corrente, mossa);
                    gamePanel.aggiungiLog(corrente.getNome()
                            + " (BOT) ha giocato: " + mossa);
                } else {
                    partita.pescaCarta(corrente);
                    gamePanel.aggiungiLog(corrente.getNome()
                            + " (BOT) ha pescato.");
                }

                if (partita.verificaVittoria(corrente)) {
                    gamePanel.aggiungiLog("🎉 " + corrente.getNome()
                            + " ha vinto la partita!");
                    JOptionPane.showMessageDialog(gamePanel,
                            corrente.getNome() + " ha vinto la partita!",
                            "Partita Terminata",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                partita.passaTurno();
                gamePanel.aggiornaTavolo(partita);
                controllaTurnoBot();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}
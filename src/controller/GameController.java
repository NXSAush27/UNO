package controller;

import model.*;
import view.*;
import javax.swing.JOptionPane;

public class GameController {
    private Partita partita;
    private GamePanel gamePanel;
    private MainFrame mainFrame;
    private boolean inTransizione = false;

    public GameController(MainFrame frame, GamePanel panel) {
        this.mainFrame = frame;
        this.gamePanel = panel;
    }

    /**
     * Called by ConfigPanel "Avvia Partita" button.
     * Creates the Partita model, distributes cards, and switches to GAME view.
     */
public void avviaNuovaPartita(Giocatore[] giocatori, int sogliaPunti) {
         // 1. Crea il model
         partita = new Partita(giocatori, sogliaPunti);
         partita.distribuisciCarteIniziali();

         // 2. Inietta GamePanel nei bot player
         for (Giocatore g : giocatori) {
             if (g instanceof GiocatoreBot bot) {
                 bot.setGamePanel(gamePanel);
             }
         }

         // 3. Aggiorna la grafica per il primo turno
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

        if (inTransizione) return; // Ignore clicks during transition overlay

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
            boolean currentWasHuman = corrente instanceof GiocatoreUmano;
            partita.passaTurno();
            Giocatore next = partita.getGiocatoreCorrente();

            // Update the UI BEFORE showing overlay (so hand reflects played card)
            gamePanel.aggiornaTavolo(partita);

            // Check for hotseat transition: next is human AND previous was human
            if (next instanceof GiocatoreUmano && currentWasHuman) {
                inTransizione = true;
                gamePanel.mostraTransizionePassaggio(next.getNome());
                return;
            }

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

        if (inTransizione) return; // Ignore during overlay

        Giocatore corrente = partita.getGiocatoreCorrente();
        boolean currentWasHuman = corrente instanceof GiocatoreUmano;
        partita.pescaCarta(corrente);
        gamePanel.aggiungiLog(corrente.getNome() + " ha pescato una carta.");

        partita.passaTurno();

        Giocatore next = partita.getGiocatoreCorrente();
        
        // Update the UI BEFORE showing overlay
        gamePanel.aggiornaTavolo(partita);
        
        if (next instanceof GiocatoreUmano && currentWasHuman) {
            inTransizione = true;
            gamePanel.mostraTransizionePassaggio(next.getNome());
            return;
        }

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

    public void caricaPartita() {
        try {
            Partita caricata = Partita.caricaPartita("savegame.dat");
            if (caricata != null) {
                this.partita = caricata;

                // Re-inject GamePanel into any bot players
                for (Giocatore g : partita.getGiocatori()) {
                    if (g instanceof GiocatoreBot bot) {
                        bot.setGamePanel(gamePanel);
                    }
                }

                gamePanel.aggiornaTavolo(partita);
                gamePanel.aggiungiLog("Partita caricata da savegame.dat");
                mainFrame.showPanel("GAME");

                controllaTurnoBot();
            } else {
                JOptionPane.showMessageDialog(gamePanel,
                        "Nessuna partita salvata trovata!",
                        "Errore", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gamePanel,
                    "Errore nel caricamento: " + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called by GamePanel "Dichiara UNO!" button.
     */
    public void dichiaraUno() {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();
        int handSize = corrente.getMano().getCarte().size();
        partita.SegnalaUno(corrente);
        gamePanel.aggiungiLog(corrente.getNome()
                + " ha dichiarato UNO! (carte=" + handSize + ")");
    }

    /**
     * Called by GamePanel when overlay is clicked to confirm player change.
     */
    public void confermaPassaggio() {
        inTransizione = false;
        gamePanel.nascondiTransizione();
        gamePanel.aggiornaTavolo(partita);
        controllaTurnoBot();
    }

    /**
     * If the current player is a bot, executes its turn after a short delay.
     */
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
                // NOT calling controllaTurnoBot() here recursively.
                // The next trigger comes ONLY from aggiornaTavolo via the
                // human click path (gestisciClickCarta / gestisciClickPesca /
                // confermaPassaggio). This gives the human time to think.
            });
            timer.setRepeats(false);
            System.out.println("[BOT] Starting timer for " + corrente.getNome());
            timer.start();
        }
    }
}
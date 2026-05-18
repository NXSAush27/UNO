package controller;

import model.*;
import view.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameController {
    private Partita partita;
    private GamePanel gamePanel;
    private MainFrame mainFrame;
    private boolean inTransizione = false;

    // Variabili per il sistema "Dichiara UNO" e "Penalizza"
    private boolean unoDichiaratoInAnticipo = false;
    private Giocatore giocatoreVulnerabile = null;

    public GameController(MainFrame frame, GamePanel panel) {
        this.mainFrame = frame;
        this.gamePanel = panel;
    }

    public void avviaNuovaPartita(Giocatore[] giocatori, int sogliaPunti) {
        partita = new Partita(giocatori, sogliaPunti);
        partita.distribuisciCarteIniziali();

        for (Giocatore g : giocatori) {
            if (g instanceof GiocatoreBot bot) bot.setGamePanel(gamePanel);
        }

        unoDichiaratoInAnticipo = false;
        giocatoreVulnerabile = null;

        gamePanel.aggiornaTavolo(partita);
        gamePanel.aggiungiLog("Partita iniziata! Turno di: " + partita.getGiocatoreCorrente().getNome());
        mainFrame.showPanel("GAME");
        controllaTurnoBot();
    }

    public void gestisciClickCarta(int indiceCarta) {
        if (partita == null || inTransizione) return;

        try {
            Giocatore corrente = partita.getGiocatoreCorrente();
            if (corrente instanceof GiocatoreBot) return;

            if (indiceCarta < 0 || indiceCarta >= corrente.getMano().getCarte().size()) return;
            Carta cartaScelta = corrente.getMano().get(indiceCarta);

            if (partita.isMossaValida(cartaScelta)) {
                if (giocatoreVulnerabile != null && giocatoreVulnerabile != corrente) {
                    giocatoreVulnerabile = null;
                }

                int cartePrima = corrente.getMano().getCarte().size();

                // --- FEATURE ILLUMINA MANO ---
                // Se la carta è un Jolly (4) o un +4 (5), accendi tutta la mano prima di bloccare lo schermo!
                if (cartaScelta.getTipo() == 4 || cartaScelta.getTipo() == 5) {
                    gamePanel.illuminaTutteLeCarte();
                }
                // -----------------------------

                partita.giocaCarta(corrente, cartaScelta);
                gamePanel.aggiungiLog(corrente.getNome() + " ha giocato: " + cartaScelta);

                if (cartePrima == 2) {
                    if (!unoDichiaratoInAnticipo) {
                        giocatoreVulnerabile = corrente; 
                    }
                }
                unoDichiaratoInAnticipo = false; 

                if (partita.verificaVittoria(corrente)) {
                    mostraVittoria(corrente);
                    return;
                }

                boolean currentWasHuman = corrente instanceof GiocatoreUmano;
                partita.passaTurno();
                Giocatore next = partita.getGiocatoreCorrente();

                // Questo aggiornaTavolo finale resetterà automaticamente la mano, 
                // re-ingrigendo le carte non giocabili in base al colore che hai appena scelto!
                gamePanel.aggiornaTavolo(partita);
                inTransizione = false; 

                if (next instanceof GiocatoreUmano && currentWasHuman && corrente != next) {
                    inTransizione = true;
                    gamePanel.mostraTransizionePassaggio(next.getNome());
                    return;
                }

                if (corrente == next && currentWasHuman) {
                    gamePanel.aggiungiLog("🔄 HAI BLOCCATO L'AVVERSARIO! Se non hai carte giocabili, clicca PESCA.");
                    return; 
                }

                controllaTurnoBot();
            } else {
                gamePanel.mostraErrore("Mossa non valida! Devi rispondere al colore o al numero.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            gamePanel.mostraErrore("CRASH NEL MOTORE DEL GIOCO:\n" + e.getMessage());
        }
    }

    public void gestisciClickPesca() {
        if (partita == null || inTransizione) return;

        try {
            Giocatore corrente = partita.getGiocatoreCorrente();
            if (corrente instanceof GiocatoreBot) return; 

            if (giocatoreVulnerabile != null && giocatoreVulnerabile != corrente) {
                giocatoreVulnerabile = null;
            }

            partita.pescaCarta(corrente);
            unoDichiaratoInAnticipo = false; 
            
            Carta pescata = corrente.getMano().getCarte().get(corrente.getMano().getCarte().size() - 1);
            
            if (partita.isMossaValida(pescata)) {
                JOptionPane.showMessageDialog(gamePanel,
                        "Hai pescato:\n" + pescata.toString() + "\n\nÈ giocabile! Viene giocata automaticamente.",
                        "Colpo di Fortuna! ⚡",
                        JOptionPane.INFORMATION_MESSAGE);
                
                // --- FEATURE ILLUMINA MANO ---
                if (pescata.getTipo() == 4 || pescata.getTipo() == 5) {
                    gamePanel.illuminaTutteLeCarte();
                }
                // -----------------------------

                partita.giocaCarta(corrente, pescata);
                gamePanel.aggiungiLog("⚡ " + corrente.getNome() + " ha pescato e giocato subito: " + pescata);

                if (partita.verificaVittoria(corrente)) {
                    mostraVittoria(corrente);
                    return;
                }
            } else {
                gamePanel.aggiungiLog(corrente.getNome() + " ha pescato una carta non giocabile e passa.");
            }

            boolean currentWasHuman = corrente instanceof GiocatoreUmano;
            partita.passaTurno();
            Giocatore next = partita.getGiocatoreCorrente();
            
            gamePanel.aggiornaTavolo(partita);
            
            if (next instanceof GiocatoreUmano && currentWasHuman && corrente != next) {
                inTransizione = true;
                gamePanel.mostraTransizionePassaggio(next.getNome());
                return;
            }

            if (corrente == next && currentWasHuman) {
                gamePanel.aggiungiLog("🔄 HAI BLOCCATO L'AVVERSARIO CON LA CARTA PESCATA! Tocca di nuovo a te!");
                return; 
            }

            controllaTurnoBot();
        } catch (Exception e) {
            e.printStackTrace();
            gamePanel.mostraErrore("CRASH NEL PESCARE:\n" + e.getMessage());
        }
    }

    public void dichiaraUno() {
        if (partita == null) return;
        Giocatore corrente = partita.getGiocatoreCorrente();
        if (corrente instanceof GiocatoreBot) return;

        int carteInMano = corrente.getMano().getCarte().size();

        // FIX FIX FIX: Modificata la gestione per rispecchiare la logica a 2 carte
        if (carteInMano == 2) {
            corrente.setDettoUno(true); // Imposta direttamente il flag per Partita.java senza innescare la penalità
            unoDichiaratoInAnticipo = true;
            gamePanel.aggiungiLog(corrente.getNome() + " ha pre-dichiarato UNO!");
        } else if (carteInMano == 1) {
            partita.SegnalaUno(corrente);
            if (giocatoreVulnerabile == corrente) {
                giocatoreVulnerabile = null; 
            }
            gamePanel.aggiungiLog(corrente.getNome() + " ha dichiarato UNO!");
        } else {
            partita.SegnalaUno(corrente); // Penalità automatica se premuto con troppe carte
            gamePanel.aggiungiLog(corrente.getNome() + " ha urlato UNO a vuoto ed è stato penalizzato.");
        }
        gamePanel.aggiornaTavolo(partita);
    }

    public void penalizza() {
        if (partita == null) return;
        if (giocatoreVulnerabile != null) {
            gamePanel.aggiungiLog("🚨 GOTCHA! " + giocatoreVulnerabile.getNome() + " penalizzato per non aver detto UNO!");
            partita.pescaCarta(giocatoreVulnerabile);
            partita.pescaCarta(giocatoreVulnerabile);
            giocatoreVulnerabile = null; 
            gamePanel.aggiornaTavolo(partita);
        } else {
            gamePanel.aggiungiLog("Nessuno è penalizzabile in questo momento.");
        }
    }

    public void salvaPartita() {
        if (partita == null) return;
        partita.salvaPartita();
        gamePanel.aggiungiLog("Partita salvata con successo.");
    }

    public void caricaPartita() {
        try {
            Partita caricata = Partita.caricaPartita("savegame.dat");
            if (caricata != null) {
                this.partita = caricata;
                for (Giocatore g : partita.getGiocatori()) {
                    if (g instanceof GiocatoreBot bot) bot.setGamePanel(gamePanel);
                }
                unoDichiaratoInAnticipo = false;
                giocatoreVulnerabile = null;
                gamePanel.aggiornaTavolo(partita);
                gamePanel.aggiungiLog("Partita caricata da savegame.dat");
                mainFrame.showPanel("GAME");
                controllaTurnoBot();
            }
        } catch (Exception e) {
            gamePanel.mostraErrore("Errore nel caricamento: " + e.getMessage());
        }
    }

    public void confermaPassaggio() {
        inTransizione = false;
        gamePanel.nascondiTransizione();
        gamePanel.aggiornaTavolo(partita);
        controllaTurnoBot();
    }

    private void mostraVittoria(Giocatore vincitore) {
        gamePanel.aggiungiLog("🎉 " + vincitore.getNome() + " ha vinto la partita!");
        JOptionPane.showMessageDialog(gamePanel,
                vincitore.getNome() + " ha vinto la partita!",
                "Partita Terminata",
                JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showPanel("MENU");
    }

    private void controllaTurnoBot() {
        if (partita == null) return;

        Giocatore corrente = partita.getGiocatoreCorrente();

        if (corrente instanceof GiocatoreBot) {
            javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
                try {
                    if (giocatoreVulnerabile != null && giocatoreVulnerabile != corrente) {
                        giocatoreVulnerabile = null;
                    }

                    int cartePrima = corrente.getMano().getCarte().size();
                    Carta mossa = ((GiocatoreBot) corrente).decidiMossa(partita);
                    
                    if (mossa != null && partita.isMossaValida(mossa)) {
                        partita.giocaCarta(corrente, mossa);
                        gamePanel.aggiungiLog(corrente.getNome() + " (BOT) ha giocato: " + mossa);
                        
                        if (cartePrima == 2) {
                            partita.SegnalaUno(corrente); 
                            gamePanel.aggiungiLog(corrente.getNome() + " (BOT) ha dichiarato UNO!");
                        }
                    } else {
                        partita.pescaCarta(corrente);
                        Carta pescata = corrente.getMano().getCarte().get(corrente.getMano().getCarte().size() - 1);
                        if (partita.isMossaValida(pescata)) {
                            partita.giocaCarta(corrente, pescata);
                            gamePanel.aggiungiLog("⚡ " + corrente.getNome() + " (BOT) ha pescato e giocato subito: " + pescata);
                        } else {
                            gamePanel.aggiungiLog(corrente.getNome() + " (BOT) ha pescato e passa.");
                        }
                    }

                    if (partita.verificaVittoria(corrente)) { 
                        mostraVittoria(corrente);
                        return;
                    }
                    
                    partita.passaTurno();
                    gamePanel.aggiornaTavolo(partita);
                    
                    Giocatore next = partita.getGiocatoreCorrente();
                    
                    if (corrente == next) {
                        gamePanel.aggiungiLog("🔄 " + corrente.getNome() + " (BOT) gioca di nuovo!");
                        controllaTurnoBot();
                    } else if (next instanceof GiocatoreBot) {
                        controllaTurnoBot();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    partita.passaTurno();
                    gamePanel.aggiornaTavolo(partita);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}
package model;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import utils.App;

public class Partita implements Serializable {
    private static final long serialVersionUID = 1L;
    Giocatore[] giocatori;
    Mazzo mazzo;
    Stack<Carta> pilascarti;
    Carta cartaInGioco;
    boolean direzioneGioco; // true = orario, false = antiorario
    int turno; // Indice del giocatore attivo
    int sogliaPunti;

    public Partita(Giocatore[] giocatori, int sogliaPunti) {
        this.giocatori = giocatori;
        this.mazzo = new Mazzo(108);
        this.pilascarti = new Stack<>();
        this.cartaInGioco = null;
        this.direzioneGioco = true;
        this.turno = 0;
        this.sogliaPunti = sogliaPunti;
        for (int i = 0; i < Math.random() * 100; i++) {
            this.mazzo.mescola();
        }
    }

    public void inizializzaMano() {
        for (Giocatore giocatore : giocatori) {
            for (int i = 0; i < 7; i++) {
                pescaCarta(giocatore);
            }
        }
        cartaInGioco = mazzo.getCarte().get(0);
        mazzo.getCarte().remove(0);
    }

    // Backwards-compatible alias
    public void distribuisciCarteIniziali() {
        inizializzaMano();
    }

    public Giocatore getGiocatoreCorrente() {
        return giocatori[turno];
    }

    public Carta getCartaInGioco() {
        return cartaInGioco;
    }

    public Giocatore[] getGiocatori() {
        return giocatori;
    }

    public boolean isMossaValida(Carta carta) {
        return verificaMossaValida(carta);
    }

    public void giocaCarta(Giocatore giocatore, Carta carta) {
        int posizione = giocatore.getMano().getCarte().indexOf(carta);
        if (posizione >= 0) {
            giocaCarta(giocatore, posizione);
        } else {
            List<Carta> list = giocatore.getMano().getCarte();
            for (int i = 0; i < list.size(); i++) {
                Carta c = list.get(i);
                if (c.getNumero() == carta.getNumero()
                        && c.getColore() == carta.getColore()
                        && c.getTipo() == carta.getTipo()) {
                    giocaCarta(giocatore, i);
                    return;
                }
            }
            System.err.println("Carta non trovata nella mano: " + carta + " - Mano: " + list);
        }
    }

    /**
     * Advance one turn. If the next player is marked as stunned (haSaltato)
     * they are skipped and the flag is consumed immediately.
     */
    public void passaTurno() {
        boolean trovato = false;
        int safeCounter = 0; // Evita loop infiniti assoluti
        
        while (!trovato && safeCounter < giocatori.length * 2) {
            safeCounter++;
            
            // 1. Avanza fisicamente di una sedia
            if (direzioneGioco) {
                turno = (turno + 1) % giocatori.length;
            } else {
                turno = (turno - 1 < 0) ? giocatori.length - 1 : turno - 1;
            }
            
            // 2. Controlla se la persona su questa sedia è stordita (bloccata)
            if (giocatori[turno].isHaSaltato()) {
                // Se è bloccato, consumiamo il blocco...
                giocatori[turno].setHaSaltato(false); 
                // ...e il ciclo riparte, facendo un altro passo avanti!
            } else {
                // Trovato un giocatore libero
                trovato = true; 
            }
        }
    }

    // Overload used by applicaEffettoCarta — only sets the flag, does NOT advance
    public void PassaTurno(Giocatore giocatore) {
        giocatore.setHaSaltato(true);
    }

    public void giocaCarta(Giocatore giocatore, int posizioneCarta) {
        Carta cartaGiocata = giocatore.getMano().getCarte().get(posizioneCarta);
        System.out.println("DEBUG giocaCarta: carta=" + cartaGiocata + ", posizione=" + posizioneCarta);
        System.out.println("DEBUG verificaMossaValida=" + verificaMossaValida(cartaGiocata) + ", cartaInGioco=" + cartaInGioco);
        if (verificaMossaValida(cartaGiocata)) {
            Mano mano = giocatore.getMano();

            giocatore.rimuoviCartaAPosizione(posizioneCarta);
            pilascarti.push(cartaInGioco);
            cartaInGioco = cartaGiocata;
            System.out.println("DEBUG carta rimossa, carte in mano: " + mano.getCarte().size());

            // UNO / penalità
            if (mano.getCarte().size() == 1 && !giocatore.isDettoUno()) {
                penalizzaGiocatore(giocatore);
            }

            if (cartaGiocata.getTipo() != 0) {
                applicaEffettoCarta(giocatore, cartaGiocata);
            }

            // Reset UNO flag AFTER the penalty check
            giocatore.setDettoUno(false);
            giocatore.provaDichiaraUno(this);
        } else {
            System.out.println("DEBUG Mossa NON valida!");
        }
    }

    public void pescaCarta(Giocatore giocatore) {
        // SISTEMA DI RICARICA MAZZO AUTOMATICA
        if (mazzo.getCarte().isEmpty()) {
            if (pilascarti.isEmpty()) {
                System.out.println("Mazzo e scarti vuoti! Nessuna carta disponibile.");
                return; // Evita crash nei rarissimi casi in cui tutte le 108 carte sono in mano
            }
            
            // Travasa tutti gli scarti nel mazzo vuoto
            mazzo.getCarte().addAll(pilascarti);
            pilascarti.clear(); // Svuota gli scarti (cartaInGioco rimane intatta sul tavolo)
            
            // Rimescola il nuovo mazzo
            java.util.Collections.shuffle(mazzo.getCarte());
            System.out.println("🔄 IL MAZZO È FINITO: Gli scarti sono stati rimescolati per formare un nuovo mazzo!");
        }

        // Procedura standard di pescaggio
        if (!mazzo.getCarte().isEmpty()) {
            Carta cartaPescata = mazzo.getCarte().get(0);
            giocatore.aggiungiCarta(cartaPescata);
            System.out.println(giocatore.getNome() + " ha pescato: " + cartaPescata.toString());
            mazzo.getCarte().remove(0);
        }
    }

    public void applicaEffettoCarta(Giocatore giocatore, Carta carta) {
        int bersaglio;
        if (direzioneGioco) {
            bersaglio = (turno + 1) % giocatori.length;
        } else {
            bersaglio = (turno - 1 < 0) ? giocatori.length - 1 : turno - 1;
        }

        switch (carta.getTipo()) {
            case 1: // +2
                for (int i = 0; i < 2; i++) {
                    pescaCarta(giocatori[bersaglio]);
                }
                giocatori[bersaglio].setHaSaltato(true); // Stordisce
                break;
            case 2: // Inverti
                direzioneGioco = !direzioneGioco;
                // REGOLE UFFICIALI UNO: In 2 giocatori, l'Inverti vale come Blocco!
                if (giocatori.length == 2) {
                    giocatori[bersaglio].setHaSaltato(true);
                }
                break;
            case 3: // Salta (Blocco)
                giocatori[bersaglio].setHaSaltato(true); // Stordisce
                break;
            case 4: // Jolly
                pilascarti.pop();
                int coloreJolly = giocatore.scegliColore(this);
                cartaInGioco = new Carta(0, coloreJolly, 4);
                break;
            case 5: // +4
                for (int i = 0; i < 4; i++) {
                    pescaCarta(giocatori[bersaglio]);
                }
                giocatori[bersaglio].setHaSaltato(true); // Stordisce
                pilascarti.pop();
                int colorePlus4 = giocatore.scegliColore(this);
                cartaInGioco = new Carta(0, colorePlus4, 5);
                break;
        }
    }

    public void applicaEffettoCartaInizio(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2 — il giocatore 0 pesca due
                for (int i = 0; i < 2; i++) {
                    pescaCarta(giocatori[0]);
                }
                giocatori[0].setHaSaltato(true); // IL GIOCO ORA SALTA L'AVVERSARIO
                break;
            case 2: // Inverti
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta — giocatore 0 stunnato
                giocatori[0].setHaSaltato(true);
                break;
            case 4: // Jolly
                pilascarti.pop();
                Carta cartacoloreScelto = new Carta(0, App.scegliColore(), 4);
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4 — giocatore 0 pesca 4 e viene stunnato
                for (int i = 0; i < 4; i++) {
                    pescaCarta(giocatori[0]);
                }
                giocatori[0].setHaSaltato(true);
                pilascarti.pop();
                Carta cartacoloreScelto4 = new Carta(0, App.scegliColore(), 5);
                cartaInGioco = cartacoloreScelto4;
                break;
        }
    }

    public boolean verificaVittoria(Giocatore giocatore) {
        return giocatore.getMano().getCarte().isEmpty();
    }

    public void salvaPartita(String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
        }
    }

    // Convenience overload — saves to default file
    public void salvaPartita() {
        try {
            salvaPartita("savegame.dat");
        } catch (IOException e) {
            System.err.println("Errore salvataggio: " + e.getMessage());
        }
    }

    public static Partita caricaPartita(String filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Partita) ois.readObject();
        }
    }

    public void iniziaPartita() {
        inizializzaMano();
        applicaEffettoCartaInizio(cartaInGioco);
        CicloGioco();
    }

    public void terminaPartita(Giocatore giocatore) {
        System.out.println("Il giocatore:" + giocatore.getNome() + " ha vinto!!");
    }

    public boolean verificaMossaValida(Carta cartaGiocata) {
        return cartaInGioco == null
            // wild / +4 always playable
            || cartaGiocata.getTipo() >= 4
            // match colour
            || cartaGiocata.getColore() == cartaInGioco.getColore()
            // same face value on number cards, OR same action type on action cards
            || (cartaGiocata.getTipo() == 0
                ? cartaGiocata.getNumero() == cartaInGioco.getNumero()
                : cartaGiocata.getTipo() == cartaInGioco.getTipo());
    }

    public void SegnalaUno(Giocatore giocatore) {
        if (giocatore.getMano().getCarte().size() == 1) {
            giocatore.setDettoUno(true);
        } else {
            penalizzaGiocatore(giocatore);
        }
    }

    public void penalizzaGiocatore(Giocatore giocatore) {
        // Sfruttiamo pescaCarta così abbiamo la ricarica del mazzo in automatico
        pescaCarta(giocatore);
        pescaCarta(giocatore); // La penalità ufficiale dell'UNO è di 2 carte
    }

    public void gestisciTurno(Giocatore giocatore) {
        if (giocatore.getMano().getCarte().isEmpty()) {
            return;
        }
        if (giocatore.isHaSaltato()) {
            System.out.println("Il giocatore " + giocatore.getNome() + " salta il turno!");
            giocatore.setHaSaltato(false);
            return;
        }
        giocatore.setHaGiocato(false);
        System.out.println("Turno di " + giocatore.getNome());
        System.out.println("Carta in gioco: " + cartaInGioco);

        Carta cartaScelta = giocatore.decidiMossa(this);
        if (cartaScelta != null) {
            if (verificaMossaValida(cartaScelta)) {
                giocaCarta(giocatore, cartaScelta);
            } else {
                pescaCarta(giocatore);
            }
        } else {
            pescaCarta(giocatore);
        }
        giocatore.setHaGiocato(true);
    }

    public void CicloGioco() {
        while (true) {
            boolean prevDirezione = direzioneGioco;
            if (direzioneGioco) {
                for (int i = turno; i < giocatori.length; i++) {
                    if (prevDirezione != direzioneGioco) break;
                    gestisciTurno(giocatori[turno]);
                    if (verificaVittoria(giocatori[turno])) {
                        terminaPartita(giocatori[turno]);
                        return;
                    }
                    turno = (turno + 1) % giocatori.length;
                }
            } else {
                for (int i = giocatori.length - turno; i >= 0; i--) {
                    if (prevDirezione != direzioneGioco) break;
                    gestisciTurno(giocatori[turno]);
                    if (verificaVittoria(giocatori[turno])) {
                        terminaPartita(giocatori[turno]);
                        return;
                    }
                    turno = (turno - 1 < 0) ? giocatori.length - 1 : turno - 1;
                }
            }
        }
    }
}

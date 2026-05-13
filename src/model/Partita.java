package model;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
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

    private int sogliaPunti;

    public Partita(Giocatore[] giocatori) {
        this.giocatori = giocatori;
        this.mazzo = new Mazzo(108);
        this.pilascarti = new Stack<>();
        this.cartaInGioco = null;
        this.direzioneGioco = true;
        this.turno = 0;
        this.sogliaPunti = 500;
        for (int i = 0; i < Math.random() * 100; i++) {
            this.mazzo.mescola();
        }
    }

    public Partita(int numGiocatori, int sogliaPunti) {
        this(creaGiocatoriDefault(numGiocatori), sogliaPunti);
    }

    private static Giocatore[] creaGiocatoriDefault(int numGiocatori) {
        Giocatore[] arr = new Giocatore[numGiocatori];
        for (int i = 0; i < numGiocatori; i++) {
            arr[i] = (i == 0) ? new GiocatoreUmano("Giocatore " + (i+1))
                              : new GiocatoreBot("Bot " + i);
        }
        return arr;
    }

    public void distribuisciCarteIniziali() {
        for (Giocatore giocatore : giocatori) {
            for (int i = 0; i < 7; i++) {
                pescaCarta(giocatore);
            }
        }
        cartaInGioco = mazzo.getCarte().get(0);
        mazzo.getCarte().remove(0);
    }

    public Giocatore getGiocatoreCorrente() {
        return giocatori[turno];
    }

    public boolean isMossaValida(Carta carta) {
        return cartaInGioco == null || carta.getColore() == cartaInGioco.getColore()
            || carta.getNumero() == cartaInGioco.getNumero()
            || carta.getTipo() == 4 || carta.getTipo() == 5;
    }

    public void giocaCarta(Giocatore giocatore, Carta carta) {
        int posizione = giocatore.getMano().getCarte().indexOf(carta);
        if (posizione >= 0) {
            giocaCarta(giocatore, posizione);
        }
    }

    public void passaTurno() {
        turno = (turno + 1) % giocatori.length;
    }

    public void salvaPartita(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(mazzo);
            oos.writeObject(pilascarti);
            oos.writeObject(cartaInGioco);
            oos.writeBoolean(direzioneGioco);
            oos.writeInt(turno);
            oos.writeInt(giocatori.length);
            for (Giocatore g : giocatori) {
                oos.writeObject(g);
            }
            System.out.println("Partita salvata con successo!");
        } catch (IOException e) {
            System.out.println("Errore durante il salvataggio: " + e.getMessage());
        }
    }

    public void salvaPartita() {
        salvaPartita("savegame.dat");
    }

    public Partita caricaPartita(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.mazzo = (Mazzo) ois.readObject();
            this.pilascarti = (Stack<Carta>) ois.readObject();
            this.cartaInGioco = (Carta) ois.readObject();
            this.direzioneGioco = ois.readBoolean();
            this.turno = ois.readInt();
            int numGiocatori = ois.readInt();
            this.giocatori = new Giocatore[numGiocatori];
            for (int i = 0; i < numGiocatori; i++) {
                this.giocatori[i] = (Giocatore) ois.readObject();
            }
            System.out.println("Partita caricata con successo!");
            return this;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore durante il caricamento: " + e.getMessage());
            return null;
        }
    }

    public Partita caricaPartita() {
        return caricaPartita("savegame.dat");
    }

    public Giocatore[] getGiocatori() {
        return giocatori;
    }

    public Mazzo getMazzo() {
        return mazzo;
    }

    public Stack<Carta> getPilascarti() {
        return pilascarti;
    }

    public Carta getCartaInGioco() {
        return cartaInGioco;
    }

    public boolean isDirezioneGioco() {
        return direzioneGioco;
    }

    public void setCartaInGioco(Carta cartaInGioco) {
        this.cartaInGioco = cartaInGioco;
    }

    public void setDirezioneGioco(boolean direzioneGioco) {
        this.direzioneGioco = direzioneGioco;
    }

    public int getTurno() {
        return turno;
    }

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

    public void giocaCarta(Giocatore giocatore, int posizioneCarta) {
        Carta cartaGiocata = giocatore.getMano().getCarte().get(posizioneCarta);
        if (verificaMossaValida(cartaGiocata)) {
            giocatore.rimuoviCartaAPosizione(posizioneCarta);
            pilascarti.push(cartaInGioco);
            cartaInGioco = cartaGiocata;
            // Allow player to declare UNO (bot auto, human no-op unless button pressed earlier)
            giocatore.provaDichiaraUno(this);
            if (giocatore.getMano().getCarte().size() == 1 && !giocatore.isDettoUno()) {
                penalizzaGiocatore(giocatore);
            }
            if (cartaGiocata.getTipo() != 0) {
                applicaEffettoCarta(giocatore, cartaGiocata);
            }
        }
    }

    public void pescaCarta(Giocatore giocatore) {
        if (mazzo.getCarte().size() > 0) {
            Carta cartaPescata = mazzo.getCarte().get(0);
            giocatore.aggiungiCarta(cartaPescata);
            System.out.println(giocatore.getNome() + " ha pescato: " + cartaPescata.toString());
            mazzo.getCarte().remove(0);
        }
    }

    public void passaTurno(Giocatore giocatore) {
        giocatore.setHaGiocato(true);
    }

    public void applicaEffettoCarta(Giocatore giocatore, Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                if (direzioneGioco) {
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[(turno + 1) % giocatori.length]);
                    }
                } else {
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]);
                    }
                }
                break;
            case 2: // Inverti
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta
                if (direzioneGioco) {
                    PassaTurno(giocatori[(turno + 1) % giocatori.length]);
                } else {
                    PassaTurno(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]);
                }
                break;
            case 4: // Jolly
                pilascarti.pop();
                int coloreJolly = giocatore.scegliColore(this);
                Carta cartacoloreScelto = new Carta(0, coloreJolly, 4);
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4
                if (direzioneGioco) {
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[(turno + 1) % giocatori.length]);
                    }
                    PassaTurno(giocatori[(turno + 1) % giocatori.length]);
                } else {
                    int prevIndex = turno - 1 < 0 ? giocatori.length - 1 : turno - 1;
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[prevIndex]);
                    }
                    PassaTurno(giocatori[prevIndex]);
                }
                pilascarti.pop();
                int colorePlus4 = giocatore.scegliColore(this);
                Carta cartacoloreScelto4 = new Carta(0, colorePlus4, 4);
                cartaInGioco = cartacoloreScelto4;
                break;
        }
    }

    public void applicaEffettoCartaInizio(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                for (int i = 0; i < 2; i++) {
                    pescaCarta(giocatori[0]);
                }
                PassaTurno(giocatori[0]);
                break;
            case 2: // Inverti
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta
                PassaTurno(giocatori[0]);
                break;
            case 4: // Jolly
                pilascarti.pop();
                Carta cartacoloreScelto = new Carta(0, App.scegliColore(), 4);
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4
                if (direzioneGioco) {
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[(turno + 1) % giocatori.length]);
                    }
                    PassaTurno(giocatori[(turno + 1) % giocatori.length]);
                } else {
                    int prevIndex = turno - 1 < 0 ? giocatori.length - 1 : turno - 1;
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[prevIndex]);
                    }
                    PassaTurno(giocatori[prevIndex]);
                }
                pilascarti.pop();
                Carta cartacoloreScelto4 = new Carta(0, App.scegliColore(), 4);
                cartaInGioco = cartacoloreScelto4;
                break;
        }
    }

    public boolean verificaVittoria(Giocatore giocatore) {
        return giocatore.getMano().getCarte().isEmpty();
    }

    public void iniziaPartita() {
        for (Giocatore giocatore : giocatori) {
            for (int i = 0; i < 7; i++) {
                pescaCarta(giocatore);
            }
        }
        cartaInGioco = new Carta(-1, 1, 1);
        mazzo.getCarte().remove(0);
        applicaEffettoCartaInizio(cartaInGioco);
        CicloGioco();
    }

    public void terminaPartita(Giocatore giocatore) {
        System.out.println("Il giocatore:" + giocatore.getNome() + " ha vinto!!");
    }

    public boolean verificaMossaValida(Carta cartaGiocata) {
        return cartaInGioco == null || cartaGiocata.getColore() == cartaInGioco.getColore()
            || cartaGiocata.getNumero() == cartaInGioco.getNumero()
            || cartaGiocata.getTipo() == 4 || cartaGiocata.getTipo() == 5;
    }

    public void SegnalaUno(Giocatore giocatore) {
        if (giocatore.getMano().getCarte().size() == 1) {
            giocatore.setDettoUno(true);
        } else {
            penalizzaGiocatore(giocatore);
        }
    }

    public void penalizzaGiocatore(Giocatore giocatore) {
        giocatore.aggiungiCarta(mazzo.getCarte().get(0));
        mazzo.getCarte().remove(0);
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

    public void PassaTurno(Giocatore giocatore) {
        giocatore.setHaSaltato(true);
    }

    public void CicloGioco() {
        while (true) {
            boolean prevDirezione = direzioneGioco;
            if (direzioneGioco) {
                for (int i = turno; i < giocatori.length; i++) {
                    if (prevDirezione != direzioneGioco) {
                        break;
                    }
                    gestisciTurno(giocatori[turno]);
                    if (verificaVittoria(giocatori[turno])) {
                        terminaPartita(giocatori[turno]);
                        return;
                    }
                    turno = (turno + 1) % giocatori.length;
                }
            } else {
                for (int i = giocatori.length - turno; i >= 0; i--) {
                    if (prevDirezione != direzioneGioco) {
                        break;
                    }
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
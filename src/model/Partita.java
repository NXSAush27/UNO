package model;
import java.util.Stack;

public class Partita {
    Giocatore[] giocatori;
    Mazzo mazzo;
    Stack<Carta> pilascarti;
    Carta cartaInGioco;
    boolean direzioneGioco; // true = orario, false = antiorario
    public Partita(Giocatore[] giocatori){
        this.giocatori = giocatori;
        this.mazzo = new Mazzo(112);
        this.pilascarti = new Stack<>();
        this.cartaInGioco = null;
        this.direzioneGioco = true;
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
    // TODO: implementare i metodi per giocare una carta, pescare una carta, passare il turno, ecc.
    public void giocaCarta(Giocatore giocatore, int posizioneCarta) {
        Carta cartaGiocata = giocatore.getMano().getCarte().get(posizioneCarta);
        if (verificaMossaValida(cartaGiocata)) {
            giocatore.rimuoviCartaAPosizione(posizioneCarta);
            pilascarti.push(cartaInGioco);
            cartaInGioco = cartaGiocata;
        }
    }
    public void pescaCarta(Giocatore giocatore) {
        if (!mazzo.getCarte()[0].equals(null)) {
            Carta cartaPescata = mazzo.getCarte()[0];
            giocatore.aggiungiCarta(cartaPescata);
            // Rimuovi la carta pescata dal mazzo
            Mazzo nuoveCarte = new Mazzo(mazzo.getCarte().length - 1);
            System.arraycopy(mazzo.getCarte(), 1, nuoveCarte.getCarte(), 0, nuoveCarte.getCarte().length);
            mazzo = nuoveCarte;
        }
    }
    public void passaTurno() {

    }
    public void applicaEffettoCarta(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                // Il giocatore successivo pesca 2 carte e salta il turno
                break;
            case 2: // Inverti
                // Cambia la direzione del gioco
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta
                // Il giocatore successivo salta il turno
                break;
            case 4: // Jolly
                // Il giocatore sceglie il colore da giocare
                break;
            case 5: // +4
                // Il giocatore successivo pesca 4 carte, salta il turno e il giocatore sceglie il colore da giocare
                break;
        }
    }
    public boolean verificaVittoria(Giocatore giocatore) {
        return giocatore.getMano().getCarte().isEmpty();
    }
    public void iniziaPartita() {
        // Distribuisci le carte ai giocatori
        for (Giocatore giocatore : giocatori) {
            for (int i = 0; i < 7; i++) {
                pescaCarta(giocatore);
            }
        }
        // Posiziona la prima carta sul tavolo
        cartaInGioco = mazzo.getCarte()[0];
        // Rimuovi la prima carta dal mazzo
        Carta[] nuoveCarte = new Carta[mazzo.getCarte().length - 1];
        System.arraycopy(mazzo.getCarte(), 1, nuoveCarte, 0, nuoveCarte.length);
        mazzo = new Mazzo(112);
        mazzo.getCarte()[0] = null; // Rimuovi la prima carta
        System.arraycopy(nuoveCarte, 0, mazzo.getCarte(), 0, nuoveCarte.length);
    }
    public void terminaPartita() {
        //TODO: Logica per terminare la partita, ad esempio dichiarare il vincitore
    }
    public boolean verificaMossaValida(Carta cartaGiocata) {
        return cartaInGioco == null || cartaGiocata.getColore() == cartaInGioco.getColore() || cartaGiocata.getNumero() == cartaInGioco.getNumero() || cartaGiocata.getTipo() == 4 || cartaGiocata.getTipo() == 5;
    }
    public void salvaPartita() {
        //TODO: Logica per salvare la partita con serializzazione e json
    }
    public void caricaPartita() {
        //TODO: Logica per caricare la partita da un file di salvataggio
    }
    public void SegnalaUno(Giocatore giocatore) {
        if (giocatore.getMano().getCarte().size() == 1) {
            giocatore.setDettoUno(true);
        }
        else{
            penalizzaGiocatore(giocatore);
        }
    }
    public void penalizzaGiocatore(Giocatore giocatore) {
        giocatore.aggiungiCarta(mazzo.getCarte()[0]);
        // Rimuovi la carta pescata dal mazzo
        Mazzo nuoveCarte = new Mazzo(mazzo.getCarte().length - 1);
        System.arraycopy(mazzo.getCarte(), 1, nuoveCarte.getCarte(), 0, nuoveCarte.getCarte().length);
        this.mazzo = nuoveCarte;
    }
    public void gestisciTurno(Giocatore giocatore) {
        //TODO: Logica per gestire il turno di un giocatore
    }

    public void CicloGioco() {
        while (true) {
            for (Giocatore giocatore : giocatori) {
                gestisciTurno(giocatore);
                if (verificaVittoria(giocatore)) {
                    terminaPartita();
                    return;
                }
            }
        }
    }
}
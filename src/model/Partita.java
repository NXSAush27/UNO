package model;
import java.util.Stack;

public class Partita {
    Giocatore[] giocatori;
    Mazzo mazzo;
    Stack<Carta> pilascarti;
    Carta cartaInGioco;
    boolean direzioneGioco; // true = orario, false = antiorario
    int turno; // Indice del giocatore attivo
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
    public void giocaCarta(Giocatore giocatore, int posizioneCarta) {
        Carta cartaGiocata = giocatore.getMano().getCarte().get(posizioneCarta);
        if (verificaMossaValida(cartaGiocata)) {
            giocatore.rimuoviCartaAPosizione(posizioneCarta);
            pilascarti.push(cartaInGioco);
            cartaInGioco = cartaGiocata;
            if (giocatore.getMano().getCarte().size() == 1 && !giocatore.isDettoUno()) {
                penalizzaGiocatore(giocatore);
            }
            if(cartaGiocata.getTipo() != 0) {
                applicaEffettoCarta(cartaGiocata);
            }
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
        //TODO: Logica per passare il turno al giocatore successivo in base alla direzione del gioco
    }
    public void applicaEffettoCarta(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                if (direzioneGioco) {
                    // Il giocatore successivo pesca 2 carte e salta il turno
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[turno + 1 % giocatori.length]);
                    }
                } else {
                    // Il giocatore precedente pesca 2 carte e salta il turno
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]);
                    }
                }
                break;
            case 2: // Inverti
                // Cambia la direzione del gioco
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta
                // Il giocatore successivo salta il turno
                if (direzioneGioco) {
                    PassaTurno(giocatori[turno + 1 % giocatori.length]);
                } else {
                    PassaTurno(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]); // Ternary operator per gestire il wrap-around dell'indice del giocatore precedente
                }
                break;
            case 4: // Jolly
                // Il giocatore sceglie il colore da giocare
                pilascarti.pop();
                Carta cartacoloreScelto = new Carta(0, 0, 4); // Placeholder per la carta jolly con colore scelto
                while(true){
                    System.out.println("Scegli il colore da giocare: 0 = Rosso, 1 = Verde, 2 = Blu, 3 = Giallo");
                    try {
                        int coloreScelto = System.in.read() - '0'; // Converti il carattere letto in un numero
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (Exception e) {
                        System.out.println("Input non valido, riprova.");
                    }
                }
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4
                // Il giocatore successivo pesca 4 carte, salta il turno e il giocatore sceglie il colore da giocare
                if (direzioneGioco) {
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[turno + 1 % giocatori.length]);
                    }
                    PassaTurno(giocatori[turno + 1 % giocatori.length]);
                } else {
                    for (int i = 0; i < 4; i++) {
                        pescaCarta(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]);
                    }
                    PassaTurno(giocatori[turno - 1 < 0 ? giocatori.length - 1 : turno - 1]);
                }
                                pilascarti.pop();
                Carta cartacoloreScelto4 = new Carta(0, 0, 4); // Placeholder per la carta jolly con colore scelto
                while(true){
                    System.out.println("Scegli il colore da giocare: 0 = Rosso, 1 = Verde, 2 = Blu, 3 = Giallo");
                    try {
                        int coloreScelto = System.in.read() - '0'; // Converti il carattere letto in un numero
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto4 = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (Exception e) {
                        System.out.println("Input non valido, riprova.");
                    }
                }
                cartaInGioco = cartacoloreScelto4;
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
        if (giocatore.getMano().getCarte().isEmpty()) {
            return; // Il giocatore ha già vinto, salta il turno
        }
        // Logica per decidere se il giocatore gioca una carta, pesca o passa
        
        while (!giocatore.HaGiocato()) {
            switch(giocatore.decidiMossa()) {
                case 0: // Gioca una carta
                for (int i = 0; i < giocatore.getMano().getCarte().size(); i++) {
                    if (verificaMossaValida(giocatore.getMano().getCarte().get(i))) {
                        giocaCarta(giocatore, i);
                    applicaEffettoCarta(cartaInGioco);
                    giocatore.setHaGiocato(true);
                    break;
                    }
                }
                break;
                case 1: // Pesca una carta
                    pescaCarta(giocatore);
                    giocatore.setHaGiocato(true);
                    break;
                case 2: // Passa
                    PassaTurno(giocatore);
                    break;
            }
        }
    }

    public void PassaTurno(Giocatore giocatore) {
        giocatore.setHaGiocato(true);
    }

    public void CicloGioco() {
        while (true) {
            if (direzioneGioco) {
                for (int i = 0; i < giocatori.length; i++) {
                    gestisciTurno(giocatori[i]);
                    if (verificaVittoria(giocatori[i])) {
                        terminaPartita();
                        return;
                    }
                    turno++;
                }
            } else {
                for (int i = giocatori.length - 1; i >= 0; i--) {
                    gestisciTurno(giocatori[i]);
                    if (verificaVittoria(giocatori[i])) {
                        terminaPartita();
                        return;
                    }
                    turno++;
                }
            }
        }
    }
}
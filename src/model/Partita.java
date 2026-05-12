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
    public Partita(Giocatore[] giocatori){
        this.giocatori = giocatori;
        this.mazzo = new Mazzo(108);
        this.pilascarti = new Stack<>();
        this.cartaInGioco = null;
        this.direzioneGioco = true;
        this.turno = 0;
        for(int i = 0; i<Math.random() * 100;i++){
            this.mazzo.mescola();
        }
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
        if (mazzo.getCarte().size() > 0) {
            Carta cartaPescata = mazzo.getCarte().get(0);
            giocatore.aggiungiCarta(cartaPescata);
            // Rimuovi la carta pescata dal mazzo
            mazzo.getCarte().remove(0);
        }
    }
    public void passaTurno(Giocatore giocatore) {
        giocatore.setHaGiocato(true);
    }
    public void applicaEffettoCarta(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                if (direzioneGioco) {
                     // Il giocatore successivo pesca 2 carte e salta il turno
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[(turno + 1) % giocatori.length]);
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
                    PassaTurno(giocatori[(turno + 1) % giocatori.length]);
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
                        int coloreScelto = Integer.parseInt(App.scanner.nextLine().trim());
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Input non valido, riprova.");
                    }
                }
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4
                // Il giocatore successivo pesca 4 carte, salta il turno e il giocatore sceglie il colore da giocare
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
                Carta cartacoloreScelto4 = new Carta(0, 0, 4); // Placeholder per la carta jolly con colore scelto
                while(true){
                    System.out.println("Scegli il colore da giocare: 0 = Rosso, 1 = Verde, 2 = Blu, 3 = Giallo");
                    try {
                        int coloreScelto = Integer.parseInt(App.scanner.nextLine().trim());
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto4 = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Input non valido, riprova.");
                    }
                }
                cartaInGioco = cartacoloreScelto4;
                break;
        }
    }
    public void applicaEffettoCartaInizio(Carta carta) {
        switch (carta.getTipo()) {
            case 1: // +2
                     // Il giocatore successivo pesca 2 carte e salta il turno
                    for (int i = 0; i < 2; i++) {
                        pescaCarta(giocatori[(0) % giocatori.length]);
                    }
                break;
            case 2: // Inverti
                // Cambia la direzione del gioco
                direzioneGioco = !direzioneGioco;
                break;
            case 3: // Salta
                 // Il giocatore successivo salta il turno
                    PassaTurno(giocatori[(0) % giocatori.length]);
                    PassaTurno(giocatori[0]); 
                break;
            case 4: // Jolly
                // Il giocatore sceglie il colore da giocare
                pilascarti.pop();
                Carta cartacoloreScelto = new Carta(0, 0, 4); // Placeholder per la carta jolly con colore scelto
                while(true){
                    System.out.println("Scegli il colore da giocare: 0 = Rosso, 1 = Verde, 2 = Blu, 3 = Giallo");
                    try {
                        int coloreScelto = Integer.parseInt(App.scanner.nextLine().trim());
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Input non valido, riprova.");
                    }
                }
                cartaInGioco = cartacoloreScelto;
                break;
            case 5: // +4
                // Il giocatore successivo pesca 4 carte, salta il turno e il giocatore sceglie il colore da giocare
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
                Carta cartacoloreScelto4 = new Carta(0, 0, 4); // Placeholder per la carta jolly con colore scelto
                while(true){
                    System.out.println("Scegli il colore da giocare: 0 = Rosso, 1 = Verde, 2 = Blu, 3 = Giallo");
                    try {
                        int coloreScelto = Integer.parseInt(App.scanner.nextLine().trim());
                        if (coloreScelto >= 0 && coloreScelto <= 3) {
                            cartacoloreScelto4 = new Carta(0, coloreScelto, 4);
                            break;
                        } else {
                            System.out.println("Colore non valido, riprova.");
                        }
                    } catch (NumberFormatException e) {
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
        cartaInGioco = mazzo.getCarte().get(0);
        // Rimuovi la prima carta dal mazzo
        mazzo.getCarte().remove(0);
        // Inizia il ciclo di gioco
        //applicaEffettoCartaInizio(cartaInGioco);
        CicloGioco();
    }
    public void terminaPartita(Giocatore giocatore) {
        System.out.println("Il giocatore:"+giocatore.getNome()+ " ha vinto!!");
    }
    public boolean verificaMossaValida(Carta cartaGiocata) {
        return cartaInGioco == null || cartaGiocata.getColore() == cartaInGioco.getColore() || cartaGiocata.getNumero() == cartaInGioco.getNumero() || cartaGiocata.getTipo() == 4 || cartaGiocata.getTipo() == 5;
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
        giocatore.aggiungiCarta(mazzo.getCarte().get(0));
        // Rimuovi la carta pescata dal mazzo
        mazzo.getCarte().remove(0);
    }
    public void gestisciTurno(Giocatore giocatore) {
        if (giocatore.getMano().getCarte().isEmpty()) {
            return; // Il giocatore ha già vinto, salta il turno
        }
        if (giocatore.isHaSaltato()) {
            System.out.println("Il giocatore " + giocatore.getNome() + " salta il turno!");
            giocatore.setHaSaltato(false);
            return;
        }
        giocatore.setHaGiocato(false);
        // Logica per decidere se il giocatore gioca una carta, pesca o passa
        System.out.println("Turno di " + giocatore.getNome());
        System.out.println("Carta in gioco: " + cartaInGioco);
        while (!giocatore.HaGiocato()) {
            switch(giocatore.decidiMossa()) {
                case 0: // Gioca una carta
                // Mostra le carte giocabili e chiedi quale giocare
                System.out.println("Carte giocabili:");
                int giocabiliCount = 0;
                for (int i = 0; i < giocatore.getMano().getCarte().size(); i++) {
                    if (verificaMossaValida(giocatore.getMano().getCarte().get(i))) {
                        System.out.println(giocabiliCount + ": " + giocatore.getMano().getCarte().get(i).toString());
                        giocabiliCount++;
                    }
                }
                if (giocabiliCount == 0) {
                    System.out.println("Nessuna carta giocabile. Peschi una carta.");
                    pescaCarta(giocatore);
                    giocatore.setHaGiocato(true);
                    break;
                }
                System.out.println("Digita il numero della carta da giocare:");
                try {
                    int sceltaCarta = Integer.parseInt(App.scanner.nextLine().trim());
                    if (sceltaCarta < 0 || sceltaCarta >= giocabiliCount) {
                        System.out.println("Scelta non valida, riprova.");
                        break;
                    }
                    // Trova l'indice reale della carta giocabile selezionata
                    int indiceReale = 0;
                    int contatore = 0;
                    for (int i = 0; i < giocatore.getMano().getCarte().size(); i++) {
                        if (verificaMossaValida(giocatore.getMano().getCarte().get(i))) {
                            if (contatore == sceltaCarta) {
                                indiceReale = i;
                                break;
                            }
                            contatore++;
                        }
                    }
                    giocaCarta(giocatore, indiceReale);
                    giocatore.setHaGiocato(true);
                } catch (NumberFormatException e) {
                    System.out.println("Input non valido, riprova.");
                }
                break;
                case 1: // Pesca una carta
                    pescaCarta(giocatore);
                    giocatore.setHaGiocato(true);
                    break;
                case 2: // Passa
                    PassaTurno(giocatore);
                    break;
                case 3: // Segnala UNO
                    giocaCarta(giocatore, turno);
                    SegnalaUno(giocatore);
                    giocatore.setHaGiocato(true);
                    break;
                case 4: // Salva partita
                    salvaPartita();
                    break;
                case 5: // Carica partita
                    Partita partitaCaricata = caricaPartita();
                    if (partitaCaricata != null) {
                        partitaCaricata.CicloGioco();
                        return; // Termina il turno corrente dopo aver caricato la partita
                    }
                    break;
            }
        }
    }

    public void PassaTurno(Giocatore giocatore) {
        giocatore.setHaGiocato(true);
    }

    public void CicloGioco() {
        while (true) {
            boolean prevDirezione = direzioneGioco;
            if (direzioneGioco) {
                for (int i = turno; i < giocatori.length; i++) {
                    if(prevDirezione != direzioneGioco) {
                        break; // Se la direzione è cambiata durante il ciclo, interrompi e ricomincia con la nuova direzione
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
                    if(prevDirezione != direzioneGioco) {
                        break; // Se la direzione è cambiata durante il ciclo, interrompi e ricomincia con la nuova direzione
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
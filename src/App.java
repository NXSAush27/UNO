import model.Partita;
import model.Giocatore;
public class App {
    public static void main(String[] args) throws Exception {
        Partita partita;
        while(true) {
            System.out.println("Benvenuto al gioco UNO!");
            System.out.println("1: Inizia una nuova partita");
            System.out.println("2: Esci");
            try {
                int scelta = System.in.read() - '0'; // Converti il carattere in un numero
                if (scelta == 1) {
                    System.out.println("Dimmi il numero di giocatori (2-8):");
                try {
                    int numGiocatori = System.in.read() - '0'; // Converti il carattere in un numero
                    if (numGiocatori < 2 || numGiocatori > 8) {
                        System.out.println("Numero di giocatori non valido, riprova.");
                        continue;
                    }
                    Giocatore[] giocatori = new Giocatore[numGiocatori];
                    for (int i = 0; i < numGiocatori; i++) {
                        System.out.println("Dimmi il nome del giocatore " + (i + 1) + ":");
                        String nome = new java.util.Scanner(System.in).nextLine();
                        giocatori[i] = new model.GiocatoreUmano(nome);
                    }
                    partita = new Partita(giocatori);
                    partita.iniziaPartita();
                } catch (Exception e) {
                    System.out.println("Input non valido, riprova.");
                    continue;
                }
                } else if (scelta == 2) {
                    System.out.println("Grazie per aver giocato! Arrivederci!");
                    break;
                } else {
                    System.out.println("Scelta non valida, riprova.");
                }
            } catch (Exception e) {
                System.out.println("Input non valido, riprova.");
            }
        }
    }
}

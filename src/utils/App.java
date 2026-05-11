package utils;
import model.Partita;
import model.Giocatore;
import java.util.Scanner;
import java.io.File;

public class App {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        Partita partita = null;
        while(true) {
            System.out.println("Benvenuto al gioco UNO!");
            System.out.println("1: Inizia una nuova partita");
            System.out.println("2: Salva partita");
            System.out.println("3: Carica partita");
            System.out.println("4: Esci");
            try {
                System.out.print("Scelta: ");
                int scelta = leggiIntero();
                if (scelta == 1) {
                    System.out.println("Dimmi il numero di giocatori (2-8):");
                    int numGiocatori = leggiIntero();
                    if (numGiocatori < 2 || numGiocatori > 8) {
                        System.out.println("Numero di giocatori non valido, riprova.");
                        continue;
                    }
                    Giocatore[] giocatori = new Giocatore[numGiocatori];
                    for (int i = 0; i < numGiocatori; i++) {
                        System.out.println("Dimmi il nome del giocatore " + (i + 1) + ":");
                        String nome = scanner.nextLine();
                        giocatori[i] = new model.GiocatoreUmano(nome);
                    }
                    partita = new Partita(giocatori);
                    partita.iniziaPartita();
                } else if (scelta == 2) {
                    if (partita == null) {
                        System.out.println("Nessuna partita in corso da salvare!");
                        continue;
                    }
                    System.out.println("Inserisci il nome del file di salvataggio (default: savegame.dat):");
                    String nomeFile = scanner.nextLine().trim();
                    if (nomeFile.isEmpty()) {
                        nomeFile = "savegame.dat";
                    }
                    partita.salvaPartita(nomeFile);
                } else if (scelta == 3) {
                    System.out.println("Inserisci il nome del file di salvataggio (default: savegame.dat):");
                    String nomeFile = scanner.nextLine().trim();
                    if (nomeFile.isEmpty()) {
                        nomeFile = "savegame.dat";
                    }
                    File f = new File(nomeFile);
                    if (!f.exists()) {
                        System.out.println("File di salvataggio non trovato: " + nomeFile);
                        continue;
                    }
                    partita = new Partita(new Giocatore[0]);
                    partita = partita.caricaPartita(nomeFile);
                    if (partita != null) {
                        System.out.println("Partita caricata! Vuoi continuare a giocare? (s/n)");
                        String risposta = scanner.nextLine().trim().toLowerCase();
                        if (risposta.equals("s")) {
                            partita.iniziaPartita();
                        }
                    }
                } else if (scelta == 4) {
                    System.out.println("Grazie per aver giocato! Arrivederci!");
                    break;
                } else {
                    System.out.println("Scelta non valida, riprova.");
                }
            } catch (Exception e) {
                System.out.println("Input non valido, riprova. Errore: " + e.getMessage());
            }
        }
    }

    private static int leggiIntero() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero intero:");
            }
        }
    }
}

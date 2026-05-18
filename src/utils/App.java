package utils;

import javax.swing.JOptionPane;

public class App {
    public static int scegliColore() {
        String[] opzioni = {"Rosso", "Giallo", "Verde", "Blu"};
        
        int scelta = JOptionPane.showOptionDialog(
                null,
                "Hai giocato un Jolly! Scegli il nuovo colore:",
                "Cambio Colore",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opzioni,
                opzioni[0]
        );

        // Se l'utente chiude la finestra con la 'X', forziamo il Rosso (0) di default
        return (scelta >= 0) ? scelta : 0; 
    }
}
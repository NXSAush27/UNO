package model;

public class Mazzo {
    private Carta[] carte;
    public Mazzo(int size){
        this.carte = new Carta[size];
        for (int i = 0; i < size; i++) {
            int numero = i % 10; // 0-9 per carte normali
            int colore = (i / 10) % 5; // 0-4 per i colori (incluso jolly)
            int tipo;
            if (colore == 4) { // Jolly
                tipo = (i / 50) + 4; // 4 per jolly, 5 per +4
            } else {
                tipo = (i / 20) % 6; // 0-5 per tipi di carte normali
            }
            carte[i] = new Carta(numero, colore, tipo);
        }
    }
    public void mescola() {
        Carta[] nuoveCarte = new Carta[carte.length];
        for (int i = 0; i < carte.length; i++) {
            int indiceCasuale = (int) (Math.random() * carte.length);
            nuoveCarte[i] = carte[indiceCasuale];
        }        
        this.carte = nuoveCarte;
    }
    public Carta[] getCarte() {
        return carte;
    }
}
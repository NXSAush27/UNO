package model;

public class Mazzo {
    private Carta[] carte;
    public Mazzo(int size){
        // TODO: inizializzare il mazzo con le carte corrette con un for per ogni colore e numero, più i jolly
        this.carte = new Carta[size];
    }
    public Carta[] getCarte() {
        return carte;
    }
}
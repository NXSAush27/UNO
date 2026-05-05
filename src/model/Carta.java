package model;

public class Carta {
    final  int numero;
    final int colore;
    public Carta(int numero, int colore){
        this.numero = numero;
        this.colore = colore;
    }
    public int getNumero() {
        return numero;
    }
    public int getColore() {
        return colore;
    }
}
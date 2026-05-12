package model;

import java.io.Serializable;

public class Carta implements Serializable{
    final int numero; // 0-9 per carte normali, -1 per jolly e +4
    final int colore; // 0 = rosso, 1 = verde, 2 = blu, 3 = giallo, 4 = jolly
    int tipo; // 0 = normale, 1 = +2, 2 = inverti, 3 = salta, 4 = jolly, 5 = +4
    
    // COSTRUTTORE
    public Carta(int numero, int colore, int tipo){
        this.numero = numero;
        this.colore = colore;
        this.tipo = tipo;
    }
    
    // GETTER
    public int getNumero() {
        return numero;
    }
    
    public int getColore() {
        return colore;
    }
    
    public int getTipo() {
        return tipo;
    }
    
    // METODO toString()
    public String toString() {
        String[] colori = {"Rosso", "Verde", "Blu", "Giallo", "Jolly"};
        String[] tipi = {"Normale", "+2", "Inverti", "Salta", "Jolly", "+4"};
        
        if (tipo != 0) {
            return tipi[tipo] + " (" + colori[colore] + ")";
        }
        return "" + numero + " (" + colori[colore] + ")";
    }
}
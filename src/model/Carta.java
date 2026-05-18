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
    @Override
    public String toString() {
        String[] nomiColori = {"Rosso", "Giallo", "Verde", "Blu", "Jolly"};
        String nomeColore = (colore >= 0 && colore < nomiColori.length) ? nomiColori[colore] : "";

        if (tipo == 0) return nomeColore + " " + numero;
        if (tipo == 1) return nomeColore + " +2";
        if (tipo == 2) return nomeColore + " Inverti";
        if (tipo == 3) return nomeColore + " Salta";
        if (tipo == 4) return "Cambio Colore";
        if (tipo == 5) return "+4";

        return "Carta (" + colore + ", " + tipo + ")";
    }
}
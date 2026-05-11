package model;
import java.io.Serializable;
import java.util.ArrayList;

public class Mano implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Carta> carte;
    public Mano(){
        this.carte = new ArrayList<>();
    }
    public void aggiungiCarta(Carta carta){
        this.carte.add(carta);
    }
    public void rimuoviCartaAPosizione(int posizione){
        this.carte.remove(posizione);
    }
    public ArrayList<Carta> getCarte() {
        return carte;
    }
}
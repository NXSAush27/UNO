package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Mazzo implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Carta> carte;
    public Mazzo(int size){
        this.carte = new ArrayList<>();
        for(int i= 0; i<4; i++){
            carte.add(new Carta(0, i, 0));
            for(int j = 1; j<=9; j++){
                carte.add(new Carta(j, i, 0));
                carte.add(new Carta(j, i, 0));
            }
            for(int j = 0; j<2; j++){
                carte.add(new Carta(-1, i, 1)); // +2
                carte.add(new Carta(-1, i, 2)); // Inverti
                carte.add(new Carta(-1, i, 3)); // Salta
            }
            carte.add(new Carta(-1, 4, 4)); // Jolly
            carte.add(new Carta(-1, 4, 5)); // +4
        }

    }
    public void mescola() {
        for (int i = 0; i < carte.size(); i++) {
            int indiceCasuale = (int) (Math.random() * carte.size());
            Carta carta = carte.get(indiceCasuale);
            carte.set(indiceCasuale, carte.get(i));
            carte.set(i, carta);
        }
    }
    public ArrayList<Carta> getCarte() {
        return carte;
    }
}
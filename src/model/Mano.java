package model;

import java.util.ArrayList;
import java.util.List;

public class Mano {
    private List<Carta> carte;

    public Mano() {
        this.carte = new ArrayList<>();
    }

    public void addCarta(Carta c) {
        carte.add(c);
    }

    public List<Carta> getCarte() {
        return carte;
    }
}
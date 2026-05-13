package model;

import java.io.Serializable;

public abstract class Giocatore implements Serializable {
    private static final long serialVersionUID = 1L;
    private Mano mano;
    private boolean DettoUno;
    private boolean haGiocato = false;
    private boolean haSaltato = false;
    public Giocatore() {
        this.mano = new Mano();
        this.DettoUno = false;
    }

    public Mano getMano() {
        return mano;
    }

    public boolean HaGiocato() {
        return haGiocato;
    }

    public void setHaGiocato(boolean haGiocato) {
        this.haGiocato = haGiocato;
    }
    public String getNome(){
        return "";
    }
    public boolean isDettoUno() {
        return DettoUno;
    }

    public void setDettoUno(boolean dettoUno) {
        DettoUno = dettoUno;
    }

    public void aggiungiCarta(Carta carta) {
        mano.aggiungiCarta(carta);
    }

    public void rimuoviCartaAPosizione(int posizione) {
        mano.rimuoviCartaAPosizione(posizione);
    }
    public boolean isHaSaltato() {
        return haSaltato;
    }
    public void setHaSaltato(boolean haSaltato) {
        this.haSaltato = haSaltato;
    }
    public boolean getHaSaltato() {
        return haSaltato;
    }
    public abstract Carta decidiMossa(Partita partita);
    public abstract int scegliColore(Partita partita);
    public abstract void provaDichiaraUno(Partita partita);
}

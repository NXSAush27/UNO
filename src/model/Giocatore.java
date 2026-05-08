package model;

public abstract class Giocatore {
    private Mano mano;
    private boolean DettoUno;
    private boolean haGiocato = false;
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
    public abstract int decidiMossa();
}

package model;

import java.io.Serializable;

public class GiocatoreBot extends Giocatore implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    public GiocatoreBot(String nome){
        super();
        this.nome = nome;
    }
    @Override
    public Mano getMano() {
        return super.getMano();
    }
    @Override
    public void aggiungiCarta(Carta carta) {
        super.aggiungiCarta(carta);
    }
    @Override
    public void setHaSaltato(boolean haSaltato) {
        super.setHaSaltato(haSaltato);
    }
    @Override
    public boolean getHaSaltato() {
        return super.getHaSaltato();
    }
    @Override
    public void rimuoviCartaAPosizione(int posizione) {
        super.rimuoviCartaAPosizione(posizione);
    }
    @Override
    public String getNome() {
        return nome;
    }
    @Override
    public Carta decidiMossa(Partita partita) {
        for (Carta carta : getMano().getCarte()) {
            if (partita.verificaMossaValida(carta)) {
                return carta;
            }
        }
        return null;
    }

    @Override
    public int scegliColore(Partita partita) {
        int[] counts = new int[4];
        for (Carta c : getMano().getCarte()) {
            int col = c.getColore();
            if (col >= 0 && col <= 3) {
                counts[col]++;
            }
        }
        int total = counts[0] + counts[1] + counts[2] + counts[3];
        if (total == 0) {
            return (int) (Math.random() * 4);
        }
        int maxIdx = 0;
        for (int i = 1; i < 4; i++) {
            if (counts[i] > counts[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    @Override
    public void provaDichiaraUno(Partita partita) {
        if (getMano().getCarte().size() == 1) {
            setDettoUno(true);
        }
    }
}

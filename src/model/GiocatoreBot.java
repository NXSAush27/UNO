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
}

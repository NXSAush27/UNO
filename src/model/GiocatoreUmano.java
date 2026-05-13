package model;

import java.io.Serializable;

public class GiocatoreUmano extends Giocatore implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    public GiocatoreUmano(String nome){
        super();
        this.nome = nome;
    }
    @Override
    public Mano getMano() {
        return super.getMano();
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
    public void aggiungiCarta(Carta carta) {
        super.aggiungiCarta(carta);
    }
    @Override
    public void rimuoviCartaAPosizione(int posizione) {
        super.rimuoviCartaAPosizione(posizione);
    }
    @Override
    public String getNome() {
        return nome;
    }
    /**
     * Nel flusso GUI le mosse dell'umano sono gestite dalla GamePanel
     * (click sulle carte). Questo metodo non viene invocato dalla GUI.
     */
    @Override
    public Carta decidiMossa(Partita partita) {
        return null;
    }
}

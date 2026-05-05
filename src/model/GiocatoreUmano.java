package model;

public class GiocatoreUmano implements Giocatore {
    private Mano mano;
    private String nome;
    public GiocatoreUmano(String nome){
        this.mano = new Mano();
        this.nome = nome;
    }
    @Override
    public Mano getMano() {
        return mano;
    }
    @Override
    public void aggiungiCarta(Carta carta) {
        this.mano.aggiungiCarta(carta);
    }
    @Override
    public void rimuoviCarta(Carta carta) {
        this.mano.getCarte().remove(carta);
    }
    
}

package model;

public class GiocatoreUmano extends Giocatore {
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
    public void aggiungiCarta(Carta carta) {
        super.aggiungiCarta(carta);
    }
    @Override
    public void rimuoviCartaAPosizione(int posizione) {
        super.rimuoviCartaAPosizione(posizione);
    }
    public String getNome() {
        return nome;
    }
}

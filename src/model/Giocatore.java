package model;

public interface Giocatore {
    Mano getMano();
    void aggiungiCarta(Carta carta);
    void rimuoviCarta(Carta carta);

}

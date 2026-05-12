package model;

import java.io.Serializable;

import utils.App;
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
    @Override
    public int decidiMossa() {
        while(true) {
            System.out.println("Giocatore " + nome + ", è il tuo turno. Scegli una carta da giocare o pesca una carta.");
            for (int i = 0; i < getMano().getCarte().size(); i++) {
                System.out.println(i + ": " + getMano().getCarte().get(i).toString());
            }
            System.out.println("Digita 0 per giocare una carta, 1 per pescare una carta, 2 per passare, 3 per dire UNO, 4 per salvare la partita, 5 per caricare una partita");
            try {
                String input = App.scanner.nextLine().trim();
                int scelta = Integer.parseInt(input);
                if (scelta < 0 || scelta > 5) {
                    System.out.println("Scelta non valida, riprova.");
                    continue;
                }
                return scelta;
            } catch (NumberFormatException e) {
                System.out.println("Scelta non valida, riprova.");
            }
        }
    }
}

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
    @Override
    public int decidiMossa() {
        while(true) {
            System.out.println("Giocatore " + nome + ", è il tuo turno. Scegli una carta da giocare o pesca una carta.");
            for (int i = 0; i < getMano().getCarte().size(); i++) {
                System.out.println(i + ": " + getMano().getCarte().get(i).toString());
            }
            System.out.println("Digita 0 per giocare una carta, 1 per pescare una carta, 2 per passare.");
            try {
                int scelta = System.in.read() - '0'; // Converti il carattere in un numero
                if (scelta < 0 || scelta > 2) {
                    System.out.println("Scelta non valida, riprova.");
                    continue;
                }
                return scelta;
            } catch (Exception e) {
                System.out.println("Input non valido, riprova.");
            }
        }
    }
}

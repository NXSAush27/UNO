package utils;
import java.io.*;
import model.Partita;

public class GestoreSalvataggio {
    public static void salva(Partita partita, String nomeFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomeFile))) {
            oos.writeObject(partita);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Partita carica(String nomeFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nomeFile))) {
            return (Partita) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
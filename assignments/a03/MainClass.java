import java.util.Vector;
import java.lang.Math;
import java.util.Random;

public class MainClass {
    public static void main(String[] args) {
        final Random rng = new Random(System.currentTimeMillis());
        final int numPC = 20;
        // leggo il numero di studenti, tesisti e professori
        int students = Integer.valueOf(args[0]);
        int thesis = Integer.valueOf(args[1]);
        int professors = Integer.valueOf(args[2]);
        // creo tutti i thread studenti, tesisti e professori
        // FIXME: Vector<Runnable>
        // FIXME: Array fisso?
        Vector<Utente> all_users = new Vector<Utente>(students + thesis + professors);
        Laboratorio lab = new Laboratorio(numPC, students, thesis, professors, all_users);

        for(int i = 0; i < students; i++) {
            all_users.add(new Studente(numPC, lab));
        }
        for(int i = 0; i < thesis; i++) {
            all_users.add(new Tesista(numPC, lab, Math.abs(rng.nextInt()) % numPC));
        }
        for(int i = 0; i < professors; i++) {
            all_users.add(new Professore(numPC, lab));
        }

        lab.start();
    }
}

import java.util.Vector;
import java.lang.Math;
import java.util.Random;

public class MainClass {
    private static final int numPC = 20;

    public static void main(String[] args) {
        final Random rng = new Random(System.currentTimeMillis());
        // leggo il numero di studenti, tesisti e professori
        int students = Integer.valueOf(args[0]);
        int thesis = Integer.valueOf(args[1]);
        int professors = Integer.valueOf(args[2]);
        // creo tutti i thread studenti, tesisti e professori
        if (students + thesis + professors <= 0) {
            System.out.println("Non sono stati inseriti abbastanza Utenti: studenti + tesisti + professori > 0");
            return;
        }
        Vector<Utente> all_users = new Vector<Utente>(students + thesis + professors);
        Laboratorio lab = new Laboratorio(numPC, students, thesis, professors, all_users);

        for (int i = 0; i < students; i++) {
            all_users.add(new Studente(numPC, lab));
        }
        for (int i = 0; i < thesis; i++) {
            all_users.add(new Tesista(numPC, lab, Math.abs(rng.nextInt()) % numPC));
        }
        for (int i = 0; i < professors; i++) {
            all_users.add(new Professore(numPC, lab));
        }

        lab.start();
    }
}

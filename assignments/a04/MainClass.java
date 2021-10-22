import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;

/**
 * Classe contenente il metodo main per l'avvio della simulazione del laboratorio
 * Il programma si aspetta di ricevere come argomenti da riga di comando
 * tre interi positivi: numero di studenti, di tesisti e di professori
 */
public class MainClass {
    // Fisso il numero di PC presenti nel laboratorio
    private static final int numPC = 20;
    private static final Random rng = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: java MainClass <studenti> <tesisti> <professori>, argomenti interi >= 0");
            return;
        }
        // ottengo il numero di studenti, tesisti e professori dagli argomenti passati al programma
        int students = Integer.valueOf(args[0]);
        int thesis = Integer.valueOf(args[1]);
        int professors = Integer.valueOf(args[2]);
        if (students < 0 || thesis < 0 || professors < 0) {
            System.out.println("Usage: java MainClass <studenti> <tesisti> <professori>, argomenti interi >= 0");
            return;
        }
        if (students + thesis + professors <= 0) {
            System.out.println("Non sono stati inseriti abbastanza Utenti: studenti + tesisti + professori > 0");
            return;
        }
        // creo tutte le task Utente e l'istanza di Laboratorio
        List<Utente> all_users = new ArrayList<Utente>(students + thesis + professors);
        Laboratorio labMarzotto = new Laboratorio(numPC, students, thesis, professors, all_users);

        for (int i = 0; i < students; i++) {
            all_users.add(new Studente(numPC, labMarzotto));
        }
        for (int i = 0; i < thesis; i++) {
            // il tesista viene creato con un argomento che è un intero casuale in [0,19] che sarà l'indice del PC che richede
            all_users.add(new Tesista(numPC, labMarzotto, Math.abs(rng.nextInt()) % numPC));
        }
        for (int i = 0; i < professors; i++) {
            all_users.add(new Professore(numPC, labMarzotto));
        }
        // faccio partire la simulazione: la terminazione del thread di tipo Laboratorio è nel suo metodo run()
        labMarzotto.start();
    }
}

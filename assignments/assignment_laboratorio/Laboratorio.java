import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Laboratorio {
    public static void main(String[] args) {
        final int numPC = 20;
        
        int students = Integer.valueOf(args[1]);
        int thesis = Integer.valueOf(args[2]);
        int professors = Integer.valueOf(args[3]);
        int nUsers = students + thesis + professors;
        // creo il vettore di tutti i PC, con i rispettivi indici
        Vector<Computer> all_PCs = new Vector<Computer>(nUsers);
        for(int i = 0; i < nUsers; i++) {
            all_PCs.add(new Computer(i));
        }
        // creo una threadpool (fixed, perché ne conosco il numero) per ogni Utente
        ExecutorService tpool = Executors.newFixedThreadPool(nUsers);
        // creo tutti i thread studenti, tesisti e professori
        // FIXME: Vector<Runnable>
        // FIXME: Array fisso?
        Vector<Utente> all_users = new Vector<Utente>(nUsers);
        for(int i = 0; i < students; i++) {
            all_users.add(new Studente(numPC,all_PCs));
        }
        for(int i = 0; i < students; i++) {
            all_users.add(new Tesista(numPC,all_PCs));
        }
        for(int i = 0; i < students; i++) {
            all_users.add(new Professore(numPC,all_PCs));
        }
        // ora eseguo la submit di ogni soggetto
        // scelgo a caso l'utente da prelevare per la execute per essere più fair
        int maxUser = all_users.size() - 1;
        Random rng = new Random(System.currentTimeMillis());
        while(all_users.size() > 0) {
            tpool.execute(all_users.get(rng.nextInt() % maxUser));
        }

        tpool.shutdown();
        while(tpool.isTerminated() == false) {
            // attendi terminazione. Maybe con Lock/CV?
        }
    }
}

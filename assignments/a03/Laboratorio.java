import java.util.Vector;
import java.lang.Math;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Classe che contiene l'implementazione del laboratorio di informatica
 */
public class Laboratorio extends Thread {
    // rng per selezionare utenti da far partire
    private static final Random rng = new Random(System.currentTimeMillis());

    // lock su tutto il laboratorio, con relativa condition variable e contatore PC occupati
    private Lock labLock = new ReentrantLock();
    private Condition clab = labLock.newCondition();
    private int occupied = 0;
    // Ciascun PC è rappresentato da una lock + variabile di condizione
    private Vector<Lock> all_PCs;
    private Vector<Condition> pc_states;
    private int numPCs;
    // Tutti gli utenti del laboratorio
    private Vector<Utente> users;
    private int tot_users;
    // Threadpool che gestisce gli utenti
    ExecutorService tpool;
    // Flag per indicare, rispettivamente, che un tesista o un professore sono in attesa di un PC
    private Vector<Boolean> thesis_waiting; // una flag per ogni PC
    private boolean professor_waiting; // una sola flag, dato che attende che tutti i PC si liberino

    // Costruttore del Laboratorio: prende in input il numero di PC ed il numero di utenti, differenziati per ruolo
    // Inoltre riceve in input un array contenente tutti gli Utenti (già inizializzati)
    public Laboratorio(int nPC, int nStudenti, int nTesisti, int nProfessori, Vector<Utente> all_users) {
        // creo tutti i PC
        this.numPCs = nPC;
        this.all_PCs = new Vector<Lock>(nPC);
        this.pc_states = new Vector<Condition>(nPC);
        for (int i = 0; i < nPC; i++) {
            Lock l = new ReentrantLock();
            this.all_PCs.add(l);
            this.pc_states.add(l.newCondition());
        }
        // inizializzo gli utenti e la threadpool
        this.users = all_users;
        this.tot_users = nStudenti + nTesisti + nProfessori;
        // numero fisso di thread, dato che conosco esattamente il numero di utenti
        this.tpool = Executors.newFixedThreadPool(this.tot_users);
        // inizialmente nessuno in attesa
        this.thesis_waiting = new Vector<Boolean>(this.numPCs);
        for (int i = 0; i < this.all_PCs.size(); i++) {
            this.thesis_waiting.add(false);
        }
        this.professor_waiting = false;
    }

    // Il thread Laboratorio prende un utente a caso nell'array, lo rimuove e fa eseguire
    // tale task alla threadpool, fino a che non vi sono più utenti da servire
    public void run() {
        int usersLeft = this.tot_users;
        while (usersLeft > 0) {
            Utente user = this.users.remove(Math.abs(rng.nextInt()) % usersLeft);
            try {
                tpool.execute(user);
            } catch (RejectedExecutionException cant_exec) {
                System.out.println("Impossibile eseguire la task: " + cant_exec);
            } catch (NullPointerException np) {
                System.out.println("Task null: " + np);
            }
            usersLeft--; // fuori dal try_catch per assicurare terminazione
        }
        // quando tutti sono stati inseriti allora metto la threadpool in shutdown ed attendo la terminazione
        tpool.shutdown();
        while (!tpool.isTerminated()) {
            try {
                tpool.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                System.out.println("Thread laboratorio interrotto durante la terminazione");
            }
        }
    }

    // un Utente richiede l'utilizzo del PC idx (se who è un professore il secondo parametro è ignorato)
    public void request(Object who, int idx) {
        Lock pc = null;
        Condition cv = null;
        // studente o tesista prendono lock sul laboratorio: se non ci sono professori in attesa
        // e ci sono PC liberi provano a prendere la lock su quello richiesto, altrimenti aspettano
        if (who instanceof Studente || who instanceof Tesista) {
            this.labLock.lock();
            while (this.occupied == this.numPCs || this.professor_waiting) {
                try {
                    System.out.println("Studente o Tesista: aspetto che un PC sia libero");
                    clab.await();
                } catch (InterruptedException ex) {
                    ;
                }
            }
            // recupero il PC e lo locko
            pc = this.all_PCs.get(idx);
            cv = this.pc_states.get(idx);
            pc.lock();
            this.occupied++;

            this.labLock.unlock();
        }

        // Se uno studente ha ottenuto lock sul PC allora esamina le flag
        // e si mette in attesa fino a che ci sono professori o tesisti in attesa
        if (who instanceof Studente) {
            while (this.thesis_waiting.get(idx)) {
                try {
                    System.out.println("Studente: Un tesista sta aspettando: cedo lock");
                    cv.await();
                } catch (InterruptedException ex) {
                    System.out.println("Thread studente interrotto durante l'attesa");
                }
            }
        }
        // Se è un professore deve aspettare finchè tutti i PC sono liberi, quindi cerca di
        // prendere la ME dal primo all'ultimo, in ordine, settando la flag per evitare che
        // ad altri studenti o tesisti sia assegnato un PC mentre lui sta attendendo che uno con indice
        // minore si liberi (cosa che prima o poi avverrà)
        else if (who instanceof Professore) {
            // il professore controlla se c'è almeno un PC occupato: se c'è allora si mette in attesa
            this.labLock.lock();

            this.professor_waiting = true;
            while (this.occupied > 0) {
                System.out.println("Professor is waiting for the lab to be empty");
                try {
                    clab.await();
                } catch (InterruptedException ex) {
                    ;
                }
            }
            // tutti i PC liberi: li metto tutti sotto lock
            for (int i = 0; i < numPCs; i++) {
                this.all_PCs.get(i).lock();
            }
            System.out.println("Professor: obtained all PCs");
            this.occupied = numPCs; // tutti i PC sono occupati

            // Il professore ha ottenuto tutti i PC: rimetto a false la flag
            this.professor_waiting = false;

            this.labLock.unlock();
        }
    }

    public void freePC(Object who, int idx) {
        // Se il PC è rilasciato da uno studente o tesista si segnala solo chi era in attesa di quel PC
        if (who instanceof Studente || who instanceof Tesista) {
            Lock l = this.all_PCs.get(idx);
            Condition cv = this.pc_states.get(idx);
            cv.signal();
            l.unlock();

            this.labLock.lock();

            this.occupied--;
            System.out.println("PC " + idx + " has been freed");
            this.clab.signalAll();

            this.labLock.unlock();
        }
        // invece se era un Professore segnalo tutte le CV (libera tutti i PC)
        else if (who instanceof Professore) {
            this.labLock.lock();
            this.occupied = 0;
            for (int i = 0; i < this.all_PCs.size(); i++) {
                //this.pc_states.get(i).signalAll();
                this.all_PCs.get(i).unlock();
            }
            System.out.println("Professore: laboratorio libero");
            this.clab.signalAll();
            this.labLock.unlock();
        }
    }
}

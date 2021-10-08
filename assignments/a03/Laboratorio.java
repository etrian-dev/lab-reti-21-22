import java.util.Vector;
import java.lang.Math;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Classe che contiene l'implementazione del laboratorio di informatica
 */
public class Laboratorio extends Thread {
    // rng per selezionare casualmente gli utenti che entrano in esecuzione
    private static final Random rng = new Random(System.currentTimeMillis());

    // numero di PC contenuti nel laboratorio
    private int numPCs;
    // lock su tutto il laboratorio, con relativa condition variable e contatore PC occupati
    private Lock labLock = new ReentrantLock();
    private Condition clab = labLock.newCondition();
    private int occupied = 0;
    // Ciascun PC è rappresentato da una lock + variabile di condizione
    private Vector<Lock> all_PCs;
    private Vector<Condition> pc_states;
    // Tutti gli utenti del laboratorio (studenti, professori e tesisti)
    private Vector<Utente> users;
    private int tot_users;
    // Threadpool che gestisce l'esecuzione delle task utente
    ExecutorService tpool;
    // Flag per indicare che un tesista è in attesa che si liberi il PC con tale indice
    private Vector<Boolean> thesis_waiting; // una flag per ogni PC

    // Costruttore del Laboratorio: prende in input il numero di PC ed il numero di utenti, differenziati per ruolo
    // Inoltre riceve in input un vector contenente tutti gli Utenti (già inizializzati)
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

    // Metodo tramite il quale un Utente richiede l'utilizzo del PC idx 
    // (se who è un professore il secondo parametro è ignorato)
    public void request(Object who, int idx) {
        Lock pc = null;
        Condition cv = null;

        // se who è uno studente o un tesista allora devono aspettare che non vi siano professori in attesa
        if (who instanceof Studente || who instanceof Tesista) {
            pc = this.all_PCs.get(idx);
            cv = this.pc_states.get(idx);
            // ottengo lock del laboratorio: finché il PC richiesto non è stato liberato attendo
            // (se non venisse rilasciata labLock non avanzerebbero gli altri utenti)
            this.labLock.lock();
            while(!pc.tryLock()) {
                try {
                    clab.await();
                } catch (Exception e) {
                    System.out.println("Thread interrotto durante l'attesa");
                }
            }
            this.occupied++;

            this.labLock.unlock();
        }

        // Se uno studente ha ottenuto lock sul PC allora esamina le flag
        // e si mette in attesa fino a che ci sono altri tesisti in attesa sullo stesso PC
        if (who instanceof Studente) {
            while (this.thesis_waiting.get(idx)) {
                try {
                    System.out.println("Studente: Un tesista sta aspettando sul PC " + idx + ", quindi cedo lock");
                    cv.await();
                } catch (InterruptedException ex) {
                    System.out.println("Thread studente interrotto durante l'attesa");
                }
            }
        }
        // Se è un professore deve aspettare finchè tutti i PC sono liberi
        else if (who instanceof Professore) {
            // il professore controlla se c'è almeno un PC occupato: se c'è allora si mette in attesa
            this.labLock.lock();

            // se vi è almeno un PC occupato allora il professore deve attendere
            while (this.occupied > 0) {
                System.out.println("Professore: aspetto che il laboratorio si svuoti");
                try {
                    clab.await();
                } catch (InterruptedException ex) {
                    ;
                }
            }
            // tutti i PC liberi: eseguo lock su tutti
            for (int i = 0; i < numPCs; i++) {
                this.all_PCs.get(i).lock();
            }
            System.out.println("Professore: ottenuto ME su tutti i PC");
            this.occupied = numPCs; // tutti i PC sono occupati

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

            // devo anche aggiornare il contatore dei pc occupati e segnalarlo a chi era sospeso sulla lock del laboratorio
            this.labLock.lock();

            System.out.println("Studente o Tesista: PC " + idx + " libero");
            this.occupied--;
            this.clab.signalAll();

            this.labLock.unlock();
        }
        // invece se era un Professore segnalo tutte le CV (libera tutti i PC)
        else if (who instanceof Professore) {
            for (int i = 0; i < this.all_PCs.size(); i++) {
                //this.pc_states.get(i).signalAll();
                this.all_PCs.get(i).unlock();
            }
            this.labLock.lock();
            
            this.occupied = 0;
            System.out.println("Professore: laboratorio libero");
            this.clab.signalAll();
            
            this.labLock.unlock();
        }
    }
}

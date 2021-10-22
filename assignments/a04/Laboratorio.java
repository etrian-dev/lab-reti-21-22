import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Classe che contiene l'implementazione del laboratorio di informatica
 */
public class Laboratorio extends Thread {
    // rng per selezionare casualmente gli utenti che entrano in esecuzione
    private static final Random rng = new Random(System.currentTimeMillis());

    // Tutti gli utenti del laboratorio (studenti, professori e tesisti)
    private List<Utente> users;
    private int tot_users;
    // Threadpool che gestisce l'esecuzione delle task utente
    ExecutorService tpool;

    private List<Computer> allPCs;
    // numero di PC contenuti nel laboratorio
    private int numPCs;
    // Contatore dei PC occupati: vale 0 <= occupiedPCs <= numPCs
    private volatile int occupiedPCs = 0; // dichiarato volatile per rendere visibili gli aggiornamenti a ciascun thread
    // Array per indicare che un tesista è in attesa che si liberi il PC con tale indice
    private Integer[] thesis_waiting; // Il valore dell'elemento i-esimo è il numero di tesisti in attesa di tale PC
    // numero di Professori in attesa di utilizzare il Laboratorio
    private int prof_waiting = 0;
    // Flag che indica se un Professore sta utilizzando il Laboratorio al momento
    private boolean prof_in = false;

    // Costruttore del Laboratorio: prende in input il numero di PC ed il numero di utenti, differenziati per ruolo
    // Inoltre riceve in input un vector contenente tutti gli Utenti (già inizializzati)
    public Laboratorio(int nPC, int nStudenti, int nTesisti, int nProfessori, List<Utente> all_users) {
        // creo tutti i PC
        this.numPCs = nPC;
        this.allPCs = new ArrayList<Computer>(nPC);
        for (int i = 0; i < nPC; i++) {
            this.allPCs.add(new Computer());
        }
        // inizializzo gli utenti e la threadpool
        this.users = all_users;
        this.tot_users = nStudenti + nTesisti + nProfessori;
        // numero fisso di thread, dato che conosco esattamente il numero di utenti
        this.tpool = Executors.newFixedThreadPool(this.tot_users);

        // inizialmente nessuno in attesa
        this.thesis_waiting = new Integer[this.numPCs];
        for (int i = 0; i < nPC; i++) {
            this.thesis_waiting[i] = 0;
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

    // Uno studente ha richiesto l'uso del computer idx per worktime millisecondi
    public void student_request(int idx, long worktime) {
        synchronized (this.allPCs) {
            // se vi è un professore all'interno o in fila per il laboratorio allora attendo
            while (this.prof_in || this.prof_waiting > 0) {
                try {
                    //System.out.println(
                    //        "User " + Thread.currentThread().getId() + " (Studente): Professori in attesa => wait()");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + "(Studente): Attesa interrotta");
                }
            }
            this.occupiedPCs++; // anche se non ho ancora effettivamente occupato il PC incremento il contatore
        }
        // occupa il PC quando diventa libero e non vi è più alcun tesista in attesa sullo stesso PC
        synchronized (this.allPCs.get(idx)) {
            while (!this.allPCs.get(idx).isFree() || this.thesis_waiting[idx] > 0) {
                try {
                    System.out.println("User " + Thread.currentThread().getId()
                            + "(Studente): PC occupato o Tesisti in attesa => wait() su " + idx);
                    this.allPCs.get(idx).wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + "(Studente): Attesa interrotta");
                }
            }

            this.allPCs.get(idx).occupy();
            try {
                Thread.sleep(worktime);
            } catch (Exception e) {
                System.out.println("User " + Thread.currentThread().getId()
                    + " (Studente): lavoro su PC " + idx + " interrotto");
            }
            // quindi il PC viene liberato e l'evento viene notificato prima dell'uscita dal blocco sincronizzato
            // NOTA: notifico a tutti gli Utenti in attesa su questo PC
            // poiché la notifica potrebbe arrivare ad un Utente la cui condizione non è ancora verificata
            this.allPCs.get(idx).free();
            this.allPCs.get(idx).notifyAll();
        }
        // adesso posso decrementare il numero di PC occupati e notificarlo agli Utenti in attesa su allPCs
        // NOTA: Se non lo notificassi a tutti potrei non recapitare la notifica ad un Professore in attesa
        // e questo potrebbe provocare deadlock, in quanto la presenza di un professore non
        // permette l'avanzamento di altri Studenti o Tesisti in attesa
        synchronized (this.allPCs) {
            this.occupiedPCs--;
            this.allPCs.notifyAll();
        }
    }

    // Un tesista ha richiesto l'uso del computer idx per worktime millisecondi
    public void thesis_request(int idx, long worktime) {
        // come sopra, cambiano solo le stampe
        synchronized (this.allPCs) {
            // se vi è un professore in fila per il laboratorio allora attendo
            while (this.prof_in || this.prof_waiting > 0) {
                try {
                    //System.out.println(
                    //        "User " + Thread.currentThread().getId() + " (Tesista): Professori in attesa => wait()");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + " (Tesista): Attesa interrotta");
                }
            }
            this.occupiedPCs++;
        }
        // occupa il PC quando diventa libero (e non vi sono altri tesisti in attesa oltre a lui, per fairness)
        synchronized (this.allPCs.get(idx)) {
            this.thesis_waiting[idx]++;
            while (!this.allPCs.get(idx).isFree()) {
                try {
                    System.out.println("User " + Thread.currentThread().getId()
                            + " (Tesista) PC occupato: wait(" + idx + ")");
                    this.allPCs.get(idx).wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + " (Tesista) Attesa interrotta");
                }
            }
            this.thesis_waiting[idx]--;

            this.allPCs.get(idx).occupy();
            try {
                Thread.sleep(worktime);
            } catch (Exception e) {
                System.out.println("User " + Thread.currentThread().getId()
                    + " (Tesista): lavoro su PC " + idx + " interrotto");
            }
            // valgono le stesse osservazioni fatte in student_request
            this.allPCs.get(idx).free();
            this.allPCs.get(idx).notifyAll();
        }
        // valgono le stesse osservazioni fatte in student_request
        synchronized (this.allPCs) {
            this.occupiedPCs--;
            this.allPCs.notifyAll();
        }
    }

    // Un professore ha richiesto l'uso del laboratorio per worktime millisecondi
    public void professor_request(long worktime) {
        // un Professore deve attendere fino a che tutti i PC del laboratorio non sono liberi
        synchronized (this.allPCs) {
            this.prof_waiting++; // per segnalare che vi è un professore in più in attesa
            while (this.occupiedPCs > 0) {
                try {
                    //System.out.println("User " + Thread.currentThread().getId()
                     //       + " (Professore): Almeno un PC è occupato => wait()");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + " (Professore): Attesa interrotta");
                }
            }
            // Senza uscire dal blocco sincronizzato occupo tutti i PC
            // È garantito che tutti i PC siano liberi per le seguenti ragioni:
            // 1) l'update di occupiedPCs avviene dopo la liberazione del PC
            // 2) Il fatto che il professore fosse in fila blocca altri studenti o tesisti dall'avanzare
            for (Computer c : this.allPCs) {
                c.occupy();
            }
            this.occupiedPCs = this.numPCs;
            this.prof_in = true;
            this.prof_waiting--;
        }

        try {
            Thread.sleep(worktime);
        } catch (Exception e) {
            System.out.println("User " + Thread.currentThread().getId() + " (Professore): lavoro interrotto");
        }
        // Quindi rilascio tutti i PC
        synchronized (this.allPCs) {
            this.occupiedPCs = 0;
            this.prof_in = false;
            System.out.println("User " + Thread.currentThread().getId() + " (Professore): liberati tutti i PC");
            this.allPCs.notifyAll();
            for (Computer c : this.allPCs) {
                synchronized (c) {
                    c.free();
                    c.notifyAll();
                }
            }
        }
    }
}

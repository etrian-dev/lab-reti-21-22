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
    private List<Computer> allPCs;
    // numero di PC contenuti nel laboratorio
    private int numPCs;
    // Tutti gli utenti del laboratorio (studenti, professori e tesisti)
    private List<Utente> users;
    private int tot_users;
    // Threadpool che gestisce l'esecuzione delle task utente
    ExecutorService tpool;
    // Flag per indicare che un tesista è in attesa che si liberi il PC con tale indice
    private Integer[] thesis_waiting; // una flag per ogni PC
    private int prof_waiting = 0;
    private boolean prof_in = false;
    private volatile int occupiedPCs = 0;

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

    public void student_request(int idx, long worktime) {
        synchronized (this.allPCs) {
            // se vi è un professore in fila per il laboratorio allora attendo
            while (this.prof_in || this.prof_waiting > 0) {
                try {
                    System.out.println(
                            "User " + Thread.currentThread().getId() + " (Studente) Professori in attesa: wait()");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + "(Studente) Attesa interrotta");
                }
            }
            this.occupiedPCs++;
        }
        // occupo il PC se quando diventa libero
        synchronized (this.allPCs.get(idx)) {
            while (!this.allPCs.get(idx).isFree() || this.thesis_waiting[idx] > 0) {
                try {
                    System.out.println("User " + Thread.currentThread().getId()
                            + "(Studente) PC occupato o Tesisti in attesa: wait(" + idx + ")");
                    this.allPCs.get(idx).wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + "(Studente) Attesa interrotta");
                }
            }
            this.allPCs.get(idx).occupy();
            // work
            try {
                Thread.sleep(worktime);
            } catch (Exception e) {
                //TODO: handle exception
                ;
            }
            this.allPCs.get(idx).free();
            this.allPCs.get(idx).notify();
        }
        synchronized (this.allPCs) {
            this.occupiedPCs--;
            this.allPCs.notifyAll();
        }
    }

    public void thesis_request(int idx, long worktime) {
        synchronized (this.allPCs) {
            // se vi è un professore in fila per il laboratorio allora attendo
            while (this.prof_in || this.prof_waiting > 0) {
                try {
                    System.out.println(
                            "User " + Thread.currentThread().getId() + " (Tesista) Professori in attesa: wait()");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + " (Tesista) Attesa interrotta");
                }
            }
            this.occupiedPCs++;
        }
        // occupo il PC se quando diventa libero
        synchronized (this.allPCs.get(idx)) {
            while (!this.allPCs.get(idx).isFree() || this.thesis_waiting[idx] > 0) {
                try {
                    System.out.println("User " + Thread.currentThread().getId()
                            + " (Tesista) PC occupato o Tesisti in attesa: wait(" + idx + ")");
                    this.allPCs.get(idx).wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + " (Tesista) Attesa interrotta");
                }
            }
            this.allPCs.get(idx).occupy();
            // work
            try {
                Thread.sleep(worktime);
            } catch (Exception e) {
                //TODO: handle exception
                ;
            }
            this.allPCs.get(idx).free();
            this.allPCs.get(idx).notify();
        }
        synchronized (this.allPCs) {
            this.occupiedPCs--;
            this.allPCs.notifyAll();
        }
    }

    public void professor_request(long worktime) {
        synchronized (this.allPCs) {
            this.prof_waiting++;
            while (this.occupiedPCs > 0) {
                try {
                    System.out.println("User " + Thread.currentThread().getId()
                            + "(Professore) Attendo che laboratorio sia libero");
                    this.allPCs.wait();
                } catch (Exception e) {
                    System.out.println("User " + Thread.currentThread().getId() + "(Professore) Attesa interrotta");
                }
            }
            this.prof_waiting--;
            this.prof_in = true;

            for (Computer c : this.allPCs) {
                c.occupy();
            }
            this.occupiedPCs = this.numPCs;
        }
        // work
        try {
            Thread.sleep(worktime);
        } catch (Exception e) {
            //TODO: handle exception
            ;
        }
        synchronized (this.allPCs) {
            this.occupiedPCs = 0;
            this.prof_in = false;
            System.out.println("User " + Thread.currentThread().getId() + "(Professore) liberati tutti i PC");
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

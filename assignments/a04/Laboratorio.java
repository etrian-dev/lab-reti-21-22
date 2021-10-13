import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private List<Boolean> thesis_waiting; // una flag per ogni PC
    private boolean prof_waiting = false;
    private AtomicInteger occupiedPCs;

    // Costruttore del Laboratorio: prende in input il numero di PC ed il numero di utenti, differenziati per ruolo
    // Inoltre riceve in input un vector contenente tutti gli Utenti (già inizializzati)
    public Laboratorio(int nPC, int nStudenti, int nTesisti, int nProfessori, Vector<Utente> all_users) {
        // creo tutti i PC
        this.numPCs = nPC;
        this.allPCs = new ArrayList<Computer>(nPC);
        for (int i = 0; i < nPC; i++) {
            this.allPCs.add(new Computer(i));
        }
        this.occupiedPCs.set(0);
        // inizializzo gli utenti e la threadpool
        this.users = all_users;
        this.tot_users = nStudenti + nTesisti + nProfessori;
        // numero fisso di thread, dato che conosco esattamente il numero di utenti
        this.tpool = Executors.newFixedThreadPool(this.tot_users);
        // inizialmente nessuno in attesa
        this.thesis_waiting = new ArrayList<Boolean>(this.numPCs);
        for (int i = 0; i < nPC; i++) {
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
        // se who è uno studente o un tesista allora devono aspettare che non vi siano professori in attesa
        synchronized(allPCs) {
            while(this.prof_waiting) {
                try {
                    allPCs.wait();
                } catch (Exception e) {
                    System.out.println("Attesa studente o tesista su lab interrotta");
                }
            }
            if (who instanceof Studente) {
                // serializzo accesso al pc richiesto
                synchronized(allPCs.get(idx)) {
                    while(!allPCs.get(idx).isFree() || this.thesis_waiting.get(idx)) {
                        try {
                            allPCs.get(idx).wait();
                        } catch (Exception e) {
                            System.out.println("Attesa studente interrotta");
                        }
                    }
                    // occupy this PC
                    allPCs.get(idx).occupy();
                    int pc_left = this.occupiedPCs.incrementAndGet();
                    System.out.println("(Studente) Occupato il PC" + idx + ": " + (this.numPCs - pc_left) + "liberi");
                }
            }

            if (who instanceof Tesista) {
                synchronized(allPCs.get(idx)) {
                    while(!allPCs.get(idx).isFree() || this.prof_waiting) {
                        try {
                            allPCs.get(idx).wait();
                        } catch (Exception e) {
                            System.out.println("Attesa tesista interrotta");
                        }
                    }
                    // occupy this PC
                    allPCs.get(idx).occupy();
                    int pc_left = this.occupiedPCs.incrementAndGet();
                    System.out.println("(Studente) Occupato il PC" + idx + ": " + (this.numPCs - pc_left) + "liberi");
                }
            }
        }

        // Se è un professore deve aspettare finchè tutti i PC sono liberi
        if (who instanceof Professore) {
            synchronized(allPCs) {
                while(this.occupiedPCs.get() > 0) {
                    try {
                        allPCs.wait();
                    } catch (Exception e) {
                        System.out.println("Attess")
                    }
                }
            }
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

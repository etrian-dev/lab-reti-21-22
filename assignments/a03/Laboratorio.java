import java.util.Vector;
import java.lang.Math;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Classe che contiene l'implementazione del laboratorio di informatica
 */
public class Laboratorio extends Thread {
    private static final Random rng = new Random(System.currentTimeMillis());
    // Ciascun PC è rappresentato da una lock + variabile di condizione
    private Vector<Lock> all_PCs;
    private Vector<Condition> pc_states;
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
        this.all_PCs = new Vector<Lock>(nPC);
        this.pc_states = new Vector<Condition>(nPC);
        for(int i = 0; i < nPC; i++) {
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
        this.thesis_waiting = new Vector<Boolean>(this.all_PCs.size());
        for(int i = 0; i < this.all_PCs.size(); i++) {
            this.thesis_waiting.add(false);
        }
        this.professor_waiting = false;
    }

    // Il thread Laboratorio prende un utente a caso nell'array, lo rimuove e fa eseguire
    // tale task alla threadpool, fino a che non vi sono più utenti da servire
    public void run() {
        int usersLeft = this.tot_users;
        while(usersLeft > 0) {
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
        while(!tpool.isTerminated()) {
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
        // studente o tesista prendono lock sul PC richiesto e salvano la relativa condition variable
        if(who instanceof Studente || who instanceof Tesista) {
            pc = this.all_PCs.get(idx);
            cv = this.pc_states.get(idx);
            pc.lock();
        }

        // Se uno studente ha ottenuto lock sul PC allora esamina le flag
        // e si mette in attesa fino a che ci sono professori o tesisti in attesa
        if(who instanceof Studente) {
            while(this.thesis_waiting.get(idx) || this.professor_waiting) {
                try {
                    cv.await();
                } catch (InterruptedException ex) {
                    System.out.println("Thread studente interrotto durante l'attesa");
                }
            }
        }
        // Se è un tesista, similmente, aspetta finchè non ci sono professori in attesa
        else if(who instanceof Tesista) {
            while(this.professor_waiting) {
                this.thesis_waiting.setElementAt(true, idx); // il tesista si mette in attesa: deve segnalarlo
                try {
                    cv.await();
                } catch (InterruptedException ex) {
                    System.out.println("Thread tesista interrotto durante l'attesa");
                }
            }
            // Il tesista ha ottenuto il PC: rimetto a false la flag
            this.thesis_waiting.setElementAt(false, idx);
        }
        // Se è un professore deve aspettare finchè tutti i PC sono liberi, quindi cerca di
        // prendere la ME dal primo all'ultimo, in ordine, settando la flag per evitare che
        // ad altri studenti o tesisti sia assegnato un PC mentre lui sta attendendo che uno con indice
        // minore si liberi (cosa che prima o poi avverrà)
        else if(who instanceof Professore) {
            int numPCs = this.all_PCs.size();
            int obtained = 0;
            int i = 0;
            Vector<Boolean> obtained_PCs = new Vector<Boolean>(numPCs);
            for(i = 0; i < numPCs; i++) {
                obtained_PCs.add(false);
            }

            this.professor_waiting = true;
            // finché non ottiene tutti i PC il professore rimane in attesa
            while(obtained < numPCs) {
                for(i = 0; i < numPCs; i++) {
                    // tento di ottenere lock sul PC solo se non è stato già ottenuto
                    if(obtained_PCs.get(i) == false) {
                        if(this.all_PCs.get(i).tryLock()) {
                            // se la tryLock ha successo allora è stato ottenuto il PC
                            obtained_PCs.setElementAt(true, i);
                            obtained++;
                        }
                        else {
                            // attendo di ottenere il PC con indice i
                            // NOTA: un modo alternativo di gestire questo aspetto sarebbe stato di
                            // non fare await() su questo pc e passare al prossimo
                            // ma ciò creerebbe attesa attiva, riducendo potenzialmente il troughput
                            //try {
                                //this.pc_states.get(i).await();
                            //} catch (InterruptedException ex) {
                                //;
                            //}
                        }
                    }
                }
            }
            // Il professore ha ottenuto tutti i PC: rimetto a false la flag
            this.professor_waiting = false;
        }
    }

    public void freePC(Object who, int idx) {
        // Se il PC è rilasciato da uno studente o tesista si segnala solo chi era in attesa di quel PC
        if(who instanceof Studente || who instanceof Tesista) {
            Lock l = this.all_PCs.get(idx);
            Condition cv = this.pc_states.get(idx);
            cv.signal();

            l.unlock();
        }
        // invece se era un Professore segnalo tutte le CV (libera tutti i PC)
        else if(who instanceof Professore) {
            for(Condition c : this.pc_states) {
                c.signal();
            }
            for(Lock l : this.all_PCs) {
                l.unlock();
            }
        }
    }
}

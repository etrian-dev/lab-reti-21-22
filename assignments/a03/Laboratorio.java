import java.util.Vector;
import java.lang.Math;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

public class Laboratorio extends Thread{
    private Vector<Lock> all_PCs;
    private Vector<Condition> pc_states;
    private Vector<Utente> users;
    ExecutorService tpool;
    private boolean thesis_waiting;
    private boolean professor_waiting;
    private int tot_users;

    public Laboratorio(int nPC, int nStudenti, int nTesisti, int nProfessori, Vector<Utente> all_users) {
        // creo tutti i PC
        this.all_PCs = new Vector<Lock>(nPC);
        this.pc_states = new Vector<Condition>(nPC);
        this.users = all_users;
        for(int i = 0; i < nPC; i++) {
            Lock l = new ReentrantLock();
            this.all_PCs.add(l);
            this.pc_states.add(l.newCondition());
        }
        this.tot_users = nStudenti + nTesisti + nProfessori;
        this.thesis_waiting = false;
        this.professor_waiting = false;
        // creo una threadpool (fixed, perché ne conosco il numero) per ogni Utente
        this.tpool = Executors.newFixedThreadPool(this.tot_users);
    }

    public void run() {
        final Random rng = new Random(System.currentTimeMillis());
        // prendo un Utente a caso e lo eseguo nel thread dedicato
        int usersLeft = this.tot_users;
        while(usersLeft > 0) {
            int idx = Math.abs(rng.nextInt()) % usersLeft;
            Utente user = this.users.remove(idx);
            tpool.execute(user);
            usersLeft--;
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

    // un Utente fa una richiesta per il PC idx (se who è un professore il parametro è ignorato)
    public void request(Object who, int idx) {
        Lock pc = null;
        Condition cv = null;

        if(who instanceof Studente || who instanceof Tesista) {
            pc = this.all_PCs.get(idx);
            cv = this.pc_states.get(idx);
            pc.lock();
        }

        // Se è uno studente ad aver richesto il PC, allora si mette in attesa finchè non ci sono professori o tesisti in attesa
        if(who instanceof Studente) {
            while(this.thesis_waiting || this.professor_waiting) {
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
                this.thesis_waiting = true;
                try {
                    cv.await();
                } catch (InterruptedException ex) {
                    System.out.println("Thread studente interrotto durante l'attesa");
                }
            }
            // Il tesista ha ottenuto il PC: rimetto a false la flag
            this.thesis_waiting = false;
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
            while(obtained < numPCs) {
                for(i = 0; i < numPCs; i++) {
                    if(obtained_PCs.get(i).booleanValue() == false && this.all_PCs.get(i).tryLock()) {
                        obtained_PCs.set(i, true);
                        obtained++;
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

    public static void main(String[] args) {
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
            all_users.add(new Tesista(numPC, lab, i));
        }
        for(int i = 0; i < professors; i++) {
            all_users.add(new Professore(numPC, lab));
        }

        lab.start();
    }
}

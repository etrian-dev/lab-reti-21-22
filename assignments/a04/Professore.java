import java.lang.Math;

/**
 * Classe Professore: richiede tutti i PC del laboratorio contemporaneamente
 */
public class Professore extends Utente {
    public Professore(int nPCs, Laboratorio lab) {
        super(nPCs, lab);
    }

    public void run() {
        while (super.numAccesses > 0) {
            long interval = Math.abs(super.rng.nextLong()) % 100;
            long workTime = Math.abs(super.rng.nextLong()) % 100;
            try {
                // il professore richiede tutti i PC
                super.laboratorio.professor_request(workTime);
                System.out.printf("User %d (Professore): ottenuto tutti i PC\n", Thread.currentThread().getId());
                // il professore usa il laboratorio
                //Thread.sleep(workTime);

                System.out.printf("User %d (Professore): ho lavorato per %dms su tutti i PC\n",
                        Thread.currentThread().getId(), workTime);
                // il professore ha terminato: libera tutti i PC
                //super.laboratorio.freePC(this, -1);

                System.out.printf("User %d (Professore): aspetto %dms\n", Thread.currentThread().getId(), interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, -1);
            }
            super.numAccesses--;
        }
    }
}

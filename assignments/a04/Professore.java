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
                // il professore richiede tutti i PC e lavora su di essi per il tempo richiesto
                super.laboratorio.professor_request(workTime);
                System.out.printf("User %d (Professore):"
                    + "\n\tottenuto il laboratorio"
                    + "\n\tho lavorato per %dms su tutti i PC"
                    + "\n\taspetto %dms\n"
                    , Thread.currentThread().getId(), workTime, interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, -1);
            }
            super.numAccesses--;
        }
    }
}

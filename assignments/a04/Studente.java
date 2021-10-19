import java.lang.Math;

/**
 * Classe studente: richiede l'utilizzo di un PC qualsiasi
 */
public class Studente extends Utente {
    public Studente(int nPCs, Laboratorio lab) {
        super(nPCs, lab);
    }

    public void run() {
        while (super.numAccesses > 0) {
            long interval = Math.abs(super.rng.nextLong()) % 100;
            long workTime = Math.abs(super.rng.nextLong()) % 100;
            int requested = 0;
            try {
                // lo studente richiede il PC e lavora su di esso per il tempo richiesto
                requested = Math.abs(super.rng.nextInt()) % super.maxPC;
                super.laboratorio.student_request(requested, workTime);
                System.out.printf("User %d (Studente):"
                    + "\n\tottenuto il PC %d"
                    + "\n\tho lavorato per %dms sul PC %d"
                    + "\n\taspetto %dms\n"
                    , Thread.currentThread().getId(), requested, workTime, requested, interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, requested);
            }
            super.numAccesses--;
        }
    }
}

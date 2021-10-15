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
                // lo studente richiede il PC
                requested = Math.abs(super.rng.nextInt()) % super.maxPC;
                super.laboratorio.student_request(requested, workTime);
                System.out.printf("User %d (Studente): ottenuto il PC %d\n", Thread.currentThread().getId(), requested);
                // lo studente usa il PC
                //Thread.sleep(workTime);

                System.out.printf("User %d (Studente): ho lavorato per %dms su %d\n", Thread.currentThread().getId(),
                        workTime, requested);
                // lo studente ha terminato: libera il PC
                //super.laboratorio.freePC(this, requested);

                System.out.printf("User %d (Studente): aspetto %dms\n", Thread.currentThread().getId(), interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, requested);
            }
            super.numAccesses--;
        }
    }
}

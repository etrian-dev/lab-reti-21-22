import java.lang.Math;

/**
 * Classe Tesista: richede l'utilizzo sempre dello stesso PC
 */
public class Tesista extends Utente {
    private int myPC;

    public Tesista(int nPCs, Laboratorio lab, int hisPC) {
        super(nPCs, lab);
        this.myPC = hisPC;
    }

    public void run() {
        while (super.numAccesses > 0) {
            long interval = Math.abs(super.rng.nextLong()) % 100;
            long workTime = Math.abs(super.rng.nextLong()) % 100;
            try {
                // il tesista richiede il suo PC e lavora su di esso per il tempo richiesto
                super.laboratorio.thesis_request(this.myPC, workTime);
                System.out.printf("User %d (Tesista):"
                    + "\n\tottenuto il PC %d"
                    + "\n\tho lavorato per %dms sul PC %d"
                    + "\n\taspetto %dms\n"
                    , Thread.currentThread().getId(), this.myPC, workTime, this.myPC, interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, this.myPC);
            }
            super.numAccesses--;
        }
    }
}

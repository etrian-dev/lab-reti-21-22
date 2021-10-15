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
                // il tesista richiede il suo PC
                super.laboratorio.thesis_request(this.myPC, workTime);
                System.out.printf("User %d (Tesista): ottenuto il PC %d\n", Thread.currentThread().getId(), this.myPC);
                // lo studente usa il PC
                //Thread.sleep(interval);

                System.out.printf("User %d (Tesista): ho lavorato per %dms su %d\n", Thread.currentThread().getId(),
                        workTime, this.myPC);
                // il tesista ha terminato: libera il PC
                //super.laboratorio.freePC(this, this.myPC);

                System.out.printf("User %d (Tesista): aspetto %dms\n", Thread.currentThread().getId(), interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                //super.laboratorio.freePC(this, this.myPC);
            }
            super.numAccesses--;
        }
    }
}

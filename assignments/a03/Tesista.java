import java.lang.Math;

public class Tesista extends Utente implements Runnable {
    private int myPC;

    public Tesista(int nPCs, Laboratorio lab, int hisPC) {
        super(nPCs, lab);
        this.myPC = hisPC;
    }

    public void run() {
        while(super.numAccesses > 0) {
            long interval = Math.abs(super.rng.nextLong()) % 100;
            long workTime = Math.abs(super.rng.nextLong()) % 100;
            try {
                // il tesista richiede il suo PC
                super.laboratorio.request(this, this.myPC);
                System.out.printf("User %d (Tesista): ottenuto il PC %d\n", Thread.currentThread().getId(), this.myPC);
                System.out.printf("User %d (Tesista): lavoro per %dms su %d\n", Thread.currentThread().getId(), workTime, this.myPC);
                // lo studente usa il PC
                Thread.sleep(interval);

                // il tesista ha terminato: libera il PC
                super.laboratorio.freePC(this, this.myPC);

                System.out.printf("User %d (Tesista): aspetto %dms\n", Thread.currentThread().getId(), interval);
                // attesa tra richieste successive
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                super.laboratorio.freePC(this, this.myPC);
            }
            super.numAccesses--;
        }
    }
}

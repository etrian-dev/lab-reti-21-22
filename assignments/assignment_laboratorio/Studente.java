import java.lang.Math;
import java.util.Random;
import java.util.Vector;

public class Studente implements Utente,Runnable {
    private long numAccesses;
    private int maxPC;
    private static final Random rng = new Random(System.currentTimeMillis());

    public Studente(int nPCs, Vector<Computer> all_PCs) {
        /// TODO: improve
        this.numAccesses = Math.round(Math.random() * 1000);
        this.maxPC = nPCs;
    }

    public void makeRequest() {
        int request = rng.nextInt() % this.maxPC + 1;
        
    }

    public void run() {
        while(this.numAccesses > 0) {
            long interval = Math.round(Math.random() * 1000);
            long workTime = Math.round(Math.random() * 1000);
            try {
                Thread.sleep(interval);
                makeRequest();
                Thread.sleep(interval);
            } catch (Exception e) {
                //TODO: handle exception
                ;
            }
        }
    }
}

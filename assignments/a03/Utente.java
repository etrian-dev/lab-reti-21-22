import java.util.Random;
import java.lang.Math;

public abstract class Utente implements Runnable{
    protected long numAccesses;
    protected int maxPC;
    protected static final Random rng = new Random(System.currentTimeMillis());
    protected Laboratorio laboratorio;

    public Utente(int nPCs, Laboratorio lab) {
        /// TODO: improve
        this.numAccesses = Math.abs(rng.nextLong()) % 5;
        this.maxPC = nPCs;
        this.laboratorio = lab;
    }
    public abstract void run();
}

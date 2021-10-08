import java.util.Random;
import java.lang.Math;

/**
 * Classe Utente (di cui Studente, Professore e Tesista sono sottoclassi):
 * definisce il costruttore del generico Utente e sceglie il numero di accessi che tale Utente
 * compierà durante la simulazione
 */
public abstract class Utente implements Runnable{
    protected long numAccesses;
    protected int maxPC;
    protected static final Random rng = new Random(System.currentTimeMillis());
    protected Laboratorio laboratorio;

    // Il costruttore di Utente riceve in input il numero di PC del laboratorio ed un riferimento al laboratorio
    public Utente(int nPCs, Laboratorio lab) {
        // limito il numero di accessi in [1,10] per evitare output verboso
        this.numAccesses = 1 + Math.abs(rng.nextLong()) % 10;
        this.maxPC = nPCs;
        this.laboratorio = lab;
    }
    // il metodo run è diverso a seconda delle sottoclassi, per cui viene definito in esse
    public abstract void run();
}

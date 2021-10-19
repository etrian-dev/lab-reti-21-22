/**
 * Classe che incapsula lo stato di un PC del Laboratorio
 */
public class Computer {
    // stato del PC: free = true sse un Utente sta utilizzando il computer
    private boolean free;

    public Computer() {
        this.free = true;
    }

    public boolean isFree() {
        return this.free;
    }

    public void occupy() {
        this.free = false;
    }

    public void free() {
        this.free = true;
    }
}

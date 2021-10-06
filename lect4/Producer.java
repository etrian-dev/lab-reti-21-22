import java.util.Random;

/*
* 3. Definire un task Producer il cui metodo costruttore prende in ingresso il riferimento ad
* un’istanza di Dropbox. Nel metodo run genera un intero in modo random, nel range [0,100), e
* invoca il metodo put sull’istanza di Dropbox.
*/
import java.util.Random;

public class Producer implements Runnable {
    private static Random rng = new Random();
    private Dropbox box;
    
    public Producer(Dropbox box) {
        this.box = box;
    }

    public void run() {
        int x = Math.abs(rng.nextInt()) % 100;
        this.box.put(x);
    }
}
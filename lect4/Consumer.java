/*
 * 2. Definire un task Consumer il cui metodo costruttore prende in ingresso un valore booleano
 * (true per consumare valori pari e false per valori dispari) e il riferimento ad un’istanza di
 * Dropbox. Nel metodo run invoca il metodo take sull’istanza di Dropbox.
 */

public class Consumer implements Runnable {
    private boolean consume_even;
    private Dropbox box;

    public Consumer(boolean parity, Dropbox box) {
        this.consume_even = parity;
        this.box = box;
    }

    public void run() {
        int x;
        while(true) {
            if(this.consume_even) {
                x = this.box.take(true);
            }
            else {
                x = this.box.take(false);
            }
            System.out.println("Consumed " + x);
        }
    }
}

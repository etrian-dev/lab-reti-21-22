/*
 * 5. Estendere la classe Dropbox (overriding dei metodi take e put) usando il costrutto del
 * monitor per gestire l’accesso di Consumer e Producer al buffer. Notare la differenza nell’uso
 * di notify vs notifyall
 */
public class MyDropbox extends Dropbox {
    Object lock = new Object();

    @Override
    public synchronized int take(boolean e) {
        String s = e ? "Pari" : "Dispari";
        int n = super.num;
            while(!super.full || (super.num % 2 == 0) != e) {
                try {
                    System.out.println("Attendi per: " + s);
                    this.wait();
                }
                catch (InterruptedException ex) {
                    System.out.println("Interrupted while waiting for a suitable number");
                }
            }

            n = num;
            super.full = false;
            System.out.println(s + " <-> " + num);
            this.notifyAll();
        return n;
    }

    @Override
    public synchronized void put(int n) {
        try {
           while(super.full) {
               this.wait();
           }
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while waiting for the box to be empty");
        }
        System.out.println("Producer ha inserito " + n);
        super.num = n;
        super.full = true;

        this.notify();
    }
}

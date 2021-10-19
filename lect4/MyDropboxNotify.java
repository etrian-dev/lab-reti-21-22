/// NOTE: This is broken

/*
 * 5. Estendere la classe Dropbox (overriding dei metodi take e put) usando il costrutto del
 * monitor per gestire l’accesso di Consumer e Producer al buffer. Notare la differenza nell’uso
 * di notify vs notifyall
 */
public class MyDropboxNotify extends Dropbox {
    private Object new_even = new Object();
    private Object new_odd = new Object();

    @Override
    public synchronized int take(boolean e) {
        String s = e ? "Pari" : "Dispari";
        int n;

        if(e) {
            synchronized(this.new_even) {
                n = super.num;
                try {
                    while(n % 2 != 0) {
                        this.new_even.wait();
                    }
                } catch (InterruptedException ex) {;}
                super.full = false;

                this.new_even.notifyAll();
            }
        }
        else {
            synchronized(this.new_odd) {
                n = super.num;
                 try {
                    while(n % 2 == 0) {
                        this.new_odd.wait();
                    }
                } catch (InterruptedException ex) {;}
                super.full = false;

                this.new_odd.notifyAll();
            }
        }
        System.out.println(s + " <-> " + n);
        // Se questa chiamata fosse notify allora andrei a svegliare solo un thread in attesa
        // e questo thread potrebbe non essere il producer, pertanto il thread svegliato
        // potrebbe verificare che la condizione su cui era sospeso è ancora falsa e
        // rimettersi a dormire: perdo la notify per il consumer e posso andare in deadlock

        return n;
    }

    @Override
    public synchronized void put(int n) {
        if(n % 2 == 0) {
            synchronized(this.new_even) {
                try {
                   while(super.full) {
                       System.out.println("Dropbox is full. Waiting for it to be empty");
                       this.wait();
                   }
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted while waiting for the box to be empty");
                }
                System.out.println("Inserted " + n);
                super.num = n;
                super.full = true;
                new_even.notify();
            }
        }
        else {
            synchronized(this.new_odd) {
                try {
                   while(super.full) {
                       System.out.println("Dropbox is full. Waiting for it to be empty");
                       this.wait();
                   }
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted while waiting for the box to be empty");
                }
                System.out.println("Inserted " + n);
                super.num = n;
                super.full = true;
                new_odd.notify();
            }
        }
    }
}

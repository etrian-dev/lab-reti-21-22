/*
 * 5. Estendere la classe Dropbox (overriding dei metodi take e put) usando il costrutto del
 * monitor per gestire l’accesso di Consumer e Producer al buffer. Notare la differenza nell’uso
 * di notify vs notifyall
 */
public class MyDropboxNotifyAll extends Dropbox {
    @Override
    public synchronized int take(boolean e) {
        String s = e ? "Pari" : "Dispari";
        while(!super.full || (super.num % 2 == 0) != e) {
            try {
                System.out.println("Attendi per: " + s);
                this.wait();
            }
            catch (InterruptedException ex) {
                System.out.println("Interrupted while waiting for a suitable number");
            }
        }

        int n = num;
        super.full = false;
        System.out.println(s + " <-> " + num);
        // Se questa chiamata fosse notify allora andrei a svegliare solo un thread in attesa
        // e questo thread potrebbe non essere il producer, pertanto il thread svegliato
        // potrebbe verificare che la condizione su cui era sospeso è ancora falsa e
        // rimettersi a dormire: perdo la notify per il consumer e posso andare in deadlock
        this.notifyAll();
        return n;
    }

    @Override
    public synchronized void put(int n) {
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
        // Vale quanto osservato nella notify per il metodo take: se produco un valore dispari ma
        // la notifica è ricevuta dal consumatore pari allora andrà in deadlock, perché l'altro
        // thread non viene mai svegliato
        //(il consumer alla prossima chiamata a put si sospende perché il buffer è ancora pieno)
        this.notifyAll();
    }
}

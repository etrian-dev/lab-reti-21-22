/*
 * 4. Definire una classe contenente il metodo main. Nel main viene creata un’istanza di Dropbox.
 * Vengono quindi creati 2 oggetti di tipo Consumer (uno true e uno false) e un oggetto di tipo
 * Producer, ciascuno eseguito da un thread distinto.
 */

public class Main {
    public static void main(String[] args) {
        //Dropbox db = new Dropbox();
        MyDropbox db = new MyDropbox();
        Consumer c1 = new Consumer(false, db);
        Consumer c2 = new Consumer(true, db);
        Producer p = new Producer(db);
        Thread t1 = new Thread(c1);
        Thread t2 = new Thread(c2);
        Thread t3 = new Thread(p);
        t1.start();
        t2.start();
        t3.start();
    }
}
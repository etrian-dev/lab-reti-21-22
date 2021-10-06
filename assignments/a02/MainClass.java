import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        // creo una istanza di Scanner per leggere la capacità della sala interna ed il numero di clienti
        Scanner scan = new Scanner(System.in);
        int k = 0, clients = 0;
        try {
            System.out.print("Capacità sala interna: ");
            k = scan.nextInt();
            System.out.print("Numero clienti: ");
            clients = scan.nextInt();
        } catch (Exception e) {
            System.out.println("Fallita la lettura di uno dei parametri");
            scan.close();
            return;
        }
        scan.close();
        if (k <= 0 || clients <= 0) {
            System.out.println("Valore parametri non valido");
            return;
        }

        // inizializzo una BlockingQueue e vi inserisco tutti i clienti
        BlockingQueue<Person> all_the_people = new LinkedBlockingQueue<Person>(clients);
        for (int i = 0; i < clients; i++) {
            if (!all_the_people.offer(new Person(i))) {
                System.out.printf("Cliente %d: fallito inserimento in coda", i);
            }
        }
        // creo un ufficio postale la cui sala d'attesa interna può ospitare fino a k persone
        PostOffice office = new PostOffice(k, all_the_people);
        office.start();
        // lo shutdown della threadpool avviene nel metodo run() di PostOffice
    }
}

import java.util.Scanner; // Utilizzo la classe Scanner per ricevere input da tastiera

public class MainClass {
    public static void main(String[] args) {
        // Prendo input da standard input
        Scanner scan = new Scanner(System.in);
        // Chiedo all'utente di immettere i valori (con gestione di eventuali eccezioni)
        double accuracy = 0;
        long max_time = 0;
        try {
                // cicla finchè l'utente non inserisce un valore strettamente positivo
                // per entrambi i parametri
                while(accuracy <= 0) {
                        System.out.print("Precisione (reale, >0): ");
                        accuracy = scan.nextDouble();
                }
                while(max_time <= 0) {
                        System.out.print("Tempo massimo (millisecondi, >0): ");
                        max_time = scan.nextLong();
                }
        }
        catch (Exception e) {
                System.out.println("Errore nella lettura dell'input");
        }
        scan.close();

        // Creo un oggetto della classe che definisce la task, dando come parametro la precisione
        // poi creo e avvio l'esecuzione di un thread che esegue tale task
        LeibnitzSeries calcPI = new LeibnitzSeries(accuracy);
        Thread t = new Thread(calcPI);
        t.start();
        // join() aspetta al più max_time millisecondi; se entro tale intervallo di tempo
        // il thread non è terminato allora viene interrotto
        try {
                t.join(max_time);
        }
        catch (InterruptedException ex) {
                // anche il thread main potrebbe essere interrotto prima che la join ritorni
                System.out.println("Thread main interrotto");
        }
        if(t.isAlive()) {
            t.interrupt();
        }
    }
}

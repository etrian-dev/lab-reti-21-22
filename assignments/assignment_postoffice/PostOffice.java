import java.util.concurrent.*;

public class PostOffice extends Thread {
    private ThreadPoolExecutor sportelli;
    private BlockingQueue<Person> clientQueue;

    public PostOffice(long roomCap, BlockingQueue<Person> clients) {
        // 4 sportelli => 4 thread
        this.sportelli = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>((int) roomCap));
        // la coda di clienti in attesa di accedere alla seconda sala (unbounded)
        this.clientQueue = clients;
    }

    public void run() {
        while (this.clientQueue.size() > 0) {
            // esamino la testa della coda della prima sala
            Person p = this.clientQueue.peek();
            try {
                if (p != null) {
                    // cerco di far passare il cliente nella seconda sala, ma se è piena
                    // viene lanciata l'eccezione RejectedExecutionException. Altrimenti
                    // devo rimuovere la task dalla coda della prima sala, perché è stata accettata
                    this.sportelli.execute(p);
                    this.clientQueue.take();
                    System.out.printf("Cliente %d: entrato nella sala interna\n", p.myNumber());
                }
            } catch (RejectedExecutionException noex) {
                System.out.printf("Cliente %d: Sala interna piena: aspetto 50ms\n", p.myNumber());
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.out.printf("Cliente %d: Risvegliato\n", p.myNumber());
                }
            } catch (InterruptedException ex) {
                // potrei ricevere un interrupt anche mentre sto rimuovendo il cliente dalla coda della prima sala
                System.out.println("Cliente %d: Interrotto durante la rimozione dalla coda");
            }
        }

        // la coda della prima sala è vuota, per cui lancio lo shutdown e faccio in modo da far terminare tutti i thread presenti
        this.sportelli.shutdown();
        if (!this.sportelli.isShutdown()) {
            try {
                // potrebbe essere interrotto durante l'attesa di terminazione dei thread del pool, ma è improbabile
                this.sportelli.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                System.out.println(
                        "Thread interrotto durante attesa shutdown threadpool. Chiamo shutdownNow()");
                this.sportelli.shutdownNow();
            }
        }
    }
}

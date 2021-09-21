import java.util.concurrent.*;
import java.util.Vector;

public class PostOffice extends Thread {
    private ThreadPoolExecutor sportelli;
    private Vector<Person> clientQueue;

    public PostOffice(long roomCap, Vector<Person> clients) {
        // 4 sportelli => 4 thread
        this.sportelli = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>((int)roomCap));
        // la coda di clienti in attesa di accedere alla seconda sala (unbounded)
        this.clientQueue = clients;
    }

    public void run() {
        while(this.clientQueue.size() > 0) {
            Person p = this.clientQueue.remove(0);
            try {
                this.sportelli.execute(p);
            } catch (RejectedExecutionException noex) {
                System.out.printf("Cliente %d: Sala interna piena\n", p.myNumber());
            }
        }
        this.sportelli.shutdown();
        while(!this.sportelli.isShutdown()) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                //TODO: handle exception
                ;
            }
        }
    }
}

import java.util.concurrent.*;

public class Tickets {
    public static void main(String[] args) {
        // spawns # of ticket machines threads
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(10);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, q);
        for(int i = 0; i < 50; i++) {
            Person traveler = new Person(i);
            try {
                pool.execute(traveler);
                Thread.sleep(50);  
            } catch (RejectedExecutionException ex) {
                System.out.printf("Traveler no. %d: sala esaurita\n", i);
            } catch (InterruptedException e2) {
                System.out.println("Interrupted main thread");
            }
        }
        System.out.println("Tasks: " + pool.getTaskCount());
        pool.shutdown();
        while(!pool.isShutdown()) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                //TODO: handle exception
                ;
            }
        }
    }

}
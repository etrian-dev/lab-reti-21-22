import java.util.concurrent.*; 

public class Main {
    public static void main(String[] args) {
        Counter c = new Counter();
        ExecutorService tpool = Executors.newFixedThreadPool(10);

        long start = System.currentTimeMillis();

        for(int i = 0; i < 20; i++) {
            Writer w = new Writer(c);
            tpool.submit(w);
        }
        for(int i = 0; i < 20; i++) {
            Reader r = new Reader(c);
            tpool.submit(r);
        }

        tpool.shutdown();
        while(!tpool.isShutdown()) {
            try {
                tpool.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException ex) {
                ;
            }
            finally {
                tpool.shutdownNow();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + "ms");
    }
}
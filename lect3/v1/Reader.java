import java.util.concurrent.*;

public class Reader implements Runnable {
    private Counter counter;

    public Reader(Counter c) {
        this.counter = c;
    }
    public void run() {
        long res = this.counter.get();
        System.out.println(res);
    }
}
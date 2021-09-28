public class Writer implements Runnable {
    private Counter counter;

    public Writer(Counter c) {
        this.counter = c;
    }
    public void run() {
        this.counter.increment();
    }
}
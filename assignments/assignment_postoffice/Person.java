public class Person implements Runnable {
    private long queueNumber;

    public Person(long num) {
        this.queueNumber = num;
    }

    public long myNumber() {
        return this.queueNumber;
    }

    public void run() {
        System.out.printf("Cliente %d: entrato nello sportello\n", this.queueNumber);
        // aspetta da 0 a 1000 ms
        try {
            Thread.sleep(Math.round(Math.random() * 1000));
        } catch (InterruptedException ex) {
            ;
        }
        System.out.printf("Cliente %d: operazione conclusa: esco dallo sportello\n", this.queueNumber);
    }
}

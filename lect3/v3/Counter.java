import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {
    long value;
    ReentrantReadWriteLock rlock = new ReentrantReadWriteLock();

    public Counter() {
        this.value = 0;
    }

    public void increment() {
        Lock l = rlock.writeLock();
        l.lock();
        value++;
        l.unlock();
    }
    public long get() {
        Lock l = rlock.readLock();
        l.lock();
        long res = value;
        l.unlock();
        return res;
    }
}
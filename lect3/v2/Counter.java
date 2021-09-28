import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {
    long value;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    public Counter() {
        this.value = 0;
    }

    public void increment() {
        Lock l = rwlock.writeLock();
        l.lock();
        value++;
        l.unlock();
    }
    public long get() {
        Lock l = rwlock.readLock();
        l.lock();
        long res = value;
        l.unlock();
        return res;
    }
}
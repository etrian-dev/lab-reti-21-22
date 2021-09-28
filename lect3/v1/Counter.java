import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {
    long value;
    ReentrantLock lock = new ReentrantLock();

    public Counter() {
        this.value = 0;
    }

    public void increment() {
        this.lock.lock();
        value++;
        this.lock.unlock();
    }
    
    public long get() {
        this.lock.lock();
        long res = value;
        this.lock.unlock();
        return res;
    }
}
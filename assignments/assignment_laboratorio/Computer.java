import java.util.concurrent.locks.*;

public class Computer {
    private int index;
    private final Lock l = new ReentrantLock();
    private Condition isOccupied = l.newCondition();

    public Computer(int i) {
        this.index = i;
    }


}

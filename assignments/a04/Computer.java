public class Computer {
    private int idx;
    private boolean free;

    public Computer(int id) {
        this.idx = id;
        this.free = true;
    }

    public boolean isFree() {
        return this.free;
    }

    public void occupy() {
        this.free = false;
    }
    public void free() {
        this.free = true;
    }
}
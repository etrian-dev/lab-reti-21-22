public class Computer {
    private boolean free;

    public Computer() {
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
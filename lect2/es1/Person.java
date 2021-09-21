import java.lang.Math;

public class Person implements Runnable {
    private int id;
    public Person(int id) {this.id = id;}
    public void run() {
        System.out.printf("Viaggiatore %d: sto acquistando il biglietto\n", Thread.currentThread().getId());
        try {
            long amount = Math.round(Math.random() * 1000);
            Thread.sleep(amount);
        } catch (Exception e) {
            ;
        }
        System.out.printf("Viaggiatore %d: ho acquistato il biglietto\n", Thread.currentThread().getId());
    }
}

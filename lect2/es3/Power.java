import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class Power implements Callable<Double> {
    private double num;
    private int exponent;

    public Power(double n, int exp) {
        this.num = n;
        this.exponent = exp;
    }

    public Double call() {
        double x = Math.pow(this.num, this.exponent);
        System.out.printf("Esecuzione %f ^ %d in %d\n", this.num, this.exponent, Thread.currentThread().getId());
        return x;
    }

    public static void main(String[] args) {
        BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(50);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 16, 60, TimeUnit.SECONDS, q);
        List<Future<Double>> aList = new ArrayList<Future<Double>>();
        for(int i = 2; i <= 50; i++) {
            Power p = new Power(2, i);
            aList.add(pool.submit(p));
        }
        pool.shutdown();
        Double sum = 0.0;
        for(Future<Double> res : aList) {
            try {
                sum = res.get();
            }
            catch (Exception e) {
                ;
            }
        }
        System.out.println("Result = " + sum);
    }
}

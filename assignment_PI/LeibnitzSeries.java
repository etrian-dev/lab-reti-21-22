import java.lang.Math;

/**
 * This class executes a task to approximate the value of PI, according to the
 * Gregory-Leibnitz series, up to a certain accuracy (given as a parameter) or when the 
 * task in interrupted
 */
public class LeibnitzSeries implements Runnable{
    // current PI approximation
    private double approx;
    // maximum allowed distance from Math.PI
    private final double accuracy;

    private Thread th;

    public LeibnitzSeries(double accuracy, Thread mainTh) {
        // the series starts from 4
        this.approx = 4;
        this.accuracy = accuracy;
        this.th = mainTh;
    }

    public void run() {
        int i = 1;
        // if the thread is interrupted then it exits even though the accuracy
        // hasn't been reached yet. Otherwise it exits if the required accuracy was reached
        while(Thread.currentThread().isInterrupted() == false || Math.abs(approx - Math.PI) >= this.accuracy) {
            double next = 4.0 / (2 * i + 1);
            if(i % 2 == 0) {
                this.approx += next;
            }
            else {
                this.approx -= next;
            }
            i++;
        }
        System.out.printf("PI is around %s (iterations: %d, accuracy: %e)\n", this.approx, i, this.accuracy);
        this.th.interrupt();
    }
}

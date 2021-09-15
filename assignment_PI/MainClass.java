import java.util.Scanner;

public class MainClass {
   public static void main(String[] args) {
       Scanner scan = new Scanner(System.in);
       // takes from stdin the accuracy (a double) and the maximum time (an integer)
       System.out.print("Accuracy: ");
       double accuracy = scan.nextDouble();
       System.out.print("Max time (milliseconds): ");
       int max_time = scan.nextInt();
       scan.close();
       // A new 
       LeibnitzSeries calcPI = new LeibnitzSeries(accuracy, Thread.currentThread());
       Thread t = new Thread(calcPI);
       t.start();
       // sleeps for max_time milliseconds, then interrupts the thread t
       try {
           Thread.sleep(max_time);
       }
       catch (InterruptedException ex) {
           System.out.println("Thread main interrotto");
       }
       // when the thread t is interrupted 
       t.interrupt();
   }
}

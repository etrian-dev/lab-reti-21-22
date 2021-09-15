import java.util.Calendar;

class DatePrinterThread extends Thread {
    public static void main(String[] args) {
        DatePrinterThread dt = new DatePrinterThread();
        dt.start();
        System.out.printf("Terminated: %s\n", Thread.currentThread().getName());
    }
    public void run() {
        while(true) {
            Calendar c = Calendar.getInstance();
            System.out.println(
                Thread.currentThread().getName() + ": " + 
                c.get(java.util.Calendar.HOUR_OF_DAY) + ":" + c.get(java.util.Calendar.MINUTE) + "   " +
                c.get(java.util.Calendar.DAY_OF_MONTH) +  "/" + c.get(java.util.Calendar.MONTH) + "/" + c.get(java.util.Calendar.YEAR));
            try {
                Thread.sleep(2000);
            }
            catch(InterruptedException x) {
                ;
            }
        }
    }
}
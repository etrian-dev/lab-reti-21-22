import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Consumer extends Thread {
    int consumerID;
    LinkedList<String> lst;
    Lock lst_lock;
    Condition cond_lst;

    public Consumer(int idx, LinkedList<String> lst, Lock mux, Condition cond) {
	this.consumerID = idx;
	this.lst = lst;
	this.lst_lock = mux;
	this.cond_lst = cond;
    }

    public void run() {
	// fino a che il crawler non ha terminato viene prelevato il path di una directory
	// dalla coda e sono stampati i nomi dei file ivi contenuti
	boolean terminate = false;
	while(!terminate) {
	    this.lst_lock.lock();

	    while(this.lst.size() == 0) {
		try {
		    cond_lst.await();
		} catch (InterruptedException e) {
		    System.out.println("Attesa Consumer interrotta");
		}
	    }
	    String elem = this.lst.peekFirst();
	    if(elem.equals("")) {
		terminate = true;
	    }
	    else {
		File dir = new File(this.lst.pop());
		System.out.println(elem + "/ :");
		for (File f : dir.listFiles()) {
		    if(!f.isDirectory()) {
			System.out.println(f.getName());
		    }
		}
		System.out.println("");
	    }

	    this.lst_lock.unlock();
	}
    }
}

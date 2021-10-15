import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Crawler extends Thread {
	File start_dir;
	LinkedList<String> file_q;
	Lock q_mux;
	Condition c_lock;

	// Il crawler parte dal File start ed esplora ricorsivamente le sottodirectory
	public Crawler(File start, LinkedList<String> lst, Lock mux, Condition cond) {
		this.start_dir = start;
		this.file_q = lst;
		this.q_mux =mux;
		this.c_lock = cond;
	}

	public void run() {
		System.out.println("*** Crawler starts from [" + this.start_dir.getPath() + "] ***");
		exploreChild(this.start_dir);
		// alla fine aggiungo un null alla lista per segnalare ai consumer di terminare
		this.q_mux.lock();
		this.file_q.add(""); // posso farlo su una linked list
		this.c_lock.signalAll(); // deve arrivare a tutti i consumer
		this.q_mux.unlock();
		System.out.println("*** Crawler Done ***");
	}

	// esplora ricorsivamente a partire dalla directory (o file) f
	private void exploreChild(File f) {
		if(f.isDirectory()) {
			// se f è una directory, allora devo inserirne il nome nella coda
			this.q_mux.lock();
			//System.out.println("Crawler: added subdir [" + f.getPath() + "]");
			this.file_q.add(f.getPath());
			this.c_lock.signal();
			this.q_mux.unlock();
			// ogni directory contenuta in questa directory è visitata ricorsivamente
			for(File child : f.listFiles()) {
				exploreChild(child);
			}
		}
		// Se f non è una directory ritorno direttamente
	}
}

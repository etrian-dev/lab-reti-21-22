import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * La classe Crawler riceve il riferimento al File start ed esplora ricorsivamente 
 * tutte le sottodirectory inserendone il path nella lista condivisa
 */
public class Crawler extends Thread {
	private File start_dir;
	private LinkedList<String> file_q;
	private Lock q_mux;
	private Condition c_lock;

	public Crawler(File start, LinkedList<String> lst, Lock mux, Condition cond) {
		this.start_dir = start;
		this.file_q = lst;
		this.q_mux = mux;
		this.c_lock = cond;
	}

	public void run() {
		System.out.println("[Crawler] starts from \'" + this.start_dir.getPath() + "\'\n");
		// esplorazione ricorsiva della directory
		exploreChild(this.start_dir);
		// aggiungo una stringa vuota alla lista per segnalare ai consumer di terminare
		this.q_mux.lock();
		this.file_q.add("");
		this.c_lock.signalAll(); // deve arrivare a tutti i consumer
		this.q_mux.unlock();
		System.out.println("[Crawler] Done\n");
	}

	// Esplora ricorsivamente a partire dalla directory (o file) f
	// Se f non è una directory ritorno senza fare niente
	private void exploreChild(File f) {
		if (f.isDirectory()) {
			// se f è una directory, allora devo inserirne il nome nella coda
			this.q_mux.lock();
			this.file_q.add(f.getPath()); // inserisco in fondo alla lista
			this.c_lock.signal(); // segnalo l'inserimento ad uno qualunque dei consumer
			this.q_mux.unlock();
			// ogni directory contenuta in questa directory è visitata ricorsivamente
			for (File child : f.listFiles()) {
				if (child.isDirectory()) {
					exploreChild(child);
				}
			}
		}
	}
}

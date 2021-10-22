import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * La classe Consumer preleva il path di una directory dalla coda e ne stampa i nomi dei file
 * (le subdirectories hanno il nome tra parentesi tonde). 
 * Il formato dell'output è simile a quello del comando "ls -R" applicato alla directory iniziale
 */
public class Consumer extends Thread {
	private int consumerID;
	private int consumed = 0;
	private LinkedList<String> lst;
	private Lock lst_lock;
	private Condition cond_lst;

	public Consumer(int idx, LinkedList<String> lst, Lock mux, Condition cond) {
		this.consumerID = idx;
		this.lst = lst;
		this.lst_lock = mux;
		this.cond_lst = cond;
	}

	// Il consumer attende che vi sia almeno un elemento nella lista condivisa
	// e lo preleva: se l'elemento letto è una stringa vuota ("") allora termina,
	// altrimenti ottiene tutti i files contenuti nella directory e stampa i loro nomi su stdout
	public void run() {
		boolean terminate = false;
		while (!terminate) {
			this.lst_lock.lock();

			// se la lista è vuota peekFirst ritorna null, altrimenti ritorna 
			// (ma non rimuove) la testa della lista
			String elem = null;
			while ((elem = this.lst.peekFirst()) == null) {
				try {
					cond_lst.await();
				} catch (InterruptedException e) {
					System.out.println("Attesa Consumer" + this.consumerID + " interrotta");
				}
			}

			// controllo la testa della lista prima di rimuoverla
			if (elem.equals("")) {
				System.out.println("[Consumer " + this.consumerID + "] Done: printed "
						+ this.consumed + " directories\n");
				terminate = true;
			} else {
				// rimuovo l'elemento
				File dir = new File(this.lst.pop());
				System.out.println("[Consumer " + this.consumerID + "]\n" + elem + ":");
				int count_files = 0;
				for (File f : dir.listFiles()) {
					if (f.isDirectory()) {
						System.out.print("(" + f.getName() + ")  ");
					} else {
						System.out.print(f.getName() + "  ");
					}
					count_files++;
				}
				System.out.println((count_files > 0 ? "\n" : "*** no files ***\n"));
				this.consumed++;
			}

			// Se rilasciassi lock prima di aver stampato l'interleaving dei thread
			// non garantirebbe che le stampe su stdout dei nomi dei file siano ordinate
			// Chiaramente è possibile implementare politiche di gestione della concorrenza
			// anche per la stampa, ma ho ritenuto questa soluzione più lineare
			this.lst_lock.unlock();
		}
	}
}

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe contenente il main del file crawler: lancia il crawler indicando la directory
 * da esplorare ricorsivamente e specificando il numero di thread consumer che stamperanno
 * i nomi dei file contenuti delle directory trovate
 */
public class MainClass {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: MainClass <consumers> <directory>");
			return;
		}
		try {
			// il numero di thread che stampano i file
			int k = Integer.parseInt(args[0]);
			// La directory da cui parte l'esplorazione
			File dirpath = new File(args[1]);
			// La coda grazie alla quale comunicano il crawler ed i consumatori
			LinkedList<String> files_lst = new LinkedList<String>();
			Lock mux = new ReentrantLock();
			Condition cond = mux.newCondition();

			// prima di lanciare il crawler controllo che il path astratto corrisponda ad una directory esistente
			if (!dirpath.exists()) {
				System.out.println("Errore: la directory" + dirpath.getPath() + " non esiste");
				return;
			}
			else if (!dirpath.isDirectory()) {
				System.out.println("Errore: il path" + dirpath.getPath() + " non corrisponde ad una directory");
				return;
			} else {
				// Creo k thread consumer e li lancio
				for(int i = 0; i < k; i++) {
					Consumer cc = new Consumer(i, files_lst, mux, cond);
					cc.start();
				}
				// Creo il thread crawler per esplorare la directory
				Crawler explorer = new Crawler(dirpath, files_lst, mux, cond);
				explorer.start();
			}
		} catch (NullPointerException e) {
			System.out.println("Usage: MainClass <consumers> <directory>");
			e.printStackTrace();
		}
	}
}

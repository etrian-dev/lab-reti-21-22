import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainClass {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: MainClass <consumers> <directory>");
			return;
		}
		try {
			int k = Integer.parseInt(args[0]);
			// La directory da cui parte l'esplorazione
			File dirpath = new File(args[1]);
			// La coda grazie alla quale comunicano il crawler ed i consumatori
			LinkedList<String> files_lst = new LinkedList<String>();
			Lock mux = new ReentrantLock();
			Condition cond = mux.newCondition();

			if (!dirpath.exists()) {
				System.out.println("Errore: la directory" + dirpath.getPath() + " non esiste");
			} else {
				// Creo k thread consumer
				for(int i = 0; i < k; i++) {
					Consumer cc = new Consumer(i, files_lst, mux, cond);
					cc.start();
				}
				// Creo un thread crawler
				Crawler explorer = new Crawler(dirpath, files_lst, mux, cond);
				explorer.start();
			}
		} catch (NullPointerException e) {
			System.out.println("Errore: " + e.getMessage());
		}
	}
}

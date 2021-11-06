import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe contenente il metodo main del file crawler. Per lanciare il programma è sufficiente
 * specificare il numero di thread consumatori e la directory di partenza (anche path relativo).
 * Un thread crawler è incaricato si esplorare l'albero di directory in modo ricorsivo, mentre
 * i k thread consumatori leggono i path delle directory dalla lista e stamperanno i nomi dei file
 * contenuti in tale directory. I file che sono directory sono stampati tra parentesi tonde.
 * Il formato dell'output è simile a quello del comando "ls -R" applicato alla directory iniziale
 */
public class MainClass {
	private static final String usage_msg = "Usage: java MainClass <consumers> <directory>";

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println(usage_msg);
			return;
		}
		try {
			// il numero di thread che stampano i file
			int k = Integer.parseInt(args[0]);
			if (k <= 0) {
				System.out.println("Errore: il numero di consumatori deve essere un intero > 0"
						+ "\nParametro inserito: " + args[0]);
				return;
			}
			// La directory da cui parte l'esplorazione è usata per creare un oggetto File
			File dirpath = new File(args[1]);
			// Creo la coda grazie alla quale comunicano il crawler ed i consumatori
			// e la lock e condition variable per la sincronizzazione
			LinkedList<String> files_lst = new LinkedList<String>();
			Lock mux = new ReentrantLock();
			Condition cond = mux.newCondition();

			// prima di lanciare il crawler controllo che il path astratto corrisponda ad una directory esistente
			if (!dirpath.exists()) {
				System.out.println("Errore: la directory" + dirpath.getPath() + " non esiste");
				return;
			} else if (!dirpath.isDirectory()) {
				System.out.println("Errore: il path" + dirpath.getPath()
						+ " non corrisponde ad una directory");
				return;
			} else {
				System.out.println("********************** NOTA **********************\n"
						+ "| (dirname) indica che tale file è una directory |\n"
						+ "| L'assenza di parentesi indica un file regolare |\n"
						+ "**************************************************");

				// Creo k thread consumer e li lancio (si metteranno in attesa di elementi nella lista)
				for (int i = 0; i < k; i++) {
					Consumer cc = new Consumer(i, files_lst, mux, cond);
					cc.start();
				}
				// Creo e faccio partire il thread crawler per esplorare la directory
				Crawler explorer = new Crawler(dirpath, files_lst, mux, cond);
				explorer.start();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println(usage_msg);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(usage_msg);
		}
	}
}

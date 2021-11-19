package ContiCorrenti.src;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Classe che implementa la rilettura del file JSON generato
 */
public class FileProcessor {
	private static Random rng = new Random();

	// La mappa conta il numero di causali dei movimenti presenti nel file
	private ConcurrentMap<String, Long> contaCausali;

	public FileProcessor() {
		this.contaCausali = new ConcurrentHashMap<>(Movimento.CAUSALI.length);
		for (String causale : Movimento.CAUSALI) {
			this.contaCausali.put(causale, 0L);
		}
	}

	// massimo numero di conti correnti e movimenti per conto corrente
	public static int MAX_CCS = 100;
	public static int MAX_MOVEMENTS = 1000;

	public static void main(String[] args) {
		// Dichiaro una mappa che contiene, per ogni possibile causale, il numero 
		// di movimenti con quella causale generati nella sezione seguente
		Map<String, Long> mapVerifica = new HashMap<>(Movimento.CAUSALI.length);
		for (String s : Movimento.CAUSALI) {
			mapVerifica.put(s, 0L);
		}
		// crea un certo numero di conti correnti e genera loro movimenti
		List<ContoCorrente> ccList = new ArrayList<>();
		int nCCs = 1 + rng.nextInt(MAX_CCS);
		for (int i = 0; i < nCCs; i++) {
			ccList.add(new ContoCorrente("ContoCorrente_" + i));
		}
		System.out.println("Conti correnti creati: " + nCCs);
		int tot_movs = 0;
		for (ContoCorrente x : ccList) {
			int nMovimenti = rng.nextInt(MAX_MOVEMENTS);
			for (int i = 0; i < nMovimenti; i++) {
				Movimento newMov = FileCreator.generateMovement();
				// aggiorno la mappa
				String causale = newMov.getCausale();
				mapVerifica.put(causale, mapVerifica.get(causale) + 1);
				x.addMovimento(newMov);
			}
			tot_movs += nMovimenti;
		}
		System.out.println("Movimenti totali: " + tot_movs);
		// la lista di conti correnti viene passata alla classe FileCreator, che genera il file JSON
		FileCreator fc = new FileCreator();
		// generazione del file con NIO
		boolean ok = fc.generateFile(FileCreator.GENERATED_FILE, ccList);
		System.out.println("\nCreazione: " + (ok ? "OK" : "FALLITA"));

		if (!ok) {
			return; // termino immediatamente se la creazione del file è fallita
		}
		ok = false;

		// La threadpool che eseguirà la task per il conteggio
		ExecutorService tpool = Executors.newFixedThreadPool(5);
		ObjectMapper mapper = new ObjectMapper();
		// Crea un istanza della classe che dichiara il contatore di causali
		FileProcessor fproc = new FileProcessor();
		JsonFactory fact = new JsonFactory(); // factory per creazione parser
		try (BufferedInputStream streamIn = new BufferedInputStream(
				new FileInputStream(new File(FileCreator.GENERATED_FILE)));) {
			// inizializzazione del parser che va ad agire sul file
			JsonParser jparse = fact.createParser(streamIn);
			jparse.setCodec(mapper);
			JsonToken tok = jparse.nextToken();
			// Effettua il parsing del file
			if (tok != JsonToken.START_ARRAY) {
				// Il file non inizia con '[', quindi il formato è errato
				throw new Exception("ERR: Formato file " + FileCreator.GENERATED_FILE + " errato");
			}
			while ((tok = jparse.nextToken()) != JsonToken.END_ARRAY) {
				// Legge un conto corrente dall'array (può sollevare eccezioni, catturate in seguito)
				ContoCorrente cc = jparse.readValueAs(ContoCorrente.class);
				// Task che viene eseguita dalla threadpool per contare le causali dei movimenti di questo cc
				// Riceve in input anche il contatore da aggiornare, condiviso
				CausalCounterTask t = new CausalCounterTask(cc, fproc.contaCausali);
				tpool.submit(t); // può dare RejectedExecutionException
			}
			// Shutdown controllato
			tpool.shutdown();
			while (!tpool.isTerminated()) {
				tpool.awaitTermination(60, TimeUnit.SECONDS);
			}
		} catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException parserr) {
			ok = false;
			System.out.println("ERR: deserializzazione file, formato file '"
					+ FileCreator.GENERATED_FILE + "' errato");
		} catch (IOException ioex) {
			ok = false;
			ioex.printStackTrace();
		} catch (Exception e) {
			ok = false;
			e.printStackTrace();
		} finally {
			ok = true;
		}

		// Verifico l'esito del processing: se la mappa contaCausali 
		// e quella calcolata alla generazione coincidono allora stampo un riepilogo
		if (ok && fproc.contaCausali != null && fproc.contaCausali.equals(mapVerifica)) {
			System.out.printf("\n%-10s\t%-10s\n", "Causale", "Occorrenze");
			for (String key : fproc.contaCausali.keySet()) {
				System.out.printf("%-10s\t%-10d\n", key, fproc.contaCausali.get(key));
			}
		} else {
			ok = false;
		}

		System.out.println("\nRilettura: " + (ok ? "OK" : "FALLITA"));
	}
}

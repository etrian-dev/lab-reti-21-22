package ContiCorrenti.src;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Classe che implementa la generazione e rilettura del file "conti_correnti.json" 
 */
public class FileCreator {
	// PRNG per la generazione di movimenti
	private static Random rng = new Random();
	// Numero di ms in due anni: usato per limitare inferiormente la data del movimento generato
	private static long TWO_YEARS_MS = 6307200000L;

	// Oggetto definito dalla libreria Jackson usato per serializzare/deserializzare
	private ObjectMapper mapper;

	public FileCreator() {
		this.mapper = new ObjectMapper();
		// Per abilitare pretty printing degli oggetti JSON
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	// Genera un timestamp che corrisponde ad una data compresa 
	// tra l'istante corrente ed i due anni precedenti
	private static long genTstamp() {
		return System.currentTimeMillis() - Math.abs(rng.nextLong() % TWO_YEARS_MS);
	}

	// Genera un nuovo movimento scegliendo casualmente una data ed una causale
	public static Movimento generateMovement() {
		Date mDate = new Date(genTstamp());
		String mCausale = Movimento.CAUSALI[rng.nextInt(Movimento.CAUSALI.length)];
		return new Movimento(mDate, mCausale);
	}


	/**
	 * Funzione usata per generare il file JSON target
	 * nel quale serializzare i conti correnti in listaConti.
	 * Utilizza un channel NIO per la scrittura dei dati
	 * @param target
	 * @param listaConti
	 * @return true sse la scrittura dei conti (serializzati) in listaConti sul file ha successo,
	 * false altrimenti
	 */
	public boolean generateFile(String target, List<ContoCorrente> listaConti) {
		// Flag per indicare se la generazione è riuscita
		boolean ok = false;
		// Uso un FileChannel che apre il file in scrittura, creandolo se non esiste
		// ed azzerandone i contenuti se già esisteva
		try (FileChannel fchan = FileChannel.open(Paths.get(target), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);) {
			byte[] buf = mapper.writeValueAsBytes(listaConti);
			//System.out.println("JSON byte array len: " + buf.length);

			ByteBuffer bbuf = ByteBuffer.allocate(100);
			int offt = 0;
			int len = 0;
			while (offt < buf.length) {
				if (buf.length - offt < bbuf.capacity()) {
					len = buf.length - offt;
				} else {
					len = bbuf.capacity();
				}
				bbuf.put(buf, offt, len);
				bbuf.flip();
				while (bbuf.hasRemaining()) {
					fchan.write(bbuf);
				}
				bbuf.clear();
				offt += len;
			}
			//System.out.println("End: " + offt);
		} catch (InvalidPathException pathEx) {
			// La stringa target non può essere convertita in un path
			pathEx.printStackTrace();
			System.out
					.println("ERR: \"" + target + "\" non può essere convertito ad un path valido");
		} catch (UnsupportedOperationException optsEx) {
			// Il path in cui andare a creare il file non supporta le opzioni elencate
			// oppure non supporta la creazione di questo tipo di channel
			optsEx.printStackTrace();
			System.out.println(
					"ERR: Il FileChannel non può essere creato o opzioni di creazione non supportate");
		} catch (IOException ioEx) {
			// Un errore di I/O (può avere molteplici cause)
			ioEx.printStackTrace();
			System.out.println("ERR: Errore I/O (può avere molteplici cause)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// La scrittura degli oggetti è terminata con successo: setto flag OK
			ok = true;
		}
		return ok;
	}

	/**
	 * Funzione che legge il file JSON source e conta le occorrenze di ciascuna 
	 * causale nei movimenti contenuti
	 * @param source
	 * @return Una mappa di coppie <Causale, occorrenze che riassume le 
	 * occorrenze di ogni possibile causale nei movimenti contenuti nel file source.
	 * Se la deserializzazione fallisce allora la funzione ritorna null
	 */
	public Map<String, Long> movementStats(String source) {
		// La mappa ritornata in caso di successo
		Map<String, Long> contaCausali = new HashMap<>(Movimento.CAUSALI.length);
		for (String causale : Movimento.CAUSALI) {
			contaCausali.put(causale, 0L);
		}
		// Con un FileReader tenta di leggere il file in un JsonNode
		try (FileReader fread = new FileReader(new File(source))) {
			JsonNode root = mapper.readTree(fread);
			// Verifico di avere un array di ContiCorrenti
			if (!root.isArray()) {
				return null;
			}
			// Itero su tutti i conti correnti nell'array per estrarre i movimenti
			for (JsonNode conto : root) {
				JsonNode movs = conto.get("movimenti");
				if (!movs.isArray()) {
					break;
				} else {
					for (JsonNode movement : movs) {
						JsonNode causaObj = movement.get("causale");
						if (causaObj.isTextual()) {
							String causale = causaObj.asText();
							// incremento contatore causale
							contaCausali.put(causale, contaCausali.get(causale) + 1);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return contaCausali;
	}

	public static void main(String[] args) {
		// crea utenti e genera loro movimenti
		List<ContoCorrente> ccList = new ArrayList<>();
		//int nContoCorrente = 1 + rng.nextInt(10);
		int nContoCorrente = 5;
		for (int i = 0; i < nContoCorrente; i++) {
			ccList.add(new ContoCorrente("ContoCorrente_" + i));
		}
		System.out.println("ContiCorrenti: " + ccList.size());
		for (ContoCorrente x : ccList) {
			//int nMovimenti = rng.nextInt(100);
			int nMovimenti = 3;
			for (int i = 0; i < nMovimenti; i++) {
				x.addMovimento(FileCreator.generateMovement());
			}
		}
		// la lista di utenti viene passata alla classe che li serializza sul file
		FileCreator fc = new FileCreator();
		boolean ok = fc.generateFile("cc.json", ccList);
		System.out.println("File creation: " + ok);
		Map<String, Long> occurrences = fc.movementStats("cc.json");
		ok = (occurrences != null ? true : false);
		System.out.println("File parsing: " + ok);
		System.out.printf("%-10s\t%-10s\n", "Causale", "Occorrenze");
		for (String key : occurrences.keySet()) {
			System.out.printf("%-10s\t%-10d\n", key, occurrences.get(key));
		}
	}
}

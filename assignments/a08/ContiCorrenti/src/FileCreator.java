package ContiCorrenti.src;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Classe che implementa la generazione e rilettura del file "cc.json" 
 */
public class FileCreator {
	// Il file generato da questa classe
	public static String GENERATED_FILE = "cc.json";
	// PRNG per la generazione di movimenti
	private static Random rng = new Random();
	// Numero di ms in due anni: usato per limitare inferiormente la data del movimento generato
	private static long TWO_YEARS_MS = 6307200000L;

	private ObjectMapper mapper; // per serializzare/deserializzare

	public FileCreator() {
		this.mapper = new ObjectMapper();
		// abilito pretty printing
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	// Genera un timestamp che corrisponde ad una data compresa 
	// tra l'istante corrente e i due anni precedenti
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
			// Trasformo in un array di bytes la lista di conti correnti in JSON
			byte[] json_bytes = mapper.writeValueAsBytes(listaConti);
			// Prelevo da json_bytes scrivendo i dati in un ByteBuffer di dimensione fissa
			ByteBuffer bbuf = ByteBuffer.allocate(100);
			int offt = 0; // offset nell'array
			int len = 0; // numero di bytes da scrivere sul buffer
			while (offt < json_bytes.length) {
				// Scrivo sul ByteBuffer una porzione dell'array
				len = Math.min(json_bytes.length - offt, bbuf.capacity());
				bbuf.put(json_bytes, offt, len);
				// Passo in modalità lettura
				bbuf.flip();
				while (bbuf.hasRemaining()) {
					fchan.write(bbuf);
				}
				// Il buffer viene riutilizzato, perciò devo fare clear
				bbuf.clear();
				offt += len;
			}
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
}

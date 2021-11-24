package ContiCorrenti.src;

import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * Classe che implementa l'aggiornamento del contatore globale di causali
 */
public class CausalCounterTask implements Runnable {
	private ContoCorrente conto;
	private ConcurrentMap<String, Long> ccMap;
	// Funzione che incrementa il suo secondo argomento (Long) di uno
	// usata per l'update atomico della ConcurrentMap
	private BiFunction<String, Long, Long> nextval = (s, val) -> (val + 1);

	public CausalCounterTask(ContoCorrente cc, ConcurrentMap<String, Long> counterMap) {
		this.conto = cc;
		this.ccMap = counterMap;
	}

	public void run() {
		// Per ogni movimento recupero la causale ed incremento
		// il valore della mappa che corrisponde a tale chiave
		// Se la chiave non era presente nella mappa allora è una causale non valida,
		// dato che ccMap è stata inizializzata nel main con tutte le causali ammesse
		for (Movimento m : this.conto.getMovimenti()) {
			String causale = m.getCausale();
			// Incremento atomico tramite la funzione nextval
			if (this.ccMap.computeIfPresent(causale, nextval) == null) {
				System.out.println("ERR: Causale '" + causale + "' non valida");
			}
		}
	}
}

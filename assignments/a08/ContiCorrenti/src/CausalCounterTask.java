package ContiCorrenti.src;

public class CausalCounterTask implements Runnable {
	ContoCorrente conto;

	public CausalCounterTask(ContoCorrente cc) {
		this.conto = cc;
	}

	public void run() {
		for (Movimento m : this.conto.getMovimenti()) {
			// update sync'd map with causali
		}
	}
}

package ContiCorrenti.src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che implementa un conto corrente: un conto corrente
 * è caratterizzato dal nome del correntista e la lista dei movimenti.
 * I movimenti registrati nel conto corrente sono relativi agli ultimi due anni
 */
public class ContoCorrente implements Serializable {
	public static long SerialVersionUID = 1L;

	private String nome;
	private List<Movimento> movimenti;

	public ContoCorrente() {
		this.nome = null;
		this.movimenti = null;
	}

	public ContoCorrente(String username) {
		this.nome = username;
		this.movimenti = new ArrayList<>();
	}

	public void addMovimento(Movimento newMov) {
		this.movimenti.add(newMov);
	}

	// Setters

	public void setName(String newName) {
		this.nome = newName;
	}

	public void setMovimenti(List<Movimento> mList) {
		this.movimenti = mList;
	}

	// Getters

	public String getName() {
		return this.nome;
	}

	public List<Movimento> getMovimenti() {
		return this.movimenti;
	}

	// toString
	public String toString() {
		String cc = "nome: " + this.nome + "\n";
		for (Movimento mov : this.movimenti) {
			cc += mov.toString() + "\n";
		}
		return cc;
	}
}

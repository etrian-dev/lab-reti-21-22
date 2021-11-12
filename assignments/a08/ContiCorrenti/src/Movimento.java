package ContiCorrenti.src;

import java.io.Serializable;
import java.util.Date;

/**
 * Classe che incapsula lo stato di un movimento del conto corrente
 * Un movimento è caratterizzato da una data ed una causale 
 * (una tra quelle specificate di seguito)
 */
public class Movimento implements Serializable {
	public static long SerialVersionUID = 1L;
	// Le possibili causali di un movimento
	public static String[] CAUSALI = {"Bonifico", "Accredito", "Bollettino", "F24", "PagoBancomat"};

	private Date data;
	private String causale;

	public Movimento() {
		this.data = null;
		this.causale = null;
	}

	public Movimento(Date dataMov, String causaleMov) {
		this.data = dataMov;
		this.causale = causaleMov;
	}

	// Setters

	public void setDate(Date newDate) {
		this.data = newDate;
	}

	public void setCausale(String newCausale) {
		this.causale = newCausale;
	}

	// Getters

	public Date getData() {
		return this.data;
	}

	public String getCausale() {
		return this.causale;
	}

	// toString
	public String toString() {
		return this.data.toString() + ": " + this.causale;
	}
}

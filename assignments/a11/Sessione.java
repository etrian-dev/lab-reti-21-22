import java.io.Serializable;

public class Sessione implements Serializable {
	public static final long SerialVersionUID = 1L;
	public static final int MAX_SPEAKERS = 5;

	private int available;
	private String[] speakers;

	public Sessione() {
		this.available = 0;
		this.speakers = new String[Sessione.MAX_SPEAKERS];
	}

	public Sessione(String[] speakerList) throws IllegalArgumentException {
		if (speakerList.length <= Sessione.MAX_SPEAKERS) {
			this.speakers = speakerList.clone();
		}
		throw new IllegalArgumentException("Too many speakers");
	}

	public boolean setAvailable(int navail) {
		if (navail < 0 || navail >= Sessione.MAX_SPEAKERS) {
			return false;
		}
		this.available = Sessione.MAX_SPEAKERS - navail;
		return true;
	}

	public boolean setSpeaker(String name) {
		if (this.available < Sessione.MAX_SPEAKERS) {
			this.speakers[available] = new String(name);
			this.available++;
			return true;
		}
		return false;
	}

	public int getAvailable() {
		return Sessione.MAX_SPEAKERS - available;
	}

	public String[] getSpeakers() {
		return this.speakers.clone();
	}
}

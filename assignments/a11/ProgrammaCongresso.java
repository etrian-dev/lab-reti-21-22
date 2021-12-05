import java.io.Serializable;
import java.util.ArrayList;

public class ProgrammaCongresso implements Serializable {
	public static final long SerialVersionUID = 1L;
	public static final int NUM_GIORNATE = 3;
	public static final int NUM_SESSIONI = 12;

	// La tabella delle sessioni in ogni giornata del congresso
	private Sessione[][] timetable;

	public ProgrammaCongresso() {
		this.timetable = new Sessione[ProgrammaCongresso.NUM_GIORNATE][];
		for (int x = 0; x < ProgrammaCongresso.NUM_GIORNATE; x++) {
			this.timetable[x] = new Sessione[ProgrammaCongresso.NUM_SESSIONI];
		}
		int i = 0;
	}

	private boolean checkDay(int giornata) {
		if (giornata < 0 || giornata >= ProgrammaCongresso.NUM_GIORNATE) {
			return false;
		}
		return true;
	}

	private boolean checkSession(int nsessione) {
		if (nsessione < 0 || nsessione >= ProgrammaCongresso.NUM_SESSIONI) {
			return false;
		}
		return true;
	}

	public boolean setSpeaker(int giornata, int nsessione, String speaker) {
		if (checkDay(giornata) && checkSession(nsessione)) {
			if (this.timetable[giornata][nsessione].setSpeaker(speaker)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<String[]> getDailyProgram(int giornata) {
		if (!checkDay(giornata)) {
			return null;
		}
		ArrayList<String[]> dailyProgram = new ArrayList<>(12);
		for (Sessione sess : this.timetable[giornata]) {
			dailyProgram.add(sess.getSpeakers());
		}
		return dailyProgram;
	}

	public ArrayList<ArrayList<String[]>> getProgram() {
		ArrayList<ArrayList<String[]>> congress = new ArrayList<ArrayList<String[]>>();
		for (int i = 0; i < ProgrammaCongresso.NUM_GIORNATE; i++) {
			congress.add(this.getDailyProgram(i));
		}
		return congress;
	}
}

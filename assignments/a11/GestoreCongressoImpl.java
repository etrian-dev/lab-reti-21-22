import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GestoreCongressoImpl extends UnicastRemoteObject implements GestoreCongresso {
	private ProgrammaCongresso programma;

	public GestoreCongressoImpl() throws RemoteException {
		this.programma = new ProgrammaCongresso();
	}

	public ArrayList<ArrayList<String[]>> getCongressSummary() throws RemoteException {
		return this.programma.getProgram();
	}

	public boolean registerSpeaker(int day, int session, String speaker) throws RemoteException {
		return (this.programma.setSpeaker(day, session, speaker) ? true : false);
	}
}

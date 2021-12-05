import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GestoreCongresso extends Remote {
	public ArrayList<ArrayList<String[]>> getCongressSummary() throws RemoteException;

	public boolean registerSpeaker(int day, int session, String speaker) throws RemoteException;
}

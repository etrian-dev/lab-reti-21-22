import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GestoreCongresso extends Remote {
	public void getCongressSummary() throws RemoteException;

	public boolean registerSpeaker(String speaker, int session) throws RemoteException;
}

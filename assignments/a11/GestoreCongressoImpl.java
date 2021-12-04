import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class GestoreCongressoImpl extends UnicastRemoteObject implements GestoreCongresso {
	int x;

	public GestoreCongressoImpl() throws RemoteException {
		this.x = 10;
	}

	public void getCongressSummary() throws RemoteException {
		System.out.println("Sommario congresso");
	}

	public boolean registerSpeaker(String speaker, int session) throws RemoteException {
		System.out.println("Registrazione speaker");
		return true;
	}
}

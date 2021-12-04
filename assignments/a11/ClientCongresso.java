import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientCongresso {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(ServerMain.PORT);

			GestoreCongresso serv = (GestoreCongresso) reg.lookup(ServerMain.GESTORE);
			serv.getCongressSummary();
			serv.registerSpeaker("Mario Rossi", 10);
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

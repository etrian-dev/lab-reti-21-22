import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain {
	public static final int PORT = 11111;
	public static final String GESTORE = "GESTORE_CONGRESSO";

	public static void main(String[] args) {
		try {
			GestoreCongressoImpl gestoreImpl = new GestoreCongressoImpl();

			LocateRegistry.createRegistry(ServerMain.PORT);
			Registry reg = LocateRegistry.getRegistry(ServerMain.PORT);
			reg.rebind(ServerMain.GESTORE, gestoreImpl);
			System.out.println("Server pronto");
		} catch (RemoteException rex) {
			rex.printStackTrace();
		}
	}
}

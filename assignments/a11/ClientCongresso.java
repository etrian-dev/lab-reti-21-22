import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ClientCongresso {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.getRegistry(ServerMain.PORT);

			GestoreCongresso serv = (GestoreCongresso) reg.lookup(ServerMain.GESTORE);

			// register some sessions
			serv.registerSpeaker(1, 1, "D1 S1 (1)");
			serv.registerSpeaker(1, 1, "D1 S1 (2)");
			serv.registerSpeaker(1, 1, "D1 S1 (3)");
			serv.registerSpeaker(1, 1, "D1 S1 (4)");
			serv.registerSpeaker(1, 1, "D1 S1 (5)");
			serv.registerSpeaker(2, 1, "D2 S1 (1)");
			serv.registerSpeaker(3, 4, "D3 S4 (1)");
			serv.registerSpeaker(4, 1, "D4 S1 (1)"); // --> error
			serv.registerSpeaker(2, 13, "D2 S13 (1)"); // --> error

			// print a summary
			ArrayList<ArrayList<String[]>> summary = serv.getCongressSummary();
			int dayNum = 0;
			int sessionNum = 0;
			for (ArrayList<String[]> day : summary) {
				System.out.println("--------- Day " + dayNum + "---------");
				System.out.printf("%10s|%20s|%20s|%20s|%20s|%20s\n", "Sessione", "Intervento 1",
						"Intervento 2", "Intervento 3", "Intervento 4", "Intervento 5");
				sessionNum = 0;
				for (String[] sessions : day) {
					System.out.printf("%10s", "S" + sessionNum);
					for (String speaker : sessions) {
						System.out.printf("|%10s", speaker);
					}
					System.out.append('\n');
					sessionNum++;
				}
				dayNum++;
			}
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

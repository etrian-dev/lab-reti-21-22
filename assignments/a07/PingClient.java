import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

// Classe che implementa il PingClient
public class PingClient extends Thread {
    // Formato utilizzato per i messaggi di ping: PING seqno timestamp
    private static final String ping_msg_format = "PING %d %d (%d)";
    // Numero di messaggi di ping inviati al server
    private static final int tries = 10;
    // Timeout del socket su receive (ms)
    private static final long timeout = 2000;

    private InetAddress host;
    private int host_port;

    // Il costruttore inizializza indirizzo e porta del server, sollevando 
    // un'eccezione se sname non può essere risolto
    // NOTA: se la porta passata non è quella su cui il server è in ascolto, ma è libera,
    // allora tutti i messaggi di ping inviati andranno in timeout
    public PingClient(String sname, int port) throws UnknownHostException {
        this.host_port = port;
        this.host = InetAddress.getByName(sname);
    }

    // Il thread crea una DatagramSocket ed invia e riceve tramite essa messaggi di ping
    public void run() {
        try (DatagramSocket ds = new DatagramSocket()) {
            // Timeout settato a 2 secondi
            ds.setSoTimeout((int) PingClient.timeout);
            // Inizializzo le variabili per le statistiche di ping
            int replies = 0;
            long start_rtt = 0, end_rtt = 0, rtt = 0;
            long min_rtt = PingClient.timeout;
            long max_rtt = 0;
            long total_wait = 0;
            // Invia una sequenza di messaggi di ping, attendendo la risposta o il timeout prima di
            // inviare il prossimo ping
            // NOTA: il delay introdotto dal server potrebbe non essere esattamente uguale a quello stampato
            // in quanto viene preso il timestamp iniziale prima di inviare il messaggio e quello finale dopo averlo ricevuto
            // ma è possibile che si introduca un ulteriore ritardo: in generale per ogni messaggio vale che
            // delay server <= delay stampato dal client
            for (int i = 0; i < PingClient.tries; i++) {
                // timestamp iniziale, scritto nel messaggio ed usato nel calcolo di rtt
                start_rtt = System.currentTimeMillis();
                String msg = String.format(PingClient.ping_msg_format, i, start_rtt, ds.getLocalPort());
                DatagramPacket ping_packet = new DatagramPacket(msg.getBytes(), msg.length());
                ping_packet.setAddress(this.host);
                ping_packet.setPort(this.host_port);
                try {
                    ds.send(ping_packet);
                    // Attende la risposta da parte del server (oppure va in timeout e viene sollevata l'eccezione)
                    String response = new String();
                    // Il messaggio ricevuto potrebbe anche essere quello del ping precedente, che arriva dopo il timeout
                    // per cui va scartato ogni messaggio fino a che non ho uguaglianza con quello atteso
                    do {
                        ds.receive(ping_packet);
                        end_rtt = System.currentTimeMillis();
                        // controllo se sono già oltre il timeout 
                        // (devo farlo prima di stampare RTT, altrimenti stampa RTT > timeout)
                        if (System.currentTimeMillis() - start_rtt >= PingClient.timeout) {
                            throw new SocketTimeoutException();
                        }
                        // Controllo che il messaggio ricevuto sia lo stesso che è stato inviato
                        response = new String(ping_packet.getData(), ping_packet.getOffset(),
                                ping_packet.getLength());
                        if (response.equals(msg)) {
                            rtt = end_rtt - start_rtt;
                            total_wait += rtt;
                            replies++;
                            min_rtt = (min_rtt > rtt ? rtt : min_rtt);
                            max_rtt = (max_rtt < rtt ? rtt : max_rtt);
                            // stampo rtt del ping corrente
                            System.out.println(msg + " RTT: " + rtt + " ms");
                        } // altrimenti messaggio proveniente dal ping precedente che è andato in timeout
                    } while(!response.equals(msg));
                } catch (SocketTimeoutException e) {
                    // Il client non ha ricevuto risposta entro il timeout 
                    // (non viene conteggiato ai fini del calcolo dell'RTT medio)
                    System.out.println(msg + " RTT: *");
                    //total_wait += PingClient.timeout;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Il datagramma non può essere inviato o ricevuto");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Altro tipo di errore");
                }

            }
            String rtt_format = new String();
            // Caso limite: nessuna risposta ricevuta
            if (replies == 0) {
                rtt_format = "-/-/-";
            } else {
                rtt_format = String.format("%d/%.2f/%d", min_rtt,
                        (double) total_wait / (double) replies, max_rtt);
            }
            // All PINGs sent: print statistics
            System.out.printf(
                    "--- PING Statistics ---\n"
                            + "%d packets transmitted, %d packets received, %d%% packet loss\n"
                            + "round-trip (ms) min/avg/max = %s\n",
                    PingClient.tries, replies, (PingClient.tries - replies) * 10, rtt_format);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    // Main del programma client: effettua il controllo degli argomenti da riga di comando e
    // fa partire il thread PingClient
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PingClient hostname port");
            return;
        }
        if (args.length != 2) {
            System.out.println("ERR -arg 1");
            return;
        }
        String serv_name = args[0];
        int serv_port;
        try {
            serv_port = Integer.parseUnsignedInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("ERR -arg 2");
            return;
        }
        try {
            PingClient client = new PingClient(serv_name, serv_port);
            client.start();
        } catch (UnknownHostException ex) {
            System.out.println("ERR -arg 1");
        }
    }
}

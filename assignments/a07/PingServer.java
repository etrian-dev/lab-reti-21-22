import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

// Classe che implementa il PingServer: prende come argomento il numero di porta sul quale
// il server deve essere in ascolto con un DatagramSocket per ricevere pacchetti di ping dal client
// e rispedirli indietro
public class PingServer extends Thread {
    // generatore usato per decidere se il server deve rispondere o meno ad messaggio ricevuto
    private static final Random rng = new Random();
    // classe interna usata per implementare la task di echo
    private class EchoSender implements Runnable {
        private DatagramSocket ds;
        private DatagramPacket packet;
        public EchoSender(DatagramPacket dp) {
            try {
                // il socket UDP per l'eco è su una porta qualunque
                this.ds = new DatagramSocket();
            } catch (SocketException e) {
                System.out.println("ERR: EchoSender socket creation failed");
            }// il pacchetto di cui effettuare l'eco
            this.packet = dp;
        }
        public void run() {
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.print(packet.getSocketAddress().toString().substring(1) + "> " + msg);
            // Decide (con probabilità circa del 25% se mandare o no risposta ed attende < 2000ms in caso affermativo)
            if (rng.nextBoolean() || rng.nextBoolean()) {
                // NOTA: Il client potrebbe andare comunque in timeout se si introduce un ritardo vicino al timeout
                // e l'invio ritardasse (per qualche motivo) di alcuni ms.
                // In tal caso il messaggio inviato viene scartato (dettagliato nel client)
                long waits = Math.abs(rng.nextLong()) % 2000;
                System.out.println(" ACTION: delayed " + waits + " ms");
                try {
                    Thread.sleep(waits);
                } catch (InterruptedException e) {
                    System.out.println("ERR: Server delay interrupted");
                }
                try {
                    ds.send(packet);
                } catch (IOException e) {
                    System.out.println("ERR: Fallito invio messaggio");
                }
            } else {
                System.out.println(" ACTION: not sent");
            }
        }
    }

    private int ping_port;
    private ExecutorService tpool = Executors.newCachedThreadPool();

    public PingServer(int port) {
        this.ping_port = port;
    }

    // Metodo run del thread: crea la DatagramSocket e si mette in ascolto di richieste da parte dei client
    // Viene creato ed aperto un socket in ascolto sulla porta passata come parametro
    // Tale socket viene utilizzato anche per inviare le risposte ai client
    public void run() {
        try (DatagramSocket ping_sock = new DatagramSocket(this.ping_port)) {
            while (true) {
                // waits for a "PING"
                byte[] buf = new byte[100];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                try {
                    ping_sock.receive(dp);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Ioex");
                }
                // if the request was a "PING"
                String msg =
                        new String(dp.getData(), dp.getOffset(), dp.getLength());
                if (msg.startsWith("PING")) {
                    EchoSender s = new EchoSender(dp);
                    try {
                        tpool.execute(s);
                    } catch(RejectedExecutionException rejEx) {
                        // Se non posso eseguire la task lo stampo a video: il client andrà in timeout
                        System.out.print(dp.getSocketAddress().toString().substring(1) + "> " + msg);
                        System.out.println(" ACTION: not sent");
                    }
                }
            }
        } catch (BindException be) {
            // Distinguo le eccezioni di binding per dare all'utente un minimo di informazione aggiuntiva
            System.out.println("ERR: -arg 1: Porta non disponibile al momento");
        } catch (Exception exc) {
            // Eccezione generica dovuta a qualche errore nella creazione della socket o invio/ricezione messaggi
            System.out.println("ERR: -arg 1");
        }
    }



    // Main del programma server: effettua il controllo degli argomenti da riga di comando e
    // fa partire il thread PingServer
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PingServer port");
            return;
        }
        if (args.length != 1) {
            System.out.println("ERR -arg 1");
            return;
        }
        try {
            int server_port = Integer.parseUnsignedInt(args[0]);
            // controllo che la porta sia nel range consentito dal S.O.
            if (server_port > 65535) {
                throw new IllegalArgumentException();
            }
            PingServer serv = new PingServer(server_port);
            serv.start();
        } catch (Exception e) {
            System.out.println("ERR -arg 1");
        }
    }
}

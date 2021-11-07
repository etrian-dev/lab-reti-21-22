import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

// Classe che implementa il PingServer: prende come argomento il numero di porta sul quale
// il server deve essere in ascolto con un DatagramSocket per ricevere pacchetti di ping dal client
// e rispedirli indietro
public class PingServer extends Thread {
    // generatore usato per decidere se il server deve rispondere o meno ad messaggio ricevuto
    private static final Random rng = new Random();

    private int ping_port;
    private byte[] in_buf;
    private DatagramPacket in_pack;

    public PingServer(int port) {
        this.ping_port = port;
        // alloco un array di dimensione fissa che sicuramente può contenere il messaggio
        this.in_buf = new byte[1024];
        this.in_pack = new DatagramPacket(this.in_buf, this.in_buf.length);
    }

    // Metodo run del thread: crea la DatagramSocket e si mette in ascolto di richieste da parte dei client
    // Viene creato ed aperto un socket in ascolto sulla porta passata come parametro
    // Tale socket viene utilizzato anche per inviare le risposte ai client
    public void run() {
        try (DatagramSocket ping_sock = new DatagramSocket(this.ping_port)) {
            while (true) {
                // waits for a "PING"
                try {
                    ping_sock.receive(in_pack);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Ioex");
                }
                // if the request was a "PING"
                String msg =
                        new String(in_pack.getData(), in_pack.getOffset(), in_pack.getLength());
                if (msg.startsWith("PING")) {
                    System.out
                            .print(in_pack.getSocketAddress().toString().substring(1) + "> " + msg);
                    // Decide (con probabilità circa del 25% se mandare o no risposta ed attende < 2000ms in caso affermativo)
                    if (rng.nextBoolean() || rng.nextBoolean()) {
                        long waits = Math.abs(rng.nextLong()) % 2000;
                        try {
                            Thread.sleep(waits);
                        } catch (InterruptedException e) {
                            System.out.println("ERR: Server delay interrupted");
                        }
                        System.out.println(" ACTION: delayed " + waits + " ms");
                        try {
                            ping_sock.send(in_pack);
                        } catch (IOException e) {
                            System.out.println("ERR: Fallito invio messaggio");
                        }
                    } else {
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

public class PingServer extends Thread {
    private static final String rcv_msg = "PING";
    //private static final String snd_msg = "PONG";
    private static final Random rng = new Random();


    private int ping_port;
    private byte[] in_buf;
    private DatagramPacket in_pack;

    public PingServer(int port) {
        this.ping_port = port;
        this.in_buf = new byte[1024];
        this.in_pack = new DatagramPacket(this.in_buf, this.in_buf.length);
    }

    // the run method
    public void run() {
        try (DatagramSocket dsock = new DatagramSocket(this.ping_port)) {
            while(true) {
                // waits for a "PING"
                try {
                    dsock.receive(in_pack);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Ioex");
                }
                // if the request was a "PING"
                int offt = in_pack.getOffset();
                int len = in_pack.getLength();
                byte[] data = in_pack.getData();
                String msg = new String(data, offt, len);
                if(msg.startsWith(PingServer.rcv_msg)) {
                    System.out.print(msg);
                    // PING received: do a coin flip to decide whether to PONG back
                    // NOTE: the default packet loss percentage is 25% (though it's a PRNG...)
                    if(rng.nextBoolean() || rng.nextBoolean()) {
                        // wait for a random amount of ms before sending it
                        long waits = Math.abs(rng.nextLong()) % 2000;
                        try {
                            Thread.sleep(waits);
                            System.out.println(" ACTION: delayed " + waits + " ms");
                        } catch (Exception e) {
                            //TODO: handle exception
                            e.printStackTrace();
                        }
                        // echo the packet back
                        try {
                            dsock.send(in_pack);
                        } catch (Exception e) {
                            //TODO: handle exception
                            e.printStackTrace();
                        }
                    }
                    else {
                        System.out.println(" ACTION: not sent");
                    }
                }
            }
        } catch (SocketException e) {
            System.out.printf("ERR: il socket non può essere inizializzato");
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("ERR -arg 1: nessuna porta specificata");
        }
        else {
            try {
                // parses the commandline arg as the port where the server will be listening
                int server_port = Integer.parseUnsignedInt(args[0]);

                PingServer serv = new PingServer(server_port);
                serv.start();
            } catch (NumberFormatException ex) {
                System.out.println("ERR -arg 1: numero porta non valido");
            }
        }
    }
}
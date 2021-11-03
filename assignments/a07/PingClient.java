import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

public class PingClient extends Thread {
    // the ping message format is PING seqno timestamp
    private static final String ping_msg_format = "PING %d %d";
    //private static final String snd_msg = "PONG";
    private static final Random rng = new Random();
    private static final int tries = 10;
    private static final long timeout = 2000;

    private byte[] in_buf;
    private DatagramPacket ping_packet;

    public PingClient(String sname, int port) {
        this.in_buf = new byte[1024];
        this.ping_packet = new DatagramPacket(this.in_buf, this.in_buf.length);
        try {
            this.ping_packet.setPort(port);
            this.ping_packet.setAddress(InetAddress.getByName(sname));
        } catch (UnknownHostException uex) {
            System.out.println("Host non trovato");
            uex.printStackTrace();
        }
    }
    public void run() {
        try (DatagramSocket ds = new DatagramSocket()) {
            // timeout 2 secondi
            try {
                ds.setSoTimeout((int)PingClient.timeout);
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            int replies = 0;
            long rtt = 0;
            long min_rtt = PingClient.timeout;
            long max_rtt = 0;
            long total_wait = 0;
            // pings this many times
            for(int i = 0; i < PingClient.tries; i++) {
                // prepare this ping message
                String msg = String.format(PingClient.ping_msg_format, i, System.currentTimeMillis());
                in_buf = msg.getBytes();
                try {
                    ping_packet.setData(msg.getBytes());
                    ds.send(ping_packet);
                    rtt = System.currentTimeMillis();
                    ds.receive(ping_packet);
                    rtt = System.currentTimeMillis() - rtt;
                    System.out.println(msg + " RTT: " + rtt + " ms");
                    total_wait += rtt;
                    replies++;
                    min_rtt = (min_rtt > rtt ? rtt : min_rtt);
                    max_rtt = (max_rtt < rtt ? rtt : max_rtt);
                } catch (SocketTimeoutException e) {
                    // socket timeout expired
                    System.out.println(msg + " RTT: *");
                    //total_wait += PingClient.timeout;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            // All PINGs sent: print statistics
            System.out.println("--- PING Statistics ---\n"
            + PingClient.tries + " packets transmitted, " 
            + replies + " packets received, " 
            + (PingClient.tries - replies) * 10 + "% packet loss\n"
            + "round-trip (ms) min/avg/max = " + min_rtt + "/" + (double)(total_wait / replies) + "/" + max_rtt);
            // QUESTION: ping average RTT on packets received or all packets?
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serv_name = args[0];
        int serv_port;
        try {
            serv_port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("ERR -arg 1: the port number is invalid");
            return;
        }
        PingClient client = new PingClient(serv_name, serv_port);
        client.start();
    }
}

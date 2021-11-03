import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random; // to simulate packet loss and delay
import java.lang.Math;

public class PingPongClient implements Runnable {
    // The message to be delivered by the datagram
    private static final String send_msg = "PING";
    // The message to be received from PingPongServer
    private static final String rcv_msg = "PONG";
    // the port where the server is listening
    private static final int serverPort = 50000;

    // instance variables
    private DatagramPacket in_packet;
    private byte[] in_databuf;
    private DatagramPacket out_packet;
    private byte[] out_databuf;

    private final Random rng = new Random();

    // initializes the datagram packet and its buffer
    public PingPongClient() {
        this.in_databuf = new byte[PingPongClient.rcv_msg.length()]; // a fixed buffer, since the message length is
                                                                     // known
        this.in_packet = new DatagramPacket(this.in_databuf, in_databuf.length);
        this.out_databuf = new byte[PingPongClient.send_msg.length()]; // a fixed buffer, since the message length is
                                                                       // known
        this.out_packet = new DatagramPacket(this.out_databuf, out_databuf.length);
    }

    public void run() {
        // a new UDP socket is created and bind to a random port
        int timeout = rng.nextInt(10) * 1000;
        try (DatagramSocket ds = new DatagramSocket()) {
            // sets the timeout for this socket
            ds.setSoTimeout(timeout);

            // sends the first "PING" datagram to the server
            out_packet.setPort(PingPongClient.serverPort);
            out_packet.setAddress(InetAddress.getByName("localhost"));
            out_packet.setData(PingPongClient.send_msg.getBytes());
            out_packet.setLength(PingPongClient.send_msg.length());
            ds.send(out_packet);
            do {
                // blocks until a datagram has been received from a client
                // or the timeout expires
                ds.receive(in_packet);
                in_databuf = in_packet.getData();
                // translates the received message as a String and compares it to "PONG"
                String rcvmsg = new String(in_databuf, 0, in_databuf.length);
                if (rcvmsg.equals(PingPongClient.rcv_msg)) {
                    // the client received a "PONG" -> responds that host with a "PING" message
                    // retrieve the sender's address first
                    InetAddress addr = in_packet.getAddress();
                    int port = in_packet.getPort();
                    System.out.println("Received PONG from server " + addr.toString() + " at port " + port);

                    // now prepare the response message
                    out_packet.setAddress(addr);
                    out_packet.setPort(port);
                    out_packet.setData(PingPongClient.send_msg.getBytes());
                    out_packet.setLength(PingPongClient.send_msg.length());

                    // the server introduces a random delay in [0,1000)
                    long sleep_amount = Math.abs(rng.nextLong() % 1000);
                    try {
                        Thread.sleep(sleep_amount);
                    } catch (InterruptedException e) {
                        System.out.println("Sleep interrupted");
                    }

                    // then send the packet using the datagram socket
                    ds.send(out_packet);
                    System.out.println("PING sent back after " + sleep_amount + "ms");

                }
            } while (true); // actually loops until the socket times out => an exception is thrown
        } catch (SocketTimeoutException soex) {
            System.out.println("Timeout expired (" + timeout + "ms): termination");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A simple main to start the execution
    public static void main(String[] args) {
        PingPongClient pclient = new PingPongClient();
        // starts some clients
        // for(int i = 0; i < 10; i++) {
        Thread t = new Thread(pclient);
        t.start();
        // }
    }
}
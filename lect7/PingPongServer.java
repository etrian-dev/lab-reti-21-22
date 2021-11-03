import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random; // to simulate packet loss and delay
import java.lang.Math;

public class PingPongServer implements Runnable {
    // The message to be delivered by the datagram
    private static final String send_msg = "PONG";
    // The message to be received from PingPongClients
    private static final String rcv_msg = "PING";
    // the port where the server is listening
    private static final int port = 50000;

    // instance variables
    private DatagramPacket in_packet;
    private byte[] in_databuf;
    private DatagramPacket out_packet;
    private byte[] out_databuf;

    private final Random rng = new Random();

    // initializes the datagram packet and its buffer
    public PingPongServer() {
        this.in_databuf = new byte[PingPongServer.rcv_msg.length()]; // a fixed buffer, since the message length is
                                                                     // known
        this.in_packet = new DatagramPacket(this.in_databuf, in_databuf.length);
        this.out_databuf = new byte[PingPongServer.send_msg.length()]; // a fixed buffer, since the message length is
                                                                       // known
        this.out_packet = new DatagramPacket(this.out_databuf, out_databuf.length);
    }

    public void run() {
        // a new UDP socket is created and bind to the default port
        try (DatagramSocket ds = new DatagramSocket(PingPongServer.port)) {
            System.out.println("Server waiting for \"PING\" on port " + ds.getLocalPort());
            while (true) {
                // blocks until a datagram has been received from a client
                ds.receive(in_packet);
                in_databuf = in_packet.getData();
                // translates the received message as a String and compares it to "PING"
                String rcvmsg = new String(in_databuf, 0, in_databuf.length);
                if (rcvmsg.equals(PingPongServer.rcv_msg)) {
                    // the server received a "PING" -> responds that host with a "PONG" message
                    // retrieve the sender's address first
                    InetAddress addr = in_packet.getAddress();
                    int port = in_packet.getPort();
                    System.out.println("Received PING from client " + addr.toString() + " at port " + port);

                    // the server simulates a coin flip to decide whether to send back a reply or
                    // not
                    if (rng.nextBoolean()) {
                        // now prepare the response message
                        out_packet.setAddress(addr);
                        out_packet.setPort(port);
                        out_packet.setData(PingPongServer.send_msg.getBytes());
                        out_packet.setLength(PingPongServer.send_msg.length());

                        // the server introduces a random delay in [0,1000)
                        long sleep_amount = Math.abs(rng.nextLong() % 1000);
                        try {
                            Thread.sleep(sleep_amount);
                        } catch (InterruptedException e) {
                            System.out.println("Sleep interrupted");
                        }

                        // then send the packet using the datagram socket
                        ds.send(out_packet);
                        System.out.println("PONG sent back after " + sleep_amount + "ms");
                    } else {
                        System.out.println("PONG lost");
                    }
                }
            }
        } catch (SocketException soex) {
            soex.printStackTrace();
            System.out.println("Fallita inizializzazione del socket: " + soex.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A simple main to start the execution
    public static void main(String[] args) {
        PingPongServer pserver = new PingPongServer();
        Thread t = new Thread(pserver);
        t.start();
    }
}
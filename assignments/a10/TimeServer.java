import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class TimeServer extends Thread {
    private DatagramSocket msock;
    private DatagramPacket dp;
    private InetSocketAddress mcastaddr;

    public TimeServer(InetAddress addr, int port) throws IOException {
        this.msock = new DatagramSocket();
        this.mcastaddr = new InetSocketAddress(addr, port);
    }

    // Invia il timestamp sul gruppo multicast specificato
    public void send_tstamp() throws IOException {
        Long stamp = System.currentTimeMillis();
        String stamp_str = stamp.toString();
        // Il datagramma creato deve contenere l'indirizzo del gruppo multicast
        this.dp = new DatagramPacket(stamp_str.getBytes(), stamp_str.length(), this.mcastaddr);
        System.out.println("timestamp: " + stamp_str + " inviato a " + this.mcastaddr.toString());
        try {
            this.msock.send(dp);
        } catch (IOException e) {
            throw new IOException("Impossibile inviare il datagramma");
        }
    }

    // Loop del server: invia un datagramma ogni secondo sul gruppo multicast
    public void run() {
        while (true) {
            try {
                send_tstamp();
                Thread.sleep(1000);
            } catch (InterruptedException eint) {
                System.out.println("Wait interrupted");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    // Il main legge 
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java TimeServer <ip>");
            return;
        }
        boolean addr_ok = true;
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(args[0]);
            if (!(addr.isMulticastAddress() && addr.isMCSiteLocal())) {
                System.out
                        .println("Indirizzo " + args[0] + " non multicast o multicast site-local");
                addr_ok = false;
            }
        } catch (UnknownHostException uex) {
            System.out.println("Host " + args[0] + " sconosciuto");
            addr_ok = false;
        }

        if (addr_ok) {
            try {
                // Creo e faccio partire il server, che spedirà datagrammi all'indirizzo
                // addr, alla porta PORT predefinita dal client
                TimeServer ts = new TimeServer(addr, TimeClient.PORT);
                ts.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

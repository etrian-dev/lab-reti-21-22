import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

public class TimeClient {
    public static final int PORT = 22222;

    private MulticastSocket msock;
    private InetAddress dategroup;
    private NetworkInterface netif;
    private DatagramPacket dp;
    private byte[] timestamp;

    public TimeClient(InetAddress addr, NetworkInterface netiface) throws IOException {
        this.dategroup = addr;
        this.netif = netiface;
        this.msock = new MulticastSocket(PORT);
        this.msock.setReuseAddress(true); // per rendere possibile il binding di più client contemporaneamente
        this.msock.joinGroup(new InetSocketAddress(this.dategroup, PORT), this.netif);
        System.out.println("Join gruppo multicast " + this.dategroup);
    }

    public ArrayList<String> getTimestamps() throws IOException {
        ArrayList<String> tstamps = new ArrayList<>(10);
        // Alloca un datagramma nel quale verranno letti i timestamp mandati al gruppo
        System.out.print("Ricevuti: ");
        this.timestamp = new byte[1024];
        this.dp = new DatagramPacket(timestamp, timestamp.length);
        for (int i = 0; i < 10; i++) {
            this.msock.receive(dp);
            System.out.print(i + " ");
            // Ogni datagramma viene trasformato in una stringa e messo in coda all'arraylist
            tstamps.add(new String(dp.getData(), dp.getOffset(), dp.getLength()));
        }
        System.out.append('\n');
        // Una volta ricevuti i timestamp posso lasciare il gruppo
        try {
            this.msock.leaveGroup(new InetSocketAddress(this.dategroup, PORT), this.netif);
            System.out.println("Gruppo multicast " + this.dategroup + " abbandonato");
        } catch (IOException e) {
            System.out.println("Failed to leave multicast group: " + e.getMessage());
        }

        return tstamps;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java TimeClient <ip> <netif>");
            return;
        }
        boolean param_ok = false;
        InetAddress addr = null;
        NetworkInterface netif = null;
        try {
            addr = InetAddress.getByName(args[0]);
            netif = NetworkInterface.getByName(args[1]);
            if (!(addr.isMulticastAddress() && addr.isMCSiteLocal())) {
                System.out
                        .println("Indirizzo " + args[0] + " non multicast o multicast site-local");
            }
        } catch (UnknownHostException uex) {
            System.out.println("Host " + args[0] + " non raggiungibile");
        } catch (SocketException sock_ex) {
            System.out.println("Interfaccia di rete " + args[1] + " errata");
        } finally {
            param_ok = true;
        }

        if (!param_ok) {
            return;
        }

        try {
            TimeClient tc = new TimeClient(addr, netif);
            ArrayList<String> stamps = tc.getTimestamps();

            // stampa output
            System.out.printf("%-10s|%-20s\n", "Sequenza", "Data ed ora");
            System.out.println("-------------------------------------------");
            for (int j = 0; j < stamps.size(); j++) {
                System.out.printf("%-10d|%-20s\n", j, new Date(Long.valueOf(stamps.get(j))));
            }
        } catch (UnknownHostException uke) {
            uke.printStackTrace();
        } catch (BindException be) {
            be.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

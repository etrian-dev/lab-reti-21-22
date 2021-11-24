import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    ServerSocketChannel schan_listener;
    SelectionKey key;
    ExecutorService tpool = Executors.newFixedThreadPool(10);
    boolean msgRecv = false;

    public static int BUFSZ = 8192;

    public EchoServer(Selector sel) throws IOException {
        // Creates a ServerSocketChannel to listen for connections from clients
        schan_listener = ServerSocketChannel.open();
        schan_listener.configureBlocking(false);
        schan_listener.bind(new InetSocketAddress(InetAddress.getByName("localhost"), 9999));
        key = schan_listener.register(sel, SelectionKey.OP_ACCEPT);
    }

    // Accepts a new connection from a client and registers the SocketChannel opened for reading
    public void accept_conn(Selector sel) {
        ServerSocketChannel schan = (ServerSocketChannel) key.channel();
        // May throw IOException, so it's wrapped in a try-with-resources
        try (SocketChannel ss = schan.accept();) {
            System.out
                    .println("Accepted connection from client " + ss.getLocalAddress().toString());
            // The SocketChannel must be set nonblocking
            ss.configureBlocking(false);
            // A new buffer for this client is alloc'd and passed as an attachment
            ByteBuffer buf = ByteBuffer.allocate(EchoServer.BUFSZ);
            // register the socket for reading operations on the selector
            ss.register(sel, SelectionKey.OP_READ, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle_read(Selector sel, SelectionKey key, SocketChannel chan) {
        try {
            System.out.println("Ready to read");
            ByteBuffer bbuf = (ByteBuffer) key.attachment();
            chan.read(bbuf);
            System.out.println("Read " + bbuf.toString());
            bbuf.flip();
            chan.register(sel, SelectionKey.OP_WRITE, bbuf);
            this.msgRecv = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle_write(Selector sel, SelectionKey key, SocketChannel chan) {
        try {
            if (this.msgRecv) {
                ByteBuffer bbuf = (ByteBuffer) key.attachment();
                String output = new String(bbuf.array());
                output.replaceAll("\n", " ");
                output += " [echoed by server]\n";
                ByteBuffer outBuf = ByteBuffer.wrap(output.getBytes());
                System.out.println("Ready to write");
                chan.write(outBuf);
                bbuf.clear();
                System.out.println("Written");
                this.msgRecv = false;
                chan.register(sel, SelectionKey.OP_READ, bbuf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Selector sel = null;
        try {
            // creates a new selector and passed it to an instance of the EchoServer
            sel = Selector.open();
            EchoServer serv = new EchoServer(sel);
        } catch (IOException io) {
            System.out.println("ERR: Apertura selettore fallita");
            io.printStackTrace();
        }
        while (true) {
            try {
                if (sel.select() > 0) {
                    // Get the set of ready keys into an iterator
                    Set<SelectionKey> readyKeys = sel.selectedKeys();
                    Iterator<SelectionKey> iter = readyKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        // once a SelectionKey has been retrieved, 
                        //remove it from the set of selected keys
                        iter.remove();
                        if (key.isAcceptable()) {
                            // The ServerSocketChannel is ready to accept a new connection
                            serv.accept_conn(sel); // creates a new socket SocketChannel and registers it
                        }
                        if (key.isReadable()) {
                            // Bytes can be read from this (Socket)Channel
                            serv.handle_read(sel, key, (SocketChannel) key.channel());
                        }
                        if (key.isWritable()) {
                            // Bytes can be written to this (Socket)Channel
                            serv.handle_write(sel, key, (SocketChannel) key.channel());
                        }
                    }
                }
            } catch (IOException io) {
                System.out.println("ERR: select fallita (IOException)");
                io.printStackTrace();
            }
        }
    }
}

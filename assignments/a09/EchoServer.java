import java.io.IOException;
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
    public static String ECHO_MSG = " [echoed by server]\n";

    public EchoServer(Selector sel, String host, int port) throws IOException {
        // Creates a ServerSocketChannel to listen for connections from clients
        schan_listener = ServerSocketChannel.open();
        schan_listener.configureBlocking(false);
        String serv_host = (host != null ? host : "localhost");
        int serv_port = (port > 1024 && port < 65536 ? port : 0);
        schan_listener.bind(new InetSocketAddress(serv_host, serv_port));
        System.out.println("Server listening for connections on port "
                + schan_listener.socket().getLocalPort());
        // register for accepting operations to the selector
        key = schan_listener.register(sel, SelectionKey.OP_ACCEPT);
    }

    // Accepts a new connection from a client and registers 
    // the newly created SocketChannel for reading with the same selector
    public void accept_conn(Selector sel) {
        ServerSocketChannel schan = (ServerSocketChannel) key.channel();
        try {
            SocketChannel ss = schan.accept();
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

    // The SocketChannel is ready for reading
    public void handle_read(Selector sel, SelectionKey key, SocketChannel chan) {
        try {
            System.out.println("Ready to read");
            // get the attached buffer
            ByteBuffer bbuf = (ByteBuffer) key.attachment();
            bbuf.clear();
            int bytes_read = chan.read(bbuf);
            // End of stream if -1 is returned
            if (bytes_read == -1) {
                key.cancel();
                chan.close();
                return;
            }
            // set the limit to the bytes read
            bbuf.put(EchoServer.ECHO_MSG.getBytes());
            bbuf.limit(bytes_read + EchoServer.ECHO_MSG.length());
            System.out.println("Read " + bytes_read + "bytes: bbuf is" + bbuf.toString());
            // register the write operation on the channel (passing in the buffer to be written)
            chan.register(sel, SelectionKey.OP_WRITE, bbuf);
            // this ensures that only the write operation is in the interest set of this key
            key.interestOpsAnd(SelectionKey.OP_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The SocketChannel is ready for writing
    public void handle_write(Selector sel, SelectionKey key, SocketChannel chan) {
        try {
            ByteBuffer bbuf = (ByteBuffer) key.attachment();
            bbuf.flip();
            System.out.println("Ready to write");
            chan.write(bbuf);
            System.out.println("Written " + new String(bbuf.array(), 0, bbuf.limit()));
            // this ensures that only the read operation is in the interest set of this key
            chan.register(sel, SelectionKey.OP_READ, bbuf);
            key.interestOpsAnd(SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Selector sel = null;
        EchoServer serv = null;
        try {
            // creates a new selector and passed it to an instance of the EchoServer
            sel = Selector.open();
            serv = new EchoServer(sel, "localhost", 9999);
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
                        } else if (key.isWritable()) {
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

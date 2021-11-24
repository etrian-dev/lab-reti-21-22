import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    ServerSocketChannel schan_listener;
    SelectionKey key;
    ExecutorService tpool = Executors.newFixedThreadPool(10);

    public EchoServer(Selector sel) {
        // create the listening channel
        try {
            schan_listener = ServerSocketChannel.open();
            schan_listener.configureBlocking(false);
            schan_listener.bind(new InetSocketAddress("127.0.0.1", 9999));
            key = schan_listener.register(sel, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void accept_conn(Selector sel) {
        try {
            ServerSocketChannel schan = (ServerSocketChannel) key.channel();
            SocketChannel ss = schan.accept();
            System.out.println("Accepted connection");
            // Store the reference to the connected client socket channel
            ss.configureBlocking(false);
            ByteBuffer buf = ByteBuffer.allocate(8192);
            // register for read & write operations on the selector
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
            String output = new String(bbuf.array());
            output.replaceAll("\n", " ");
            output += " [echoed by server]\n";
            ByteBuffer outBuf = ByteBuffer.wrap(output.getBytes());
            chan.register(sel, SelectionKey.OP_WRITE, outBuf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle_write(Selector sel, SelectionKey key, SocketChannel chan) {
        try {
            System.out.println("Ready to write");
            ByteBuffer bbuf = (ByteBuffer) key.attachment();
            chan.write(bbuf);
            bbuf.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Selector sel = Selector.open();
            EchoServer serv = new EchoServer(sel);
            while (true) {
                int nselected = sel.select();

                Set<SelectionKey> readyKeys = sel.selectedKeys();
                Iterator<SelectionKey> iter = readyKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        serv.accept_conn(sel);
                    }
                    if (key.isReadable()) {
                        serv.handle_read(sel, key, (SocketChannel) key.channel());
                    }
                    if (key.isWritable()) {
                        serv.handle_write(sel, key, (SocketChannel) key.channel());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
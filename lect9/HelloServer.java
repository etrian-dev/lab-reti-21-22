import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.Iterator;

public class HelloServer {
    

    public HelloServer() {
        ;
    }

    public static void main(String[] args) {
        ServerSocketChannel schan;
        Selector sel;
        
        while(true) {
            try {
                schan = ServerSocketChannel.open();
                schan.bind(null);
                System.out.println(schan.getLocalAddress().toString());
                schan.configureBlocking(false);
                sel = Selector.open();

                SelectionKey skey = schan.register(sel, SelectionKey.OP_ACCEPT);
                SelectionKey skey_client = null;
                SocketChannel client_sock = null;
                try {
                    while(true) {
                        int nready = sel.select();
                        Iterator<SelectionKey> select_set = sel.selectedKeys().iterator();
                        while(select_set.hasNext()) {
                            SelectionKey ready_k = select_set.next();
                            select_set.remove();
                            if(ready_k.isAcceptable()) {
                                ServerSocketChannel ss = (ServerSocketChannel) ready_k.channel();
                                client_sock = ss.accept();
                                // register for writing
                                skey_client = client_sock.register(sel, SelectionKey.OP_WRITE);
                            }
                            else if(ready_k.isWritable()) {
                                byte[] msg = "HelloClient".getBytes();
                                ByteBuffer buf = ByteBuffer.wrap(msg);
                                buf.flip();
                                System.out.println(buf.toString());
                                client_sock.write(buf);
                                client_sock.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }
}
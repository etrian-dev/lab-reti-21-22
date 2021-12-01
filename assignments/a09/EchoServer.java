import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.cli.*;

public class EchoServer {
    // Vari valori di default
    public static String PORT_DFLT = "33333";
    public static int BUFSZ = 8192;
    public static String ECHO_MSG = " [echoed by server]";

    // Il socketchannel del server
    private ServerSocketChannel schan_listener;
    SelectionKey key;

    // Costruttore del server: prende come parametro il selector sul quale registra
    // il ServerSocketChannel per l'operazione di accept e controlla porte
    public EchoServer(Selector sel, int port) throws IOException {
        this.schan_listener = ServerSocketChannel.open();
        // Settato in modalità non bloccante
        this.schan_listener.configureBlocking(false);
        // Controllo porta valida
        if (port < 1024 || port > 65536) {
            throw new IOException("Porta fuori dal range o occupata");
        }
        // Potrebbe comunque essere occupata, per cui catch con messaggio
        try {
            this.schan_listener.bind(new InetSocketAddress("localhost", port));
        } catch (BindException e) {
            throw new IOException("Porta " + port + " occupata");
        }
        System.out.println("Server in ascolto sulla porta " + port);
        // Registro il server sul selettore per accept
        this.key = schan_listener.register(sel, SelectionKey.OP_ACCEPT);
    }

    // Accetta una nuova connessione dal client e registra il SocketChannel creato in lettura
    public void accept_conn(Selector sel) throws IOException {
        ServerSocketChannel schan = (ServerSocketChannel) key.channel();
        SocketChannel ss = schan.accept();
        System.out.println("Connessione accettata dal client " + ss.getLocalAddress().toString());
        // SocketChannel settato non bloccante
        ss.configureBlocking(false);
        // Il client usa sempre questo buffer passato come attachment
        ByteBuffer buf = ByteBuffer.allocate(EchoServer.BUFSZ);
        ss.register(sel, SelectionKey.OP_READ, buf);
    }

    // SocketChannel pronto per lettura
    public void handle_read(Selector sel, SelectionKey key, SocketChannel chan) throws IOException {
        // Ottiene il ByteBuffer e tenta di leggere
        ByteBuffer bbuf = (ByteBuffer) key.attachment();
        bbuf.clear();
        int bytes_read = chan.read(bbuf);
        // End of stream se ritorna -1 => chiudo il channel dopo aver fatto cancel
        if (bytes_read == -1) {
            key.cancel();
            chan.close();
            return;
        }
        // Concateno il messaggio del server e setto limit al messaggio generato
        bbuf.put(EchoServer.ECHO_MSG.getBytes());
        bbuf.limit(bytes_read + EchoServer.ECHO_MSG.length());
        // Registro il channel per scrittura (e cancello la registrazione per la lettura)
        chan.register(sel, SelectionKey.OP_WRITE, bbuf);
        key.interestOpsAnd(SelectionKey.OP_WRITE);

    }

    // SocketChannel pronto per scrittura
    public void handle_write(Selector sel, SelectionKey key, SocketChannel chan)
            throws IOException {
        // Recupero il buffer contenente il messaggio da inviare
        ByteBuffer bbuf = (ByteBuffer) key.attachment();
        bbuf.flip();
        chan.write(bbuf);
        System.out.println("Echoed \"" + new String(bbuf.array(), 0, bbuf.limit()) + "\"");
        // Cancello registrazione per scrittura e la metto per lettura
        chan.register(sel, SelectionKey.OP_READ, bbuf);
        key.interestOpsAnd(SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        // Definisco l'opzione da riga di comando -p per specificare la porta
        Options all_opts = new Options();
        Option port_opt =
                new Option("p", true, "Porta su cui il server è in ascolto per nuove connessioni ("
                        + EchoServer.PORT_DFLT + " di default)");
        port_opt.setOptionalArg(true);
        all_opts.addOption(port_opt);
        HelpFormatter help = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();

        Integer port = null;
        try {
            CommandLine parsed_args = parser.parse(all_opts, args);
            port = Integer
                    .valueOf(new String(parsed_args.getOptionValue("p", EchoServer.PORT_DFLT)));
        } catch (Exception parseEx) {
            help.printHelp("EchoServer", all_opts);
            return;
        }

        Selector sel = null;
        try {
            // creates a new selector and passed it to an instance of the EchoServer
            sel = Selector.open();
        } catch (IOException io) {
            System.out.println("ERR: Apertura selettore fallita");
            return;
        }

        EchoServer serv = null;
        try {
            serv = new EchoServer(sel, port);
        } catch (IOException e) {
            System.out.println("ERR: " + e.getMessage());
            return;
        }

        // Loop di processing del server: effettua select ed esegue la funzione appropriata
        while (true) {
            try {
                if (sel.select() > 0) {
                    Set<SelectionKey> readyKeys = sel.selectedKeys();
                    Iterator<SelectionKey> iter = readyKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            // Il ServerSocketChannel è pronto per ricevere una nuova connessione
                            // Crea un SocketChannel per il client e lo registra per lettura
                            serv.accept_conn(sel);
                        }
                        if (key.isReadable()) {
                            // Il SocketChannel indicato è pronto in lettura
                            serv.handle_read(sel, key, (SocketChannel) key.channel());
                        } else if (key.isWritable()) {
                            // Il SocketChannel indicato è pronto in scrittura
                            serv.handle_write(sel, key, (SocketChannel) key.channel());
                        }
                    }
                }
            } catch (IOException io) {
                System.out.println("ERR: " + io.getMessage());
            }
        }
    }
}

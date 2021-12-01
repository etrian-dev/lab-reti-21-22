import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import org.apache.commons.cli.*;

public class EchoClient {
	public static String HOST_DFLT = "localhost";

	SocketChannel schan;

	public EchoClient(String hostname, int port) throws IOException {
		System.out.println("Connessione al server sulla porta " + port);
		schan = SocketChannel.open();
		schan.connect(new InetSocketAddress(hostname, port));
	}

	public boolean send_data(String data) {
		if (!schan.isOpen()) {
			return false;
		}

		ByteBuffer bbuf = ByteBuffer.wrap(data.getBytes());
		int written = 0;
		try {
			while (bbuf.hasRemaining()) {
				written += schan.write(bbuf);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	public boolean read_reply(int len) {
		ByteBuffer buf = ByteBuffer.allocate(len + EchoServer.ECHO_MSG.length() + 1);
		try {
			int bread = schan.read(buf);

			if (bread == -1) {
				return false;
			}
			String msg = new String(buf.array(), 0, bread);
			System.out.println(msg);
		} catch (ClosedChannelException cce) {
			System.out.println("ERR: The socket was closed by the server");
			return false;
		} catch (IOException e) {
			System.out.println("ERR: I/O error");
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		String host = null;
		Integer port = null;

		// Define command line options
		Options all_opts = new Options();
		Option host_opt = new Option("h", true, "Hostname del server (localhost di default)");
		host_opt.setOptionalArg(true);
		Option port_opt = new Option("p", true, "Porta su cui il server è in ascolto");
		port_opt.setOptionalArg(true);
		all_opts.addOption(port_opt);
		HelpFormatter help = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine parsed_args = parser.parse(all_opts, args);
			host = parsed_args.getOptionValue("h", EchoClient.HOST_DFLT);
			port = Integer.valueOf(parsed_args.getOptionValue("p", EchoServer.PORT_DFLT));
		} catch (Exception parseEx) {
			help.printHelp("EchoClient", all_opts);
			return;
		}
		// Leggo con lo scanner una riga da stdin
		try (Scanner scan = new Scanner(System.in);) {

			EchoClient client = new EchoClient(host, port);
			while (true) {
				try {
					String line = scan.nextLine();
					if (!client.send_data(line)) {
						System.out.println("ERR: Impossibile inviare il messaggio");
						return;
					}
					if (!client.read_reply(line.length())) {
						System.out.println("ERR: Impossibile ricevere il messaggio");
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			System.out.println("ERR:I/O error: " + ex.getMessage());
		}
	}
}

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
		schan = SocketChannel.open();
		schan.connect(new InetSocketAddress(hostname, port));
	}

	public void send_data(String data) throws IOException {
		ByteBuffer bbuf = ByteBuffer.wrap(data.getBytes());
		int written = 0;
		while (bbuf.hasRemaining()) {
			written += schan.write(bbuf);
		}
	}

	public void read_reply(int len) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(len + EchoServer.ECHO_MSG.length() + 1);
		int bread = schan.read(buf);
		if(bread == -1) {
			return;
		}
		String msg = new String(buf.array(), 0, bread);
		System.out.println(msg);
	}

	public static void main(String[] args) {
		String host = null;
		Integer port = null;

		// Define command line options
        Options all_opts = new Options();
        Option host_opt = new Option("h", true,
				"The hostname of the server (localhost by default)");
        Option port_opt = new Option("p", true,
                "The port the server listens to for new TCP connections");
        all_opts.addOption(host_opt);
        all_opts.addOption(port_opt);
        HelpFormatter help = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine parsed_args = parser.parse(all_opts, args);
            host = parsed_args.getOptionValue(host_opt);
            port = Integer.valueOf(parsed_args.getOptionValue(port_opt));
        } catch (Exception parseEx) {
            parseEx.printStackTrace();
            help.printHelp("EchoClient", all_opts);
            return;
        }
        if(host == null) {
			host = EchoClient.HOST_DFLT;
		}
        if(port == null) {
            port = EchoServer.PORT_DFLT;
        }
		try (Scanner scan = new Scanner(System.in);) {

			EchoClient client = new EchoClient(host, port);
			while (true) {
				try {
					String line = scan.nextLine();
					try {
						client.send_data(line);
						client.read_reply(line.length());
					} catch (ClosedChannelException closed) {
						System.out.println("ERR: socket closed: " + closed.getMessage());
						return;
					} catch (IOException ex) {
						System.out.println("ERR: IO error: " + ex.getMessage());
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

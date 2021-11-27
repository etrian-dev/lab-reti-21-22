import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class EchoClient {
	SocketChannel schan;

	public EchoClient(String hostname, int port) throws IOException {
		schan = SocketChannel.open();
		schan.connect(new InetSocketAddress(hostname, port));
	}

	public void send_data(String data) {
		System.out.println("Data: " + data);
		ByteBuffer bbuf = ByteBuffer.wrap(data.getBytes());
		//bbuf.flip();
		int written = 0;
		try {
			while (bbuf.hasRemaining()) {
				written += schan.write(bbuf);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Written " + written + " bytes");
	}

	public void print_reply(int len) {
		ByteBuffer buf = ByteBuffer.allocate(Math.max(len * 2, 1024));
		try {
			int bread = schan.read(buf);
			System.out.println(buf.toString());
			String msg = new String(buf.array(), 0, bread);
			System.out.println("RECEIVED: " + msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String host = "localhost";
		int port = 9999;
		if (args.length == 2) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
		try (Scanner scan = new Scanner(System.in);) {

			EchoClient client = new EchoClient(host, port);
			while (true) {
				try {
					String line = scan.nextLine();
					client.send_data(line);
					client.print_reply(line.length());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

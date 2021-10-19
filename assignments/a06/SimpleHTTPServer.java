import java.net.*;
import java.util.Random;
import java.io.*;

public class SimpleHTTPServer {
    public static void main(String[] args) {
        int listen_port;
        if(args.length == 1) {
            listen_port = Integer.parseInt(args[0]);
        }
        else {
            Random rng = new Random(System.currentTimeMillis());
            listen_port = (1024 + rng.nextInt(65536)) % 65536;
        }

        // DEBUG
        listen_port = 1111;
        
        try {
            // il server ascolta sulla porta specificata
            ServerSocket listen_sock = new ServerSocket(listen_port);
            System.out.println("Server HTTP in ascolto sulla porta " + listen_sock.getLocalPort());

            //while(true) {
                // blocco fino all'accettazione della connessione
                Socket cs = listen_sock.accept();
                // solo una connessione: posso chiudere la socket in ascolto
                // send the bytes from the file trough the socket
                InputStream reqStream = cs.getInputStream();
                OutputStream repStream = cs.getOutputStream();
                
                byte[] request = new byte[1024];
                int bread = reqStream.read(request);
                String textReq = new String(request);
                System.out.println("Read " + bread + "bytes");
                System.out.println(textReq);

                String notfound = "HTTP/1.0 404 File not found\r\n\r\n";
                String badreq = "HTTP/1.0 400 Bad request\r\n\r\n";
                String rep = "HTTP/1.0 200 OK\r\n\r\n";

                String[] all_lines = textReq.split("\r\n", 2);
                String request_line = all_lines[0];
                String[] request_fields = request_line.split(" ");
                System.out.println("Method: " + request_fields[0]);
                System.out.println("Resource: " + request_fields[1]);
                System.out.println("HTTP version: " + request_fields[2]);
                if(request_fields[0].equals("GET")) { // risponde solo a "GET"
                    File resource = new File(request_fields[1].substring(1)); // erase the leading slash from the request
                    if(!resource.exists()) {
                        // write a 404 (Resource not found) response
                        repStream.write(notfound.getBytes());
                        // write html page no resources
                    }
                    else {
                        // open the file and read its contents in a byte buffer
                        FileInputStream fin = new FileInputStream(resource);
                        byte[] content = new byte[1024];
                        fin.read(content);
                        fin.close();
                        // write the 200 (OK) response 
                        repStream.write(rep.getBytes());
                        // the write the file's contents    
                        repStream.write(content);
                        repStream.flush();
                    }
                }
                else {
                    // bad request
                    repStream.write(badreq.getBytes());
                    String badtext = "<html><head><title>Huh</title><body><h1>The server did not understand the request</h1><p>"+textReq+"</p>";
                    repStream.write(badtext.getBytes());
                    repStream.flush();
                }
                
                // chiudo socket attiva verso il client (bidirezionale)
                cs.close();

            listen_sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
import java.net.*;
import java.util.Random;
import java.io.*;

/**
 * La classe implementa un semplice server HTTP, che risponde solo a richieste GET
 */
public class SimpleHTTPServer {
    private static final String responseFormat = "HTTP/1.%d %3d %s\r\n\r\n";
    private static final String resp_skel =
            new String("<html>\n\t<head>\n\t\t<title>Not Found</title>\n\t</head>\n\t<body>"
                    + "%s\n\t</body>\n</html>");

    public static void main(String[] args) {
        // La porta del socket sul quale il server si mette in ascolto di richieste di connessione
        int conn_port;
        // Se viene specificata una porta come parametro da riga di comando viene usata quella
        // per il socket (se disponibile), altrimenti ne viene selezionata una di default
        if (args.length == 1) {
            conn_port = Integer.parseInt(args[0]);
        } else {
            Random rng = new Random(System.currentTimeMillis());
            conn_port = 0; // a random port gets assigned by the OS to this socket
        }

        // DEBUG
        conn_port = 1111;

        try {
            // il server stampa su quale porta è in ascolto
            ServerSocket listen_sock = new ServerSocket(conn_port);
            System.out.println("Server HTTP in ascolto sulla porta " + listen_sock.getLocalPort());

            // il server si mette in attesa di richieste di connessione da parte di un client alla volta
            boolean exit = false;
            while (!exit) {
                Socket conn_sock = null;
                try {
                    conn_sock = listen_sock.accept();
                } catch (InterruptedIOException ex) {
                    System.out.println("IO interrupted");
                    exit = true;
                }
                // creata la connessione TCP:
                // 1) legge una richiesta HTTP dall'input stream
                // 2) la serve mandando sull'output stream la risposta
                //      200: risorsa trovata
                //      400: richiesta diversa da GET
                //      404: risorsa non trovata
                // 3) chiude la connessione (connessione non persistente)
                try (BufferedReader requestStream =
                        new BufferedReader(new InputStreamReader(conn_sock.getInputStream()));
                        BufferedOutputStream replyStream =
                                new BufferedOutputStream(conn_sock.getOutputStream());) {

                    // leggo la request line
                    String requestLine = requestStream.readLine();
                    if (requestLine == null) {
                        conn_sock.close();
                        continue;
                    }
                    // stampo la request line letta su terminale
                    System.out.println(requestLine);

                    // Se era una richiesta diversa da GET non la gestisco (400)
                    if (!requestLine.startsWith("GET")) {
                        String badreq = String.format(responseFormat, 0, 400, "Bad Request");
                        String body = String.format(resp_skel, "The request <i>" + requestLine
                                + "</i? cannot be processed by this server");
                        // stampo sul terminale l'esito (in rosso)
                        System.out.println((char) 27 + "[31m" + badreq + (char) 27 + "[0m");
                        // invio al client la risposta
                        replyStream.write(badreq.getBytes());
                        replyStream.write(body.getBytes());
                        replyStream.flush();
                    }
                    // altrimenti ottengo l'URL della risorsa da inviare
                    else {
                        String resource = requestLine.split(" ")[1];
                        if (resource.startsWith("/")) {
                            resource = resource.substring(1);
                        }
                        // Creo un oggetto File il cui path astratto punta alla risorsa
                        File res = new File(resource);

                        // Se la risorsa non esiste rispondo con 404
                        if (!res.exists()) {
                            String nfound = String.format(responseFormat, 0, 404, "Not Found");
                            String text = String.format(resp_skel,
                                    "Resource at URL <i>" + resource + "</i> not found");
                            // stampo sul terminale l'esito (in rosso)
                            System.out.println((char) 27 + "[31m" + nfound + (char) 27 + "[0m");
                            // invio la risposta al client
                            replyStream.write(nfound.getBytes());
                            replyStream.write(text.getBytes());
                            replyStream.flush();
                        } else {
                            // la risorsa esiste: 200
                            String response = String.format(responseFormat, 0, 200, "OK");
                            replyStream.write(response.getBytes());
                            // adesso trasferisco sullo stream il body del messaggio
                            byte[] resp_body;
                            try (BufferedInputStream file_in =
                                    new BufferedInputStream(new FileInputStream(res))) {
                                resp_body = file_in.readAllBytes();
                            } catch (IOException ex) {
                                System.out.println("Impossibile leggere la risorsa");
                                continue;
                            }
                            // stampo su terminale l'esito (in verde)
                            System.out.println((char) 27 + "[32m" + response + (char) 27 + "[0m");

                            replyStream.write(resp_body);
                            replyStream.flush();
                        }
                    }

                    // La connessione non è persistente: chiudo il socket
                    conn_sock.close();
                } catch (IOException ioerr) {
                    System.out.println("IO socket error");
                    ioerr.printStackTrace();
                }

            }
            listen_sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

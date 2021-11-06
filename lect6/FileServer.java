import java.net.*;
import java.io.*;

public class FileServer {
    public static void main(String[] args) {
        int listen_port = Integer.parseInt(args[0]);
        String target_file = args[1];
        try {
            // controllo esistenza del file al path specificato
            File fp = new File(target_file);
            if(!(fp.exists() && fp.isFile())) {
                System.out.println("Il file " + target_file + " non esiste oppure è una directory");
                return;
            }
            // leggo i byte del file
            FileInputStream fin = new FileInputStream(fp);
            // get the bytes from the file
            byte[] bytes = new byte[(int)fp.length()];
            fin.read(bytes, 0, (int)fp.length());
            fin.close();
            
            /**
             * Connessione del client
             */
            // il server ascolta sulla porta specificata
            ServerSocket listen_sock = new ServerSocket(listen_port, 0);
            // blocco fino all'accettazione della connessione
            Socket cs = listen_sock.accept();
            // solo una connessione: posso chiudere la socket in ascolto
            listen_sock.close();
            // send the bytes from the file trough the socket
            OutputStream fout = cs.getOutputStream();
            fout.write(bytes);
            // chiudo socket attiva verso il client (bidirezionale)
            cs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

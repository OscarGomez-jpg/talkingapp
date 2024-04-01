import java.io.*;
import java.net.*;

public class Lector implements Runnable {
    private BufferedReader in;

    public Lector(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("null")) break;
                System.out.println(message); // Mostrar mensaje del servidor al usuario
            }
        } catch (SocketException e) {
            return; 
        } catch (IOException e) {
            return;
        }
    }

}
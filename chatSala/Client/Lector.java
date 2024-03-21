import java.io.*;
import java.net.*;

public class Lector implements Runnable {
    private Socket socket;

    public Lector(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message); // Mostrar mensaje del servidor al usuario
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
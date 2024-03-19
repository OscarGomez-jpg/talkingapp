import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 6789;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            System.out.println("Conectado al servidor.");

            // Canal de entrada para el usuario
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Obtener el nombre de usuario
            String username;
            do {
                System.out.print("Ingrese su nombre de usuario: ");
                username = userInput.readLine();
                if (username.trim().isEmpty()) {
                    System.out.println("El nombre de usuario no puede estar vacío.");
                }
            } while (username.trim().isEmpty());

            // Enviar el nombre de usuario al servidor
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username);

            // Crear el objeto Lector y lanzar un hilo para leer mensajes del servidor
            Reader reader = new Reader(socket);
            Thread readerThread = new Thread(reader);
            readerThread.start();

            // Estar atento a la entrada del usuario para enviar mensajes al servidor
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message); // Enviar mensaje al servidor
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase interna para leer mensajes del servidor en un hilo separado
    static class Reader implements Runnable {
        private Socket socket;

        public Reader(Socket socket) {
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
}

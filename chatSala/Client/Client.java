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

            // Usando el socket, crear los canales de entrada in y salida out
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Solicitar al usuario un alias, o nombre y enviarlo al servidor
            String username;
            do {
                System.out.print("Ingrese su nombre de usuario: ");
                username = userInput.readLine();
                if (username.trim().isEmpty()) {
                    System.out.println("El nombre de usuario no puede estar vacío.");
                } else {
                    // Enviar el nombre de usuario al servidor
                    out.println(username);

                    // Esperar la respuesta del servidor
                    String response = in.readLine();
                    if (response.startsWith("REJECTED")) {
                        System.out.println(response);
                    } else if (response.equals("ACCEPTED")) {
                        System.out.println(response);
                        break;
                    }
                }
            } while (true);

            // Crear el objeto Lector y lanzar un hilo para leer mensajes del servidor
            Lector lector = new Lector(socket);
            Thread lectorThread = new Thread(lector);
            lectorThread.start();

            // Estar atento a la entrada del usuario para enviar mensajes al servidor
            String message;
            while ((message = userInput.readLine()) != null) {
                if (!message.trim().isEmpty()) {
                    out.println(username + ": " + message); // Enviar mensaje al servidor si no está vacío
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

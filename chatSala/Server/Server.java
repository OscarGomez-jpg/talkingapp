import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        int PORT = 6789;
        Chatters clientes = new Chatters(); // lista de clientes

        // Crear un ThreadPool
        ExecutorService executor = Executors.newFixedThreadPool(8);

        try {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Servidor iniciado. Esperando clientes...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuevo cliente conectado: " + clientSocket);

                    ClientHandler newClient = new ClientHandler(clientSocket, clientes);
                    // Enviamos el ClientHandler al ThreadPool.
                    executor.execute(newClient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
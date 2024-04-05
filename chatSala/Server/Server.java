import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        int TCPPORT = 6789;
        int UDPPORT = 9876;
        Chatters clientes = new Chatters(); // lista de clientes

        // Crear un ThreadPool
        ExecutorService executor = Executors.newFixedThreadPool(8);

        try {
            try (ServerSocket serverSocketTCP = new ServerSocket(TCPPORT)) {
                DatagramSocket serverSocketUDP = new DatagramSocket(UDPPORT);
                clientes.setDatagramSocket(serverSocketUDP);
                System.out.println("Servidor iniciado. Esperando clientes...");

                while (true) {
                    Socket clientSocket = serverSocketTCP.accept();
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
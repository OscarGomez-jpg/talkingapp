import java.io.*;
import java.net.*;

public class Server {

     public static void main(String[] args) {

        int PORT = 6789;
        Chatters clientes = new Chatters(); //lista de clientes

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Esperando clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);
                
                ClientHandler newClient = new ClientHandler(clientSocket, clientes);
                Thread clientThread = new Thread(newClient);
                clientThread.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
}


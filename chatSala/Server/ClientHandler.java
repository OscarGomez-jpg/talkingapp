import java.io.*;
import java.net.*;

//esta clase se debe encargar de gestionar los clientes de forma individual
//implementa la interfaz Runnable y en el metodo run valida el nombre de usuario
//agrega el usuario y su canal de comunicacion a la lista de chatters
//permite enviar mensajes a todos los usuarios
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    Chatters clientes;

    public ClientHandler(Socket socket, Chatters clientes) {
        // asignar los objetos que llegan a su respectivo atributo en la clase
        this.clientSocket = socket;
        this.clientes = clientes;
        // crear canales de entrada in y de salida out para la comunicacion
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        // implementar la logica que permita soliciar a un cliente un nombre de usuario
        try {
            clientName = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // verificar que no exista en chatters
        while (clientes.nameExists(clientName) == true) {
            out.println("REJECTED"); // Notify client that the username is rejected
            out.println("Username already exists. Please choose another one:");
        }
        out.println("ACCEPTED");

        // notificar a los demas clientes que un nuevo usuario se ha unido
        clientes.broadcastMessage(clientName + " has joined the chat.");

        // agregar al nuevo usuario a chatters junto con su canal de salida out
        Person newCLient = new Person(clientName, out);
        clientes.addUser(newCLient);

        // notificar al cliente que ha sido aceptado
        newCLient.getOut().println("ACCEPTED");

        // ante un nuevo mensaje de ese cliente, enviar el mensaje a todos los usuarios
        try {
            while ((message = in.readLine()) != null) {
                message = in.readLine();
                clientes.broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

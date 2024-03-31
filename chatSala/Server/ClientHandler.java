import java.io.*;
import java.net.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private void logIn() {
        // implementar la logica que permita solicitar a un cliente un nombre de usuario
        try {
            clientName = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // verificar que no exista en chatters
        while (clientes.nameExists(clientName)) {
            out.println("REJECTED");
            try {
                clientName = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // notificar a los demas clientes que un nuevo usuario se ha unido
        clientes.broadcastMessage("", clientName + " has joined the chat.");
        clientes.addChatHistory("", clientName + " has joined the chat.");

        //agregar al nuevo usuario a chatters junto con su canal de salida out
        Person newCLient = new Person(clientName, out);
        clientes.addUser(newCLient);

        //notificar al cliente que ha sido aceptado
        out.println("ACCEPTED");
    }

    private void chat() {
        // ante un nuevo mensaje de ese cliente, enviar el mensaje a todos los usuarios
        try {
            String message;
            while (true) {
                message = in.readLine();
                if (clientSocket.isClosed() || message == null) {
                    break;
                }
                if (message.equals("DISCONNECT")) {
                    System.out.println(clientName + " has left the chat.");
                    clientes.removeUser(clientes.getUser(clientName));
                } else {
                    System.out.println("Actual message: " + message);
                }
                
                // Comparar si el mensaje contiene un ":" para saber si es un mensaje privado
                if (message.contains("record")) {
                    clientes.recordAudio(clientName, message);
                } else if (message.equalsIgnoreCase("stop")) {
                    clientes.stopRecording();
                } else {
                    if (message.contains(":")) {
                        String[] parts = message.split(":", 2);
                        String receiver = parts[0].trim();
                        String privateMessage = parts[1].trim();
                        clientes.sendPrivateMessage(clientName, receiver, privateMessage);
                    } else if (message.equals("DISCONNECT")) {
                        clientes.broadcastMessage("", clientName + " has left the chat.");
                        List<String> chatHistory = clientes.getChatHistory(clientName);
                        File directory = new File("history");
                        if (!directory.exists()) {
                            directory.mkdir();
                        }
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                        String now = LocalDateTime.now().format(dtf);
                        try (PrintWriter writer = new PrintWriter(new File(directory, clientName + "_history_" + now + ".txt"))) {
                            for (String chatMessage : chatHistory) {
                                writer.println(chatMessage);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        clientes.broadcastMessage(clientName, message);
                    }
                }

                // Guardar el mensaje en el historial de chat
                if (message.equals("DISCONNECT")) {
                    clientes.addChatHistory("", clientName + " has left the chat.");
                } else {
                    clientes.addChatHistory(clientName, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logIn();
        chat();
    }
}

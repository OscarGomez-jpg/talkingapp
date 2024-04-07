import java.io.*;
import java.net.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

//esta clase se debe encargar de gestionar los clientes de forma individual
//implementa la interfaz Runnable y en el metodo run valida el nombre de usuario
//agrega el usuario y su canal de comunicacion a la lista de chatters
//permite enviar mensajes a todos los usuarios
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private OutputStream outAudio;
    private String clientName;
    private DatagramSocket udpDatagramSocket;
    private InetAddress address;
    private int udpPort;
    private boolean audioSended;
    Chatters clientes;

    public ClientHandler(Socket socket, Chatters clientes) {
        // asignar los objetos que llegan a su respectivo atributo en la clase
        this.clientSocket = socket;
        this.clientes = clientes;
        this.address = socket.getInetAddress();
        this.audioSended = false;
        // crear canales de entrada in y de salida out para la comunicacion
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            outAudio = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logIn() {
        // implementar la logica que permita solicitar a un cliente un nombre de usuario
        do {
            try {
                String nameAndPort = in.readLine();
                // God, forgiveme for the evil I'm making here
                // Theres no place on hell for people like me
                // [0] = Name, [1] = port
                String[] spltdIn = nameAndPort.split(":");
                clientName = spltdIn[0];
                udpPort = Integer.parseInt(spltdIn[1]);   
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (clientes.nameExists(clientName)) {
                out.println("REJECTED");
            } else {
                out.println("ACCEPTED");
            }
        } while (clientes.nameExists(clientName));

        // notificar a los demas clientes que un nuevo usuario se ha unido
        clientes.broadcastMessage("", clientName + " has joined the chat.");
        clientes.addChatHistory("", clientName + " has joined the chat.");

        //agregar al nuevo usuario a chatters junto con su canal de salida out
        Person newCLient = new Person(clientName, out, address, udpPort, outAudio);
        clientes.addUser(newCLient);
    }

    private void chat() {
        try {
            String message;
            while ((message = in.readLine()) != null || audioSended && !clientSocket.isClosed()) {
                if (message.equals("DISCONNECT")) {
                    handleDisconnect();
                    break;
                }

                if (audioSended) {
                    String[] parts = message.split("-", 2); // Divide el mensaje en dos partes en base al primer "-"
                    String sendingMessage = parts[0]; // La primera parte antes del "-"
                    message = parts.length > 1 ? parts[1] : ""; // La segunda parte después del "-", si existe
                    byte[] audio = Base64.getDecoder().decode(message);
                    clientes.handleVoiceNotes(clientName, sendingMessage, audio);
                    audioSended = false;
                } else {
                    handleMessages(message);
                    saveChatHistory(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnect() {
        System.out.println(clientName + " has left the chat.");
        clientes.removeUser(clientes.getUser(clientName));
        clientes.broadcastMessage("", clientName + " has left the chat.");

        saveChatHistory("");

        List<String> chatHistory = clientes.getChatHistory(clientName);
        saveChatHistoryToFile(chatHistory);
    }

    private void handleMessages(String message) {
        if (message.contains("recording")) {
            clientes.recordAudio(clientName, message);
        } else if (message.contains("stop")) {
            audioSended = true;
        } else if (message.equalsIgnoreCase("calling")) {
            clientes.handleCalls(clientName);
        } else {
            if (message.contains(":")) {
                handlePrivateMessage(message);
            } else {
                clientes.broadcastMessage(clientName, message);
            }
        }
    }

    private void handlePrivateMessage(String message) {
        String[] parts = message.split(":", 2);
        String receiver = parts[0].trim();
        String privateMessage = parts[1].trim();
        clientes.sendPrivateMessage(clientName, receiver, privateMessage);
    }

    private void saveChatHistory(String message) {
        clientes.addChatHistory(clientName.equals("DISCONNECT") ? "" : clientName, message);
    }

    private void saveChatHistoryToFile(List<String> chatHistory) {
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
    }

    @Override
    public void run() {
        logIn();
        chat();
    }

    public DatagramSocket getUdpDatagramSocket() {
        return udpDatagramSocket;
    }

    public void setUdpDatagramSocket(DatagramSocket udpDatagramSocket) {
        this.udpDatagramSocket = udpDatagramSocket;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }       
}

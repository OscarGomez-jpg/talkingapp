import java.util.Set;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.net.Socket;

public class Chatters {
    // Atributo para almacenar los usuarios conectados
    private Set<Person> clientes;

    // Udp socket
    private DatagramSocket udpSocket;

    // Tcp socket
    private Socket tcpSocket;

    // Atributo para almacenar el historial de chat
    private List<String> chatHistory;

    public Chatters() {
        clientes = new HashSet<>();
        chatHistory = new ArrayList<>();
    }

    // Metodo para verificar si un usuario existe, retorna true si existe
    public boolean userExists(Person user) {
        return clientes.contains(user);
    }

    // Metodo para verificar si un nombre de usuario existe, retorna true si existe
    public boolean nameExists(String name) {
        for (Person cliente : clientes) {
            if (name.equalsIgnoreCase(cliente.getName())) {
                return true;
            }
        }

        return false;
    }

    // Metodo para agregar un usuario nuevo
    public void addUser(Person user) {
        clientes.add(user);
    }

    // Metodo para eliminar un usuario
    public void removeUser(Person user) {
        clientes.remove(user);
    }

    // Metodo para buscar un usuario por su nombre
    public Person getUser(String name) {
        for (Person user : clientes) {
            if (name.equalsIgnoreCase(user.getName())) {
                return user;
            }
        }

        return null;
    }

    // Metodo para enviar un mensaje a todos los usuarios
    public void broadcastMessage(String emisor, String message) {
        synchronized (clientes) {
            for (Person user : clientes) {
                if (!user.getName().equalsIgnoreCase(emisor)) {
                    try {
                        System.out.println("User " + emisor + " said: " + message);
                        if (message.contains("has joined the chat.") || message.contains("has left the chat.")) {
                            user.getOut().println(message);
                        } else {
                            user.getOut().println(emisor + ": " + message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Metodo para enviar un mensaje a un usuario en especifico
    public void sendPrivateMessage(String clientName, String receiver, String privateMessage) {
        if (nameExists(receiver)) {
            synchronized (clientes) {
                for (Person user : clientes) {
                    if (user.getName().equalsIgnoreCase(receiver)) {
                        try {
                            user.getOut().println("(Private chat) " + clientName + ": " + privateMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            broadcastMessage(clientName, privateMessage);
        }
    }

    private Person findUserByName(String clientName) {
        synchronized (clientes) {
            for (Person user : clientes) {
                if (user.getName().equalsIgnoreCase(clientName)) {
                    return user;
                }
            }
        }
        return null;
    }

    public void recordAudio(String clientName, String message) {
        Person user = findUserByName(clientName);
        if (user == null) {
            return;
        }
        user.getOut().println("Recording audio...\nPress enter 'stop' to stop recording.");
    }

    public void handleVoiceNotes(String clientName, String message) {
        byte[] audioData;
        try {
            audioData = tcpSocket.getInputStream().readAllBytes();
            Thread sendVoiceNote = new Thread(() -> {
                sendVoiceNote(clientName, message, audioData);
            });
            sendVoiceNote.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendVoiceNote(String clientName, String message, byte[] audioData) {
        String receiver = extractReceiver(message);
        try {
            
            for (Person user : clientes) {
                if (shouldPlayAudioForUser(clientName, receiver, user)) {
                    String prefix = receiver == null ? "" : "(Private chat) ";
                    user.getOut().println(prefix + clientName + " has sent an audio.\nPlaying audio...");
                    user.getOutAudio().write(audioData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCalls(String clientName) {
        byte[] buffer = new byte[160];
        
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        Thread callThread = new Thread(() -> {
            createCallThread(clientName, packet);
        });

        callThread.start();
    }

    private void createCallThread(String clientName, DatagramPacket packet) {
        while (true) {
            try {
                udpSocket.receive(packet);

                for (Person user : clientes) {
                    if (!clientName.equalsIgnoreCase(user.getName())) {
                        DatagramPacket resending = new DatagramPacket(packet.getData(), packet.getLength(),
                                user.getAddress(), user.getPort());
                        udpSocket.send(resending);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractReceiver(String message) {
        if (message.contains(":")) {
            String[] parts = message.split(":", 2);
            String receiver = parts[1].trim();
            return nameExists(receiver) ? receiver : null;
        }
        return null;
    }

    private boolean shouldPlayAudioForUser(String clientName, String receiver, Person user) {
        return (receiver == null && !user.getName().equalsIgnoreCase(clientName)) ||
                (receiver != null && user.getName().equalsIgnoreCase(receiver));
    }

    // Metodo para agregar un mensaje al historial de chat
    public void addChatHistory(String clientName, String message) {
        if (message.contains("has joined the chat.") || message.contains("has left the chat.")) {
            chatHistory.add(message + "\nYou left the chat.");
        } else {
            chatHistory.add(clientName + ": " + message);
        }
    }

    // Metodo para obtener el historial de chat de un usuario
    public List<String> getChatHistory(String clientName) {
        return chatHistory;
    }

    public DatagramSocket getUdpSocket() {
        return udpSocket;
    }

    public void setUdpSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }

    public void setTcpSocket(Socket tcpSocket) {
        this.tcpSocket = tcpSocket;
    }

    
}
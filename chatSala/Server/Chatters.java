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

    // Grupos de chat
    private List<String> chatGroups;

    public Chatters() {
        clientes = new HashSet<>();
        chatHistory = new ArrayList<>();
        chatGroups = new ArrayList<>();
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

    // Metodo para obtener la lista de usuarios conectados
    public Set<Person> getUsers() {
        return clientes;
    }

    // Metodo para enviar un mensaje a todos los usuarios
    public void broadcastMessage(String emisor, String message) {
        synchronized (clientes) {
            for (Person user : clientes) {
                if (user.getName().equals(emisor)) {
                    if (user.getGroups().isEmpty()) {
                        if (!user.getName().equalsIgnoreCase(emisor) && user.getGroups().isEmpty()) {
                            try {
                                if (message.contains("has joined the chat.") || message.contains("has left the chat.")) {
                                    user.getOut().println(message);
                                } else {
                                    System.out.println("User " + emisor + " said: " + message);
                                    user.getOut().println(emisor + ": " + message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        for (String group : user.getGroups()) {
                            for (Person userGroup : clientes) {
                                if (userGroup.getGroups().contains(group) && userGroup != user) {
                                    try {
                                        if (message.contains("has joined the group.") || message.contains("has left the group.")) {
                                            userGroup.getOut().println(message);
                                        } else {
                                            System.out.println("User " + emisor + " said: " + message);
                                            userGroup.getOut().println(emisor + ": " + message);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
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

    // Metodo para crear un grupo de chat
    public void createGroup(String clientName, String groupName) {
        for (Person user : clientes) {
            if (user.getName().equalsIgnoreCase(clientName)) {
                if (chatGroups.contains(groupName)) {
                    user.getOut().println("Group already exists.");
                    return;
                }
                chatGroups.add(groupName);
                user.addGroup(groupName);
                user.getOut().println("Group " + groupName + " has been created.");
            }
        }
    }

    // Metodo para unirse a un grupo de chat
    public void joinGroup(String clientName, String groupName) {
        for (Person user : clientes) {
            if (user.getName().equalsIgnoreCase(clientName)) {
                if (!chatGroups.contains(groupName)) {
                    user.getOut().println("Group does not exist.");
                    return;
                } else if (user.getGroups().contains(groupName)) {
                    user.getOut().println("You are already in the group.");
                    return;
                } else {
                    user.addGroup(groupName);
                    user.getOut().println("You have joined the group.");
                    broadcastMessage(clientName, clientName + " has joined the group.");
                }
            }
        }
    }

    // Metodo para eliminar a un usuario de un grupo
    public void deleteFromGroup(String clientName, String groupName) {
        for (Person user : clientes) {
            if (user.getName().equalsIgnoreCase(clientName)) {
                if (!chatGroups.contains(groupName)) {
                    user.getOut().println("Group does not exist.");
                    return;
                } else if (!user.getGroups().contains(groupName)) {
                    user.getOut().println("You are not in the group.");
                    return;
                } else {
                    broadcastMessage(clientName, clientName + " has left the group.");
                    user.removeGroup(groupName);
                    user.getOut().println("You have been removed from the group.");
                }
            }
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
        user.getOut().println("(System) Recording audio...\n(System) Please enter 'detain' to stop recording.");
        chatHistory.add(clientName + ": [Sending audio]");
    }

    public void handleVoiceNotes(String clientName, String message, byte[] audioData) {
        Thread sendVoiceNote = new Thread(() -> {
            sendVoiceNote(clientName, message, audioData);
        });
        sendVoiceNote.start();
    }

    public void sendVoiceNote(String clientName, String message, byte[] audioData) {
        String receiver = extractReceiver(message);

        String prefix = receiver == null ? "" : "(Private chat) ";

        // Enviar los datos de audio en paquetes de 1024 bytes
        int offset = 0;

        for (Person user : clientes) {
            if (shouldPlayAudioForUser(clientName, receiver, user)) {
                user.getOut().println(prefix + clientName + " has sent an audio.");
            }
        }
        while (offset < audioData.length) {
            // Calcular la longitud del próximo paquete
            int length = Math.min(audioData.length - offset, 1024);

            DatagramPacket audioPacket;
            
            for (Person user : clientes) {
                try {
                    if (shouldPlayAudioForUser(clientName, receiver, user)) {
                        // Crear un DatagramPacket con los datos de audio
                        audioPacket = new DatagramPacket(audioData, offset, length, user.getAddress(), user.getPort());

                        // Enviar el DatagramPacket
                        udpSocket.send(audioPacket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Actualizar el offset
            offset += length;

            if (offset >= audioData.length) {
                for (Person user : clientes) {
                    if (shouldPlayAudioForUser(clientName, receiver, user)) {
                        user.getOut().println("(System) Playing audio...");
                    } else {
                        user.getOut().println("(System) Audio sent.");
                    }
                }
                break;
            }
        }
    }

    public void handleCalls(String clientName) {
        byte[] buffer = new byte[160];
        chatHistory.add(clientName + ": [Calling]");
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
                    if (!user.getName().equalsIgnoreCase(clientName)) {
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
            chatHistory.add(message);
        } else if (message == "") {
            chatHistory.add(message + "\n" + clientName + " has left the chat.");
        } else if (!message.contains("Calling") && !message.contains("recording") && !message.contains("stop") && !message.contains("record") && !message.contains("calling")) {
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
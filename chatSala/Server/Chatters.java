import java.util.Set;
import java.util.HashSet;

public class Chatters {
    private Set<Person> clientes;

    public Chatters() {
        clientes = new HashSet<>();
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

    // Metodo para enviar un mensaje a todos los usuarios
    public void broadcastMessage(String emisor, String message) {
        synchronized (clientes) {
            for (Person user : clientes) {
                if (!user.getName().equalsIgnoreCase(emisor)) {
                    try {
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
}
import java.util.Set;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Chatters {
    private Set<Person> clientes;

    public Chatters() {
        clientes = new HashSet<>();
    }

    // Metodo para verificar si un usuario existe, retorna true si existe
    public boolean userExists(Person user) {
        return clientes.contains(user);
    }

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
    public void broadcastMessage(String message) {
        for (Person user : clientes) {
            user.getOut().println(message);
        }
    }
}
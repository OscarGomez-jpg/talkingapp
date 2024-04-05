import java.io.PrintWriter;
import java.net.InetAddress;

//objeto que representa un cliente o usuario o persona en el chat
public class Person {
    private String name; //nombre de usuario
    private InetAddress address;
    private int port;
    PrintWriter out;    //canal para enviarle mensajes a ese usuario

    public Person(String name, PrintWriter out, InetAddress address, int port){
        this.name = name;
        this.out  = out;
        this.address = address;
        this.port = port;
    }
   
    public String getName() {
        return name;
    }
    
    public PrintWriter getOut() {
        return out;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
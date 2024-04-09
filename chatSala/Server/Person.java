import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

//objeto que representa un cliente o usuario o persona en el chat
public class Person {
    private String name; //nombre de usuario
    private InetAddress address;
    private int port;
    private ArrayList<String> groups; //grupos a los que pertenece
    PrintWriter out;    //canal para enviarle mensajes a ese usuario
    OutputStream outAudio; //canal para enviarle audio a ese usuario

    public Person(String name, PrintWriter out, InetAddress address, int port, OutputStream outAudio){
        this.name = name;
        this.out  = out;
        this.address = address;
        this.port = port;
        this.outAudio = outAudio;
        this.groups = new ArrayList<String>();
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

    public OutputStream getOutAudio() {
        return outAudio;
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void removeGroup(String group) {
        groups.remove(group);
    }

    public void playAudio(byte[] audioData, AudioFormat format) {
        PlayerRecording player = new PlayerRecording(format);
        player.initiateAudio(audioData);
    }
}

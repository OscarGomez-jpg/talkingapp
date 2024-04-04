import java.io.PrintWriter;
import javax.sound.sampled.AudioFormat;

//objeto que representa un cliente o usuario o persona en el chat
public class Person {
    private String name; //nombre de usuario
    PrintWriter out;    //canal para enviarle mensajes a ese usuario
    PlayerRecording playerRecording; //reproductor de audio

    public Person(String name, PrintWriter out){
        this.name = name;
        this.out  = out;
    }
   
    public String getName() {
        return name;
    }
    
    public PrintWriter getOut() {
        return out;
    }

    public void setPlayerRecording(AudioFormat audioFormat) {
        this.playerRecording = new PlayerRecording(audioFormat);
    }

    public void playAudio(byte[] audio) {
        playerRecording.initiateAudio(audio);
    }
}
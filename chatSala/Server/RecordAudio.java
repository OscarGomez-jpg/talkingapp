import java.io.ByteArrayOutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class RecordAudio implements Runnable{
    private AudioFormat format;
    private ByteArrayOutputStream out;
    private volatile boolean stop = false; // Campo para controlar cuándo detener la grabación

    private TargetDataLine targetLine; // Mover la declaración de targetLine aquí para que pueda ser accesible en stopRecording

    public RecordAudio(AudioFormat format, ByteArrayOutputStream out){
        this.format = format;
        this.out = out;
    }

    public void stopRecording() { // Método para detener la grabación
        stop = true;
    }

    @Override
    public void run(){        
        int bytesRead;
        try {
            // Abrir línea de captura de audio
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);

            // Comenzar la captura de audio
            targetLine.start();

            // grabar audio hasta que se envíe una señal de parar
            byte[] buffer = new byte[targetLine.getBufferSize() / 5];
            while (!stop) {
                bytesRead = targetLine.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }
            targetLine.stop();
            targetLine.close();
            
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace();
        }
    }
}
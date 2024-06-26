import java.io.*;

import javax.sound.sampled.*;

public class PlayerRecording {
    
    private AudioFormat format;
	private SourceDataLine out;  //salida a la tarjeta de audio
    private AudioInputStream in;

    public PlayerRecording(AudioFormat format) {
        this.format=format;       
   	}

    public void initiateAudio(byte[] audioData) {
		try {

			in = new AudioInputStream(new ByteArrayInputStream(audioData), format,
                    audioData.length / format.getFrameSize());
            // Abrir línea de salida de audio
            out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            out.start(); // Comenzar la reproducción de audio
            playAudio();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void playAudio() {
		byte[] buffer = new byte[1024];
        int count;
		try {
			// Leer datos de audio de la entrada y escribirlos en la salida
			while ((count = in.read(buffer)) != -1) {			
					out.write(buffer, 0, count);				
			}
			System.out.println("(System) End of audio");
            out.drain();
            out.stop();
            out.close();
            in.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
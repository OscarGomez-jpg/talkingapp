import java.util.Set;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;

public class Chatters {
    // Atributo para almacenar los usuarios conectados
    private Set<Person> clientes;

    // Atributos para grabar audio
    private static int SAMPLE_RATE = 16000; // Frecuencia de muestreo en Hz
    private static int SAMPLE_SIZE_IN_BITS = 16; // Tamaño de muestra en bits
    private static int CHANNELS = 1; // Mono
    private static boolean SIGNED = true; // Muestras firmadas
    private static boolean BIG_ENDIAN = false; // Little-endian
    private static boolean RECORDING = false;
    private AudioFormat format;

    public Chatters() {
        clientes = new HashSet<>();
        format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
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

    // Metodo para grabar audio
    public void recordAudio(String clientName, String message) {
        synchronized (clientes) {
            for (Person user : clientes) {
                if (user.getName().equalsIgnoreCase(clientName)) {
                    try {
                        user.getOut().println("Recording audio...\nPress enter 'stop' to stop recording.");
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                        // Iniciar objeto de grabación de audio
                        RecordAudio recorder = new RecordAudio(format, byteArrayOutputStream);
                        Thread recorderThread = new Thread(recorder);
                        recorderThread.start();
                        RECORDING = true;

                        // Iniciar otro hilo que verifique continuamente si RECORDING ha cambiado a false
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (RECORDING) {
                                    // Esperar un poco antes de verificar de nuevo
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Si RECORDING es false, detener la grabación
                                recorder.stopRecording();
                                user.getOut().println("Recording stopped.");
                                playAudio(clientName, message, byteArrayOutputStream);
                            }
                        }).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Metodo para reproducir audio
    public void playAudio(String clientName, String message, ByteArrayOutputStream byteArrayOutputStream) {
        String receiver = null;
        if (message.contains(":")) {
            String[] parts = message.split(":", 2);
            receiver = parts[1].trim();
            if (!nameExists(receiver)) {
                receiver = null;
            }
        }

        synchronized (clientes) {
            for (Person user : clientes) {
                if (receiver == null && !user.getName().equalsIgnoreCase(clientName)
                    || receiver != null && user.getName().equalsIgnoreCase(receiver)) {
                    try {
                        String prefix = receiver == null ? "" : "(Private chat) ";
                        user.getOut().println(prefix + clientName + " has sent an audio.\nPlaying audio...");
                        byte[] audioData = byteArrayOutputStream.toByteArray();
                        PlayerRecording player = new PlayerRecording(format);
                        player.initiateAudio(audioData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void stopRecording() {
        RECORDING = false;
    }

    public void startRecording() {
        RECORDING = true;
    }
}
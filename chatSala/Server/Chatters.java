import java.util.Set;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    private DatagramSocket udpSocket;
    private AudioFormat format;

    // Atributo para almacenar el historial de chat
    private List<String> chatHistory;

    public Chatters() {
        clientes = new HashSet<>();
        format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        RecordAudio recorder = new RecordAudio(format, byteArrayOutputStream);
        Thread recorderThread = new Thread(recorder);
        recorderThread.start();
        RECORDING = true;

        startRecordingChecker(recorder, user, byteArrayOutputStream, clientName, message);
    }

    private void startRecordingChecker(RecordAudio recorder, Person user, ByteArrayOutputStream byteArrayOutputStream,
            String clientName, String message) {
        new Thread(() -> {
            while (RECORDING) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            recorder.stopRecording();
            user.getOut().println("Recording stopped.");
            playAudio(clientName, message, byteArrayOutputStream);
        }).start();
    }

    public void playAudio(String clientName, String message, ByteArrayOutputStream byteArrayOutputStream) {
        String receiver = extractReceiver(message);
        for (Person user : clientes) {
            if (shouldPlayAudioForUser(clientName, receiver, user)) {
                try {
                    String prefix = receiver == null ? "" : "(Private chat) ";
                    user.getOut().println(prefix + clientName + " has sent an audio.\nPlaying audio...");
                    byte[] audioData = byteArrayOutputStream.toByteArray();
                    user.playAudio(audioData, format);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleCalls(String clientName) {
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {
            try {
                udpSocket.receive(packet);

                for (Person user : clientes) {
                    if (!clientName.equalsIgnoreCase(user.getName())) {
                        DatagramPacket resending = new DatagramPacket(packet.getData(), packet.getLength(), user.getAddress(), user.getPort());
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

    public void stopRecording() {
        RECORDING = false;
    }

    public void startRecording() {
        RECORDING = true;
    }

    public DatagramSocket getUdpSocket() {
        return udpSocket;
    }

    public void setUdpSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
    }

    
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class ClientEntryPoint {
    private InetAddress ipInetAddress;
    private int serverSocketUDP;
    private Socket socket;
    private DatagramSocket callSocket;
    private BufferedReader userKeyboard;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Lector lector;
    private Thread lectorThread;
    private AudioFormat format;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private static boolean RECORDING;

    // Atributos para grabar audio
    private static int SAMPLE_RATE = 16000; // Frecuencia de muestreo en Hz
    private static int SAMPLE_SIZE_IN_BITS = 16; // Tamaño de muestra en bits
    private static int CHANNELS = 1; // Mono
    private static boolean SIGNED = true; // Muestras firmadas
    private static boolean BIG_ENDIAN = false; // Little-endian
    private AudioFormat voiceNoteFormat; // Formato de audio para notas de voz
    private PlayerRecording player; // Reproductor de audio
    private String message; // Texto del mensaje de voz temporal
    private boolean stopCall;

    public ClientEntryPoint(String serverIp, int tcpport, int serverSocketUDP)
            throws UnknownHostException, IOException, LineUnavailableException {
        this.ipInetAddress = InetAddress.getByName(serverIp);
        this.serverSocketUDP = serverSocketUDP;
        this.socket = new Socket(serverIp, tcpport);
        this.callSocket = new DatagramSocket();//, ipInetAddress);
        this.userKeyboard = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.username = "";
        this.lector = new Lector(socket);
        this.lectorThread = new Thread(lector);
        this.format = new AudioFormat(8000.0f, 16, 1, true, true);
        this.microphone = AudioSystem.getTargetDataLine(format);
        this.speakers = AudioSystem.getSourceDataLine(format);
        this.voiceNoteFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        this.player = new PlayerRecording(voiceNoteFormat);
        this.message = "";
        ClientEntryPoint.RECORDING = false;
        this.stopCall = false;
    }

    public void logIn() {
        boolean validName = false;
        do {
            System.out.print("Ingrese su nombre de usuario: ");
            try {
                username = userKeyboard.readLine();
                if (username.trim().isEmpty()) {
                    System.out.println("El nombre de usuario no puede estar vacío.");
                } else {
                    // Enviar el nombre de usuario al servidor
                    out.println(username + ":" + callSocket.getLocalPort());

                    // Esperar la respuesta del servidor
                    String response = in.readLine();

                    if (response.startsWith("REJECTED")) {
                        System.out.println(response);
                    } else if (response.equals("ACCEPTED")) {
                        System.out.println(response);
                        validName = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!validName);
    }

    // Este es el que imprime desde Client a Server
    public void chat() {
        Thread detectConsoleOutput = new Thread(() -> {
            detectConsole();
        });
        detectConsoleOutput.start();
        String message;
        try {
            while ((message = userKeyboard.readLine()) != null) {
                if (message.equals("/disconnect")) {
                    out.println("[disconnect]");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (message.equalsIgnoreCase("/create group")) {
                    System.out.println("(System) Enter the group name: ");
                    String groupName = userKeyboard.readLine();
                    if (!groupName.trim().isEmpty()) {
                        out.println("[create group]:" + groupName);
                    }
                } else if (message.equalsIgnoreCase("/join group")) {
                    System.out.println("(System) Enter the group name:");
                    String groupName = userKeyboard.readLine();
                    if (!groupName.trim().isEmpty()) {
                        out.println("[join group]:" + groupName);
                    }
                } else if (message.equalsIgnoreCase("/leave group")) {
                    out.println("[leave group]");
                } else if (message.equalsIgnoreCase("/delete group")) {
                    System.out.println("(System) Enter the group name:");
                    out.println("[delete group]:" + userKeyboard.readLine());
                } else if (message.equalsIgnoreCase("/call")) {
                    System.out.println("Enter '/stop call' to stop the call.");
                    out.println("[calling]");
                    call();
                } else if (message.equals("/stop call")) {
                    stopCall = true;
                    stopCall();
                    out.println("[stop call]");
                } else if (message.equals("/record") || message.contains("/record:")) {
                    out.println("[recording]" + message);
                    this.message = message;
                    Thread recordThread = new Thread(() -> {
                        startRecording();
                    });
                    recordThread.start();
                } else if (message.equals("/detain") && ClientEntryPoint.RECORDING) {
                    ClientEntryPoint.RECORDING = false;
                } else if (message.substring(0, 1).equals("/")) {
                    System.out.println("Comando no reconocido.");
                } else if (!message.trim().isEmpty()) {
                    out.println(message); // Enviar mensaje al servidor si no está vacío
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void detectConsole() {
        PrintStream originalOut = System.out;
        StringBuilder sb = new StringBuilder();
        PrintStream interceptor = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                originalOut.write(b);
                char c = (char) b;
                sb.append(c);
                AtomicInteger cnt = new AtomicInteger(0);
                if (c == ' ') {
                    cnt.incrementAndGet();
                } else {
                    cnt.set(0);
                }
                if (c == ' ' && cnt.get() == 4) {
                    // Si el carácter es un espacio, limpiar el StringBuilder
                    sb.setLength(0);
                } else if (sb.toString().contains("has sent an audio.")) {
                    Thread stopRecordThread = new Thread(() -> {
                        stopRecording();
                    });
                    stopRecordThread.start();
                    // Limpiar el StringBuilder después de detectar un audio
                    sb.setLength(0);
                }
            }
        });
        System.setOut(interceptor);
    }

    public void call() {
        stopCall = false;
        // Crear e iniciar un hilo para enviar voz
        Thread sendVoiceThread = new Thread(() -> {
            sendVoice();
        });
        sendVoiceThread.start();

        // Crear e iniciar un hilo para recibir voz
        Thread receiveVoiceThread = new Thread(() -> {
            receiveVoice();
        });
        receiveVoiceThread.start();
    }

    public void stopCall() {
        // Detener el envío de voz
        microphone.stop();
        microphone.close();

        // Detener la recepción de voz
        speakers.stop();
        speakers.close();
        
        System.out.println("Llamada detenida.");
    }

    public void sendVoice() {
        try {
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[160];
            DatagramPacket packet;

            System.out.println("Llamando");

            while (!stopCall) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                packet = new DatagramPacket(buffer, bytesRead, ipInetAddress, serverSocketUDP);
                try {
                    callSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void receiveVoice() {
        try {
            speakers.open(format);
            speakers.start();

            byte[] buffer = new byte[160];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!stopCall) {
                callSocket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        try {
            microphone.open(voiceNoteFormat);
            microphone.start();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[SAMPLE_RATE];
            ClientEntryPoint.RECORDING = true;

            while (ClientEntryPoint.RECORDING) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] audioData = byteArrayOutputStream.toByteArray();

            out.println("[stop]");

            String audio = Base64.getEncoder().encodeToString(audioData);
            out.println(message + "-" + audio);
            this.message = "";

            microphone.stop();
            microphone.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            callSocket.setSoTimeout(500); // Establecer el tiempo de espera a 500 milisegundos
            while (true) {
                try {
                    callSocket.receive(packet);
                    receivedData.write(packet.getData(), 0, packet.getLength());
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
            // Imprimir los datos recibidos para comparar
            byte[] audioData = receivedData.toByteArray();
            callSocket.setSoTimeout(0);
            player.initiateAudio(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getLectorThread() {
        return this.lectorThread;
    }
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
        String message;
        try {
            while ((message = userKeyboard.readLine()) != null) {
                if (message.equals("disconnect")) {
                    out.println("DISCONNECT");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (message.equals("call")) {
                    out.println("calling");
                    call();
                } else if (message.equals("stop call")) {
                    stopCall();
                }

                if (!message.trim().isEmpty()) {
                    out.println(message); // Enviar mensaje al servidor si no está vacío
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void call() {
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

        // Detener el socket de llamada
        callSocket.close();

        System.out.println("Llamada detenida.");
    }

    public void sendVoice() {
        try {
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[256];

            System.out.println("Llamando");

            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, ipInetAddress, serverSocketUDP);
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

            byte[] buffer = new byte[256];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                callSocket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getLectorThread() {
        return this.lectorThread;
    }
}
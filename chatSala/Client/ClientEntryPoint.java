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
    private int port;
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

    public ClientEntryPoint(String serverIp, int port)
            throws UnknownHostException, IOException, LineUnavailableException {
        this.port = port;
        this.ipInetAddress = InetAddress.getByName(serverIp);
        this.socket = new Socket(serverIp, port);
        this.callSocket = new DatagramSocket(port, ipInetAddress);
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
        do {
            System.out.print("Ingrese su nombre de usuario: ");
            try {
                username = userKeyboard.readLine();
                if (username.trim().isEmpty()) {
                    System.out.println("El nombre de usuario no puede estar vacío.");
                } else {
                    // Enviar el nombre de usuario al servidor
                    out.println(username);

                    // Esperar la respuesta del servidor
                    String response;

                    response = in.readLine();

                    if (response.startsWith("REJECTED")) {
                        System.out.println(response);
                    } else if (response.equals("ACCEPTED")) {
                        System.out.println(response);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    public void chat() {
        String message;
        try {
            while ((message = userKeyboard.readLine()) != null) {
                if (message.equals("disconnect")) {
                    out.println("DISCONNECT");
                    try {
                        socket.close();
                    } catch (IOException e) {

                    }
                    break;
                } else if (message.equals("call")) {
                    out.println("Calling");

                    Thread speakThread = new Thread(() -> {
                        sendVoice();
                    });
                    Thread hearThread = new Thread(() -> {
                        receiveVoice();
                    });

                    speakThread.start();
                    hearThread.start();
                }

                if (!message.trim().isEmpty()) {
                    out.println(message); // Enviar mensaje al servidor si no está vacío
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendVoice() {
        try {
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[1024];

            System.out.println("Llamando");

            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, ipInetAddress, port);
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

            byte[] buffer = new byte[1024];

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
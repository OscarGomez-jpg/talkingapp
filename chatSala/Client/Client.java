import java.io.*;
import java.net.UnknownHostException;

import javax.sound.sampled.LineUnavailableException;

public class Client {
    private static final String SERVER_IP = "192.168.43.110";
    private static final int TCPPORT = 6789;
    private static final int SERVER_UDP_SOCKET = 9876;

    public static void main(String[] args) throws IOException {
        ClientEntryPoint clientEntryPoint;

        try {
            clientEntryPoint = new ClientEntryPoint(SERVER_IP, TCPPORT, SERVER_UDP_SOCKET);
            clientEntryPoint.logIn();
            clientEntryPoint.getLectorThread().start();
            clientEntryPoint.chat();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}

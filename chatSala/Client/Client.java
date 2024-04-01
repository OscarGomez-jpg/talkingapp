import java.io.*;
import java.net.UnknownHostException;

import javax.sound.sampled.LineUnavailableException;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 6789;

    public static void main(String[] args) {
        ClientEntryPoint clientEntryPoint;
        try {
            clientEntryPoint = new ClientEntryPoint(SERVER_IP, PORT);
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

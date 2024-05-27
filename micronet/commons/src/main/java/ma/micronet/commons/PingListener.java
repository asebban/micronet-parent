package ma.micronet.commons;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingListener implements Runnable {

    private Adressable adressable;
    public static final String PING_RESPONSE = "PING_OK";
    private Logger logger = LoggerFactory.getLogger(PingListener.class.getName());

    public PingListener(Adressable adressable) {
        this.adressable = adressable;
    }

    @Override
    public void run() {
        
        try (ServerSocket serverSocket = new ServerSocket(adressable.getPingPort())) {
            while (true) {
                Socket socket = serverSocket.accept();
                OutputStream out = socket.getOutputStream();
                logger.info("Received ping request from " + socket.getInetAddress().getHostAddress());
                out.write(PING_RESPONSE.getBytes());
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

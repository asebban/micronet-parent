package ma.micronet.commons.networking;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;

public class PingListener implements Runnable {

    private Adressable adressable;
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
                Message pingResponse = new Message();
                pingResponse.setResponseCode(Message.OK);
                pingResponse.setDirection(Message.RESPONSE);
                Gson gson = new Gson();
                String response = gson.toJson(pingResponse);
                out.write(response.getBytes());
                out.flush();
                logger.debug("Ping response sent -> " + response);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

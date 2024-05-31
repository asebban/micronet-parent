package ma.micronet.commons.networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Pinger {
    private static Logger logger = LoggerFactory.getLogger(Pinger.class.getName());
    public static final String PING_COMMAND = "PING";


    @SuppressWarnings("resource")
    public static Boolean ping(Adressable adressable) throws MicroNetException {
        Message pingMessage = new Message();
        pingMessage.setCommand(PING_COMMAND);
        pingMessage.setDirection(Message.REQUEST);
        Gson gson = new Gson();
        String pingRequest = gson.toJson(pingMessage);
        try {
            Socket socket = new Socket(adressable.getHost(), adressable.getPingPort());
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            logger.debug("Sending ping request to " + adressable.getHost() + ":" + adressable.getPingPort());
            out.write(pingRequest.getBytes());
            logger.debug("Ping request sent. Waiting for ping response...");
            out.flush();
            byte[] buffer = new byte[1024];
            in.read(buffer);
            String response = new String(buffer, "UTF-8");
            response = response.trim();
            logger.debug("Ping response received: " + response);
            Message responseMessage = gson.fromJson(response, Message.class);
            if (!responseMessage.getResponseCode().equals(Message.OK)) {
                throw new MicroNetException("There was a problem with the ping response. ReponseCode is Not OK");
            }
            logger.debug("Ping response is OK.");
            socket.close();
            return true;
        } catch (Exception e) {
            logger.error("Error pinging " + adressable.getHost() + ":" + adressable.getPingPort() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}

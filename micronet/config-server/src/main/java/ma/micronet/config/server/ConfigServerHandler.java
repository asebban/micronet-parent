package ma.micronet.config.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import java.util.Properties;

public class ConfigServerHandler implements Runnable{

    private Socket socket;
    private Adressable configServer;
    private Properties properties;
    private Logger logger = LoggerFactory.getLogger(ConfigServerHandler.class);

    public ConfigServerHandler(Socket socket, Adressable configServer, Properties properties) {
        this.socket = socket;
        this.configServer = configServer;
        this.properties = properties;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            byte[] buffer = new byte[Message.BUFFER_SIZE];
            is.read(buffer);
            String request = new String(buffer, "UTF-8");
            logger.debug("ConfigServerClientHandler: Received a message from the client :'" + request + "'");
            request = request.trim();
            Message requestMessage = Message.jsonToMessage(request);
            requestMessage.setSenderAdressable(configServer);

            if (requestMessage.getCommand().equals(Message.GET_CONFIG_COMMAND)) {
                logger.debug("ConfigServerHandler: Received a GET_CONFIG command from the client");
                OutputStream os = this.socket.getOutputStream();
                Message responseMessage = Message.copy(requestMessage);
                responseMessage.setDirection(Message.RESPONSE);
    
                Gson gson = new Gson();
                String jsonProperties = gson.toJson(properties).trim();
                responseMessage.setPayLoad(jsonProperties);
                responseMessage.setTargetAdressable(requestMessage.getSenderAdressable());
                responseMessage.setSenderAdressable(configServer);
                responseMessage.setResponseCode(Message.OK);
    
                String jsonResponse = responseMessage.toString().trim();
                os.write(jsonResponse.getBytes());
                logger.debug("ConfigServerHandler: Response sent to the client");
            } else {
                logger.error("ConfigServerHandler: Received an unknown command from the client");
                throw new MicroNetException("ConfigServerHandler: Unknown command");
            }
            
        } catch (Exception e) {
            logger.error("ConfigServerHandler: Error handling the client request: " + e.getMessage());
        } finally {
            if (this.socket != null && !this.socket.isClosed())
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

}

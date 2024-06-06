package ma.micronet.gateway.server;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;
import ma.micronet.gateway.api.Gateway;
import ma.micronet.router.api.Router;
import ma.micronet.router.api.RouterConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class GatewayClientHandler implements Runnable{

    private RouterConnection routerConnection;
    private Socket incomingSocket;
    private Gateway gateway;
    private Logger logger = LoggerFactory.getLogger(GatewayClientHandler.class);

    public GatewayClientHandler(Socket incomingSocket, Gateway gateway) throws MicroNetException, IOException {
        this.incomingSocket = incomingSocket;
        this.gateway = gateway;

        // Create a connection to a router and send the message
        logger.debug("GatewayClientHandler: Creating a connection to the router");
        
        Router router = new Router();
        RouterConnection routerConnection = router.createConnection();
        routerConnection.connect();

        this.routerConnection = routerConnection;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.incomingSocket.getInputStream();
            byte[] buffer = new byte[1024];
            is.read(buffer);
            String request = new String(buffer, "UTF-8");
            logger.debug("GatewayClientHandler: Received a message from the client :'" + request + "'");
            request = request.trim();
            Gson gson = new Gson();
            Message message = gson.fromJson(request, Message.class);
            message.setSenderAdressable(gateway);
            message.setMessageId(UIDGenerator.generateUID());
            
            routerConnection.send(message);
            logger.debug("GatewayClientHandler: Message sent to the router");

            Message response = routerConnection.receive();
            OutputStream os = this.incomingSocket.getOutputStream();
            os.write(gson.toJson(response).getBytes());
            logger.debug("GatewayClientHandler: Response sent back to the client");
        } catch (Exception e) {
            logger.error("GatewayClientHandler: Error handling the client request: " + e.getMessage());
            e.printStackTrace();
            try {
                this.incomingSocket.getOutputStream().write(Message.errorMessage("GatewayClientHandler: Error handling the client request: " + e.getMessage()).toString().getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                this.incomingSocket.close();
            } catch (IOException e) {
                logger.error("GatewayClientHandler: Error closing the socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

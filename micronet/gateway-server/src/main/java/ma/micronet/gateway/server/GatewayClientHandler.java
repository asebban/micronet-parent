package ma.micronet.gateway.server;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.router.api.RouterConnection;
import ma.micronet.router.api.RouterFactory;

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
    private Logger logger = LoggerFactory.getLogger(GatewayClientHandler.class);

    public GatewayClientHandler(Socket incomingSocket) throws MicroNetException, IOException {
        this.incomingSocket = incomingSocket;

        // Create a connection to a router and send the message
        logger.debug("GatewayClientHandler: Creating a connection to the router");
                
        RouterConnection routerConnection = RouterFactory.createRouter().createConnection();
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

            routerConnection.send(message);
            logger.debug("GatewayClientHandler: Message sent to the router");

            Message response = routerConnection.receive();
            OutputStream os = this.incomingSocket.getOutputStream();
            os.write(gson.toJson(response).getBytes());
            this.incomingSocket.close();
            logger.debug("GatewayClientHandler: Response sent back to the client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

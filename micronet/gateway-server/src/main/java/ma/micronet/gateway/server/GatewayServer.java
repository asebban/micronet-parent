package ma.micronet.gateway.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class GatewayServer {

    private static Logger logger = LoggerFactory.getLogger(GatewayServer.class);

    public static void main(String[] args) {
        try {
            logger.debug("GatewayApplication: configuration read successfully");
            GatewayListener gatewayListener = new GatewayListener();
            gatewayListener.start();
        } catch (IOException | MicroNetException e) {
            e.printStackTrace();
            logger.error("GatewayApplication: Error starting the server : " + e.getMessage());
            System.exit(1);
        }
    }
}
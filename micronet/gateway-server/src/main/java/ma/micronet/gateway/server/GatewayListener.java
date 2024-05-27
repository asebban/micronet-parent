package ma.micronet.gateway.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.gateway.api.Gateway;
import ma.micronet.gateway.api.GatewayConnection;
import ma.micronet.gateway.api.GatewayFactory;
import ma.micronet.registry.api.Registry;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class GatewayListener {

    public static final String GATEWAY_TYPE = "GATEWAY";
    private Logger logger = LoggerFactory.getLogger(GatewayListener.class);

    public void start() throws MicroNetException, IOException {

        Gateway gateway = GatewayFactory.createGateway();
        logger.debug("GatewayListener: Subscribing the gateway " + gateway.getId() + " to the registry");
        Registry.subscribe(gateway);
        logger.debug("GatewayListener: Gateway " + gateway.getId() + "subscribed to the registry");
    
        ////////////////////////////////////
        // Handle SIGINT interruption signal
        ////////////////////////////////////
        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.debug("Router Listener: Signal " + signal + " reçu. Arrêt en cours...");
                System.exit(0);
            }
        };

        // Associate signal SIGINT to the handler
        Signal.handle(new Signal("INT"), handler);

        try (ServerSocket serverSocket = new ServerSocket(gateway.getPort())) {
            logger.info("GatewayListener: Server is listening on port " + gateway.getPort());

            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("GatewayListener: New client connected");
                new Thread(new GatewayClientHandler(socket)).start();
            }
        } catch (IOException ex) {
            logger.error("GatewayListener: Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                logger.debug("GatewayListener: Unsubscribing the gateway " + gateway.getId() + " from the registry");
                Registry.unsubscribe(gateway);
                logger.debug("GatewayListener: Gateway " + gateway.getId() + "unsubscribed from the registry");
            } catch (MicroNetException e) {
                e.printStackTrace();
                logger.error("GatewayListener: Error unsubscribing the gateway: " + e.getMessage());
                throw new MicroNetException("Error unsubscribing the gateway: " + e.getMessage(), e);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("GatewayListener: Error unsubscribing the gateway: " + e.getMessage());
                throw new MicroNetException("Error unsubscribing the gateway: " + e.getMessage(), e);
            }
        }
    }

    public static GatewayConnection createGatewayConnection() {
        return new GatewayConnection();
    }
}

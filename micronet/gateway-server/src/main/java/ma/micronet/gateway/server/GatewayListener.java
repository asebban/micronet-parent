package ma.micronet.gateway.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.MicroNetMapRenewer;
import ma.micronet.commons.networking.PingListener;
import ma.micronet.gateway.api.Gateway;
import ma.micronet.gateway.api.GatewayFactory;
import ma.micronet.registry.api.Registry;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class GatewayListener {

    private Logger logger = LoggerFactory.getLogger(GatewayListener.class);
    private Gateway gateway;

    public void start() throws MicroNetException, IOException {

        this.gateway = GatewayFactory.createGateway();
        logger.debug("GatewayListener: Subscribing the gateway " + gateway.getId() + " to the registry");
        Registry.subscribe(this.gateway);
        logger.debug("GatewayListener: Gateway " + gateway.getId() + "subscribed to the registry");
    
        ////////////////////////////////////
        // Handle SIGINT interruption signal
        ////////////////////////////////////
        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.debug("Router Listener: Signal " + signal + " reçu. Arrêt en cours...");
                try {
                    Registry.unsubscribe(gateway);
                } catch (MicroNetException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        };

        // Associate signal SIGINT to the handler
        Signal.handle(new Signal("INT"), handler);

        new Thread(new PingListener(gateway)).start();

        try (ServerSocket serverSocket = new ServerSocket(gateway.getPort())) {
            logger.info("GatewayListener: Server is listening on port " + gateway.getPort());
            MicroNetMapRenewer.getInstance(this.gateway).renewMap();
            
            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("GatewayListener: New client connected");
                new Thread(new GatewayClientHandler(socket, gateway)).start();
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

}

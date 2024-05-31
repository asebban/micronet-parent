package ma.micronet.router.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.IListener;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.MicroNetMapRenewer;
import ma.micronet.commons.networking.PingListener;
import ma.micronet.registry.api.Registry;
import ma.micronet.router.api.Router;
import ma.micronet.router.api.RouterFactory;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class RouterListener implements IListener {

    private Logger logger = LoggerFactory.getLogger(RouterListener.class);

    @Override
    public void start() throws MicroNetException {
        Router router = null;

        try {
            logger.debug("Router Listener: Creating a new router");
            router = RouterFactory.createRouter();
            logger.debug("Router Listener: Subscribing the router to the registry");
            Registry.subscribe(router);
            logger.debug("Router Listener: Router subscribed successfully to the registry");
            MicroNetMapRenewer.getInstance(router).renewMap();
        } catch (MicroNetException e) {
            logger.error("Router Listener: Error subscribing the router to the registry: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (IOException e) {
            logger.error("Router Listener: Error subscribing the router to the registry: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
        
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

        new Thread(new PingListener(router)).start();

        try (ServerSocket serverSocket = new ServerSocket(router.getPort())) {
            logger.debug("Router Listener: Router is listening on port " + router.getPort());

            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("Router Listener: New client connected");
                new Thread(new RouterProcessor(socket)).start();
            }

        } catch (IOException ex) {
            logger.error("Router Listener: Server exception: " + ex.getMessage());
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                logger.debug("Router Listener: Unsubscribing the router from the registry");
                Registry.unsubscribe(router);
                logger.debug("Router Listener: Router unsubscribed successfully from the registry");
            } catch (MicroNetException e) {
                logger.error("Router Listener: Error unsubscribing the router: " + e.getMessage());
                e.printStackTrace();
                throw new MicroNetException("Error unsubscribing the router: " + e.getMessage(), e);
            } catch (IOException e) {
                logger.error("Router Listener: Error unsubscribing the router: " + e.getMessage());
                e.printStackTrace();
                throw new MicroNetException("Error unsubscribing the router: " + e.getMessage(), e);
            }
        }
    }

}

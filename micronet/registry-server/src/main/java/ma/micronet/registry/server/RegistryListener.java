package ma.micronet.registry.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.IListener;
import ma.micronet.commons.MicroNetException;
import ma.micronet.registry.api.Registry;

import java.net.ServerSocket;
import java.net.Socket;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class RegistryListener implements IListener {

    private Logger logger = LoggerFactory.getLogger(RegistryListener.class);
    
    @Override
    public void start() throws MicroNetException {

        logger.info("Starting RegistryListener");
        Registry registry = new Registry();

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
        
        try (ServerSocket serverSocket = new ServerSocket(registry.getPort())) {

            logger.info("RegistryListener started on port " + registry.getPort());

            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("RegistryListener: New connection accepted from " + socket.getInetAddress().getHostAddress());
                new Thread(new RegistryHandler(socket, registry)).start();
            }

        } catch (Exception e) {
            throw new MicroNetException("Error starting RegistryListener: " + e.getMessage());
        }

    }
}

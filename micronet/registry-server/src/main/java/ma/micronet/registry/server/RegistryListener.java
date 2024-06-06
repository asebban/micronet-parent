package ma.micronet.registry.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.IListener;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Pinger;
import ma.micronet.config.api.Config;
import ma.micronet.config.api.ConfigReader;
import ma.micronet.registry.api.Registry;
import ma.micronet.registry.api.RegistryFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import sun.misc.SignalHandler;
import sun.misc.Signal;
import java.util.concurrent.Executors; // Add this import statement
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap; // Add this import statement
import java.util.Map; // Add this import statement

public class RegistryListener implements IListener {

    private Logger logger = LoggerFactory.getLogger(RegistryListener.class);
    private Map<Adressable, Integer> pingMap = new HashMap<>();
    
    @Override
    public void start() throws MicroNetException {

        try {
            ConfigReader.getInstance(new Registry()).readProperties();
        } catch (MicroNetException | IOException e) {
            logger.error("Error reading properties file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Starting RegistryListener");
        Registry registry = RegistryFactory.createRegistry();

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

        // Schedule ping checks every periodicity in seconds
        Integer periodicity = 0;
        try {
            periodicity = Integer.parseInt(Config.getInstance().getProperty("registry.ping.perdiodicity"));
        } catch (Exception e) {
            periodicity = 5;
        }
        ScheduledExecutorService executor  = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                checkLivenessOfAdressables();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, 0, periodicity, TimeUnit.SECONDS);
        
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

    private void checkLivenessOfAdressables() {
        ArrayList<Adressable> deadAdressables = new ArrayList<>();
        logger.debug("Checking liveness of adressables ...");

        RegistryMapController.getInstance().getRegistryMap().forEach((type, adressables) -> {
            adressables.forEach(adressable -> {
                try {
                    if (!Pinger.ping(adressable)) {
                        Integer count = pingMap.get(adressable);
                        if (count != null && count >= 3) {
                            logger.debug("Adressable type " + adressable.getType() + " -> " + adressable + " is dead. Removing it from the map ...");
                            deadAdressables.add(adressable);
                            pingMap.remove(adressable);
                        }
                        else {
                            if (count == null) {
                                count = 1;
                            }
                            else {
                                count++;
                            }
                            pingMap.put(adressable, count);
                            logger.debug("Adressable type " + adressable.getType() + " -> " + adressable + " is unreachable. Attempt: " + count);
                        }
                    }
                    else {
                        logger.debug("Adressable type " + adressable.getType() + " -> " + adressable + " is alive.");
                        pingMap.remove(adressable);
                    }
                } catch (MicroNetException e) {
                    logger.error("Error pinging adressable " + adressable + ": " + e.getMessage());
                }
            });
        });

        deadAdressables.forEach(adressable -> {
            logger.debug("Adressable " + adressable + " is no longer alive. Removing it from the map ...");
            RegistryMapController.getInstance().removeService(adressable.getType(), adressable);
        });
    }
}

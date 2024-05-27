package ma.micronet.registry.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.ConfigReader;
import ma.micronet.commons.MicroNetException;

public class RegistryApplication {

    private static Logger logger = LoggerFactory.getLogger(RegistryApplication.class);
    
    public static void main(String[] args) throws MicroNetException {

        try {
            ConfigReader.getInstance().readProperties();
        } catch (MicroNetException e) {
            logger.error("Error reading properties file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            logger.error("Error reading properties file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        logger.debug("RegistryApplication: Starting RegistryListener");
        RegistryListener listener = new RegistryListener();
        listener.start();
        
    }
    
}

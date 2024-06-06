package ma.micronet.registry.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class RegistryServer {

    private static Logger logger = LoggerFactory.getLogger(RegistryServer.class);
    
    public static void main(String[] args) throws MicroNetException {

        logger.debug("RegistryApplication: Starting RegistryListener");
        RegistryListener listener = new RegistryListener();
        listener.start();
        
    }
    
}

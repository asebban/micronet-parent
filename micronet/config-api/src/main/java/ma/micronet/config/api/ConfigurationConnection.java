package ma.micronet.config.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class ConfigurationConnection extends  Connection {

    private Logger logger = LoggerFactory.getLogger(ConfigurationConnection.class);
    
    public ConfigurationConnection(Adressable adressable) throws MicroNetException, IOException {
        super(adressable);
    }

    @Override
    public void connect() throws MicroNetException {
        try {
            this.setSocket(new MicroNetSocket(this.getAdressable()));
            this.getSocket().connect();
        } catch (IOException e) {
            logger.error("Configuration Connection: Registry Server is not reachable", e);
            throw new MicroNetException("Configuration Connection: Registry Server is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

}

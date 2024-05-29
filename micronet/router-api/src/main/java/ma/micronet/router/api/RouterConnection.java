package ma.micronet.router.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class RouterConnection extends Connection {

    private Logger logger = LoggerFactory.getLogger(RouterConnection.class);

    public RouterConnection(Adressable adressable) throws MicroNetException, IOException {
        super(adressable);
    }

    @Override
    public void connect() throws MicroNetException {
        try {
            logger.debug("Router Connection: Connecting to the router");
            this.setSocket(new MicroNetSocket(this.getAdressable()));
            this.getSocket().connect();
            logger.debug("Router Connection: Connected to the router");
        } catch (IOException e) {
            logger.error("Router Connection: Router is not reachable", e);
            throw new MicroNetException("Registry is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

}


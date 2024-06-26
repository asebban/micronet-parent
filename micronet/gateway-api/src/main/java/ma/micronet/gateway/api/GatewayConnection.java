package ma.micronet.gateway.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class GatewayConnection extends Connection {

    private Logger logger = LoggerFactory.getLogger(GatewayConnection.class);

    public GatewayConnection(Adressable adressable) throws MicroNetException, IOException {
        super(adressable);
    }

    @Override
    public void connect() throws MicroNetException {
        try {
            this.setSocket(new MicroNetSocket(this.getAdressable()));
            this.getSocket().connect();
        } catch (IOException e) {
            logger.error("Gateway Connection: Gateway is not reachable", e);
            throw new MicroNetException("Gateway Connection: Gateway is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

}

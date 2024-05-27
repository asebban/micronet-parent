package ma.micronet.gateway.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class GatewayConnection extends Connection {

    public static final String GATEWAY_TYPE = "GATEWAY";

    private Logger logger = LoggerFactory.getLogger(GatewayConnection.class);

    @Override
    public void connect() throws MicroNetException {
        try {
            this.setSocket(new MicroNetSocket(GATEWAY_TYPE));
            this.getSocket().connect();
        } catch (IOException e) {
            logger.error("Gateway Connection: Gateway is not reachable", e);
            throw new MicroNetException("Gateway Connection: Gateway is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

}

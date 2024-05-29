package ma.micronet.agent.api;

import java.io.IOException;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class AgentConnection extends Connection {

    public AgentConnection(Adressable adressable) throws MicroNetException, IOException {
        super(adressable);
    }

    @Override
    public void connect() throws MicroNetException {
        try {
            this.setSocket(new MicroNetSocket(this.getAdressable()));
            this.getSocket().connect();
        } catch (IOException e) {
            throw new MicroNetException("Registry is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

    
}

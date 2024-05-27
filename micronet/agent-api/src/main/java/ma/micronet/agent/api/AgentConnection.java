package ma.micronet.agent.api;

import java.io.IOException;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.Connection;
import ma.micronet.commons.networking.MicroNetSocket;

public class AgentConnection extends Connection {

    @Override
    public void connect() throws MicroNetException {
        try {
            this.setSocket(new MicroNetSocket(Agent.AGENT_TYPE));
            this.getSocket().connect();
        } catch (IOException e) {
            throw new MicroNetException("Registry is not reachable", e); // Throw a MicroNetException if the connection fails
        }
    }

    
}

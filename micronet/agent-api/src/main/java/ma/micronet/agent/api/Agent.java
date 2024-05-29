package ma.micronet.agent.api;

import java.io.IOException;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;

public class Agent extends Adressable {
    public static final String AGENT_TYPE = "AGENT";

    public Agent() {
        this.setType(AGENT_TYPE);
        this.setId(UIDGenerator.generateUID());
    }

    @Override
    public String toString() {
        return "Agent [id=" + getId() + ", type=" + getType() + "]" + ", port=" + getPort() + ", host=" + getHost() + "]";
    }

    public AgentConnection createConnection() throws MicroNetException, IOException {
        AgentConnection connection = new AgentConnection(this);
        return connection;    
    }

}

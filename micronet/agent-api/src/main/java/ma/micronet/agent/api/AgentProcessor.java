package ma.micronet.agent.api;

import java.io.IOException;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public abstract class AgentProcessor implements IAgentProcessor{

    private Agent agent;

    public AgentProcessor() throws MicroNetException, IOException {
        this.agent = AgentFactory.createAgent(registerPath());
    }

    public abstract Message process(Message message) throws MicroNetException;
    public abstract String registerPath();

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Agent getAgent() {
        return this.agent;
    }

}

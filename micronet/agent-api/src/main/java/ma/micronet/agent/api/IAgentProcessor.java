package ma.micronet.agent.api;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public interface IAgentProcessor {
    public Message process(Message message) throws MicroNetException;
    public String registerPath();
    public void setAgent(Agent agent);
    public Agent getAgent();
}

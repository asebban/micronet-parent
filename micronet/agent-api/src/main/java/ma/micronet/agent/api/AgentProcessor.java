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

    public Message okResponseMessage(String payload) {
        Message response = new Message();
        response.setResponseCode(Message.OK);
        response.setDirection(Message.RESPONSE);
        response.setPayLoad(payload);
        response.setSenderAdressable(this.getAgent());
        return response;
    }

    public Message nokResponseMessage(String payload) {
        Message response = new Message();
        response.setResponseCode(Message.NOK);
        response.setDirection(Message.RESPONSE);
        response.setPayLoad(payload);
        response.setSenderAdressable(this.getAgent());
        return response;
    }

}

package ma.micronet.example;

import java.io.IOException;

import ma.micronet.agent.api.AgentProcessor;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public class MyAgent extends AgentProcessor{

    public MyAgent() throws MicroNetException, IOException {
        super();
    }

    @Override
    public Message process(Message request) throws MicroNetException {
        Message response = new Message();
        response.setResponseCode("OK");
        response.setDirection(Message.RESPONSE);
        response.setPayLoad("Hello from MyAgent");
        response.setSenderAdressable(this.getAgent());
        return response;
    }

    @Override
    public String registerPath() {
        return "/myagent/api";
    }

}

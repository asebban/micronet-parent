package ma.micronet.example;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.agent.api.AgentProcessor;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public class MyAgentProcessor extends AgentProcessor {

    private Logger logger = LoggerFactory.getLogger(MyAgentProcessor.class.getName());

    public MyAgentProcessor() throws MicroNetException, IOException {
        super();
    }

    @Override
    public Message process(Message message) throws MicroNetException {
        logger.debug("Processing message: " + message.toString());
        Message response = Message.copy(message);
        response.setDirection(Message.RESPONSE);
        response.setPayLoad("This is my agent response");
        response.setResponseCode(Message.OK);
        return response;
    }

    @Override
    public String registerPath() {
        return "/myagent/api";
    }

}

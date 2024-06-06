package ma.micronet.agent.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class AgentServer {

    private static Logger logger = LoggerFactory.getLogger(AgentServer.class);

    public static void main(String[] args) {
        try {
            AgentListener agentListener = new AgentListener();
            agentListener.start();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error starting the server : " + e.getMessage());
            System.exit(1);
        } catch (MicroNetException e) {
            e.printStackTrace();
            logger.error("Error starting the server : " + e.getMessage());
            System.exit(1);
        } 
    }

}

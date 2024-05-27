package ma.micronet.agent.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.ConfigReader;
import ma.micronet.commons.MicroNetException;

public class AgentApplication {

    private static Logger logger = LoggerFactory.getLogger(AgentApplication.class);

    public static void main(String[] args) {
        try {
            readProperties();
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

    private static void readProperties() throws IOException, MicroNetException{
        // Loading the properties file from the classpath
        ConfigReader.getInstance().readProperties();
    }
}

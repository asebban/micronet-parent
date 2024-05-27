package ma.micronet.agent.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.PortGenerator;

public class AgentFactory {

    private static Logger logger = LoggerFactory.getLogger(AgentFactory.class);

    public static Agent createAgent(String path) throws MicroNetException, IOException {

        Agent agent = new Agent();
        agent.setPort(PortGenerator.getInstance().generatePort());
        agent.setPingPort(PortGenerator.getInstance().generatePort());

        try {        
            InetAddress inetAddress = InetAddress.getLocalHost();
            agent.setHost(inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            logger.error("Error getting the host IP: " + e.getMessage());
            throw new MicroNetException("Error getting the host IP: " + e.getMessage(), e);
        }
        agent.setPath(path);
        logger.debug("Agent created with ID " + agent.getId() + " and registered with path -> " + path + ", IP address -> " + agent.getHost() + ", listening port -> " + agent.getPort());
        
        return agent;
    }

    public static AgentConnection createConnection() {
            return new AgentConnection();    
    }
}

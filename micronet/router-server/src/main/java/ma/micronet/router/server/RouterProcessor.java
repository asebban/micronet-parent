package ma.micronet.router.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.agent.api.Agent;
import ma.micronet.agent.api.AgentConnection;
import ma.micronet.agent.api.AgentFactory;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public class RouterProcessor implements Runnable {

    private Socket socket;
    private Logger logger = LoggerFactory.getLogger(RouterProcessor.class);

    public RouterProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            byte[] buffer = new byte[1024];
            logger.debug("Router Processor: Reading the request");
            is.read(buffer);
            logger.debug("Router Processor: Request read successfully");
            String request = new String(buffer, "UTF-8");
            String response = processRequest(request);
            OutputStream os = this.socket.getOutputStream();
            os.write(response.getBytes());
            os.flush();
            logger.debug("Router Processor: Response sent successfully");
        } catch (IOException e) {
            logger.error("Router Processor: Error processing the request: " + e.getMessage());
            e.printStackTrace();
        } catch (MicroNetException e) {
            logger.error("Router Processor: Error processing the request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String processRequest(String request) throws MicroNetException, IOException {
        // Process the request
        Gson gson = new Gson();
        logger.debug("Router Processor: Parsing the request");
        Message message = gson.fromJson(request, Message.class);
        logger.debug("Router Processor: Routing the request");
        Message response = route(message);
        logger.debug("Router Processor: Request routed successfully");
        return gson.toJson(response);
    }

    private Message route(Message message) throws MicroNetException, IOException {
        // Route the request
        if (!message.getTargetType().equalsIgnoreCase(Agent.AGENT_TYPE)) {
            logger.error("Router Processor: Invalid target type " + message.getTargetType() + ". Expected " + Agent.AGENT_TYPE + " type.");
            throw new IllegalArgumentException("Router: Invalid target type");
        }

        logger.debug("Router Processor: Creating a new agent connection");
        AgentConnection agentConnection = AgentFactory.createConnection();
        agentConnection.connect();
        logger.debug("Router Processor: Sending the request to the agent");
        Message response = agentConnection.sendSync(message);
        logger.debug("Router Processor: Request sent successfully to the agent and response received");
    
        return response;
    }

    // Use MicroNetSocket to connect to the destination
}

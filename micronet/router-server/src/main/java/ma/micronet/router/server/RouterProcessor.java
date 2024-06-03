package ma.micronet.router.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.agent.api.Agent;
import ma.micronet.agent.api.AgentConnection;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import java.net.URI;
import java.net.URISyntaxException;


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
            request = request.trim();
            String response = processRequest(request);
            OutputStream os = this.socket.getOutputStream();
            os.write(response.getBytes());
            os.flush();
            logger.debug("Router Processor: Response sent successfully");
        } catch (IOException e) {
            try {
                this.socket.getOutputStream().write(Message.errorMessage("Router Processor: Error processing the request: " + e.getMessage()).toString().getBytes());
            } catch (IOException e1) {
                logger.error("Router Processor: Error processing the request: " + e.getMessage());
                e1.printStackTrace();
            }
            logger.error("Router Processor: Error processing the request: " + e.getMessage());
            e.printStackTrace();
        } catch (MicroNetException e) {
            logger.error("Router Processor: Error processing the request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                logger.error("Router Processor: Error closing the socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String processRequest(String request) throws MicroNetException, IOException {
        // Process the request
        logger.debug("Router Processor: Parsing the request");
        Message message = Message.jsonToMessage(request);
        logger.debug("Router Processor: Routing the request " + message.toString() + " to the agent");
        Message response = route(message);
        logger.debug("Router Processor: Request routed successfully");
        return response.toString();
    }

    private Message route(Message message) throws MicroNetException, IOException {
        // Route the request
        logger.debug("Router Processor: Creating a new agent connection");
        Agent agent = new Agent();
        // Remove query parameters from the path
        String pathWithoutQueryParameters = removeQueryParameters(message.getPath());
        agent.setPath(pathWithoutQueryParameters);
        AgentConnection agentConnection = agent.createConnection();
        agentConnection.connect();
        logger.debug("Router Processor: Sending the request to the agent");
        Message response = agentConnection.sendSync(message);
        logger.debug("Router Processor: Request sent successfully to the agent and response received");

        return response;
    }

    public String removeQueryParameters(String path) {
        try {
            URI uri = new URI(path);
            return uri.getPath();
        } catch (URISyntaxException e) {
            // Handle exception
            e.printStackTrace();
            return null;
        }
    }
}

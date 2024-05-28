package ma.micronet.agent.server;

import java.io.InputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.agent.api.IAgentProcessor;
import ma.micronet.commons.Message;

public class AgentHandler implements Runnable {

    private Socket socket;
    private IAgentProcessor processor;
    private Logger logger = LoggerFactory.getLogger(AgentHandler.class);
    
    public AgentHandler(Socket socket, IAgentProcessor processor) {
        this.socket = socket;
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            byte[] buffer = new byte[1024];
            is.read(buffer);
            String request = new String(buffer, "UTF-8");
            request = request.trim();
            logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": Received request: " + request);
            Gson gson = new Gson();
            Message message = gson.fromJson(request, Message.class);
            Message response = processor.process(message);
            String responseString = gson.toJson(response);
            logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": Sending response: " + responseString);
            this.socket.getOutputStream().write(responseString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }   
    }
}

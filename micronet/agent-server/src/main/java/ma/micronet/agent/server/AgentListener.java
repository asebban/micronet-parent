package ma.micronet.agent.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.agent.api.AgentProcessor;
import ma.micronet.agent.api.AgentProcessorFactory;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.MicroNetMapRenewer;
import ma.micronet.commons.networking.PingListener;
import ma.micronet.config.api.ConfigReader;
import ma.micronet.registry.api.Registry;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class AgentListener {

    private Logger logger = LoggerFactory.getLogger(AgentListener.class);
    private AgentProcessor processor;

    public void start() throws MicroNetException, IOException {
        
        processor = AgentProcessorFactory.findProcessorInstance();

        ConfigReader.getInstance(processor.getAgent()).readProperties();
        
        Registry.subscribe(processor.getAgent());
        logger.debug("Agent Listener: Subscribed the agent " + processor.getAgent() + " to the registry");
        //new Thread(new PingListener(processor.getAgent())).start();

        ////////////////////////////////////
        // Handle SIGINT interruption signal
        ////////////////////////////////////
        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.debug("Agent Listener: Signal " + signal + " reçu. Arrêt en cours...");
                try {
                    Registry.unsubscribe(processor.getAgent());
                } catch (MicroNetException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        };

        // Associate signal SIGINT to the handler
        Signal.handle(new Signal("INT"), handler);

        new Thread(new PingListener(processor.getAgent())).start();

        logger.info("Server is listening on port " + processor.getAgent().getPort());

        try (ServerSocket serverSocket = new ServerSocket(processor.getAgent().getPort())) {

            // Register the agent to the registry
            MicroNetMapRenewer.getInstance(processor.getAgent()).renewMap();
            
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("AgentListener: New connection from " + socket.getInetAddress().getHostAddress());
                new Thread(new AgentHandler(socket, processor)).start();
            }

        } catch (IOException ex) {
            logger.error("Agent Listener: Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            logger.debug("Agent Listener: Unsubscribing the agent");
            Registry.unsubscribe(processor.getAgent());
        }   
    }
}

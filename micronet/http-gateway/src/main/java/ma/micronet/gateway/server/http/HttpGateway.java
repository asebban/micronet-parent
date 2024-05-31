package ma.micronet.gateway.server.http;

import static spark.Spark.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Config;
import ma.micronet.commons.ConfigReader;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;
import ma.micronet.gateway.api.Gateway;
import ma.micronet.gateway.api.GatewayConnection;
import sun.misc.SignalHandler;
import sun.misc.Signal;

public class HttpGateway {

    public static final String HTTP_GATEWAY_TYPE = "HTTP_GATEWAY";
    public static final String AGENT_TYPE = "AGENT";
    private static Adressable httpGateway;

    private static Logger logger = LoggerFactory.getLogger(HttpGateway.class);

    // wait for a http request
    public static void main(String[] args) throws MicroNetException, IOException {

        ////////////////////////////////////
        // Handle SIGINT interruption signal
        ////////////////////////////////////
        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.debug("HttpGateway Listener: Signal " + signal + " reçu. Arrêt en cours...");
                System.exit(0);
            }
        };

        // Associate signal SIGINT to the handler
        Signal.handle(new Signal("INT"), handler);

        ConfigReader.getInstance().readProperties();
        int port = Integer.parseInt(Config.getInstance().getProperty("server.port"));
        port(port); // Set the port on which the server listens


        logger.info("httpGateway: Listening on port " + port);

        httpGateway = getAdressable();

        get("/*", (req, res) -> {
    
            logger.debug("Http Gateway: Received a GET request");
            Gateway gateway = new Gateway();
            GatewayConnection gatewayConnection = gateway.createConnection();
            logger.debug("Http Gateway: Created a connection to the raw  gateway");
            try {
                logger.debug("Http Gateway: Connecting to the raw gateway");
                gatewayConnection.connect();
            } catch (MicroNetException e) {
                logger.error("Http Gateway: Error connecting to the raw gateway: " + e.getMessage());
                e.printStackTrace();
            }

            String remainingPath = req.splat()[0]; // Extract the remaining part of the path
            logger.debug("Http Gateway: Namespace requested: " + remainingPath);
            
            Message m = new Message();
            m.setPath(remainingPath);
            m.setSenderAdressable(httpGateway);
            m.setSenderType(HTTP_GATEWAY_TYPE);
            m.setDirection(Message.REQUEST);
            m.setCommand(Message.GET);

            req.queryMap().toMap().forEach((key, value) -> {
                m.getParameters().put(key, value);
            });

            m.setPayLoad(req.body());
            logger.debug("Http Gateway: Sending a message to the raw gateway: " + m.toString());
            Message response = gatewayConnection.sendSync(m);
            logger.debug("Http Gateway: Received a response from the raw gateway: " + response.toString());
            // Translate the response to String with Gson and return it

            // Assuming 'response' is your object
            return response.getPayLoad();

        });

        post("/*", (req, res) -> {

            logger.debug("Http Gateway: Received a GET request");
            Gateway gateway = new Gateway();
            GatewayConnection gatewayConnection = gateway.createConnection();
            logger.debug("Http Gateway: Created a connection to the raw  gateway");

            try {
                logger.debug("Http Gateway: Connecting to the raw gateway");
                gatewayConnection.connect();
            } catch (MicroNetException e) {
                logger.error("Http Gateway: Error connecting to the raw gateway: " + e.getMessage());
                e.printStackTrace();
            }

            String remainingPath = req.splat()[0]; // Extract the remaining part of the path
            
            Message m = new Message();
            m.setPath(remainingPath);
            m.setSenderAdressable(httpGateway);
            m.setSenderType(HTTP_GATEWAY_TYPE);
            m.setDirection(Message.REQUEST);
            m.setTargetType(AGENT_TYPE);
            m.setCommand(Message.ADD);

            req.queryMap().toMap().forEach((key, value) -> {
                m.getParameters().put(key, value);
            });

            m.setPayLoad(req.body());
            logger.debug("Http Gateway: Sending a message to the raw gateway: " + m.toString());
            Message response = gatewayConnection.sendSync(m);
            logger.debug("Http Gateway: Received a response from the raw gateway: " + response.toString());

            // Assuming 'response' is your object
            return response.getPayLoad();
            
        });

        put("/*", (req, res) -> {

            logger.debug("Http Gateway: Received a GET request");
            Gateway gateway = new Gateway();
            GatewayConnection gatewayConnection = gateway.createConnection();
            logger.debug("Http Gateway: Created a connection to the raw  gateway");

            try {
                logger.debug("Http Gateway: Connecting to the raw gateway");
                gatewayConnection.connect();
                logger.debug("Http Gateway: Connected to the raw gateway");
            } catch (MicroNetException e) {
                logger.error("Http Gateway: Error connecting to the raw gateway: " + e.getMessage());
                e.printStackTrace();
            }

            String remainingPath = req.splat()[0]; // Extract the remaining part of the path
            
            Message m = new Message();
            m.setPath(remainingPath);
            m.setSenderAdressable(httpGateway);
            m.setSenderType(HTTP_GATEWAY_TYPE);
            m.setDirection(Message.REQUEST);
            m.setTargetType(AGENT_TYPE);
            m.setCommand(Message.UPDATE);

            req.queryMap().toMap().forEach((key, value) -> {
                m.getParameters().put(key, value);
            });

            m.setPayLoad(req.body());
            logger.debug("Http Gateway: Sending a message to the raw gateway: " + m.toString());
            Message response = gatewayConnection.sendSync(m);
            logger.debug("Http Gateway: Received a response from the raw gateway: " + response.toString());

            return response.getPayLoad();
        });

        delete("/*", (req, res) -> {

            logger.debug("Http Gateway: Received a GET request");
            Gateway gateway = new Gateway();
            GatewayConnection gatewayConnection = gateway.createConnection();
            logger.debug("Http Gateway: Created a connection to the raw  gateway");

            try {
                logger.debug("Http Gateway: Connecting to the raw gateway");
                gatewayConnection.connect();
                logger.debug("Http Gateway: Connected to the raw gateway");
            } catch (MicroNetException e) {
                logger.error("Http Gateway: Error connecting to the raw gateway: " + e.getMessage());
                e.printStackTrace();
            }

            String remainingPath = req.splat()[0]; // Extract the remaining part of the path
            
            Message m = new Message();
            m.setPath(remainingPath);
            m.setSenderAdressable(httpGateway);
            m.setSenderType(HTTP_GATEWAY_TYPE);
            m.setDirection(Message.REQUEST);
            m.setTargetType(AGENT_TYPE);
            m.setCommand(Message.DELETE);

            req.queryMap().toMap().forEach((key, value) -> {
                m.getParameters().put(key, value);
            });

            m.setPayLoad(req.body());
            logger.debug("Http Gateway: Sending a message to the raw gateway: " + m.toString());
            Message response = gatewayConnection.sendSync(m);
            logger.debug("Http Gateway: Received a response from the raw gateway: " + response.toString());

            return response.getPayLoad();
        });

        exception(Exception.class, (exception, request, response) -> {
            logger.error("Http Gateway: An exception occurred: " + exception.getMessage());
            exception.printStackTrace();
            response.status(500);
            response.body("Custom error message: " + exception.getMessage());
        });

        init();
    }

    private static Adressable getAdressable() throws UnknownHostException {
        Adressable adressable = new Adressable();
        adressable.setHost(Config.getInstance().getCurrentHost());
        adressable.setPort(Integer.parseInt(Config.getInstance().getProperty("server.port")));
        adressable.setType(HTTP_GATEWAY_TYPE);
        adressable.setId(UIDGenerator.generateUID());
        return adressable;
    }
}

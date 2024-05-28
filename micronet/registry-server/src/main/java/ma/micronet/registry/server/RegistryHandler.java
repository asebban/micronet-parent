package ma.micronet.registry.server;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.registry.api.Registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.List;

public class RegistryHandler implements Runnable {

    private Socket socket;
    private Registry registry;
    private Logger logger = LoggerFactory.getLogger(RegistryHandler.class);

    public RegistryHandler(Socket socket, Registry registry) {
        this.socket = socket;
        this.registry = registry;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer);
            String json = new String(buffer, 0, bytesRead);
            Gson gson = new Gson();
            Message message = gson.fromJson(json, Message.class);

            Message response = process(message);
            
            OutputStream os = this.socket.getOutputStream();
            String responseJson = gson.toJson(response);
            logger.debug("RegistryHandler: Sending response: " + responseJson);
            os.write(responseJson.getBytes());
            os.flush();
        } catch (IOException e) {
            logger.error("RegistryHandler: Error getting input stream: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                logger.error("RegistryHandler: Error closing socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Message process(Message message) {
        Message response = null;

        logger.debug("RegistryHandler: Processing message: " + message.toString());
        switch(message.getCommand()) {
            case Registry.SUBSCRIBE_COMMAND:
                logger.info("RegistryHandler: Subscribing service");
                response = subscribe(message);
                break;
            case Registry.UNSUBSCRIBE_COMMAND:
                logger.info("RegistryHandler: Unsubscribing service");
                response = unsubscribe(message);
                break;
            case Registry.REGISTRY_GETMAP:
                logger.info("RegistryHandler: Answering getMap command");
                response = getMap(message);
                break;
            default:
                logger.error("RegistryHandler: Unknown command: " + message.getCommand());
                response = createErrorMessage(message, "Unknown command: " + message.getCommand());
                break;
        }

        return response;
    }

    public Message subscribe(Message message) {
        String payload = message.getPayLoad();
        Adressable adressable = (new Gson()).fromJson(payload, Adressable.class);
        RegistryMapController.getInstance().addService(message.getSenderType(), adressable);
        logger.debug("RegistryHandler.subscribe: Subsribed service " + message.getSenderType() + " at " + adressable.getHost() + ":" + adressable.getPort() + " with id " + adressable.getId());
        return createSubscribeAckMessage(message);
    }

    public Message unsubscribe(Message message) {
        String payload = message.getPayLoad();
        Adressable adressable = (new Gson()).fromJson(payload, Adressable.class);
        logger.debug("RegistryHandler.unsubscribe: Unsubscribed service " + message.getSenderType() + " at " + adressable.getHost() + ":" + adressable.getPort() + " with id " + adressable.getId());
        RegistryMapController.getInstance().removeService(message.getSenderType(), adressable);
        return createUnsubscribeAckMessage(message);
    }

    public Message getMap(Message request) {
        Map<String, List<Adressable>> registryMap = RegistryMapController.getInstance().getRegistryMap();
        Message response = createGetMapMessage(request, registryMap);
        return response;
    }

    private Message createSubscribeAckMessage(Message request) {
        Message response = new Message();
        response.setCommand(Registry.SUBSCRIBE_ACK);
        response.setDirection(Message.RESPONSE);
        response.setSenderType(Registry.REGISTRY_TYPE);
        response.setSenderId(registry.getId());
        response.setTargetType(request.getSenderType());
        response.setResponseCode(Message.OK);
        return response;
    }

    private Message createUnsubscribeAckMessage(Message request) {
        Message response = new Message();
        response.setCommand(Registry.UNSUBSCRIBE_ACK);
        response.setDirection(Message.RESPONSE);
        response.setSenderType(Registry.REGISTRY_TYPE);
        response.setSenderId(registry.getId());
        response.setTargetType(request.getSenderType());
        response.setResponseCode(Message.OK);
        return response;
    }

    private Message createGetMapMessage(Message request, Map<String, List<Adressable>> registryMap) {
        Message response = new Message();
        response.setCommand(Registry.REGISTRY_GETMAP);
        response.setDirection(Message.RESPONSE);
        response.setSenderType(Registry.REGISTRY_TYPE);
        response.setSenderId(registry.getId());
        response.setTargetType(request.getSenderType());
        Gson gson = new Gson();
        String payload = gson.toJson(registryMap);
        response.setPayLoad(payload);
        response.setResponseCode(Message.OK);
        return response;
    }

    private Message createErrorMessage(Message request, String errorMessage) {
        Message response = new Message();
        response.setResponseCode(Message.ERROR);
        response.setDirection(Message.RESPONSE);
        response.setSenderType(Registry.REGISTRY_TYPE);
        response.setSenderId(registry.getId());
        response.setTargetType(request.getSenderType());
        response.setPayLoad(errorMessage);
        response.setResponseCode(Message.ERROR);
        return response;
    }

}

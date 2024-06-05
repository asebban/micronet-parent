package ma.micronet.registry.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;
import ma.micronet.config.api.Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry extends Adressable {

    @SuppressWarnings("unused")
    private static Adressable adressable;

    private static Logger logger = LoggerFactory.getLogger(Registry.class);
    
    public Registry() {
        this.setType(Message.REGISTRY_TYPE);
        this.setId(UIDGenerator.generateUID());
    }

    public Registry(String ip, int port) {
        super(ip, port);
        this.setType(Message.REGISTRY_TYPE);
        this.setId(UIDGenerator.generateUID());
    }

    public Registry(String ip, int port, String type) {
        super(ip, port, type);
        this.setId(UIDGenerator.generateUID());
    }

    public Registry(Adressable adressable) {
        super(adressable.getHost(), adressable.getPort(), adressable.getType());
        Registry.adressable = adressable;
        this.setId(UIDGenerator.generateUID());
    }

    public static RegistryMessage createGetMapRegistryRequest(Adressable adressable) throws MicroNetException {
        RegistryMessage m = new RegistryMessage();
        m.setDirection(Message.REQUEST);
        m.setCommand(Message.REGISTRY_GETMAP_COMMAND);
        if (adressable == null) {
            logger.error("Registry.createGetMapRegistryRequest: Error creating the get map request: Adressable is null");
            throw new MicroNetException("Registry.createGetMapRegistryRequest: Error creating the get map request: Adressable is null");
        }
        m.setSenderAdressable(adressable);
        m.setSenderType(adressable.getType());
        return m;
    }

    public static void subscribe(Adressable a) throws MicroNetException {

        RegistryMessage m = new RegistryMessage();
        
        String registryHost = Config.getInstance().getProperty("registry.host");

        int registryPort=0;
        try {
            registryPort = Integer.parseInt(Config.getInstance().getProperty("registry.port"));
        } catch (NumberFormatException e) {
            logger.error("Error reading the registry port from the configuration file: " + e.getMessage());
            throw new MicroNetException(e);
        }

        logger.debug("Subscribing to the registry at " + registryHost + ":" + registryPort);

        m.setDirection(Message.REQUEST);
        m.setCommand(Message.SUBSCRIBE_COMMAND);
        m.setSenderAdressable(a);
        m.setSenderType(a.getType());
        m.setTargetType(Message.REGISTRY_TYPE);
        
        Gson gson = new GsonBuilder().setLenient().create();
        String adressable = gson.toJson(a);
        m.setPayLoad(adressable);

        Socket socket=null;
        Integer reconnectInterval=2;
        
        try {
            reconnectInterval = Integer.parseInt(Config.getInstance().getProperty("registry.reconnect.interval"));
        } catch (NumberFormatException e) {
            logger.debug("Registry: reconnect interval not configured. Using default value of " + reconnectInterval + " seconds");
        }
        
        Boolean isSubscribed = false;

        while(!isSubscribed)    {

            try {
                logger.debug("Registry: Opening a socket to the registry");
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                socket = new Socket(registryHost, registryPort);
                logger.debug("Registry: Socket opened to the registry");
            } catch (UnknownHostException e) {
                logger.error("Registry: Error opening a socket to the registry: " + e.getMessage());
                try {
                    Thread.sleep(reconnectInterval*1000);
                    continue;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    continue;
                }
            } catch (IOException e) {
                logger.error("Registry: Error opening a socket to the registry: " + e.getMessage());
                try {
                    Thread.sleep(reconnectInterval*1000);
                    continue;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    continue;
                }
            }
        
            try {
                String json = gson.toJson(m);
                logger.debug("Registry: Sending the subscription request to the registry -> " + json);
                socket.getOutputStream().write(json.getBytes());
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[Message.BUFFER_SIZE];
                logger.debug("Registry: Reading the response from the registry");
                in.read(buffer);
                String jsonResponse = new String(buffer, "UTF-8");
                jsonResponse = jsonResponse.trim();
                logger.debug("Registry: Response read from the registry -> " + jsonResponse);
                Message responseMessage = gson.fromJson(jsonResponse, Message.class);
                
                logger.debug("Response JSON: " + jsonResponse);
    
                if (responseMessage.getCommand().equals(Message.SUBSCRIBE_ACK)) {
                    logger.debug("Registry: Subscribed to the registry");
                    isSubscribed = true;
                } else {
                    logger.error("Registry: Error subscribing to the registry -> " + responseMessage.getPayLoad());
                    continue;
                }   
            } catch (UnknownHostException e) {
                e.printStackTrace();
                logger.error("Registry: Error subscribing to the registry: " + e.getMessage());
                continue;
            } catch (IOException e) {
                logger.error("Registry: Error subscribing to the registry: " + e.getMessage());
                e.printStackTrace();
                continue;
            } 
        }
        
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unsubscribe(Adressable a) throws MicroNetException, IOException {

        RegistryMessage m = new RegistryMessage();
        
        String registryHost = Config.getInstance().getProperty("registry.host").trim();
        int registryPort = Integer.parseInt(Config.getInstance().getProperty("registry.port").trim());

        logger.debug("Unsubscribing from the registry at " + registryHost + ":" + registryPort);

        m.setDirection(Message.REQUEST);
        m.setCommand(Message.UNSUBSCRIBE_COMMAND);
        m.setSenderType(a.getType());
        m.setSenderAdressable(a);
        m.setTargetType(Message.REGISTRY_TYPE);
        
        Gson gson = new Gson();
        String adressable = gson.toJson(a);
        m.setPayLoad(adressable);

        Boolean isUnsubscribed = false;
        Socket socket=null;

        while(!isUnsubscribed) {
            try {
                logger.debug("Registry: Opening a socket to the registry for unsubscribing");
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                socket = new Socket(registryHost, registryPort);
                logger.debug("Registry: Socket opened to the registry for unsubscribing");
            } catch (UnknownHostException e) {
                logger.error("Registry: Error opening a socket to the registry for unsubscribing: " + e.getMessage());
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.error("Registry: Error opening a socket to the registry for unsubscribing: " + e.getMessage());
                e.printStackTrace();
                continue;
            }

            try {
                logger.debug("Registry: Sending the unsubscription request to the registry");
                socket.getOutputStream().write(gson.toJson(m).getBytes());
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[1024];
                logger.debug("Registry: Reading the response from the registry for unsubscribing");
                in.read(buffer);
                String response = new String(buffer, "UTF-8");
                logger.debug("Registry: Response read from the registry for unsubscribing");
                RegistryMessage responseMessage = gson.fromJson(response, RegistryMessage.class);
                if (responseMessage.getResponseCode().equals(Message.UNSUBSCRIBE_ACK)) {
                    logger.debug("Unsubscribed to the registry");
                    isUnsubscribed = true;
                } else {
                    logger.error("Error subscribing to the registry: " + responseMessage.getPayLoad());
                    continue;
                }

            } catch (UnknownHostException e) {
                logger.error("Registry: Error unsubscribing from the registry: " + e.getMessage());
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                logger.error("Registry: Error unsubscribing from the registry: " + e.getMessage());
                e.printStackTrace();
                continue;
            }  
        }
        
      
    }

    public static Adressable createAdressable(String type) throws UnknownHostException {
        Adressable a = new Adressable();
        a.setHost(Config.getInstance().getCurrentHost());
        a.setPort(Integer.parseInt(Config.getInstance().getProperty("registry.port")));
        a.setType(type);
        return a;
    }

}

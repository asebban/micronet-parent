package ma.micronet.registry.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ma.micronet.commons.Adressable;
import ma.micronet.commons.Config;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry extends Adressable {

    public static final String REGISTRY_GETMAP = "GETMAP";
    public static final String REGISTRY_TYPE = "REGISTRY";
    public static final String SUBSCRIBE_COMMAND = "SUBSCRIBE";
    public static final String UNSUBSCRIBE_COMMAND = "UNSUBSCRIBE";
    public static final String SUBSCRIBE_ACK = "SUBSCRIBE_ACK";
    public static final String UNSUBSCRIBE_ACK = "UNSUBSCRIBE_ACK";
    public static final String SUBSCRIBE_NACK = "SUBSCRIBE_NACK";
    public static final String UNSUBSCRIBE_NACK = "UNSUBSCRIBE_NACK";

    public static final String GATEWAY_TYPE = "GATEWAY";

    private static Logger logger = LoggerFactory.getLogger(Registry.class);
    
    public Registry() {
        String host=null;
        host = Config.getInstance().getProperty("registry.host");
        int port = Integer.parseInt(Config.getInstance().getProperty("registry.port"));

        super.setHost(host);
        super.setPort(port);
        this.setType(REGISTRY_TYPE);
        this.setId(UIDGenerator.generateUID());
    }

    public Registry(String ip, int port) {
        super(ip, port);
        this.setType(REGISTRY_TYPE);
        this.setId(UIDGenerator.generateUID());
    }

    public Registry(String ip, int port, String type) {
        super(ip, port, type);
        this.setId(UIDGenerator.generateUID());
    }

    public static RegistryMessage createGetMapRegistryRequest(String type) throws MicroNetException {
        RegistryMessage m = new RegistryMessage();
        m.setDirection(Message.REQUEST);
        m.setCommand(Registry.REGISTRY_GETMAP);
        try {
            Adressable a = createAdressable(type);
            m.setSenderAdressable(a);
            m.setSenderType(a.getType());
        } catch (UnknownHostException e) {
            throw new MicroNetException("Error getting the current host", e);
        }
        catch (NumberFormatException e) {
            throw new MicroNetException("Error getting the registry port", e);
        }
        return m;
    }

    public static void subscribe(Adressable a) throws MicroNetException, IOException {

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
        m.setCommand(Registry.SUBSCRIBE_COMMAND);
        m.setSenderAdressable(a);
        m.setSenderType(a.getType());
        m.setTargetType(Registry.REGISTRY_TYPE);
        
        Gson gson = new GsonBuilder().setLenient().create();
        String adressable = gson.toJson(a);
        m.setPayLoad(adressable);

        Socket socket;
        try {
            logger.debug("Registry: Opening a socket to the registry");
            socket = new Socket(registryHost, registryPort);
            logger.debug("Registry: Socket opened to the registry");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            logger.error("Registry: Error opening a socket to the registry: " + e.getMessage());
            throw new MicroNetException(e);
        } catch (IOException e) {
            logger.error("Registry: Error opening a socket to the registry: " + e.getMessage());
            e.printStackTrace();
            throw new MicroNetException(e);
        }

        try {
            logger.debug("Registry: Sending the subscription request to the registry");
            String json = gson.toJson(m);
            logger.debug("Request JSON: " + json);
            socket.getOutputStream().write(json.getBytes());
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            logger.debug("Registry: Reading the response from the registry");
            in.read(buffer);
            logger.debug("Registry: Response read from the registry");
            String jsonResponse = new String(buffer, "UTF-8");
            jsonResponse = jsonResponse.trim();
            Message responseMessage = gson.fromJson(jsonResponse, Message.class);
            
            logger.debug("Response JSON: " + jsonResponse);

            if (responseMessage.getCommand().equals(Registry.SUBSCRIBE_ACK)) {
                logger.debug("Registry: Subscribed to the registry");
            } else {
                logger.error("Registry: Error subscribing to the registry");
                socket.close();
                throw new MicroNetException("Error subscribing to the registry");
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            socket.close();
            logger.error("Registry: Error subscribing to the registry: " + e.getMessage());
            throw new MicroNetException("Registry Host Unreachable", e);
        } catch (IOException e) {
            logger.error("Registry: Error subscribing to the registry: " + e.getMessage());
            e.printStackTrace();
            socket.close();
            throw new MicroNetException("Unknown IO Exception", e);
        } finally {
            socket.close();
        }
        
    }

    public static void unsubscribe(Adressable a) throws MicroNetException, IOException {

        RegistryMessage m = new RegistryMessage();
        
        String registryHost = Config.getInstance().getCurrentHost();
        int registryPort = Integer.parseInt(Config.getInstance().getProperty("registry.port"));

        logger.debug("Unsubscribing from the registry at " + registryHost + ":" + registryPort);

        m.setDirection(Message.REQUEST);
        m.setCommand(Registry.UNSUBSCRIBE_COMMAND);
        m.setSenderType(a.getType());
        m.setSenderAdressable(a);
        m.setTargetType(Registry.REGISTRY_TYPE);
        
        Gson gson = new Gson();
        String adressable = gson.toJson(a);
        m.setPayLoad(adressable);

        Socket socket;
        try {
            logger.debug("Registry: Opening a socket to the registry for unsubscribing");
            socket = new Socket(registryHost, registryPort);
            logger.debug("Registry: Socket opened to the registry for unsubscribing");
        } catch (UnknownHostException e) {
            logger.error("Registry: Error opening a socket to the registry for unsubscribing: " + e.getMessage());
            e.printStackTrace();
            throw new MicroNetException(e);
        } catch (IOException e) {
            logger.error("Registry: Error opening a socket to the registry for unsubscribing: " + e.getMessage());
            e.printStackTrace();
            throw new MicroNetException(e);
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
            if (responseMessage.getResponseCode().equals(Registry.UNSUBSCRIBE_ACK)) {
                logger.debug("Subscribed to the registry");
            } else {
                logger.error("Error subscribing to the registry");
                socket.close();
                throw new MicroNetException("Error subscribing to the registry");
            }

        } catch (UnknownHostException e) {
            logger.error("Registry: Error unsubscribing from the registry: " + e.getMessage());
            e.printStackTrace();
            socket.close();
            throw new MicroNetException("Registry Host Unreachable", e);
        } catch (IOException e) {
            logger.error("Registry: Error unsubscribing from the registry: " + e.getMessage());
            e.printStackTrace();
            socket.close();
            throw new MicroNetException("Unknown IO Exception", e);
        } finally {
            socket.close();
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

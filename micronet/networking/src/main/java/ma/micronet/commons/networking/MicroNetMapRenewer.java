package ma.micronet.commons.networking;

import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ma.micronet.commons.PropertiesReader;

public class MicroNetMapRenewer {

    private Map<String, List<Adressable>> map;
    private Map<String, List<Adressable>> agentsMap;
    private static MicroNetMapRenewer instance;
    private ScheduledExecutorService executor;
    private Logger logger = LoggerFactory.getLogger(MicroNetMapRenewer.class);
    private Adressable adressable;
    private String registryHost;
    private int registryPort;
    private Properties localProperties;

    private MicroNetMapRenewer(Adressable adressable) throws MicroNetException, IOException {

        this.map = new HashMap<>();
        this.adressable = adressable;

        PropertiesReader.readProperties();

        this.localProperties = PropertiesReader.getProperties();

        long frequency = localProperties.getProperty("map.renewer.frequency") != null ? Long.parseLong(localProperties.getProperty("map.renewer.frequency")): 3;

        executor  = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                renewMap();
            } catch (MicroNetException | IOException e) {
                e.printStackTrace();
            }
        }, frequency, frequency, TimeUnit.SECONDS);

    }

    public static MicroNetMapRenewer getInstance(Adressable adressable) throws MicroNetException, IOException {
        if (instance == null) {
            instance = new MicroNetMapRenewer(adressable);
        }
        return instance;
    }

    public synchronized Map<String, List<Adressable>> renewMap() throws MicroNetException, IOException {

        Socket registrySocket = null;

        if (PropertiesReader.getProperties() == null) {
            PropertiesReader.readProperties();
        }
        localProperties = PropertiesReader.getProperties();

        registryHost = localProperties.getProperty("registry.host");
        try {
            registryPort = Integer.parseInt(localProperties.getProperty("registry.port"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new MicroNetException("registry.port property not found in application.properties file or not a number. Unable to know the registry port", e);
        }

        if (registryHost == null || registryPort == 0) {
            logger.error("Registry host or port not set in the configuration file");
            throw new MicroNetException("Registry host or port not set in the configuration file"); // Throw a MicroNetException if the host or port is not set
        }

        Message m = createRegistryGetMapMessage(this.adressable);
        Gson gson = new Gson();
        // convert the message to a JSON string using Gson
        String json = gson.toJson(m);

        try {
            registrySocket = new Socket(registryHost, registryPort); // Connect to the Registry
        } catch (IOException e) {
            logger.error(registryHost + ":" + registryPort + " is not reachable");
            throw new MicroNetException(registryHost + ":" + registryPort + " is not reachable", e); // Throw a MicroNetException if the connection fails
        }

        InputStream is = registrySocket.getInputStream();
        OutputStream os = registrySocket.getOutputStream();
        
        byte[] toSend = json.getBytes();

        try {
            os.write(toSend); // Send the message to the Registry
            os.flush();
        } catch (IOException e) {
            logger.error("MapRenewer: Error while sending the request to the Registry");
            e.printStackTrace();
            registrySocket.close();
            throw new MicroNetException("Error while sending the message to the Registry", e);
        }

        byte[] buffer = new byte[Message.BUFFER_SIZE];

        int bytesRead;
        try {
            bytesRead = is.read(buffer);
        } catch (IOException e) {
            logger.error("MapRenewer: Error while reading the response from the Registry");
            e.printStackTrace();
            registrySocket.close();
            throw new MicroNetException("Error while reading the response from the Registry", e);
        }

        if (buffer == null || bytesRead == -1) {
            registrySocket.close();
            logger.error("MapRenewer: No response from the Registry");
            throw new MicroNetException("No response from the Registry");
        }

        String response = new String(buffer, 0, bytesRead, "UTF-8"); // Receive the response from the Registry

        // convert the response to a Message object using Gson
        try {
            response = response.trim();
            logger.debug("Response received from registry: " + response);
            Message registryMessage = gson.fromJson(response, Message.class);
            String payLoad = registryMessage.getPayLoad();
            payLoad = payLoad.trim();
            logger.debug("Payload received from registry: " + payLoad);
            Type mapType = new TypeToken<Map<String, List<Adressable>>>() {}.getType();
            this.map = gson.fromJson(payLoad, mapType);
        } catch (JsonSyntaxException e) {
            logger.error("MapRenewer: Error while parsing the response from the Registry");
            e.printStackTrace();
            registrySocket.close();
            throw new MicroNetException("Error while parsing the response from the Registry", e);
        } finally {
            registrySocket.close();
        }

        // prepare agents map for routers
        List<Adressable> agents = this.map.get(Message.AGENT_TYPE);
        // Map indexed by path
        this.agentsMap = prepareAgentsMap(agents);
        return this.map;
    }

    private Map<String, List<Adressable>> prepareAgentsMap(List<Adressable> agents) {
        
        if (agents == null) {
            return null;
        }

        Map<String, List<Adressable>> nm = new HashMap<>();
        for(Adressable agent : agents) {
            String path = agent.getPath();
            logger.debug("MapRenewer: Adding agent " + agent + " to the agents map");
            if (nm.get(path) == null) {
                nm.put(path, new ArrayList<>());
            }
            nm.get(path).add(agent);
        }            
        return nm;
    }

    public Map<String, List<Adressable>> getMap() {
        return map;
    }

    public Map<String, List<Adressable>> getAgentsMap() {
        return agentsMap;
    }

    public void stop() {
        instance.executor.shutdown();
    }

     public static Message createRegistryGetMapMessage(Adressable adressable) {
        Message m = new Message();
        m.setSenderAdressable(adressable);
        m.setSenderType(adressable.getType());
        m.setDirection(Message.REQUEST);
        m.setCommand(Message.REGISTRY_GETMAP_COMMAND);
        return m;
    }

}

package ma.micronet.commons.networking;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Config;
import ma.micronet.commons.MicroNetException;
import ma.micronet.registry.api.Registry;
import ma.micronet.registry.api.RegistryMessage;

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

public class MicroNetMapRenewer {

    private Map<String, List<Adressable>> map;
    private Map<String, List<Adressable>> agentsMap;
    private static MicroNetMapRenewer instance;
    private ScheduledExecutorService executor;
    private Logger logger = LoggerFactory.getLogger(MicroNetMapRenewer.class);
    public static final String AGENT_TYPE = "AGENT";
    public static final String MAP_RENEWER_TYPE = "MAP_RENEWER";

    private MicroNetMapRenewer() {

        this.map = new HashMap<>();

        long frequency = Config.getInstance().getProperty("map.renewer.frequency") != null ? Long.parseLong(Config.getInstance().getProperty("map.renewer.frequency")) : 30;

        executor  = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                renewMap();
            } catch (MicroNetException | IOException e) {
                e.printStackTrace();
            }
        }, 10, frequency, TimeUnit.SECONDS);

    }

    public static MicroNetMapRenewer getInstance() {
        if (instance == null) {
            instance = new MicroNetMapRenewer();
        }
        return instance;
    }

    public synchronized Map<String, List<Adressable>> renewMap() throws MicroNetException, IOException {

        Socket registrySocket = null;

        String host = Config.getInstance().getProperty("registry.host"); // Retrieve the registry host from Config
        int port = Integer.parseInt(Config.getInstance().getProperty("registry.port"));

        if (host == null || port == 0) {
            logger.error("Registry host or port not set in the configuration file");
            throw new MicroNetException("Registry host or port not set in the configuration file"); // Throw a MicroNetException if the host or port is not set
        }

        RegistryMessage m = Registry.createGetMapRegistryRequest(MAP_RENEWER_TYPE);
        Gson gson = new Gson();
        // convert the message to a JSON string using Gson
        String json = gson.toJson(m);

        try {
            registrySocket = new Socket(host, port); // Connect to the Registry
        } catch (IOException e) {
            logger.error(host + ":" + port + " is not reachable");
            throw new MicroNetException(host + ":" + port + " is not reachable", e); // Throw a MicroNetException if the connection fails
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

        byte[] buffer = new byte[4048];

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
            RegistryMessage registryMessage = gson.fromJson(response, RegistryMessage.class);
            String payLoad = registryMessage.getPayLoad();
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
        List<Adressable> agents = this.map.get(AGENT_TYPE);
        this.agentsMap = prepareAgentsMap(agents);
        return this.map;
    }

    private Map<String, List<Adressable>> prepareAgentsMap(List<Adressable> agents) {
        
        if (agents == null) {
            return null;
        }

        Map<String, List<Adressable>> nm = new HashMap<>();
        if (this.agentsMap == null) {
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
        else {
            return this.agentsMap;
        }
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

    @SuppressWarnings("unused")
    private List<Adressable> convertLinkedTreeMapToList(@SuppressWarnings("rawtypes") LinkedTreeMap<String, LinkedTreeMap> linkedTreeMap) {
        List<Adressable> list = new ArrayList<>();
        for (String key : linkedTreeMap.keySet()) {
            Adressable adressable = new Adressable();
            @SuppressWarnings("unchecked")
            LinkedTreeMap<String, String> value = linkedTreeMap.get(key);
            adressable.setHost(value.get("host"));
            adressable.setPort(Integer.parseInt(value.get("port")));
            adressable.setPath(value.get("path"));
            adressable.setType(value.get("type"));
            adressable.setId(value.get("id"));
            adressable.setPingPort(value.get("pingPort") != null ? Integer.parseInt(value.get("pingPort")) : 0);
            list.add(adressable);
        }
        return list;
    }
}

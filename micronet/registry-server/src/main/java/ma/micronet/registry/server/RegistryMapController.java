package ma.micronet.registry.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Config;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class RegistryMapController {
    // singleton
    private static RegistryMapController instance = null;
    private Map<String, List<Adressable>> registryMap = new HashMap<>();
    public static Map<Adressable, Long> lastSeenMap = new HashMap<>();
    public static int cleanTimeout=0;
    private Logger logger = LoggerFactory.getLogger(RegistryMapController.class);
    public static final Long SECOND=1000L;

    private RegistryMapController() {}
    
    public static RegistryMapController getInstance() {
        if (instance == null) {
            instance = new RegistryMapController();
            cleanTimeout = Integer.parseInt(Config.getInstance().getProperty("registry.clean.timeout"));
        }
        return instance;
    }

    public void addService(String serviceType, Adressable service) {
        if (registryMap.containsKey(serviceType)) {
            registryMap.get(serviceType).add(service);
        } else {
            List<Adressable> services = new ArrayList<>();
            services.add(service);
            registryMap.put(serviceType, services);
        }
        updateLastSeen(service);
    }

    public void removeService(String serviceType, Adressable service) {
        if (registryMap.containsKey(serviceType)) {
            registryMap.get(serviceType).remove(service);
        }
    }

    public Map<String, List<Adressable>> getRegistryMap() {
        return registryMap;
    }

    public void updateLastSeen(Adressable service) {
        lastSeenMap.put(service, System.currentTimeMillis());
    }

    public void removeLastSeen(Adressable service) {
        lastSeenMap.remove(service);
    }

    public void cleanRegistryMap() {
        
        long currentTime = System.currentTimeMillis();
        for(Map.Entry<String, List<Adressable>> entry : registryMap.entrySet()) {
            List<Adressable> services = entry.getValue();
            List<Adressable> toRemove = new ArrayList<>();
            for (Adressable service : services) {
                if (currentTime - (lastSeenMap.get(service) == null ? currentTime : lastSeenMap.get(service)) > cleanTimeout*SECOND) {
                    toRemove.add(service);
                }
            }
            for (Adressable service : toRemove) {
                logger.debug("RegistryMapController.cleanMap: Removing service " + service.getId() + " of type " + entry.getKey() + " from registry because of inactivity");
                services.remove(service);
                removeLastSeen(service);
            }
            registryMap.put(entry.getKey(), services);
        }
    }

}


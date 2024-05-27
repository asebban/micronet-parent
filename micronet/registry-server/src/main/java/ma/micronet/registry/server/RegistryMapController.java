package ma.micronet.registry.server;

import java.util.Map;
import ma.micronet.commons.Adressable;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class RegistryMapController {
    // singleton
    private static RegistryMapController instance = null;
    private Map<String, List<Adressable>> registryMap = new HashMap<>();

    private RegistryMapController() {}
    
    public static RegistryMapController getInstance() {
        if (instance == null) {
            instance = new RegistryMapController();
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
    }

    public void removeService(String serviceType, Adressable service) {
        if (registryMap.containsKey(serviceType)) {
            registryMap.get(serviceType).remove(service);
        }
    }

    public Map<String, List<Adressable>> getRegistryMap() {
        return registryMap;
    }
}


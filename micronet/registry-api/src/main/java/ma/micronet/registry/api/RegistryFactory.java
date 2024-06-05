package ma.micronet.registry.api;

import ma.micronet.config.api.Config;

public class RegistryFactory {

    public static Registry createRegistry() {
        String host=null;
        host = Config.getInstance().getProperty("registry.host");
        int port = Integer.parseInt(Config.getInstance().getProperty("registry.port"));
        Registry registry = new Registry();
        registry.setHost(host);
        registry.setPort(port);
        registry.setMapRenewFrequency(Config.getInstance().getProperty("map.renewer.frequency") != null ? Long.parseLong(Config.getInstance().getProperty("map.renewer.frequency")) : 5L);

        return registry;
    }

}

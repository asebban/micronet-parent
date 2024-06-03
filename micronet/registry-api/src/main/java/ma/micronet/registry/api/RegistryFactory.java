package ma.micronet.registry.api;

import ma.micronet.commons.Config;

public class RegistryFactory {

    public static Registry createRegistry() {
        String host=null;
        host = Config.getInstance().getProperty("registry.host");
        int port = Integer.parseInt(Config.getInstance().getProperty("registry.port"));
        Registry registry = new Registry();
        registry.setHost(host);
        registry.setPort(port);

        return registry;
    }

}

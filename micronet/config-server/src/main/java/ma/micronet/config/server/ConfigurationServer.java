package ma.micronet.config.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class ConfigurationServer {

    public static Properties props;
    private static Logger logger = LoggerFactory.getLogger(ConfigurationServer.class);
    @SuppressWarnings("unused")
    public static String[] cmdLineArgs;

    public static void main(String[] args) throws MicroNetException {
        cmdLineArgs = args;
        ConfigurationListener  listener = new ConfigurationListener();
        logger.debug("ConfigApplication: Starting ConfigServerListener");
        listener.start();
    }
}

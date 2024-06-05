package ma.micronet.config.server;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class ConfigServer {

    public static Properties props;
    private static Logger logger = LoggerFactory.getLogger(ConfigServer.class);
    @SuppressWarnings("unused")
    public static String[] cmdLineArgs;

    public static void main(String[] args) throws MicroNetException {
        cmdLineArgs = args;
        ConfigListener  listener = new ConfigListener();
        logger.debug("ConfigApplication: Starting ConfigServerListener");
        listener.start();
    }
}

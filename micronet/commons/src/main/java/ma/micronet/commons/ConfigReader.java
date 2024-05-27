package ma.micronet.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigReader {

    // singleton

    private Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private Properties props = new Properties();
    private static ConfigReader instance = null;

    private ConfigReader() {
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }
    
    public void readProperties() throws MicroNetException, IOException {
        // Loading the properties file from the classpath
        InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("application.properties");
        if (input == null) {
            logger.error("Sorry, unable to find application.properties");
            throw new MicroNetException("Sorry, unable to find application.properties");
        }
        // Load the properties file
        props.load(input);

        Config.getInstance().setProps(props);
    }

}

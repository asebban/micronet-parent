package ma.micronet.config.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

import java.net.MalformedURLException;
import java.net.URL;

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

        // temporary map to load properties
        Properties properties = new Properties();
        
        // Loading the properties file from the classpath
        InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("application.properties");
        if (input == null) {
            logger.debug("Unable to find application.properties");
        } else {
            // Load the properties file
            properties.load(input);
        }

        // Override file properties with environment variable property if a conflict occurs
        if (System.getenv("CONFIG_SOURCE_URL") != null) {
            properties.setProperty("config.source.url", System.getenv("CONFIG_SOURCE_URL"));
        }

        String configSourceUrl = properties.getProperty("config.source.url");
        
        URL url=null;
        try {
            url = new URL(configSourceUrl);
            input = url.openStream();
            Properties p = new Properties();
            p.load(input);
            properties.putAll(p);
        } catch (MalformedURLException e) {
            logger.error("ConfigReader: Error while creating the URL object from config source: " + e.getMessage() + ". May be the config source is not not declared or not well formed");
        }
        input.close();

        
        Properties p = ConfigManager.readCmdLineVariables(System.getProperty("sun.java.command").split(" "));
        properties.putAll(p); // Override file properties with command line properties if a conflict occurs

        // Override properties with environment variables if a conflict occurs
        properties.forEach((key, value) -> {
            logger.debug("ConfigReader.readProperties: Property read from conf file or cmd line " + key + " = " + value);
            String envName = ((String) key).replaceAll("\\.", "_").toUpperCase();
            if (System.getenv(envName) != null) {
                logger.debug("Overriding property " + key + " with environment variable " + envName + " which vallue is " + System.getenv(envName));
                value = System.getenv(envName);
            }

            props.setProperty((String) key, (String) value);
        });

        Config.getInstance().setProps(props);

        // Finally, get properties from config server (the most prioritary source)
        ConfigManager configManager = new ConfigManager();
        configManager.setHost(Config.getInstance().getProperty("registry.host") == null ? "localhost" : Config.getInstance().getProperty("registry.host"));
        configManager.setPort(Config.getInstance().getProperty("registry.port") == null ? 10000: Integer.parseInt(Config.getInstance().getProperty("registry.port")));
        Message request = new Message();
        
        request.setCommand(Message.GET_CONFIG_COMMAND);
        request.setSenderAdressable(configManager);
        request.setDirection(Message.REQUEST);
        request.setTargetAdressable(configManager);

        ConfigManagerConnection connection = configManager.createConnection();

        Boolean isConfigServerReached = false;
        Message response = null;
        Long reconnectInterval=(properties.get("config.reconnect.interval") != null ? Long.parseLong((String)properties.get("config.reconnect.interval")) : 5L);

        while(!isConfigServerReached) {
            try {
                connection.connect();
                response = connection.sendSync(request);
                isConfigServerReached = true;
            } catch (MicroNetException e) {
                logger.error("ConfigReader: Error while connecting to the config server: " + e.getMessage() + ". Retrying...");
                try {
                    Thread.sleep(reconnectInterval*1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (response != null && response.getResponseCode().equals(Message.OK)) {
            Gson gson = new Gson();
            String payload = response.getPayLoad();
            Properties serverProps = gson.fromJson(payload, Properties.class);
            props.putAll(serverProps);
            logger.debug("ConfigReader: Properties from the config server loaded successfully.");
        } else {
            logger.error("ConfigReader: Error while getting properties from the config server. Using local properties only.");
        }

        Config.getInstance().setProps(props);
    }

    public void readLocalProperties() throws MicroNetException, IOException {

        // temporary map to load properties
        Properties properties = new Properties();
        
        // Loading the properties file from the classpath
        InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("application.properties");
        if (input == null) {
            logger.debug("Unable to find application.properties");
        } else {
            // Load the properties file
            properties.load(input);
        }

        // Override file properties with environment variable property if a conflict occurs
        if (System.getenv("CONFIG_SOURCE_URL") != null) {
            properties.setProperty("config.source.url", System.getenv("CONFIG_SOURCE_URL"));
        }

        String configSourceUrl = properties.getProperty("config.source.url");
        
        URL url=null;
        try {
            url = new URL(configSourceUrl);
            input = url.openStream();
            Properties p = new Properties();
            p.load(input);
            properties.putAll(p);
        } catch (MalformedURLException e) {
            logger.error("ConfigReader: Error while creating the URL object from config source: " + e.getMessage() + ". May be the config source is not declared or not well formed. Using properties in the application.properties file ...");
        }
        input.close();

        
        Properties p = ConfigManager.readCmdLineVariables(System.getProperty("sun.java.command").split(" "));

        if (p != null && p.size() > 0) {
            logger.debug("Adding properties from command line: " + p);
            properties.putAll(p); // Override file properties with command line properties if a conflict occurs
        }

        // Override properties with environment variables if a conflict occurs
        properties.forEach((key, value) -> {
            logger.debug("ConfigReader.readProperties: Property read from conf file or cmd line " + key + " = " + value);
            String envName = ((String) key).replaceAll("\\.", "_").toUpperCase();
            if (System.getenv(envName) != null) {
                logger.debug("Overriding property " + key + " with environment variable " + envName + " which vallue is " + System.getenv(envName));
                value = System.getenv(envName);
            }

            props.setProperty((String) key, (String) value);
        });

        Config.getInstance().setProps(props);
    }

}

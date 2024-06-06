package ma.micronet.commons;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesReader {

    private static Logger logger = LoggerFactory.getLogger(PropertiesReader.class);
    public static Properties properties = new Properties();

    public static Properties getProperties() {
        return properties;
    }

    public static void readLocalProperties() throws MicroNetException, IOException {
        InputStream input = PropertiesReader.class.getClassLoader().getResourceAsStream("application.properties");
        if (input == null) {
            logger.debug("PropertiesReader.readLocalProperties: Unable to find application.properties");
            throw new MicroNetException("Unable to find application.properties file in the classpath. Unable to read local properties");
        } else {
            try {
                properties.load(input);
            } catch (IOException e) {
                logger.error("PropertiesReader.readLocalProperties: Error while reading application.properties file");
                e.printStackTrace();
                input.close();
                throw new MicroNetException("readLocalProperties: Error while reading application.properties file", e);
            }
        }

        String configSourceUrl = properties.getProperty("config.source.url");
        
        URL url=null;
        try {
            url = new URL(configSourceUrl);
            input.close();
            input = url.openStream();
            Properties p = new Properties();
            p.load(input);
            properties.putAll(p);
        } catch (MalformedURLException e) {
            logger.error("ConfigReader: Error while creating the URL object from config source: " + e.getMessage() + ". May be the config source is not declared or not well formed. Using properties in the application.properties file ...");
        }
        input.close();
        
    }

    public static void readCmdLineVariables(String[] args) {
        
        if (args == null) {
            logger.debug("PropertiesReader.readCmdLineVariables: null args");
            return;
        }

        for (int i=0; i<args.length; i++) {
            if ("-c".equals(args[i]) && i+1 < args.length) {
                String[] arg = args[i+1].split("=");
                if (arg.length !=2) {
                    logger.error("ConfigApplication: Invalid property declaration: " + args[i+1] + ". Use -c property=value");
                    System.exit(1);
                }
                properties.setProperty(arg[0], arg[1]);
            }
        }
    }

    public static void readEnvironmentVariables() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {            
            String key = envName.toLowerCase().replace("_", ".");
            properties.setProperty(key, env.get(envName));            
        }
    }

    public static void readProperties() throws MicroNetException, IOException {
        readLocalProperties();
        readCmdLineVariables(System.getProperty("sun.java.command").split(" "));
        readEnvironmentVariables();
    }

}

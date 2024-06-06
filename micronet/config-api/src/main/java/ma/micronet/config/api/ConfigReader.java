package ma.micronet.config.api;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.PropertiesReader;

public class ConfigReader {

    // singleton

    private Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private static ConfigReader instance = null;
    private Adressable adressable;

    private ConfigReader(Adressable adressable) {
        this.adressable = adressable;
    }

    public static ConfigReader getInstance(Adressable adressable) {
        if (instance == null) {
            instance = new ConfigReader(adressable);
        }
        return instance;
    }
    
    public void readProperties() throws MicroNetException, IOException {

        PropertiesReader.readProperties();
        Config.getInstance().setProps(PropertiesReader.getProperties());
        readServerProperties();
        Config.getInstance().setProps(PropertiesReader.getProperties());
    }

    public void readServerProperties() throws MicroNetException, IOException {

        boolean configServerEnabled = Boolean.parseBoolean(Config.getInstance().getProperty("config.server.enabled") == null ? "false" : Config.getInstance().getProperty("config.server.enabled"));

        if (!configServerEnabled) {
            // don't continue if config server not enabled
            logger.debug("ConfigReader: Config server is not enabled. Using local properties only.");
            return;
        }

        // Finally, get properties from config server (the most prioritary source)
        Configuration configuration = new Configuration();
        Message request = new Message();
        
        request.setCommand(Message.GET_CONFIG_COMMAND);
        request.setSenderAdressable(adressable);
        request.setDirection(Message.REQUEST);
        request.setTargetAdressable(configuration);
        request.setSenderType(configuration.getType());

        ConfigurationConnection configurationConnection = configuration.createConnection();

        Boolean isConfigServerReached = false;
        int authorizedAttempts = 5;
        Message response = null;

        Long reconnectInterval=(Config.getInstance().getProperty("config.reconnect.interval") != null ? Long.parseLong((String)Config.getInstance().getProperty("config.reconnect.interval")) : 10L);

        while(!isConfigServerReached && authorizedAttempts > 0) {
            try {
                configurationConnection.connect();
                response = configurationConnection.sendSync(request);
                isConfigServerReached = true;
            } catch (MicroNetException e) {
                authorizedAttempts--;
                logger.error("ConfigReader: Error while connecting to the config server: " + e.getMessage() + ". Retrying...");
                try {
                    Thread.sleep(reconnectInterval*1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (authorizedAttempts == 0) {
            logger.debug("ConfigReader: number of attempts exhausted while connecting to the config server. Using local properties only.");
            return;
        }

        if (response != null && response.getResponseCode().equals(Message.OK)) {
            Gson gson = new Gson();
            String payload = response.getPayLoad();
            Properties serverProps = gson.fromJson(payload, Properties.class);
            PropertiesReader.getProperties().putAll(serverProps);

            logger.debug("ConfigReader: Properties from the config server loaded successfully.");
        } else {
            logger.error("ConfigReader: Error while getting properties from the config server. Using local properties only.");
        }
    }
}

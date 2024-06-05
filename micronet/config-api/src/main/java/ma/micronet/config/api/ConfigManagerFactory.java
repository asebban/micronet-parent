package ma.micronet.config.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.PortGenerator;

public class ConfigManagerFactory {

    private static Logger logger = LoggerFactory.getLogger(ConfigManagerFactory.class);
    
    public static ConfigManager createConfigManager() throws MicroNetException, IOException {
        ConfigManager configManager = new ConfigManager();
        configManager.setPort(PortGenerator.getInstance().generatePort());
        configManager.setPingPort(PortGenerator.getInstance().generatePort());

        try {        
            InetAddress inetAddress = InetAddress.getLocalHost();
            configManager.setHost(inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            logger.error("Error getting the host IP: " + e.getMessage());
            throw new MicroNetException("Error getting the host IP: " + e.getMessage(), e);
        }
        
        return configManager;

    }
}

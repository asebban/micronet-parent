package ma.micronet.config.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Config {
    private static Config instance;
    private Properties props = new Properties();

    private Config() {
    }

    public String getProperty(String key) {
        String property = props.getProperty(key);
        if (property == null) {
            key = key.replaceAll("\\.", "_").toUpperCase();
            // get the value of the environment variable with the given name, or null if no such variable exists
            property = System.getenv(key);
        }
        return property;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }
    
    public Properties getProps() {
        return props;
    }


    public void setProps(Properties props) {
        this.props = props;
    }


    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getCurrentHost() throws UnknownHostException {
            
            // Get the InetAddress object for the local host
            InetAddress inetAddress = InetAddress.getLocalHost();
            
            // Retrieve the IP address
            String ipAddress = inetAddress.getHostAddress();
            return ipAddress;
    }

}

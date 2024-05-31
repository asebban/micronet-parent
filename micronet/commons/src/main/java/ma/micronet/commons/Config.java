package ma.micronet.commons;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class Config {
    private int port;
    private static Config instance;
    private Properties props = new Properties();

    private Config() {
    }

    public String getProperty(String key) {
        String property = props.getProperty(key);
        if (property == null) {
            key = key.toUpperCase();
            key = key.replace(".", "_");
            // get the value of the environment variable with the given name, or null if no such variable exists
            property = System.getenv(key);
        }
        return property;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCurrentHost() throws UnknownHostException {
            
            // Get the InetAddress object for the local host
            InetAddress inetAddress = InetAddress.getLocalHost();
            
            // Retrieve the IP address
            String ipAddress = inetAddress.getHostAddress();
            return ipAddress;
    }

}

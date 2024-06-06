package ma.micronet.config.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.IListener;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.networking.PingListener;
import ma.micronet.config.api.Config;
import ma.micronet.config.api.Configuration;
import ma.micronet.config.api.ConfigurationFactory;
import ma.micronet.registry.api.Registry;
import sun.misc.SignalHandler;
import sun.misc.Signal;
import java.net.URL;

public class ConfigurationListener implements IListener {

    private Configuration configuration;
    private Logger logger = LoggerFactory.getLogger(ConfigurationListener.class);
    private Properties cmdLineProps = new Properties();
    private Properties properties;


    @Override
    public void start() throws MicroNetException {
        // load properties (priority to command line properties if they exist)
        try {
            properties = loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            this.configuration = ConfigurationFactory.createConfigManager();
            logger.debug("ConfigServerListener: Subscribing the config server " + configuration.getId() + " to the registry");
            Registry.subscribe(this.configuration);
            logger.debug("ConfigServerListener: ConfigServer " + configuration.getId() + "subscribed to the registry");
        } catch (MicroNetException | IOException e) {
            e.printStackTrace();
        }

        ////////////////////////////////////
        // Handle SIGINT interruption signal
        ////////////////////////////////////
        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                logger.debug("ConfigListener: Signal " + signal + " reçu. Arrêt en cours...");
                try {
                    Registry.unsubscribe(configuration);
                } catch (MicroNetException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        };

        // Associate signal SIGINT to the handler
        Signal.handle(new Signal("INT"), handler);

        new Thread(new PingListener(configuration)).start();

        try (ServerSocket serverSocket = new ServerSocket(configuration.getPort())) {
            logger.info("ConfigServerListener: Server is listening on port " + configuration.getPort());

            while (true) {
                Socket socket = serverSocket.accept();
                logger.debug("ConfigServerListener: New client connected");
                new Thread(new ConfigurationHandler(socket, configuration, properties)).start();
            }
        } catch (IOException e) {
            logger.error("ConfigServerListener: Error while creating the server socket. Exiting..." + e.getMessage());
            e.printStackTrace();
        }

    }

    public void setCmdLineProps(Properties props) {
        this.cmdLineProps = props;
    }

    private Properties loadProperties() throws IOException {

        Properties properties = new Properties();
        
        // Load application.properties file in the classpath
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        Properties p = new Properties();
        p.load(input); // Load it as a properties file

        // Override file properties with environment variable property if a conflict occurs
        if (System.getenv("CONFIG_SOURCE_URL") != null) {
            logger.debug("ConfigListener: Overriding config.source.url with environment variable CONFIG_SOURCE_URL");
            p.setProperty("config.source.url", System.getenv("CONFIG_SOURCE_URL"));
        }

        String configSourceUrl = p.getProperty("config.source.url");
        
        URL url=null;

        try {
            url = new URL(configSourceUrl);
            input.close();
            input = url.openStream();
            properties.load(input);
            input.close();    
        } catch (MalformedURLException e) {
            logger.error("ConfigListener: Error while creating the URL object from config source: " + e.getMessage() + ". May be the config source is not declared or not well formed. Using properties from application.properties file...");
        }

        properties.putAll(p);
        
        for (String arg : ConfigurationServer.cmdLineArgs) {
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                cmdLineProps.setProperty(parts[0], parts[1]);
            }
        }

        if (!cmdLineProps.isEmpty())
            properties.putAll(cmdLineProps);

        Config.getInstance().setProps(properties);

        return properties;
    }
}

package ma.micronet.config.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;
import java.util.Properties;

public class ConfigManager extends Adressable{


    private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    public ConfigManager() {
        super();
        setType(Message.CONFIG_TYPE);
        setId(UIDGenerator.generateUID());
    }

    public ConfigManagerConnection createConnection() throws MicroNetException, IOException {
        ConfigManagerConnection connection = new ConfigManagerConnection(this);
        return connection;
    }

    public static Message createGetConfigMessage() {
        Message message = new Message();
        message.setCommand(Message.GET_CONFIG_COMMAND);
        message.setDirection(Message.REQUEST);
        return message;
    }

    public static Properties readCmdLineVariables(String[] args) {
        Properties props = new Properties();
        for (int i=0; i<args.length; i++) {
            if ("-c".equals(args[i]) && i+1 < args.length) {
                String[] env = args[i+1].split("=");
                if (env.length !=2) {
                    logger.error("ConfigApplication: Invalid property declaration: " + args[i+1] + ". Use -c property=value");
                    System.exit(1);
                }
                props.setProperty(env[0], env[1]);
            }
        }
        return props;
    }
}

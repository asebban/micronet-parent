package ma.micronet.gateway.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.PortGenerator;

public class GatewayFactory {

    private static Logger logger = LoggerFactory.getLogger(GatewayFactory.class);

    public static Gateway createGateway() throws MicroNetException, IOException {
        Gateway gateway = new Gateway();
        gateway.setPort(PortGenerator.getInstance().generatePort());
        gateway.setPingPort(PortGenerator.getInstance().generatePort());

        try {        
            InetAddress inetAddress = InetAddress.getLocalHost();
            gateway.setHost(inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            logger.error("Error getting the host IP: " + e.getMessage());
            throw new MicroNetException("Error getting the host IP: " + e.getMessage(), e);
        }
        
        return gateway;
    }

}

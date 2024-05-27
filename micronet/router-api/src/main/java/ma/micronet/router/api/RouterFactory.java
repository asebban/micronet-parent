package ma.micronet.router.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.PortGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterFactory {

    private static Logger logger = LoggerFactory.getLogger(RouterFactory.class);

    public static Router createRouter() throws MicroNetException, IOException {
        Router router = new Router();
        router.setPort(PortGenerator.getInstance().generatePort());
        router.setPingPort(PortGenerator.getInstance().generatePort());
        router.setType(Router.ROUTER_TYPE);
        logger.debug("Router Factory: Created a new router with port: " + router.getPort());

        try {        
            InetAddress inetAddress = InetAddress.getLocalHost();
            router.setHost(inetAddress.getHostAddress());
            logger.debug("Router Factory: Set the host IP for router: " + router.getHost());
        } catch (UnknownHostException e) {
            logger.error("Router Factory: Error getting the host IP for router: " + e.getMessage());
            throw new MicroNetException("Error getting the host IP: " + e.getMessage(), e);
        }
        
        return router;
    }
}

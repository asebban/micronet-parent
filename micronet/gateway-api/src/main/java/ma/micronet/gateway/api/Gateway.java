package ma.micronet.gateway.api;

import java.io.IOException;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;

public class Gateway extends Adressable {

    public static final String GATEWAY_TYPE = "GATEWAY";

    public Gateway() {
        super();
        setType(GATEWAY_TYPE);
        setId(UIDGenerator.generateUID());
    }
    
    public GatewayConnection createConnection() throws MicroNetException, IOException {
        GatewayConnection connection = new GatewayConnection(this);
        return connection;
    }
}

package ma.micronet.gateway.api;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.UIDGenerator;

public class Gateway extends Adressable {

    public static final String GATEWAY_TYPE = "GATEWAY";

    public Gateway() {
        super();
        setType(GATEWAY_TYPE);
        setId(UIDGenerator.generateUID());
    }    
}

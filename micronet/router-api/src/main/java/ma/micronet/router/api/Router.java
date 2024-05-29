package ma.micronet.router.api;

import java.io.IOException;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.MicroNetException;
import ma.micronet.commons.UIDGenerator;

public class Router extends Adressable {

    public static final String ROUTER_TYPE = "ROUTER";

    public Router() {
        super();
        setType(ROUTER_TYPE);
        setId(UIDGenerator.generateUID());
    }

    public RouterConnection createConnection() throws MicroNetException, IOException {
        RouterConnection connection = new RouterConnection(this);
        return connection;
    }
}

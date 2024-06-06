package ma.micronet.router.server;

import java.io.IOException;

import ma.micronet.commons.MicroNetException;

public class RouterServer {

    public static void main(String[] args) throws MicroNetException, IOException {
        
        RouterListener routerListener = new RouterListener();
        try {
            routerListener.start();
        } catch (MicroNetException e) {
            e.printStackTrace();
            System.exit(1);
        }   
    }
}

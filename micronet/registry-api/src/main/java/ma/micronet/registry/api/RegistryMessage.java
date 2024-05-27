package ma.micronet.registry.api;

import ma.micronet.commons.Message;

public class RegistryMessage extends Message {
    private String requestedMapType; // Router, Worker, etc.

    public String getRequestedMapType() {
        return requestedMapType;
    }

    public void setRequestedMapType(String requestedMapType) {
        this.requestedMapType = requestedMapType;
    }
}

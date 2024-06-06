package ma.micronet.commons;

public class Adressable {
    private String type; // Router, Worker, etc.
    private String path;
    private String host;
    private Integer port;
    private Integer pingPort;
    private String id;
    private Long timestamp;

    public Adressable(String host, Integer port, String type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public Adressable(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public Adressable() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPingPort() {
        return pingPort;
    }

    public void setPingPort(Integer pingPort) {
        this.pingPort = pingPort;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String namespace) {
        this.path = namespace;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Adressable [id=" + id + ", type=" + type + ", path=" + path + ", port=" + port + ", host=" + host + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }

        if (obj instanceof Adressable) {
            Adressable other = (Adressable) obj;
            if ((getHost() == null && other.getHost() != null) || (getHost() != null && other.getHost() == null)) {
                return false;
            }

            if ((getPort() == 0 && other.getPort() != 0) || (getPort() != 0 && other.getPort() == 0)) {
                return false;
            }

            if (getHost() == null) {
                return true;
            }

            return this.getHost().equals(other.getHost()) && this.getPort().equals(other.getPort());
        }
        else {
            return false;
        }
    }
}

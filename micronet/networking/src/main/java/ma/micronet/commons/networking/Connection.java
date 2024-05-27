package ma.micronet.commons.networking;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public abstract class Connection {
    private String ip;
    private int port;
    private MicroNetSocket socket;

    public Connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Connection() {
    }

    
    public MicroNetSocket getSocket() {
        return socket;
    }

    public void setSocket(MicroNetSocket socket) {
        this.socket = socket;
    }

    public abstract void connect() throws MicroNetException;
 
    public void send(Message message) throws MicroNetException {
        Gson gson = new Gson();
        String json = gson.toJson(message);
        try {
            this.socket.send(json);
        } catch (MicroNetException e) {
            e.printStackTrace();
            throw new MicroNetException("Error sending message", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MicroNetException("Error sending message", e);
        }
    }


    public Message sendSync(Message message) throws MicroNetException {
        Gson gson = new Gson();
        String json = gson.toJson(message);
        try {
            this.socket.send(json);
        } catch (MicroNetException e) {
            e.printStackTrace();
            throw new MicroNetException("Error sending message", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MicroNetException("Error sending message", e);
        }

        Message m = receive();
        return m;
    }

    public Message receive() throws MicroNetException {

        String json;
        try {
            json = this.socket.recv(2024);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MicroNetException("Network error receiving message", e);
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(json, Message.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            throw new MicroNetException("Error parsing received JSON", e);
        }
    }


    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}

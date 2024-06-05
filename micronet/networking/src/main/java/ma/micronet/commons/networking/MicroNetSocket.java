package ma.micronet.commons.networking;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ma.micronet.commons.Adressable;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MicroNetSocket {

    public static Integer DEFAULT_REST_FREQUENCY = 3;

    private Socket socket;
    private String type;
    public static final String AGENT_TYPE = "AGENT";
    private Logger logger = LoggerFactory.getLogger(MicroNetSocket.class.getName());
    private Adressable adressable;

    public MicroNetSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    public MicroNetSocket(Adressable adressable) throws MicroNetException, IOException {
        this.adressable = adressable;
    }

    public void connect(String host, int port) throws IOException {
        if (this.socket == null || this.socket.isClosed()) {
            throw new IOException("Socket is null or closed");
        }

        this.socket = new Socket(host, port);
    }
    
    public void connect() throws MicroNetException, IOException {

        if (this.adressable != null && MicroNetMapRenewer.getInstance(this.adressable).getMap() != null && MicroNetMapRenewer.getInstance(this.adressable).getMap().size() > 0) {
            // the map has already been retrieved
            return;
        }

        if (this.adressable == null) {
            throw new MicroNetException("MicroNetSocket.connect: adressable was not provided");
        }

        // Here type is set
        MicroNetMapRenewer.getInstance(this.adressable).renewMap();
    }

    public void send(String data) throws MicroNetException, IOException{

        logger.debug("MicroNetSocket.send: Sending data " + data);

        if (data == null) {
            logger.error("MicroNetSocket.send: Data to send is null");
            throw new MicroNetException("Data to send is null");
        }

        logger.debug("MicroNetSocket.send: data to send is : " + data);

        if (this.adressable == null) {
            logger.debug("MicroNetSocket.send: Type of the destination is not set, sending data to the current socket");
            byte[] bytes = data.getBytes();
            
            if (this.socket == null) {
                logger.error("MicroNetSocket.send: Socket and adressable are null");
                throw new MicroNetException("Socket and adressable are null");
            }

            OutputStream outputStream = this.socket.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            return;
        }

        // Here type is set
        logger.debug("MicroNetSocket.send: Sender adressable is " + this.adressable);

        // type is set, we need to send the data to the appropriate destination
        Map<String, List<Adressable>> map = MicroNetMapRenewer.getInstance(this.adressable).getMap();

        if (map == null || map.size() == 0) {
            logger.error("MicroNetSocket.send: You must connect first to registry to get the map of adressables");
            throw new MicroNetException("You must connect first to registry to get the map of adressables");
        }

        logger.debug("MicroNetSocket.send: Map of adressables is available");

        Message m = Message.jsonToMessage(data);

        Adressable adressable = loadBalanceAdressables(m);

        logger.debug("MicroNetSocket.send: Sending data to " + adressable);
        OutputStream outputStream = this.socket.getOutputStream();
        byte[] bytes = data.getBytes();
        outputStream.write(bytes);
        outputStream.flush();
        logger.debug("MicroNetSocket.send: Data sent to " + adressable);
    }

    private Adressable loadBalanceAdressables(Message m) throws MicroNetException, IOException {

        List<Adressable> adressables = getTargetAddressableList(m); // get the list of target adressables for this message

        if (adressables == null || adressables.size() == 0) {
            logger.error("MicroNetSocket.send: No adressable found for type " + this.adressable.getType() + (m.getPath() != null && !m.getPath().isEmpty() ? " and path " + m.getPath() : ""));
            throw new MicroNetException("No adressable found for type " + this.adressable.getType() + " and path " + m.getPath());
        }

        int adressablesSize = adressables.size();
        // generate a random number between 0 and adressablesSize-1
        int idx = generateRandomNumber(adressablesSize);

        logger.debug("MicroNetSocket.send: List of "+ adressablesSize + " adressables found for type " + m.getTargetType());

        Adressable adressable = getNextAdressable(adressables, idx);

        boolean isAdressableReachable = false;

        while (!isAdressableReachable && adressablesSize > 0) {
            try {
                if (adressable == null) {
                    logger.error("MicroNetSocket.send: Could not find a reachable adressable");
                    throw new MicroNetException("MicroNetSocket.send: Could not find a reachable adressable");
                }
                logger.debug("MicroNetSocket.send: Trying to reach " + adressable);
                this.socket = new Socket(adressable.getHost(), adressable.getPort().intValue());
                logger.debug("MicroNetSocket.send: Reached " + adressable);
                isAdressableReachable = true;
            } catch (UnknownHostException e) {
                logger.debug(adressable + " is not reachable because of an unknown host exception");
                adressablesSize--;
                idx = (idx+1)%adressables.size();
                adressable = getNextAdressable(adressables, idx);
            } catch (IOException e) {
                logger.debug(adressable + " is not reachable because of an IO exception");
                adressablesSize--;
                idx = (idx+1)%adressables.size();
                adressable = getNextAdressable(adressables, idx);
            }
        }
        
        if (adressablesSize <= 0) {
            logger.error("MicroNetSocket.send: No reachable adressable found for type " + this.type);
            throw new MicroNetException("No reachable adressable found for type " + this.type);
        }

        return adressable;

    }

    public static int generateRandomNumber(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }
    
    public String recv(int bufsize) throws IOException {
        // Ajoutez ici toute fonctionnalité personnalisée nécessaire avant la réception
        byte[] buffer = new byte[bufsize];
        InputStream inputStream = this.socket.getInputStream();
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == 0 || bytesRead == -1) {
            throw new IOException("End of stream reached");
        }
        byte[] receivedData = new byte[bytesRead];
        System.arraycopy(buffer, 0, receivedData, 0, bytesRead);
        return new String(receivedData, StandardCharsets.UTF_8);
    }

    public void close() throws IOException {
        this.socket.close();
    }

    private Adressable getNextAdressable(List<Adressable> adressables, int idx) {

        if (adressables == null || adressables.size() == 0) {
            return null;
        }
        if (idx >= adressables.size() || idx < 0) {
            return null;
        }
        return adressables.get(idx);
    }

    List<Adressable> getTargetAddressableList(Message message) throws MicroNetException, IOException {

        if (message == null) {
            throw new MicroNetException("MicroNetSocket: Target agent list undefinable because the message is null");
        }

        if (this.adressable == null) {
            // should never be executed normally
            throw new MicroNetException("MicroNetSocket: Target list undefinable because the message Receiver type is unknown");
        }

        if (!AGENT_TYPE.equalsIgnoreCase(this.adressable.getType())) {
            logger.debug("MicroNetSocket.getTargetAddressableList: Getting the adressables list for type " + this.adressable.getType());
            List<Adressable> adressables = MicroNetMapRenewer.getInstance(this.adressable).getMap().get(this.adressable.getType());
            if (adressables == null || adressables.size() == 0) {
                    MicroNetMapRenewer.getInstance(this.adressable).renewMap();
            }
            return MicroNetMapRenewer.getInstance(this.adressable).getMap().get(this.adressable.getType());
        }

        Map<String, List<Adressable>> pathMap = MicroNetMapRenewer.getInstance(this.adressable).getAgentsMap();
        if (pathMap == null || pathMap.isEmpty()) {
            throw new MicroNetException("MicroNetSocket: No target agents available");
        }

        List<String> pathList = new ArrayList<>();

        for (String key : pathMap.keySet()) {
            String messagePath = message.getPath();

            if (messagePath == null) throw new MicroNetException("MicroNetSocket.getTargetAddressableList: No path in the message");
            
            if (!key.startsWith("/")) key = "/" + key;
            if (!messagePath.startsWith("/")) messagePath = "/" + messagePath;

            if (messagePath.startsWith(key)) {
                pathList.add(key);
            }
        }

        if (pathList.isEmpty()) {
            throw new MicroNetException("Router: No agents serving the requested path");
        }

        // Sort the pathList in descending order
        Collections.sort(pathList, Collections.reverseOrder());
        String path = pathList.get(0);
        return MicroNetMapRenewer.getInstance(this.adressable).getAgentsMap().get(path);

    }

}

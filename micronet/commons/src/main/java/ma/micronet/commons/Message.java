package ma.micronet.commons;

import java.util.Map;
import java.util.HashMap;

public class Message {

    public static final String REQUEST = "REQUEST";
    public static final String RESPONSE = "RESPONSE";
    public static final String MESSAGE_TYPE = "MESSAGE";
    public static final String ADD = "ADD";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String GET = "GET";
    public static final String OK = "OK";
    public static final String NOK = "NOK";
    public static final String ERROR = "ERROR";

    private String path;
    private String senderId;
    private String senderType;
    private Adressable senderAdressable;
    private Adressable targetAdressable;
    private String targetType;
    private String messageId;
    private String responseCode;
    private String direction; // REQUEST OR RESPONSE
    private String command;
    private Map<String, Object> parameters = new HashMap<String, Object>();
    private String payLoad;

    public Adressable getSenderAdressable() {
        return senderAdressable;
    }

    public void setSenderAdressable(Adressable senderAdressable) {
        this.senderAdressable = senderAdressable;
    }

    public Adressable getTargetAdressable() {
        return targetAdressable;
    }

    public void setTargetAdressable(Adressable targetAdressable) {
        this.targetAdressable = targetAdressable;
    }
    
    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getPath() {
        return path;
    }
    public void setPath(String namespace) {
        this.path = namespace;
    }
    public String getMessageId() {
        return messageId;
    }
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String receiverType) {
        this.targetType = receiverType;
    }
    
    public String getCommand() {
        return command;
    }
    public void setCommand(String messageType) {
        this.command = messageType;
    }
    public String getPayLoad() {
        return payLoad;
    }
    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public Map<String, Object> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
    public String getSenderType() {
        return senderType;
    }
    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
}

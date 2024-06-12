package ma.micronet.example;

import ma.micronet.agent.api.IProcessingUnit;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public class MyAgentProcessingUnit implements IProcessingUnit {

    @Override
    public Message execute(Message message) throws MicroNetException {
        Message response = new Message();
        response.setDirection(Message.RESPONSE);
        String payload = "This is my processing unit response";
        if (message.getParameters() != null) {
            // loop on the map keys and values
            for (String key : message.getParameters().keySet()) {
                payload += " " + key + " = " + message.getParameters().get(key);
            }
        }
        payload += " verb = " + message.getCommand();
        response.setPayLoad(payload);
        response.setResponseCode(Message.OK);
        return response;
    }

    @Override
    public String registerRelativePath() {
        return "/get/{id}";
    }

    @Override
    public Message add(Message message) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public Message delete(Message message) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public Message get(Message message) throws MicroNetException {
        Message response = Message.copy(message);
        response.setDirection(Message.RESPONSE);
        String payload = "This is my GET processing unit response";
        if (message.getParameters() != null) {
            // loop on the map keys and values
            for (String key : message.getParameters().keySet()) {
                payload += " " + key + " = " + message.getParameters().get(key);
            }
        }
        payload += " verb = " + message.getCommand();
        response.setPayLoad(payload);
        response.setResponseCode(Message.OK);
        return response;
    }

    @Override
    public Message update(Message message) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}

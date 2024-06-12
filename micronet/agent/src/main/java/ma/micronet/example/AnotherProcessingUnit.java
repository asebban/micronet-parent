package ma.micronet.example;

import ma.micronet.agent.api.IProcessingUnit;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import java.util.Map;

public class AnotherProcessingUnit implements IProcessingUnit {

    @Override
    public Message execute(Message request) throws MicroNetException {
        Message response = Message.copy(request);
        response.setDirection(Message.RESPONSE);
        response.setResponseCode(Message.OK);
        String payload = "This is my another processing unit response -> ";

        if (request.getParameters() != null && request.getParameters().size() > 0) {
            for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                payload += entry.getKey() + " = " + entry.getValue();
            }
        }
        response.setPayLoad(payload);
        return response;
    }

    @Override
    public String registerRelativePath() {
        return "/another/{id}";
    }

    @Override
    public Message add(Message arg0) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public Message delete(Message arg0) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public Message get(Message arg0) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public Message update(Message arg0) throws MicroNetException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}

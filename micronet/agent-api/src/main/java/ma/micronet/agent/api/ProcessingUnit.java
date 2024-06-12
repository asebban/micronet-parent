package ma.micronet.agent.api;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;
import ma.micronet.router.api.Router;
import ma.micronet.router.api.RouterConnection;

import java.io.IOException;

public abstract class ProcessingUnit implements IProcessingUnit {

    private Agent agent;

    public abstract Message execute(Message message) throws MicroNetException;

    public abstract String registerRelativePath();
    public abstract Message add(Message message) throws MicroNetException;
    public abstract Message update(Message message) throws MicroNetException;
    public abstract Message delete(Message message) throws MicroNetException;
    public abstract Message get(Message message) throws MicroNetException;

    public Message callAgent(Message request, String command, String path, String payload ) throws MicroNetException, IOException {

        Message requestToAgent = Message.copy(request);
        Router router = new Router();
        RouterConnection routerConnection = router.createConnection();
        
        requestToAgent.setCommand(command);
        requestToAgent.setDirection(Message.REQUEST);
        requestToAgent.setSenderAdressable(this.agent);
        requestToAgent.setSenderType(Message.AGENT_TYPE);
        requestToAgent.setTargetAdressable(this.agent);
        requestToAgent.setTargetType(Message.AGENT_TYPE);

        requestToAgent.setPath(path);
        requestToAgent.setPayLoad(payload);

        routerConnection.connect();
        Message response = routerConnection.sendSync(requestToAgent);
        return response;
    }

}

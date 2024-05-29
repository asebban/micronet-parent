package ma.micronet.agent.api;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public interface IProcessingUnit {
    public Message execute(Message message) throws MicroNetException;
    public String registerRelativePath();
}

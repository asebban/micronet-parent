package ma.micronet.agent.api;

import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

public interface IProcessingUnit {
    public Message execute(Message message) throws MicroNetException;
    public String registerRelativePath();
    public Message add(Message message) throws MicroNetException;
    public Message update(Message message) throws MicroNetException;
    public Message delete(Message message) throws MicroNetException;
    public Message get(Message message) throws MicroNetException;
}

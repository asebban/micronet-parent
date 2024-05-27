package ma.micronet.agent.server.exceptions;

import ma.micronet.commons.MicroNetException;

public class MicroNetAgentException extends MicroNetException {
    
        private static final long serialVersionUID = 1L;
    
        public MicroNetAgentException(String message) {
            super(message);
        }
    
        public MicroNetAgentException(String message, Throwable cause) {
            super(message, cause);
        }
}

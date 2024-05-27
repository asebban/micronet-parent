package ma.micronet.commons;

public class MicroNetException extends Exception {
    
        private static final long serialVersionUID = 1L;
    
        public MicroNetException(String message) {
            super(message);
        }
    
        public MicroNetException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public MicroNetException(Throwable cause) {
            super(cause);
        }

        public MicroNetException() {
            super("An error occurred in the MicroNet system.");
        }
}

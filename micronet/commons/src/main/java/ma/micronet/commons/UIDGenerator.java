package ma.micronet.commons;

public class UIDGenerator {
    public static String generateUID() {
        return java.util.UUID.randomUUID().toString();
    }
}

package ma.micronet.commons;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader; // Import the BufferedReader class
import java.io.FileReader; // Import the FileReader class

public class PortGenerator {

    private int lastPort;
    // Je veux que cette classe soit un singleton
    private static PortGenerator instance;

    private PortGenerator(int port) {
        this.lastPort = port;
    }

    public static PortGenerator getInstance() {
        if (instance == null) {
            instance = new PortGenerator(10000);
        }
        return instance;
    }

    public synchronized int generatePort() {

        try {
            lastPort = read();
        } catch (IOException e) {
            lastPort = 10000; // first time to be used
        }
        
        lastPort++;
        try {
            write(lastPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        
        return lastPort;
        
    }

    private void write(int number) throws IOException {
        FileWriter writer = new FileWriter("C:\\Intel\\portnumber.txt");
        writer.write(Integer.toString(number));
        writer.flush();
        writer.close();
    }

    private int read() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Intel\\portnumber.txt"));
        String line = reader.readLine();
        reader.close();
        return Integer.parseInt(line);
    }

}

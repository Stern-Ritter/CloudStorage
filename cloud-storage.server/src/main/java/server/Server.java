package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static void main(String[] args) {
        new CloudStorageServer().start();
    }
}

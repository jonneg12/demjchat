package org.example;

public class ServerStarter {
    public static void main(String[] args) {
        int port = 9997;

        Server server = new Server(port);
        server.startServer();
    }
}

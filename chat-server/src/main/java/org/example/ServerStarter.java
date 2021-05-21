package org.example;

import java.util.Optional;

public class ServerStarter {
    private final static String PROPERTIES_FILE_NAME = "server.properties";

    public static void main(String[] args) {
        final Optional<ServerConfig> configOptional = new ServerConfigLoader(PROPERTIES_FILE_NAME).getConfig();
        configOptional.ifPresent(config -> new Server(config).startServer());
    }
}

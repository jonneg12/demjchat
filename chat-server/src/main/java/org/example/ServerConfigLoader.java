package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class ServerConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfigLoader.class);

    private final String propertiesFileName;

    public ServerConfigLoader(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public Optional<ServerConfig> getConfig() {
        Properties properties = loadProperties();
        ServerConfig config;
        try {
            config = ServerConfig.builder()
                    .port(checkPort(Integer.parseInt(properties.getProperty("PORT"))))
                    .build();
            logger.info("Config loaded from {} : {}", propertiesFileName, config);
            return Optional.of(config);
        } catch (NumberFormatException | ParameterIsNorValidException e) {
            logger.error("Not valid one or more parameters", e);
            return Optional.empty();
        }
    }


    private int checkPort(int value) {
        if (value > 0 && value < 65535) {
            return value;
        } else {
            throw new ParameterIsNorValidException("Wrong port value (current value: " + value + ")");
        }
    }


    public Properties loadProperties() {
        Properties properties = new Properties();
        try (final InputStream is = ServerConfigLoader.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            properties.load(is);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return properties;
    }
}


package org.example;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServerConfig {
    private int port;
}

package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server implements TCPConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final String WRONG_MESSAGE_FORMAT = "Message you sent is in a wrong format. You will be disconnected";
    private static final String BOT_NAME = "SERVER";

    private final int port;
    private final Map<TCPConnection, String> mapConnectionUserName;
    private final List<String> userNames;

    public Server(int port) {
        this.mapConnectionUserName = new HashMap<>();
        this.userNames = new ArrayList<>();
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("SERVER RUNNING on port [{}]", serverSocket.getLocalPort());
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void connectionReady(TCPConnection connection) {
        logger.info("SERVER: CONNECTION READY");

        //test block
//        mapConnectionUserName.put(connection, "port:" + connection.getSocket().getPort());
//        logger.info("MAP changed: {}", mapConnectionUserName);
//        Message message = Message.builder()
//                .type(MessageType.NOTIFICATION)
//                .name(BOT_NAME)
//                .text("New user connected to chat. Let's welcome " + mapConnectionUserName.get(connection))
//                .time(connection.getTime())
//                .build();
//        sendMessageToAllConnections(message);
    }

    @Override
    public synchronized void connectionReceiveMessage(TCPConnection connection, Message message) {
        logger.info("SERVER: RECEIVE MESSAGE ({})", message);
        switch (message.getType()) {
            case TEXT:
                sendMessageToAllConnections(message);
                break;
            case NAME:
                String name = message.getName();
                if (!checkNameInList(name)) {
                    mapConnectionUserName.put(connection, name);
                    logger.info("MAP changed: {}", mapConnectionUserName);
                    sendMessageToAllConnections(Message.builder()
                            .type(MessageType.NOTIFICATION)
                            .name(BOT_NAME)
                            .text("New user connected to chat. Let's welcome " + name)
                            .time(connection.getTime())
                            .build());
                    sendMessageToAllConnections(Message.builder()
                            .type(MessageType.USERS)
                            .name(BOT_NAME)
                            .text(getUsersLine())
                            .build());
                } else {
                    sendMessageToOneConnection(Message.builder()
                                    .type(MessageType.NOTIFICATION)
                                    .name(BOT_NAME)
                                    .text("User with name " + name + " is already in chat")
                                    .time(connection.getTime())
                                    .build()
                            , connection);
                    connection.disconnect();
                }
                break;
            case UNRECOGNIZED:
                logger.info("GOT UNRECOGNIZED MESSAGE: {}", message);
                break;
            case DISCONNECT:
                break;
        }
    }

    @Override
    public synchronized void connectionDisconnect(TCPConnection connection) {
        if (mapConnectionUserName.get(connection) == null) {
            return;
        }
        logger.info("SERVER: DISCONNECT");
        Message message = Message.builder()
                .type(MessageType.NOTIFICATION)
                .name(BOT_NAME)
                .text("Let's say goodbye to " + mapConnectionUserName.get(connection))
                .time(connection.getTime())
                .build();
        mapConnectionUserName.remove(connection);
        logger.info("MAP changed: {}", mapConnectionUserName);
        sendMessageToAllConnections(message);
    }

    @Override
    public synchronized void connectionException(TCPConnection connection, Exception e) {
        logger.error("SERVER: EXCEPTION", e);
    }

    private void sendMessageToAllConnections(Message message) {
        logger.info("Send message server -> all connections : {}", message);
        mapConnectionUserName.forEach((k, v) -> k.sendMessage(message));
    }

    private void sendMessageToOneConnection(Message message, TCPConnection connection) {
        logger.info("Send message server -> one connection : {}", message);
        connection.sendMessage(message);
    }

    private boolean checkNameInList(String name) {
        final boolean result = mapConnectionUserName
                .values()
                .stream()
                .anyMatch(x -> x.equals(name));
        logger.error("SERVER: CHECK NAME {} in {} Result: {}.", name, mapConnectionUserName.values(), result);
        return result;
    }

    private String getUsersLine() {
        final String result = mapConnectionUserName
                .values()
                .stream()
                .sorted()
                .reduce((x, y) ->  " >> " + x + System.lineSeparator() + " >> " + y)
                .orElse("");
        logger.error("SERVER: created line of users: {}\n", result);
        return result;
    }
}

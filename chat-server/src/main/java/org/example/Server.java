package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static org.example.TCPConnection.getTime;

public class Server implements TCPConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final String SERVER_NAME = "SERVER";

    private final ServerConfig config;
    private final int port;
    private final Map<TCPConnection, String> mapConnectionUserName;

    public Server(ServerConfig config) {
        this.config = config;
        this.mapConnectionUserName = new HashMap<>();
        this.port = this.config.getPort();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("{}: running on port [{}]", SERVER_NAME, serverSocket.getLocalPort());
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    logger.error("{}: exception on creating new connection", SERVER_NAME, e);
                }
            }
        } catch (IOException e) {
            logger.error("{}: server socket error", SERVER_NAME, e);
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void connectionReady(TCPConnection connection) {
        logger.info("{} connection ready", SERVER_NAME);
    }

    // called then the line was receive
    @Override
    public synchronized void connectionReceiveMessage(TCPConnection connection, Message message) {
        logger.info("{}: receive ({})", SERVER_NAME, message);
        switch (message.getType()) {
            case TEXT:
                sendMessageToAllConnections(message);
                break;
            case NAME:
                String name = message.getName();
                if (!checkNameInList(name)) {
                    mapConnectionUserName.put(connection, name);
                    logger.info("{}: map changed to <{}>", SERVER_NAME, mapConnectionUserName);
                    sendMessageToAllConnections(buildMessage(MessageType.NOTIFICATION, name + " joined to the chat"));
                    sendMessageToAllConnections(buildMessage(MessageType.USERS, getLineWithUsers()));
                } else {
                    logger.info("{}: user <{}> is already in chat", SERVER_NAME, name);
                    sendMessageToOneConnection(buildMessage(MessageType.NOTIFICATION, "User with name " + name + " is already in the chat"), connection);
                    connection.disconnect();
                }
                break;
            case UNRECOGNIZED:
                logger.info("{}: got unrecognized message: {}", SERVER_NAME, message);
                break;
            case DISCONNECT:
                logger.info("{}: disconnect <{}>", SERVER_NAME, mapConnectionUserName.get(connection));
                connection.disconnect();
                break;
        }
    }

    // called in finally then listening user thread is interrupted
    @Override
    public synchronized void connectionDisconnect(TCPConnection connection) {
        // not registered client does not need notification
        if (mapConnectionUserName.get(connection) == null) {
            return;
        }
        String nameForNotification = mapConnectionUserName.get(connection);
        logger.info("{}: user <{}> disconnected", SERVER_NAME, nameForNotification);
        mapConnectionUserName.remove(connection);
        logger.info("{}: map changed to <{}>", SERVER_NAME, mapConnectionUserName);
        sendMessageToAllConnections(buildMessage(MessageType.USERS, getLineWithUsers()));
        sendMessageToAllConnections(buildMessage(MessageType.NOTIFICATION, "User " + nameForNotification + " left the chat"));
    }

    // called in catch block of TCP connection
    @Override
    public synchronized void connectionException(TCPConnection connection, Exception e) {
        logger.error("{}: exception {}", SERVER_NAME, connection, e);
    }

    // send message to all registered clients
    private void sendMessageToAllConnections(Message message) {
        logger.info("{}: send message server -> all connections : {}", SERVER_NAME, message);
        mapConnectionUserName.forEach((k, v) -> k.sendMessage(message));
    }

    // send private message for one user
    private void sendMessageToOneConnection(Message message, TCPConnection connection) {
        logger.info("{}: send message server -> one connection : {}", SERVER_NAME, message);
        connection.sendMessage(message);
    }

    // check users map for name in values
    private boolean checkNameInList(String name) {
        final boolean result = mapConnectionUserName
                .values()
                .stream()
                .anyMatch(x -> x.equals(name));
        logger.error("{}: check name {} in {} Result: {}.", SERVER_NAME, name, mapConnectionUserName.values(), result);
        return result;
    }

    // get converted line from map values
    private String getLineWithUsers() {
        final String result = mapConnectionUserName
                .values()
                .stream()
                .sorted()
                .map(x -> " > " + x)
                .reduce((x, y) -> x + System.lineSeparator() + y)
                .orElse("");
        logger.info("{}: created line of users:\n{}", SERVER_NAME, result);
        return result;
    }

    //build message to send from server
    private Message buildMessage(MessageType type, String note) {
        return Message.builder()
                .type(type)
                .name(SERVER_NAME)
                .text(note)
                .time(getTime())
                .build();
    }
}

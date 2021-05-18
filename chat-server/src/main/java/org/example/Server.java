package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Server implements TCPConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final String WRONG_MESSAGE_FORMAT = "Message you sent is in a wrong format. You will be disconnected";
    private static final String BOT_NAME = "SERVER";

    private final int port;
    private final List<TCPConnection> tcpConnections;
    private final List<String> userNames;

    public Server(int port) {
        this.tcpConnections = new ArrayList<>();
        this.userNames = new ArrayList<>();
        this.port = port;
    }

    public synchronized void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server running on port [{}]", serverSocket.getLocalPort());
            while (true) {
                new TCPConnection(this, serverSocket.accept());
            }
        } catch (IOException e) {
            logger.error("Exception on startServer");
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        logger.info("Method onConnectionReady (server)");
        logger.info("Get name method");
        try {
            Optional<Message> optionalMessage;
            if (!(optionalMessage = tcpConnection.readMessage()).equals(Optional.empty())) {
                Message message = optionalMessage.get();
                logger.info("Read message {}", message);
                if (validateUserName(message.getName())) {
                    onNewUserEntersChat(tcpConnection, message);
                    sendMessageToAllConnections(getGreetingsMessage(tcpConnection));
                }
            } else {
                logger.info("Read message {}", optionalMessage);
                sendMessageToOneConnection(tcpConnection,
                        Message.builder()
                                .name(BOT_NAME)
                                .text(WRONG_MESSAGE_FORMAT)
                                .time(tcpConnection.getTime())
                                .build());
                tcpConnection.disconnect();
            }
        } catch (IOException e) {
            onException(tcpConnection, e);
        }

    }

    @Override
    public synchronized void onReceiveMessage(TCPConnection tcpConnection, Message message) {
        logger.info("Received message {} from user {}", message, tcpConnection.getName());
        sendMessageToAllConnections(message);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        logger.info("Method onDisconnect (server)");
        tcpConnections.remove(tcpConnection);
        if (tcpConnection.getName() != null) {
            sendMessageToAllConnections(
                    Message.builder()
                            .name(BOT_NAME)
                            .text("Client " + tcpConnection.getName() + " disconnected\r\n")
                            .time(tcpConnection.getTime())
                            .build());
        }
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        logger.error("Method onException on server.", e);

    }

    private Message getGreetingsMessage(TCPConnection tcpConnection) {
        return Message.builder()
                .name(BOT_NAME)
                .text("Client " + tcpConnection.getName() + " connected")
                .time(tcpConnection.getTime())
                .build();
    }

    private boolean validateUserName(String name) {
        boolean isValid = true;
        //todo: create check user name if it in list
        logger.info("Name is valid: {}", isValid);
        return isValid;
    }

    private void onNewUserEntersChat(TCPConnection tcpConnection, Message message) {
        final String userName = message.getName();
        tcpConnection.setName(userName);
        userNames.add(userName);
        logger.info("User name {} added to list of user names {}:", userName, userNames);
        tcpConnections.add(tcpConnection);
        logger.info("TCPConnection {} added to list of TCPConnections {}", tcpConnection, tcpConnections);
    }

    private void sendMessageToOneConnection(TCPConnection tcpConnection, Message message) {
        logger.info("Send message server -> one connection {}", message);
        tcpConnection.sendMessage(message);
    }

    private void sendMessageToAllConnections(Message message) {
        logger.info("Send message server -> all connections : {}", message);
        tcpConnections.forEach(x -> x.sendMessage(message));
    }
}

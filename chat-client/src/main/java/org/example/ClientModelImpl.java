package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class ClientModelImpl implements TCPConnectionListener, ClientModel {
    private static final Logger logger = LoggerFactory.getLogger(ClientModelImpl.class);

    private final SwingPropertyChangeSupport propertyChangeFirer;
    private String ipAddress;
    private int port;
    private String userName;
    private TCPConnection connection;

    public ClientModelImpl() {
        logger.info("Client model created");
        this.propertyChangeFirer = new SwingPropertyChangeSupport(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        propertyChangeFirer.addPropertyChangeListener(listener);
    }

    @Override
    public void setIPAddress(String ipAddress) {
        logger.info("Set ip address {}", ipAddress);
        this.ipAddress = ipAddress;
        propertyChangeFirer.firePropertyChange("ipAddress", "", ipAddress);
    }

    @Override
    public void setPort(int port) {
        logger.info("Set port {}", port);
        this.port = port;
        propertyChangeFirer.firePropertyChange("port", "", port);
    }

    @Override
    public void setUserName(String userName) {
        logger.info("Set userName {}", userName);
        this.userName = userName;
        propertyChangeFirer.firePropertyChange("userName", "", userName);
    }

    @Override
    public void stopClient() {
        logger.info("CLIENT MODEL: stop");
        connection.disconnect();
    }

    @Override
    public void startClient() {
        logger.info("CLIENT MODEL: start");

        try {
            propertyChangeFirer.firePropertyChange("onConnectionReady", "", "");
            connection = new TCPConnection(this, ipAddress, port);
            logger.info("CLIENT MODEL: connection status: {}", connection.getSocket().isConnected());
        } catch (IOException e) {
            printLine("CONNECTION EXCEPTION" + e.getMessage());
        }
    }

    @Override
    public void connectionReady(TCPConnection connection) {
        logger.info("CLIENT MODEL: CONNECTION READY");
        printLine("Connection ready.");

    }

    @Override
    public void connectionReceiveMessage(TCPConnection connection, Message message) {
        logger.info("CLIENT MODEL: RECEIVE MESSAGE {}", message);
        String line;
        switch (message.getType()) {
            case TEXT:
                line = getLineFromMessage(message);
                printLine(line);
                break;
            case NOTIFICATION:
                line = getLineFromMessage(message);
                printLine("***" + line);
                break;
            case USERS:
                String users = message.getText();
                propertyChangeFirer.firePropertyChange("updateUsers", "", users);
                break;
            default:
                logger.info("CLIENT MODEL: receive unrecognized {}", message);
                break;
        }
    }

    @Override
    public void connectionDisconnect(TCPConnection connection) {
        logger.info("CLIENT MODEL: DISCONNECT");
        propertyChangeFirer.firePropertyChange("connectionDisconnect", "", "");
        printLine("Connection closed.");
    }

    @Override
    public void connectionException(TCPConnection connection, Exception e) {
        logger.info("CLIENT MODEL: EXCEPTION" + e.getMessage());
        printLine("Connection exception.");
    }


    @Override
    public void sendLine(String line){
        logger.info("CLIENT MODEL: send message from line {}", line);
        connection.sendMessage(Message.builder()
                .type(MessageType.TEXT)
                .name(userName)
                .text(line)
                .time(connection.getTime())
                .build());
    }

    @Override
    public void sendNameMessage() {
        logger.info("CLIENT MODEL: Send name message");
        connection.sendMessage(Message.builder()
                .type(MessageType.NAME)
                .name(userName)
                .text("")
                .time(connection.getTime())
                .build());
    }

    private synchronized void printLine(String line) {
        propertyChangeFirer.firePropertyChange("printMessageLine", "", line);
    }

    private String getLineFromMessage(Message message) {
        return "[" +
                message.getTime() +
                "] " +
                message.getName() +
                ": " +
                message.getText();
    }
}

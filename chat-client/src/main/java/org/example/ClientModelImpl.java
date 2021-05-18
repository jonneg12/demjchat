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
    private TCPConnection tcpConnection;

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
        logger.info("Stop client {} isConnected: {}", tcpConnection, tcpConnection.getSocket().isConnected());
        tcpConnection.disconnect();
        logger.info("{} isClosed: {}", tcpConnection, tcpConnection.getSocket().isClosed());
    }

    @Override
    public void startClient() {
        logger.info("Start client <{}>", userName);
        try {
            tcpConnection = new TCPConnection(this, ipAddress, port);
            tcpConnection.setName(userName);
            logger.info("Client started");
        } catch (IOException e) {
            logger.error("Start client exception {}", tcpConnection);
            throw new RuntimeException();
        }
    }

    @Override
    public void sendNewMessage(String text) {
        final Message message = Message.builder()
                .name(userName)
                .text(text)
                .time(tcpConnection.getTime())
                .build();
        logger.info("{} send new message {}", userName, message);
        tcpConnection.sendMessage(message);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        propertyChangeFirer.firePropertyChange("onConnectionReady", "", "");
        logger.info("onConnection client <{}>", userName);
        final Message message = Message.builder()
                .name(userName)
                .text("connected")
                .time(tcpConnection.getTime())
                .build();
        tcpConnection.sendMessage(message);
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message message) {
        logger.info("onReceiveMessage");
        //TODO: make check to update users list
        printMessage(message);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        logger.info("onDisconnect client {}", userName);
        propertyChangeFirer.firePropertyChange("onDisconnect", "", "");
        printMessage(Message.builder()
                .name(userName)
                .text("Connection closed")
                .time(tcpConnection.getTime())
                .build());
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        logger.info("onException client {}", userName);
        printMessage(Message.builder()
                .name(userName)
                .text("Connection exception")
                .time(tcpConnection.getTime())
                .build());
    }

    private void printMessage(Message message) {
        logger.info("Print message {}", message);
        printMessageLine(message);
    }

    private void printMessageLine(Message message) {
        propertyChangeFirer.firePropertyChange("printMessageLine", "", convertMessageToLine(message));
    }

    private String convertMessageToLine(Message message) {
        final StringBuilder builder = new StringBuilder();
        String time = message.getTime().substring(0, 8);
        return builder
                .append('[')
                .append(time)
                .append("] ")
                .append(message.getName())
                .append(": ")
                .append(message.getText())
                .append(System.lineSeparator())
                .toString();
    }
}

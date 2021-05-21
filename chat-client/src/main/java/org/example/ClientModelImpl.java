package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class ClientModelImpl implements TCPConnectionListener, ClientModel {
    private static final Logger logger = LoggerFactory.getLogger(ClientModelImpl.class);
    private static final String CLIENT_MODEL = "CLIENT MODEL";

    private final SwingPropertyChangeSupport propertyChangeFirer;

    private int port;
    private String ipAddress;
    private String userName;
    private TCPConnection connection;

    public ClientModelImpl() {
        logger.info("{} created", CLIENT_MODEL);
        this.propertyChangeFirer = new SwingPropertyChangeSupport(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        propertyChangeFirer.addPropertyChangeListener(listener);
    }

    @Override
    public boolean setUserName(String userName) {
        if ("".equals(userName)) {
            logger.info("{}: userName is empty", CLIENT_MODEL);
            // TODO fire about blanc user name value
            return false;
        }
        this.userName = userName;
        logger.info("{}: set user name <{}>", CLIENT_MODEL, userName);
        propertyChangeFirer.firePropertyChange("userName", "", userName);
        return true;
    }

    @Override
    public boolean setIPAddress(String ipAddress) {
        if ("".equals(ipAddress)) {
            logger.info("{}: ip address is empty", CLIENT_MODEL);
            return false;
        }
        this.ipAddress = ipAddress;
        logger.info("{}: set ip address <{}>", CLIENT_MODEL, ipAddress);
        propertyChangeFirer.firePropertyChange("ipAddress", "", ipAddress);
        return true;
    }

    @Override
    public boolean setPort(String portLine) {
        if ("".equals(portLine)) {
            logger.info("{}: port is empty", CLIENT_MODEL);
            // TODO fire about blanc port value
            return false;
        }
        int port;
        try {
             port = Integer.parseInt(portLine);
            if (!checkPort(port)) {
                logger.info("{}: got wrong value of port <{}>", CLIENT_MODEL, port);
                return false;
                // TODO: maybe fire about wrong port value
            }
            this.port = port;
            propertyChangeFirer.firePropertyChange("port", "", port);
            logger.info("{}: set port <{}>", CLIENT_MODEL, port);
            return true;
        } catch (NumberFormatException e) {
            // TODO: maybe fire about wrong port value
            logger.info("{}: got wrong format of port <{}>", CLIENT_MODEL, portLine);
            return false;
        }
    }

    @Override
    public void stopClient() {
        logger.info("{}: stop", CLIENT_MODEL);
        connection.sendMessage(buildMessage(MessageType.DISCONNECT, ""));
        connection.disconnect();
    }

    @Override
    public void startClient() {
        logger.info("{}: start", CLIENT_MODEL);
        try {
            connection = new TCPConnection(this, ipAddress, port);
        } catch (IOException e) {
            logger.info("{}: connection exception", CLIENT_MODEL);
            printLine("CONNECTION EXCEPTION" + e.getMessage());
        }
    }

    @Override
    public void connectionReady(TCPConnection connection) {
        propertyChangeFirer.firePropertyChange("onConnectionReady", "", "ready");
        logger.info("{}: connection ready", CLIENT_MODEL);
        printLine("Connection ready.");
    }

    @Override
    public void connectionReceiveMessage(TCPConnection connection, Message message) {
        logger.info("{}: receive <{}>", CLIENT_MODEL, message);
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
                logger.info("{}: receive unrecognized message <{}>", CLIENT_MODEL, message);
                break;
        }
    }

    @Override
    public void connectionDisconnect(TCPConnection connection) {
        logger.info("{}: connection disconnect", CLIENT_MODEL);
        propertyChangeFirer.firePropertyChange("connectionDisconnect", "", "disconnect");
        printLine("Connection closed.");
    }

    @Override
    public void connectionException(TCPConnection connection, Exception e) {
        logger.info("{}: connection exception", CLIENT_MODEL, e);
        printLine("Connection exception.");
    }


    @Override
    public void sendTextMessage(String line) {
        logger.info("{}: send line <{}>", CLIENT_MODEL, line);
        connection.sendMessage(buildMessage(MessageType.TEXT, line));
    }

    @Override
    public void sendNameMessage() {
        logger.info("{}: connection disconnect", CLIENT_MODEL);
        connection.sendMessage(buildMessage(MessageType.NAME, ""));
    }

    public Message buildMessage(MessageType type, String text) {
        return Message.builder()
                .type(type)
                .name(userName)
                .text(text)
                .time(TCPConnection.getTime())
                .build();
    }

    private synchronized void printLine(String line) {
        propertyChangeFirer.firePropertyChange("printMessageLine", "", line);
    }

    private boolean checkPort(int value) {
        return value > 0 && value < 65535;
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

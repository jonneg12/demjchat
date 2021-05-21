package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientControllerImpl implements ClientController, PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientControllerImpl.class);
    private static final String CONTROLLER_NAME = "CLIENT CONTROLLER";
    private static final String DISCONNECT = "DISCONNECT";
    private static final String CONNECT = "CONNECT";

    private final ClientView view;
    private final ClientModel model;

    public ClientControllerImpl(ClientView view, ClientModel model) {
        this.view = view;
        this.model = model;
        this.model.addListener(this);

        setupViewEvents();
    }

    @Override
    public void setupViewEvents() {
        // action to SEND button
        view.getButtonSend().addActionListener(e -> {
            logger.info("{}: triggered button <Send>", CONTROLLER_NAME);
            final String text = view.getTextInput().getText();
            if (text.equals("")) {
                return;
            }
            model.sendTextMessage(text);
            view.getTextInput().setText("");
        });

        // action to INPUT field (when press enter
        view.getTextInput().addActionListener(e -> {
            logger.info("{}: triggered input field", CONTROLLER_NAME);
            final String text = view.getTextInput().getText();
            if (text.equals("")) {
                return;
            }
            model.sendTextMessage(text);
            view.getTextInput().setText("");
        });


        // action to CONNECT button
        view.getButtonConnect().addActionListener(e -> {
            final JButton buttonConnect = view.getButtonConnect();
            switch (buttonConnect.getText()) {
                case CONNECT:
                    logger.info("{}: triggered button <Connect>. Button state is <{}>", CONTROLLER_NAME, buttonConnect.getText());
                    if (model.setUserName(view.getTextUserName().getText())
                            && model.setIPAddress(view.getTextIp().getText())
                            && model.setPort(view.getTextPort().getText())) {
                        model.startClient();
                        model.sendNameMessage();
                    }
                    break;
                case DISCONNECT: {
                    logger.info("{}: triggered button <Connect>. Button state is <{}>", CONTROLLER_NAME, buttonConnect.getText());
                    model.stopClient();
                    break;
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();
        final Object newValue = evt.getNewValue();

        switch (propertyName) {
            case "printMessageLine":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String messageLine = (String) newValue;
                view.getAreaChat().append(messageLine + System.lineSeparator());
                view.getAreaChat().setCaretPosition(view.getAreaChat().getDocument().getLength());
                logger.info("{}: message {} append to chat area", CONTROLLER_NAME, messageLine);
                break;
            case "ipAddress":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String ipAddress = (String) newValue;
                view.getTextIp().setText(ipAddress);
                logger.info("{}: ip address updated", CONTROLLER_NAME);
                break;
            case "port":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                int port = (int) newValue;
                view.getTextPort().setText(String.valueOf(port));
                logger.info("{}: port updated", CONTROLLER_NAME);
                break;
            case "userName":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String userName = (String) newValue;
                view.getTextUserName().setText(userName);
                logger.info("{}: user name updated", CONTROLLER_NAME);
                break;
            case "onConnectionReady":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                view.getTextIp().setEditable(false);
                view.getTextPort().setEditable(false);
                view.getTextUserName().setEditable(false);
                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.green));
                view.getButtonSend().setEnabled(true);
                view.getTextInput().setEditable(true);
                view.getLabelOnlineState().setText("CONNECTED");
                view.getButtonConnect().setText(DISCONNECT);
                logger.info("{}: CONNECTED", CONTROLLER_NAME);
                break;
            case "connectionDisconnect":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                view.getTextIp().setEditable(true);
                view.getTextPort().setEditable(true);
                view.getTextUserName().setEditable(true);
                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.red));
                view.getButtonSend().setEnabled(false);
                view.getTextInput().setEditable(false);
                view.getLabelOnlineState().setText("DISCONNECTED");
                view.getButtonConnect().setText(CONNECT);
                logger.info("{}: DISCONNECTED", CONTROLLER_NAME);
                break;
            case "updateUsers":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String users = (String) newValue;
                view.getAreaUsers().setText("");
                view.getAreaUsers().setText("Users online:" + System.lineSeparator() + users);
                view.getAreaUsers().setCaretPosition(view.getAreaUsers().getDocument().getLength());
                logger.info("{}: lines\n{}\nappend to user area", CONTROLLER_NAME, users);
                break;
            default:
                logger.info("Property {} not found", propertyName);
                break;
        }
    }
}

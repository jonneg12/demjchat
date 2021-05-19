package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientControllerImpl implements ClientController, PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientControllerImpl.class);
    private static final String DISCONNECT = "DISCONNECT";
    private static final String CONNECT = "CONNECT";
    private static final String CONTROLLER_NAME = "CLIENT CONTROLLER";

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
            model.sendLine(text);
            view.getTextInput().setText("");
        });

        // action to INPUT field (when press enter
        view.getTextInput().addActionListener(e -> {
            logger.info("{}: triggered input field", CONTROLLER_NAME);
            final String text = view.getTextInput().getText();
            if (text.equals("")) {
                return;
            }
            model.sendLine(text);
            view.getTextInput().setText("");
        });


        // action to CONNECT button
        view.getButtonConnect().addActionListener(e -> {
            final JButton buttonConnect = view.getButtonConnect();
            logger.info("{}: triggered button <Connect>. Button state is <{}>", CONTROLLER_NAME, buttonConnect.getText());
            switch (buttonConnect.getText()) {
                case CONNECT:
                    logger.info("{}: triggered button <Connect>. Button state is <{}>", CONTROLLER_NAME, buttonConnect.getText());
                    buttonConnect.setText(DISCONNECT);
                    model.setUserName(view.getTextUserName().getText());
                    model.setIPAddress(view.getTextIp().getText());
                    model.setPort(Integer.parseInt(view.getTextPort().getText()));
                    model.startClient();
                    model.sendNameMessage();
                    break;
                case DISCONNECT: {
                    logger.info("{}: triggered button <Connect>. Button state is <{}>", CONTROLLER_NAME, buttonConnect.getText());
                    buttonConnect.setText(CONNECT);
                    model.stopClient();
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
                logger.info("{}}: event {}", CONTROLLER_NAME, propertyName);
                String messageLine = (String) newValue;
                view.getAreaChat().append(messageLine + System.lineSeparator());
                view.getAreaChat().setCaretPosition(view.getAreaChat().getDocument().getLength());
                logger.info("Message line append to chat area");
                break;
            case "ipAddress":
                logger.info("{}}: event {}", CONTROLLER_NAME, propertyName);
                String ipAddress = (String) newValue;
                view.getTextIp().setText(ipAddress);
                logger.info("Ip address updated");
                break;
            case "port":
                logger.info("{}}: event {}", CONTROLLER_NAME, propertyName);
                int port = (int) newValue;
                view.getTextPort().setText(String.valueOf(port));
                logger.info("Port updated");
                break;
            case "userName":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String userName = (String) newValue;
                view.getTextUserName().setText(userName);
                logger.info("User name updated");
                break;
            case "onConnectionReady":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
//                view.getTextIp().setEditable(false);
//                view.getTextPort().setEditable(false);
//                view.getTextUserName().setEditable(false);
//                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.green));
//                view.getButtonSend().setEnabled(true);
//                view.getTextInput().setEditable(true);
//                view.getLabelOnlineState().setText("<CONNECTED>");
//                logger.info("CONNECTED");
                break;
            case "connectionDisconnect":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
//                view.getTextIp().setEditable(true);
//                view.getTextPort().setEditable(true);
//                view.getTextUserName().setEditable(true);
//                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.red));
//                view.getButtonSend().setEnabled(false);
//                view.getTextInput().setEditable(false);
//                view.getLabelOnlineState().setText("<DISCONNECTED>");
//                logger.info("DISCONNECTED");
                break;
            case "updateUsers":
                logger.info("{}: event {}", CONTROLLER_NAME, propertyName);
                String users = (String) newValue;
                view.getAreaUsers().setText("");
                view.getAreaUsers().setText("Users online:" + System.lineSeparator() + users + System.lineSeparator());
                view.getAreaUsers().setCaretPosition(view.getAreaUsers().getDocument().getLength());
                logger.info("Message line append to chat area");
                break;
            default:
                logger.info("Property {} not found", propertyName);
                break;
        }
    }
}

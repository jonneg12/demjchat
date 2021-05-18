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

    private final ClientView view;
    private final ClientModel model;

    public ClientControllerImpl(ClientView view, ClientModel model) {
        this.view = view;
        this.model = model;
        this.model.addListener(this);

        setupViewEvents();
    }

    @Override
    public synchronized void setupViewEvents() {
        view.getButtonSend().addActionListener(e -> {
            logger.info("Trigger button <Send>");
            final String text = view.getTextInput().getText();
            (new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    logger.info("New worker send text: {}", text);
                    model.sendNewMessage(text);
                    logger.info("New worker sent text: {}", text);
                    return null;
                }
            }).execute();
        });

        view.getButtonConnect().addActionListener(e -> {
            final JButton buttonConnect = view.getButtonConnect();
            logger.info("Trigger button <Connect>. Button state is <{}>", buttonConnect.getText());
            switch (buttonConnect.getText()) {
                case CONNECT:
                    logger.info("Case <Connect>");
                    buttonConnect.setText(DISCONNECT);
                    model.setUserName(view.getTextUserName().getText());
                    model.setIPAddress(view.getTextIp().getText());
                    model.setPort(Integer.parseInt(view.getTextPort().getText()));
//                    (new SwingWorker<Void, Void>() {
//                        @Override
//                        protected Void doInBackground() {
//                            logger.info("Started task: new worker start client");
                            model.startClient();
//                            logger.info("Stopped task: start client");
//                            return null;
//                        }
//                    }).execute();
                    break;
                case DISCONNECT: {
                    buttonConnect.setText(CONNECT);
//                    (new SwingWorker<Void, Void>() {
//                        @Override
//                        protected Void doInBackground() {
//                            logger.info("Started task: new worker stop client");
                            model.stopClient();
//                            logger.info("Stopped task: worker stop client");
//                            return null;
//                        }
//                    }).execute();
                }
            }
        });
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();
        final Object newValue = evt.getNewValue();
        logger.info("Fired <{}>", propertyName);

        switch (propertyName) {
            case "printMessageLine":
                String messageLine = (String) newValue;
                view.getAreaChat().append(messageLine);
                logger.info("Message line append to chat area");
                break;
            case "ipAddress":
                String ipAddress = (String) newValue;
                view.getTextIp().setText(ipAddress);
                logger.info("Ip address updated");
                break;
            case "port":
                int port = (int) newValue;
                view.getTextPort().setText(String.valueOf(port));
                logger.info("Port updated");
                break;
            case "userName":
                String userName = (String) newValue;
                view.getTextUserName().setText(userName);
                logger.info("User name updated");
                break;
            case "onConnectionReady":
                view.getTextIp().setEditable(false);
                view.getTextPort().setEditable(false);
                view.getTextUserName().setEditable(false);
                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.green));
                view.getButtonSend().setEnabled(true);
                view.getTextInput().setEditable(true);
                view.getLabelOnlineState().setText("<CONNECTED>");
                logger.info("CONNECTED");
                break;
            case "onDisconnect":
                view.getTextIp().setEditable(true);
                view.getTextPort().setEditable(true);
                view.getTextUserName().setEditable(true);
                view.getPanelUI().setBorder(BorderFactory.createLineBorder(Color.red));
                view.getButtonSend().setEnabled(false);
                view.getTextInput().setEditable(false);
                view.getLabelOnlineState().setText("<DISCONNECTED>");
                logger.info("DISCONNECTED");
                break;
            default:
                logger.info("Property {} not found", propertyName);
                break;
        }
    }
}

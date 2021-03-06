package org.example;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class ClientViewImpl implements ClientView {
    private static final int MESSAGE_LENGTH_LIMIT = 120;
    private static final int PORT_LENGTH_LIMIT = 4;
    private static final int IP_ADDRESS_LENGTH_LIMIT = 15;
    private static final int USER_NAME_LENGTH_LIMIT = 20;
    private static final String DEFAULT_PORT = "9999";
    private static final String DEFAULT_IP = "192.168.88.194";

    private JButton buttonSend;
    private JButton buttonConnect;
    private JTextField textInput;
    private JTextField textIp;
    private JTextField textPort;
    private JTextField textUserName;
    private JTextArea areaChat;
    private JTextArea areaUsers;
    private JPanel panelUI;
    private JLabel labelChatClient;
    private JLabel labelOnlineState;
    private JLabel labelIp;
    private JLabel labelPort;
    private JLabel labelUser;

    public ClientViewImpl() {
        initPanelUI();

    }

    @Override
    public JPanel getPanelUI() {
        return panelUI;
    }

    private void initPanelUI() {
        initLabels();
        initButtons();
        initTextFields();
        initTextAreas();

        MigLayout layout = new MigLayout("insets 15",
                "[25][50][25][50][50][50][70][100]",
                "[][][250][]"
        );
        panelUI = new JPanel(layout);
        panelUI.setBorder(BorderFactory.createLineBorder(Color.RED));

        panelUI.add(labelChatClient, "span3");
        panelUI.add(labelOnlineState, "wrap");
        panelUI.add(labelPort, "right");
        panelUI.add(textPort, "growx");
        panelUI.add(labelIp, "right");
        panelUI.add(textIp, "growx");
        panelUI.add(labelUser, "right");
        panelUI.add(textUserName, "span 2, growx");
        panelUI.add(buttonConnect, "wrap");
        panelUI.add(areaChat, "span 6, growx, growy");
        panelUI.add(areaUsers, "span, growx, growy");
        panelUI.add(textInput, "span 7, growx");
        panelUI.add(buttonSend, "growx");
    }

    private void initLabels() {
        labelChatClient = new JLabel("CHAT CLIENT");
        labelOnlineState = new JLabel("<DISCONNECTED>");
        labelIp = new JLabel("IP:");
        labelPort = new JLabel("Port:");
        labelUser = new JLabel("User:");
    }

    private void initButtons() {
        buttonSend = new JButton("SEND");
        buttonSend.setEnabled(true);
        buttonConnect = new JButton("CONNECT");
    }

    private void initTextFields() {
        textInput = new JTextField();
        textInput.setDocument(new JTextFieldLimit(MESSAGE_LENGTH_LIMIT));
        textInput.setEditable(false);

        textIp = new JTextField();
        textIp.setDocument(new JTextFieldLimit(IP_ADDRESS_LENGTH_LIMIT));
        textIp.setText(DEFAULT_IP);

        textPort = new JTextField();
        textPort.setDocument(new JTextFieldLimit(PORT_LENGTH_LIMIT));
        textPort.setText(DEFAULT_PORT);

        textUserName = new JTextField();
        textUserName.setDocument(new JTextFieldLimit(USER_NAME_LENGTH_LIMIT));
    }

    private void initTextAreas() {
        areaChat = new JTextArea();
        areaChat.setLineWrap(true);
        areaChat.setEditable(false);
        areaChat.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        areaUsers = new JTextArea();
        areaUsers.setLineWrap(true);
        areaUsers.setEditable(false);
        areaUsers.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    }

    @Override
    public JButton getButtonSend() {
        return buttonSend;
    }

    @Override
    public JTextField getTextInput() {
        return textInput;
    }

    @Override
    public JTextField getTextIp() {
        return textIp;
    }

    @Override
    public JTextField getTextPort() {
        return textPort;
    }

    @Override
    public JTextField getTextUserName() {
        return textUserName;
    }

    @Override
    public JButton getButtonConnect() {
        return buttonConnect;
    }

    @Override
    public JTextArea getAreaChat() {
        return areaChat;
    }

    @Override
    public JLabel getLabelOnlineState(){
        return labelOnlineState;
    }

    @Override
    public JTextArea getAreaUsers() {
        return areaUsers;
    }

    public static class JTextFieldLimit extends PlainDocument {
        private final int limit;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

}

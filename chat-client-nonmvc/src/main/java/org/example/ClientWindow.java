package org.example;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.IOException;

public class ClientWindow extends JFrame implements TCPConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientWindow.class);

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private static final int MESSAGE_LENGTH_LIMIT = 120;
    private static final int PORT_LENGTH_LIMIT = 4;
    private static final int IP_ADDRESS_LENGTH_LIMIT = 15;
    private static final int USER_NAME_LENGTH_LIMIT = 20;
    private static final String DEFAULT_PORT = "9997";
    private static final String DEFAULT_IP = "192.168.88.194";

    private JButton buttonSend;
    private JButton buttonConnect;
    private JTextField textInput;
    private JTextField textIp;
    private JTextField textPort;
    private JTextField textUserName;
    private JTextArea areaChat;
    private JTextArea areaUsers;
    private JLabel labelChatClient;
    private JLabel labelOnlineState;
    private JLabel labelIp;
    private JLabel labelPort;
    private JLabel labelUser;

    private TCPConnection connection;


    public ClientWindow(String ipAddress, int port) {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        initLabels();
        initButtons();
        initTextFields();
        initTextAreas();

        JPanel panelConnect = new JPanel(new MigLayout("insets 5",
                "[25][50][25][120][35][130][70][100]"
        ));
        panelConnect.setBorder(BorderFactory.createLineBorder(Color.RED));


        JPanel panelChatAndUsers = new JPanel(new MigLayout("insets 5",
                "[25][50][25][200][35][50][70][100]",
                "[350]"
        ));
        panelChatAndUsers.setBorder(BorderFactory.createLineBorder(Color.RED));

        JPanel panelSendMessage = new JPanel(new MigLayout("insets 5",
                "[25][50][25][200][35][50][70][100]"
        ));
        panelSendMessage.setBorder(BorderFactory.createLineBorder(Color.RED));


        panelConnect.add(labelChatClient, "span3, growx");
        panelConnect.add(labelOnlineState, "span3, wrap");
        panelConnect.add(labelPort, "right");
        panelConnect.add(textPort, "growx");
        panelConnect.add(labelIp, "right");
        panelConnect.add(textIp, "growx");
        panelConnect.add(labelUser, "right");
        panelConnect.add(textUserName, "span 2, growx");
        panelConnect.add(buttonConnect, "wrap");
        panelChatAndUsers.add(areaChat, "span 6, growx, growy");
        panelChatAndUsers.add(areaUsers, "span, growx, growy");
        panelSendMessage.add(textInput, "span 7, growx");
        panelSendMessage.add(buttonSend, "growx");

        add(panelConnect, BorderLayout.NORTH);
        add(panelChatAndUsers, BorderLayout.CENTER);
        add(panelSendMessage, BorderLayout.SOUTH);
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
        buttonConnect.addActionListener(e -> {
            logger.info("BUTTON CONNECT");
            if ((textUserName.getText() == null)) {
                return;
            }
            this.startClientSocket(textIp.getText(), Integer.parseInt(textPort.getText()));
            connection.sendMessage(Message.builder()
                    .type(MessageType.NAME)
                    .name(textUserName.getText())
                    .text(textInput.getText())
                    .time(connection.getTime())
                    .build());
        });
    }

    private void initTextFields() {
        textInput = new JTextField("message");
        textInput.setDocument(new JTextFieldLimit(MESSAGE_LENGTH_LIMIT));
        textInput.setEditable(true);
        textInput.addActionListener(e -> {
            logger.info("INPUT");
            if (textInput.getText().equals("")) {
                return;
            }
            connection.sendMessage(Message.builder()
                    .type(MessageType.TEXT)
                    .name(textUserName.getText())
                    .text(textInput.getText())
                    .time(connection.getTime())
                    .build());
            textInput.setText("");
        });

        textIp = new JTextField();
        textIp.setDocument(new JTextFieldLimit(IP_ADDRESS_LENGTH_LIMIT));
        textIp.setText(DEFAULT_IP);

        textPort = new JTextField();
        textPort.setDocument(new JTextFieldLimit(PORT_LENGTH_LIMIT));
        textPort.setText(DEFAULT_PORT);

        textUserName = new JTextField("user");
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

        buttonSend = new JButton("SEND");
        buttonSend.addActionListener(e -> {
            logger.info("INPUT");
            if (textInput.getText().equals("")) {
                return;
            }
            connection.sendMessage(Message.builder()
                    .type(MessageType.TEXT)
                    .name(textUserName.getText())
                    .text(textInput.getText())
                    .time(connection.getTime())
                    .build());
            textInput.setText("");
        });

        setVisible(true);
    }


    private void startClientSocket(String ipAddress, int port) {

        logger.info("CLIENT: start");

        try {
            connection = new TCPConnection(this, ipAddress, port);
            logger.info("CLIENT: connection status: {}", connection.getSocket().isConnected());
        } catch (IOException e) {
            printLine("CONNECTION EXCEPTION" + e.getMessage());
        }
    }

    @Override
    public void connectionReady(TCPConnection connection) {
        logger.info("CLIENT: CONNECTION READY");
        printLine("Connection ready.");
    }

    @Override
    public void connectionReceiveMessage(TCPConnection tcpConnection, Message message) {
        logger.info("CLIENT: RECEIVE MESSAGE {}", message);
        String line = getLineFromMessage(message);
        printLine(line);
    }

    private String getLineFromMessage(Message message) {
        return "[" +
                message.getTime() +
                "] " +
                message.getName() +
                ": " +
                message.getText();
    }

    @Override
    public void connectionDisconnect(TCPConnection tcpConnection) {
        logger.info("CLIENT: DISCONNECT");
        printLine("Connection closed.");
    }

    @Override
    public void connectionException(TCPConnection tcpConnection, Exception e) {
        logger.info("CLIENT: EXCEPTION" + e.getMessage());
        printLine("Connection exception.");
    }

    private synchronized void printLine(String line) {
        SwingUtilities.invokeLater(() -> {
            areaChat.append(line + System.lineSeparator());
            areaChat.setCaretPosition(areaChat.getDocument().getLength());
        });
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

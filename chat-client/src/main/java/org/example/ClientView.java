package org.example;
import javax.swing.*;

public interface ClientView {

    JPanel getPanelUI();

    JButton getButtonSend();

    JTextField getTextInput();

    JTextField getTextIp();

    JTextField getTextUserName();

    JTextField getTextPort();

    JButton getButtonConnect();

    JTextArea getAreaChat();

    JLabel getLabelOnlineState();

    JTextArea getAreaUsers();
}

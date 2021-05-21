package org.example;

import javax.swing.*;

public class ClientNonMVCStarter {
    public static void main(String[] args) {

        int port = 9997;
        String ipAddress = "192.168.88.194";
        SwingUtilities.invokeLater(() -> new ClientWindow(ipAddress, port));
    }
}

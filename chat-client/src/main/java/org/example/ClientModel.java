package org.example;

import java.beans.PropertyChangeListener;

public interface ClientModel {
    void addListener(PropertyChangeListener listener);

    boolean setIPAddress(String ipAddress);

    boolean setPort(String port);

    boolean setUserName(String name);

    void stopClient();

    void startClient();

    void sendTextMessage(String line);

    void sendNameMessage();
}

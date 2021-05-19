package org.example;

import java.beans.PropertyChangeListener;

public interface ClientModel {
    void addListener(PropertyChangeListener listener);

    void setIPAddress(String ipAddress);

    void setPort(int port);

    void setUserName(String name);

    void stopClient();

    void startClient();

    void sendLine(String line);

    void sendNameMessage();
}

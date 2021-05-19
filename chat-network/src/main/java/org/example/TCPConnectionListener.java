package org.example;

public interface TCPConnectionListener {

    void connectionReady(TCPConnection tcpConnection);

    void connectionReceiveMessage(TCPConnection tcpConnection, Message message);

    void connectionDisconnect(TCPConnection tcpConnection);

    void connectionException(TCPConnection tcpConnection, Exception e);

}

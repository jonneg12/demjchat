package org.example;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TCPConnection {
    private final static Logger logger = LoggerFactory.getLogger(TCPConnection.class);
    private final static String MESSAGES_SEPARATOR = "\r" + System.lineSeparator();

    private final TCPConnectionListener listener;
    private final Socket socket;
    private final Thread receiver;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final Gson gson;


    public Socket getSocket() {
        return socket;
    }

    public TCPConnection(TCPConnectionListener listener, String ipAddress, int port) throws IOException {
        this(listener, new Socket(ipAddress, port));
    }

    public TCPConnection(TCPConnectionListener listener, Socket socket) throws IOException {
        this.gson = new Gson();
        this.listener = listener;
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.connectionReady(TCPConnection.this);
                    while (!receiver.isInterrupted()) {
                        String line = reader.readLine();
                        logger.info("GOT line {}", line);
                        Message message = readMessage(line);
                        listener.connectionReceiveMessage(TCPConnection.this, message);

                    }
                } catch (Exception e) {
                    listener.connectionException(TCPConnection.this, e);
                } finally {
                    listener.connectionDisconnect(TCPConnection.this);
                }
            }
        });
        receiver.start();
    }

    private Message readMessage(String line) {
        Message message;
        try {
            message = gson.fromJson(line, Message.class);
            logger.info("READ {}", message);
        } catch (IllegalStateException e) {
            message = Message.builder()
                    .type(MessageType.UNRECOGNIZED)
                    .name("")
                    .text(line)
                    .time(getTime())
                    .build();
        }
        return message;
    }

    public synchronized void sendMessage(Message message) {
        logger.info("SEND {}", message);
        try {
            String line = gson.toJson(message);
            writer.write(line + MESSAGES_SEPARATOR);
            writer.flush();
        } catch (IOException e) {
            listener.connectionException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        logger.info("DISCONNECT {}", this);
        try {
            receiver.interrupt();
            socket.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            listener.connectionException(TCPConnection.this, e);
        }
    }

    public String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ISO_TIME).substring(0, 5);
    }

    @Override
    public String toString() {
        return "Connection(" + socket.getInetAddress() + ":" + socket.getPort() + ")";
    }
}

package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TCPConnection {
    private final static Logger logger = LoggerFactory.getLogger(TCPConnection.class);

    private final TCPConnectionListener listener;
    private final Socket socket;
    private final Thread receiver;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final Gson gson;
    private String name;

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }
    public void setName(String name) {
        this.name = name;
    }

    public TCPConnection(TCPConnectionListener listener, String ipAddress, int port) throws IOException {
        this(listener, new Socket(ipAddress, port));
        logger.info("Constructor TCPConnection-Client");
    }

    public TCPConnection(TCPConnectionListener listener, Socket socket) throws IOException {
        logger.info("Constructor TCPConnection");
        this.listener = listener;
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.gson = new Gson();

        this.receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onConnectionReady(TCPConnection.this);
                    while (!receiver.isInterrupted()) {
                        readMessage().ifPresent(message -> listener.onReceiveMessage(TCPConnection.this, message));
                    }
                } catch (IOException e) {
                    listener.onException(TCPConnection.this, e);
                } finally {
                    listener.onDisconnect(TCPConnection.this);
                }
            }
        });
        receiver.start();
    }

    public synchronized Optional<Message> readMessage() throws IOException {
        Message message = getMessage();
        return Optional.ofNullable(message);
    }

    private Message getMessage() throws IOException {
        String line = null;
        Message message = null;
        try {
            line = reader.readLine();
            message = convertJsonToMessage(line);
        } catch (JsonSyntaxException e) {
            logger.error("Reading message error: cannot parse to object line {}", line);
        }
        return message;
    }

    public synchronized void sendMessage(Message message) {
        logger.info("Method sendMessage by connection {} ({})", name, message);

        try {
            String line = convertMessageToJson(message);
            logger.info("Message converted to json: {}", line);
            writer.write(line + System.lineSeparator());
            logger.info("Writer wrote");
            writer.flush();
            logger.info("Writer flush");
        } catch (IOException e) {
            listener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    private String convertMessageToJson(Message message) {
        return gson.toJson(message);
    }

    private Message convertJsonToMessage(String line) {
        return gson.fromJson(line, Message.class);
    }


    public synchronized void disconnect() {
        logger.info("Method disconnect {}", this);
        try {
            receiver.interrupt();
            logger.info("Receiver {} is interrupted: {}", receiver.getName(), receiver.isInterrupted());
            socket.close();
        } catch (IOException e) {
            logger.error("Error in server disconnect method.", e);
            listener.onException(TCPConnection.this, e);
        }
    }

    public String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ISO_TIME);
    }

    @Override
    public String toString() {
        return "Connection(" + name + ", " + socket.getInetAddress() + ":" + socket.getPort() + ")";
    }
}

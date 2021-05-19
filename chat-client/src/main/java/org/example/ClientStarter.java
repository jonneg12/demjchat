package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class ClientStarter {
    private static final Logger logger = LoggerFactory.getLogger(ClientStarter.class);


    public static void main(String[] args) {
        int width = 600;
        int height = 400;

        ClientView view = new ClientViewImpl();
        ClientModel model = new ClientModelImpl();
        ClientController controller = new ClientControllerImpl(view, model);

        try {
            EventQueue.invokeAndWait(() -> {
                JFrame frame = createFrame(width, height);
                frame.add(view.getPanelUI());
                frame.pack();
                frame.setResizable(false);
                frame.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            logger.info("CLIENT STARTER: Event queue invokeAndWait exception", e);
        }
    }

    private static JFrame createFrame(int width, int height) {
        JFrame frame = new JFrame("Chat client");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        return frame;
    }
}

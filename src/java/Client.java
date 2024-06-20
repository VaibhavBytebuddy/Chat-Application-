import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton exitButton;
    private String userName;
    private String gender;
    private int messageCount = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }

    public Client() {
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        messageArea = new JTextArea();
        messageArea.setEditable(false);

        messageField = new JTextField();
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExit();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        panel.add(exitButton, BorderLayout.WEST);

        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);

        getUserInfo();
        connectToServer();
    }

    private void getUserInfo() {
        userName = JOptionPane.showInputDialog("Enter your name:");
        gender = (String) JOptionPane.showInputDialog(
                null,
                "Select your gender:",
                "Gender Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Male", "Female", "Other"},
                "Male"
        );
    }

    private void connectToServer() {
        String serverAddress = "localhost";
        int portNumber = 3333;

        try {
            socket = new Socket(serverAddress, portNumber);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            output.println(userName + " (" + gender + "): " + message);
            appendMessage("Client (" + gender + "): " + message);
            messageField.setText("");
            incrementMessageCount();
        }
    }

    private void handleExit() {
        if (messageCount > 15) {
            int confirm = JOptionPane.showConfirmDialog(null, "Do you really want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sendExitMessage();
            }
        } else {
            sendExitMessage();
        }
    }

    private void sendExitMessage() {
        output.println("exit");
        appendMessage("Client terminated the chat");
        closeResources();
        System.exit(0);
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    appendMessage("Server terminated the chat");
                    break;
                }
                appendMessage(message);
                incrementMessageCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }

    private void incrementMessageCount() {
        messageCount++;
    }

    private void closeResources() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


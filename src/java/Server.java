import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
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
        SwingUtilities.invokeLater(Server::new);
    }

    public Server() {
        JFrame frame = new JFrame("Server");
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
                checkBeforeExit();
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
        startServer();
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

    private void startServer() {
        int portNumber = 3333;

        try {
            serverSocket = new ServerSocket(portNumber);
            appendMessage("Server is running on port " + portNumber);

            clientSocket = serverSocket.accept();
            appendMessage("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            output.println(userName + " (" + gender + "): " + message);
            appendMessage("Server (" + gender + "): " + message);
            messageField.setText("");
            messageCount++;
        }
    }

    private void checkBeforeExit() {
        if (messageCount > 15) {
            int response = JOptionPane.showConfirmDialog(null, "You have sent more than 15 messages. Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                sendExitMessage();
            }
        } else {
            sendExitMessage();
        }
    }

    private void sendExitMessage() {
        output.println("exit");
        appendMessage("Server terminated the chat");
        closeResources();
        System.exit(0);
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    appendMessage("Client terminated the chat");
                    break;
                }
                appendMessage(message);
                messageCount++;
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

    private void closeResources() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


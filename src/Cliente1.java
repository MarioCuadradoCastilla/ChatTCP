import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente1 extends JFrame {
    private JTextField mensaje;
    private JTextArea chat;
    private JList<String> listaUsuarios;
    private String nombreUsuario;
    private PrintWriter writer;
    private Scanner sc;

    public Cliente1() {
        setTitle("Primer Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        initComponents();
        connectToServer();
    }

    public void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        chat = new JTextArea();
        chat.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chat);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        listaUsuarios = new JList<>();
        JScrollPane userScrollPane = new JScrollPane(listaUsuarios);
        userScrollPane.setPreferredSize(new Dimension(100, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        mensaje = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(mensaje, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String message = mensaje.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            mensaje.setText("");
        }
    }

    private void connectToServer() {
        nombreUsuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");

        String serverAddress = "localhost";

        try {
            Socket socket = new Socket(serverAddress, 12345);
            sc = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(nombreUsuario);

            new Thread(() -> {
                try {
                    while (sc.hasNextLine()) {
                        String serverMessage = sc.nextLine();
                        handleServerMessage(serverMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("/listaUsuarios ")) {
            String[] userArray = message.substring("/listaUsuarios ".length()).split(" ");
            SwingUtilities.invokeLater(() -> {
                listaUsuarios.setListData(userArray);
            });
        } else if (message.equals("/nombreUsuario")) {
            nombreUsuario = JOptionPane.showInputDialog("El nombre ya esta siendo usado. Introduzca uno diferente:");
            writer.println(nombreUsuario);
        } else {
            chat.append(message + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Cliente1().setVisible(true);
            }
        });
    }
}
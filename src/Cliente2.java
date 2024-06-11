import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente2 extends JFrame {
    private JTextField mensaje;
    private PrintWriter writer;
    private Scanner sc;
    private JTextArea chat;
    private JList<String> listaUsuarios;
    private String nombreDeUsuario;

    public Cliente2() {
        setTitle("Second Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        initComponents();
        conectarseAlServidor();
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
                enviarMensjae();
            }
        });

        inputPanel.add(mensaje, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void enviarMensjae() {
        String mensaje1 = mensaje.getText();
        if (!mensaje1.isEmpty()) {
            writer.println(mensaje1);
            mensaje.setText("");
        }
    }

    private void conectarseAlServidor() {
        nombreDeUsuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");

        String serverAddress = "localhost";

        try {
            Socket socket = new Socket(serverAddress, 12345);
            sc = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(nombreDeUsuario);

            new Thread(() -> {
                try {
                    while (sc.hasNextLine()) {
                        String mensajeServidor = sc.nextLine();
                        MensajeServidor(mensajeServidor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MensajeServidor(String message) {
        if (message.startsWith("/listaUsuarios ")) {
            String[] listaUsuarios = message.substring("/listaUsuarios ".length()).split(" ");
            SwingUtilities.invokeLater(() -> {
                this.listaUsuarios.setListData(listaUsuarios);
            });
        } else if (message.equals("/nombreUsado")) {
            nombreDeUsuario = JOptionPane.showInputDialog("El nombre ya está siendo usado. Introduzca uno diferente :");
            writer.println(nombreDeUsuario);
        } else {
            chat.append(message + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Cliente2().setVisible(true);
            }
        });
    }
}
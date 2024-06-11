import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Servidor {
    private static final int PORT = 12345;
    private static Set<String> usuariosConectados = new HashSet<>();
    private static Set<HiloCliente> clientes = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Puerto del servidor: " + PORT);

            while (true) {
                Socket socketCliente = serverSocket.accept();
                HiloCliente cliente= new HiloCliente(socketCliente);
                clientes.add(cliente);
                cliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class HiloCliente extends Thread {
        private Socket socketCliente;
        private String nombreUsuario;
        private PrintWriter writer;

        public HiloCliente(Socket socket) {
            this.socketCliente = socket;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(socketCliente.getInputStream());
                writer = new PrintWriter(socketCliente.getOutputStream(), true);
                // Compruebo si el nombre de usuario ya esta usado
                synchronized (usuariosConectados) {
                    while (true) {
                        nombreUsuario = scanner.nextLine();
                        if (!usuariosConectados.contains(nombreUsuario)) {
                            usuariosConectados.add(nombreUsuario);
                            break;
                        } else {
                            writer.println("/nombreUsado");
                        }
                    }
                }

                System.out.println(nombreUsuario + " se ha conectado correctamente");
                broadcast(nombreUsuario + " Bienvenido al chat!!");

                // Enviar la lista de usuarios conectados al nuevo cliente
                UsuariosConectados();

                // Controlar los mensajes de este cliente
                while (true) {
                    String clientMessage = scanner.nextLine();
                    if (clientMessage.equalsIgnoreCase("/salir")) {
                        break;
                    }
                    broadcast(nombreUsuario + ": " + clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Limpiar recursos y desconectar al usuario
                if (nombreUsuario != null) {
                    usuariosConectados.remove(nombreUsuario);
                    broadcast(nombreUsuario + " Se ha ido de el chat.");
                    System.out.println(nombreUsuario + " se ha desconectado.");
                }

                try {
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Eliminar el cliente de la lista de clientes
                clientes.remove(this);

                // Enviar la lista actualizada de usuarios a los clientes restantes
                UsuariosConectados();
            }
        }

        private void broadcast(String message) {
            synchronized (clientes) {
                for (HiloCliente client : clientes) {
                    client.writer.println(message);
                }
            }
        }

        private void UsuariosConectados() {
            synchronized (clientes) {
                StringBuilder userListMessage = new StringBuilder("/listaUsuarios ");
                for (HiloCliente client : clientes) {
                    userListMessage.append(client.nombreUsuario).append(" ");
                }
                // Envía la lista de usuarios a todos los clientes
                for (HiloCliente client : clientes) {
                    client.writer.println(userListMessage.toString());
                }
            }
        }
    }
}
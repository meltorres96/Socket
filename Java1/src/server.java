import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Lista que guarda todos los usuarios conectados
    // Clave: nombre del usuario, Valor: su canal de comunicación
    static Map<String, PrintWriter> usuariosConectados = new HashMap<>();

    public static void main(String[] args) {
        int puerto = 59420;

        System.out.println("=== LOG DEL SERVIDOR ===");
        System.out.println("Servidor iniciado y contestando OK");
        System.out.println("Esperando conexiones en puerto " + puerto + "...");
        System.out.println("========================");

        // ServerSocket es equivalente a socket() + bind() + listen() en C
        // Queda escuchando en el puerto indicado
        try (ServerSocket servidorSocket = new ServerSocket(puerto)) {

            // Bucle infinito: el servidor nunca para de aceptar clientes
            while (true) {
                // accept() se pausa aquí hasta que llegue un cliente
                Socket socketCliente = servidorSocket.accept();

                // Cada cliente se maneja en un hilo separado
                // así el servidor puede atender varios clientes al mismo tiempo
                Thread hiloCliente = new Thread(new ManejadorCliente(socketCliente));
                hiloCliente.start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Clase interna: maneja cada cliente en su propio hilo
    // -------------------------------------------------------
    static class ManejadorCliente implements Runnable {

        private Socket socket;
        private String nombreUsuario;
        private PrintWriter salida;
        private BufferedReader entrada;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Preparar canales de entrada y salida con el cliente
                entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                // PASO 1: Recibir el nombre del usuario que se conecta
                salida.println("Ingresa tu nombre de usuario:");
                nombreUsuario = entrada.readLine();

                // Registrar al usuario en la lista de conectados
                usuariosConectados.put(nombreUsuario, salida);
                System.out.println("Usuario \"" + nombreUsuario + "\" conectado");

                // Mostrarle al usuario la lista de quién más está conectado
                salida.println("=== Usuarios conectados ===");
                for (String usuario : usuariosConectados.keySet()) {
                    if (!usuario.equals(nombreUsuario)) {
                        salida.println("- " + usuario);
                    }
                }
                salida.println("===========================");
                salida.println("Escribe: [destinatario]:[mensaje]");
                salida.println("Escribe 'chao' para salir");

                // PASO 2: Bucle de recepción de mensajes
                String mensajeRecibido;
                while ((mensajeRecibido = entrada.readLine()) != null) {

                    // Si el usuario escribe "chao", termina su sesión
                    if (mensajeRecibido.equalsIgnoreCase("chao")) {
                        System.out.println("El usuario \"" + nombreUsuario + "\" abandonó");
                        salida.println("Has salido del chat. ¡Hasta luego!");
                        break;
                    }

                    // Formato esperado: destinatario:mensaje
                    // Ejemplo: poli02:Hola, ¿cómo estás?
                    if (mensajeRecibido.contains(":")) {
                        String[] partes = mensajeRecibido.split(":", 2);
                        String destinatario = partes[0].trim();
                        String contenido = partes[1].trim();

                        // Buscar al destinatario en la lista de conectados
                        PrintWriter salidaDestinatario = usuariosConectados.get(destinatario);

                        if (salidaDestinatario != null) {
                            // Enviar el mensaje al destinatario
                            salidaDestinatario.println(nombreUsuario + "--> " + contenido);
                            // Confirmar al remitente que el mensaje fue enviado
                            salida.println(nombreUsuario + "--> " + contenido);
                        } else {
                            salida.println("El usuario '" + destinatario + "' no está conectado.");
                        }
                    } else {
                        salida.println("Formato incorrecto. Usa: destinatario:mensaje");
                    }
                }

            } catch (IOException e) {
                System.err.println("Error con el cliente " + nombreUsuario + ": " + e.getMessage());
            } finally {
                // Limpiar cuando el cliente se desconecta
                if (nombreUsuario != null) {
                    usuariosConectados.remove(nombreUsuario);
                }
                try {
                    socket.close(); // close() - equivalente al close() de C
                } catch (IOException e) {
                    System.err.println("Error cerrando socket: " + e.getMessage());
                }
            }
        }
    }
}
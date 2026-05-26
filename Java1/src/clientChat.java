import java.io.*;
import java.net.*;
import java.util.Scanner;

public class clientChat {

    public static void main(String[] args) {

        // Datos de conexión al servidor
        String ipServidor = "127.0.0.1"; // localhost: el mismo computador
        int puerto = 59420;              // debe coincidir con el del servidor

        Scanner teclado = new Scanner(System.in);

        System.out.println("=== CLIENTE DE CHAT ===");
        System.out.println("IP Servidor: " + ipServidor);
        System.out.println("Puerto: " + puerto);
        System.out.println("=======================");

        // connect() - establece la conexión con el servidor
        try (Socket socket = new Socket(ipServidor, puerto)) {

            System.out.println("Conexión establecida con el servidor.");

            // Canales de comunicación con el servidor
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            // Hilo separado para recibir mensajes del servidor
            // sin bloquear la escritura del usuario
            Thread hiloRecepcion = new Thread(() -> {
                try {
                    String mensajeServidor;
                    // read() - lee continuamente lo que llega del servidor
                    while ((mensajeServidor = entrada.readLine()) != null) {
                        System.out.println(mensajeServidor);
                    }
                } catch (IOException e) {
                    System.out.println("Conexión con el servidor terminada.");
                }
            });
            hiloRecepcion.setDaemon(true);
            hiloRecepcion.start();

            // Bucle principal: el usuario escribe mensajes
            // write() - envía datos al servidor
            String mensajeUsuario;
            while (teclado.hasNextLine()) {
                mensajeUsuario = teclado.nextLine();
                salida.println(mensajeUsuario); // enviar al servidor

                // Si escribe "chao", terminar el programa
                if (mensajeUsuario.equalsIgnoreCase("chao")) {
                    Thread.sleep(500); // esperar respuesta del servidor
                    break;
                }
            }

        } catch (ConnectException e) {
            System.err.println("No se pudo conectar al servidor. ¿Está iniciado?");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("Cliente desconectado.");
        teclado.close();
    }
}

package com.mycompany.sistemagestiondetareas.util;

import com.mycompany.sistemagestiondetareas.vista.Login;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.SwingUtilities;

/**
 * Clase que maneja la conexión del cliente con el servidor.
 * Implementa la comunicación bidireccional y el manejo de reconexiones.
 */
public class Cliente {
    // Configuración de conexión
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 50;
    private static final int MAX_RETRIES = 3;          // Número máximo de intentos de reconexión
    private static final int RETRY_DELAY_MS = 1000;    // Tiempo de espera entre intentos (ms)
    
    private Socket socket;                             // Conexión con el servidor
    private BufferedReader in;                         // Flujo de entrada de datos
    private PrintWriter out;                           // Flujo de salida de datos
    private String usuario;                            // Nombre de usuario
    private boolean conectado = false;                 // Estado de la conexión
    
    /**
     * Constructor que inicia la conexión con el servidor.
     * @param usuario Nombre de usuario para autenticación
     * @param contrasena Contraseña para autenticación
     */
    public Cliente(String usuario, String contrasena) throws IOException {
        this.usuario = usuario;
        conectarAlServidor(usuario, contrasena);
    }
    
    /**
     * Establece la conexión con el servidor con reintentos automáticos.
     * @param usuario Nombre de usuario
     * @param contrasena Contraseña
     */
    private void conectarAlServidor(String usuario, String contrasena) throws IOException {
        int intentos = 0;
        while (intentos < MAX_RETRIES) {
            try {
                System.out.println("Intentando conectar al servidor...");
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Envía credenciales al servidor
                System.out.println("Enviando credenciales...");
                out.println(usuario);
                out.println(contrasena);
                
                // Espera respuesta de autenticación
                String respuesta = in.readLine();
                System.out.println("Respuesta del servidor: " + respuesta);
                
                if (respuesta != null && respuesta.equals("OK")) {
                    System.out.println("Conexión establecida con el servidor");
                    conectado = true;
                    return;
                } else {
                    throw new IOException("Error de autenticación: " + respuesta);
                }
            } catch (IOException e) {
                intentos++;
                if (intentos < MAX_RETRIES) {
                    System.out.println("Intento " + intentos + " fallido. Reintentando en " + RETRY_DELAY_MS + "ms...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Conexión interrumpida", ie);
                    }
                } else {
                    throw new IOException("No se pudo conectar al servidor después de " + MAX_RETRIES + " intentos", e);
                }
            }
        }
    }
    
    /**
     * Envía un mensaje al servidor.
     * @param mensaje Contenido del mensaje
     */
    public void enviarMensaje(String mensaje) throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión con el servidor. Por favor, reconéctese.");
        }
        if (socket == null || socket.isClosed()) {
            conectado = false;
            throw new IOException("La conexión con el servidor se ha perdido.");
        }
        out.println(mensaje);
    }
    
    /**
     * Recibe un mensaje del servidor.
     * @return Contenido del mensaje recibido
     */
    public String recibirMensaje() throws IOException {
        if (!conectado) {
            throw new IOException("No hay conexión con el servidor. Por favor, reconéctese.");
        }
        if (socket == null || socket.isClosed()) {
            conectado = false;
            throw new IOException("La conexión con el servidor se ha perdido.");
        }
        return in.readLine();
    }
    
    /**
     * Cierra la conexión con el servidor y libera los recursos.
     */
    public void cerrarConexion() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            conectado = false;
            System.out.println("Conexión cerrada");
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el nombre de usuario asociado a esta conexión.
     * @return Nombre de usuario
     */
    public String getUsuario() {
        return usuario;
    }
    
    /**
     * Verifica si la conexión está activa.
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean estaConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
    
    /**
     * Punto de entrada principal que inicia la aplicación cliente.
     */
    public static void main(String[] args) {
        try {
            // Configura el aspecto visual del sistema operativo
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignora errores de look and feel
        }
        
        // Inicia la ventana de login
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
        });
    }
} 
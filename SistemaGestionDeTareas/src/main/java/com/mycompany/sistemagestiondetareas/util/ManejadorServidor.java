package com.mycompany.sistemagestiondetareas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que maneja la comunicación con un cliente individual.
 * Gestiona la autenticación y el intercambio de mensajes en tiempo real.
 */
public class ManejadorServidor implements Runnable {
    private final Socket socket;                      // Conexión con el cliente
    private final ConcurrentHashMap<Integer, ManejadorServidor> clientes;  // Lista de clientes conectados
    private final Connection conexion;                // Conexión a la base de datos
    private final BufferedReader in;                  // Flujo de entrada de datos
    private final PrintWriter out;                    // Flujo de salida de datos
    private int idUsuario;                           // ID del usuario autenticado
    private String nombreUsuario;                     // Nombre del usuario autenticado
    
    /**
     * Constructor que inicializa los flujos de comunicación.
     * @param socket Socket de conexión con el cliente
     * @param clientes Lista de clientes conectados
     * @param conexion Conexión a la base de datos
     */
    public ManejadorServidor(Socket socket, ConcurrentHashMap<Integer, ManejadorServidor> clientes, Connection conexion) throws IOException {
        this.socket = socket;
        this.clientes = clientes;
        this.conexion = conexion;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
     * Método principal que maneja la comunicación con el cliente.
     * Realiza la autenticación y procesa los mensajes entrantes.
     */
    @Override
    public void run() {
        try {
            // Recibe y valida las credenciales del cliente
            String email = in.readLine();
            String contrasena = in.readLine();
            
            System.out.println("Cliente intentando autenticarse: " + email);
            
            // Verifica las credenciales en la base de datos
            String sql = "SELECT id, nombre, es_admin FROM usuarios WHERE email = ? AND password = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, contrasena);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    // Autenticación exitosa
                    this.idUsuario = rs.getInt("id");
                    this.nombreUsuario = rs.getString("nombre");
                    out.println("OK");
                    
                    System.out.println("Cliente autenticado exitosamente: " + nombreUsuario);
                    System.out.println("Dirección IP: " + socket.getInetAddress().getHostAddress());
                    
                    // Notifica a otros clientes sobre la nueva conexión
                    difundirMensaje(nombreUsuario + " se ha unido al chat");
                    
                    // Procesa mensajes del cliente
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        System.out.println("Mensaje recibido de " + nombreUsuario + ": " + mensaje);
                        difundirMensaje(nombreUsuario + ": " + mensaje);
                    }
                } else {
                    // Autenticación fallida
                    System.out.println("Error de autenticación para el usuario: " + email);
                    out.println("ERROR: Credenciales inválidas");
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        } finally {
            // Limpia los recursos al desconectar
            try {
                socket.close();
                clientes.remove(idUsuario);
                if (nombreUsuario != null) {
                    System.out.println("Cliente desconectado: " + nombreUsuario);
                    difundirMensaje(nombreUsuario + " ha abandonado el chat");
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Envía un mensaje a todos los clientes conectados excepto al remitente.
     * @param mensaje Contenido del mensaje a difundir
     */
    private void difundirMensaje(String mensaje) {
        for (ManejadorServidor cliente : clientes.values()) {
            if (cliente != this) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }
    
    /**
     * Envía un mensaje al cliente asociado a este manejador.
     * @param mensaje Contenido del mensaje a enviar
     */
    public void enviarMensaje(String mensaje) {
        out.println(mensaje);
    }
    
    /**
     * Obtiene el ID del usuario asociado a este manejador.
     * @return ID del usuario
     */
    public int getIdUsuario() {
        return idUsuario;
    }
} 
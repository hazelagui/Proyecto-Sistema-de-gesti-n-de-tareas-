package com.mycompany.sistemagestiondetareas.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase principal del servidor que maneja las conexiones de clientes.
 * Implementa un sistema de gestión de conexiones con límite máximo.
 */
public class Servidor {
    // Configuración del servidor
    private static final int PUERTO = 50;           // Puerto donde escucha el servidor
    private static final int MAX_CONEXIONES = 5;      // Límite máximo de conexiones simultáneas
    
    // Almacenamiento de clientes conectados
    private final ConcurrentHashMap<Integer, ManejadorServidor> clientes = new ConcurrentHashMap<>();
    private final Connection conexion;                 // Conexión a la base de datos
    
    /**
     * Constructor que inicializa la conexión a la base de datos.
     */
    public Servidor() throws SQLException {
        this.conexion = ConexionBD.obtenerConexion();
    }
    
    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     * Maneja el límite máximo de conexiones y muestra el estado en consola.
     */
    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            System.out.println("Esperando conexiones de clientes...");
            
            while (true) {
                // Espera y acepta nuevas conexiones
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nueva conexión entrante desde " + socketCliente.getInetAddress().getHostAddress());
                
                // Verifica si se ha alcanzado el límite de conexiones
                if (clientes.size() >= MAX_CONEXIONES) {
                    System.out.println("Límite de conexiones alcanzado (" + MAX_CONEXIONES + "). Rechazando nueva conexión.");
                    socketCliente.close();
                    continue;
                }
                
                // Crea un nuevo manejador para el cliente y lo inicia en un hilo separado
                ManejadorServidor manejador = new ManejadorServidor(socketCliente, clientes, conexion);
                clientes.put(manejador.getIdUsuario(), manejador);
                new Thread(manejador).start();
                
                // Muestra el estado actual de las conexiones
                System.out.println("Total de clientes conectados: " + clientes.size() + "/" + MAX_CONEXIONES);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            // Cierra la conexión a la base de datos al terminar
            try {
                if (conexion != null) conexion.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }
    
    /**
     * Punto de entrada principal para iniciar el servidor.
     */
    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor();
            servidor.iniciar();
        } catch (SQLException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
} 
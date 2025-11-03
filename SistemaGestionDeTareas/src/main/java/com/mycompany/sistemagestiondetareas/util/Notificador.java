package com.mycompany.sistemagestiondetareas.util;

import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de gestionar y enviar notificaciones a los usuarios.
 * Implementa un sistema de notificaciones múltiples (email, base de datos y en tiempo real).
 */
public class Notificador {
    private static final Logger LOGGER = Logger.getLogger(Notificador.class.getName());
    private final ConcurrentHashMap<Integer, Cliente> clientesConectados;  // Clientes actualmente conectados
    private final EmailSender emailSender;                                 // Servicio de envío de emails
    
    /**
     * Constructor que inicializa el sistema de notificaciones.
     * @param clientesConectados Mapa de clientes conectados para notificaciones en tiempo real
     */
    public Notificador(ConcurrentHashMap<Integer, Cliente> clientesConectados) {
        this.clientesConectados = clientesConectados;
        this.emailSender = new EmailSender();
    }
    
    /**
     * Notifica el cambio de estado de una tarea al responsable.
     * @param tarea Tarea que ha cambiado de estado
     * @param estadoAnterior Estado previo de la tarea
     */
    public void notificarCambioEstadoTarea(Tarea tarea, String estadoAnterior) {
        // Construye el mensaje de notificación
        String mensaje = String.format(
            "El estado de la tarea '%s' ha cambiado de '%s' a '%s'",
            tarea.getNombre(),
            estadoAnterior,
            tarea.getEstado()
        );
        
        // Obtiene el email del usuario responsable
        try (Connection conn = ConexionBD.obtenerConexion()) {
            String sql = "SELECT email FROM usuarios WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tarea.getIdResponsable());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String email = rs.getString("email");
                        enviarNotificacion(tarea.getIdResponsable(), mensaje, email);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener email del usuario", e);
        }
    }
    
    /**
     * Envía una notificación por todos los canales disponibles.
     * @param idUsuario ID del usuario destinatario
     * @param mensaje Contenido de la notificación
     * @param email Dirección de email del usuario
     */
    private void enviarNotificacion(int idUsuario, String mensaje, String email) {
        // Guarda la notificación en la base de datos
        try (Connection conn = ConexionBD.obtenerConexion()) {
            String sql = "INSERT INTO notificaciones (id_usuario, mensaje, fecha, leida) VALUES (?, ?, ?, false)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUsuario);
                stmt.setString(2, mensaje);
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al guardar notificación en la base de datos", e);
        }
        
        // Envía notificación por email si hay dirección disponible
        if (email != null && !email.isEmpty()) {
            try {
                emailSender.enviarCorreo(email, "Notificación del Sistema", mensaje);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al enviar notificación por email", e);
            }
        }
        
        // Envía notificación en tiempo real si el usuario está conectado
        Cliente cliente = clientesConectados.get(idUsuario);
        if (cliente != null) {
            try {
                cliente.enviarMensaje(mensaje);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al enviar notificación a cliente conectado", e);
            }
        }
    }
} 
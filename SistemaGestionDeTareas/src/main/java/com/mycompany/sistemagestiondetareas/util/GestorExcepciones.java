package com.mycompany.sistemagestiondetareas.util;

import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Clase utilitaria para gestionar excepciones en la aplicación.
 */
public class GestorExcepciones {
    
    /**
     * Gestiona una excepción de SQL mostrando un mensaje de error al usuario.
     * @param ex Excepción de SQL.
     * @param mensaje Mensaje adicional sobre la operación que falló.
     */
    public static void gestionarExcepcionSQL(SQLException ex, String mensaje) {
        String mensajeError = mensaje + "\nError: " + ex.getMessage();
        System.err.println(mensajeError);
        System.err.println("Código SQL: " + ex.getErrorCode());
        System.err.println("Estado SQL: " + ex.getSQLState());
        
        // Mostrar un mensaje de error al usuario
        JOptionPane.showMessageDialog(null, 
                mensajeError, 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Gestiona una excepción general mostrando un mensaje de error al usuario.
     * @param ex Excepción general.
     * @param mensaje Mensaje adicional sobre la operación que falló.
     */
    public static void gestionarExcepcion(Exception ex, String mensaje) {
        String mensajeError = mensaje + "\nError: " + ex.getMessage();
        System.err.println(mensajeError);
        ex.printStackTrace();
        
        // Mostrar un mensaje de error al usuario
        JOptionPane.showMessageDialog(null, 
                mensajeError, 
                "Error de Aplicación", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Registra un error sin mostrar un mensaje al usuario.
     * @param ex Excepción que se registrará.
     * @param mensaje Mensaje adicional sobre el error.
     */
    public static void registrarError(Exception ex, String mensaje) {
        System.err.println(mensaje + ": " + ex.getMessage());
        ex.printStackTrace();
    }
} 
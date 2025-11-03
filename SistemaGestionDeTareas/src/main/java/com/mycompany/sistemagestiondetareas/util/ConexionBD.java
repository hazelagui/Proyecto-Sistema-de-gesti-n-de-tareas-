package com.mycompany.sistemagestiondetareas.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase utilitaria para gestionar la conexión a la base de datos MySQL.
 * 
 * - Carga la configuración desde src/main/resources/db.properties.
 * - Si el archivo no existe, usa valores por defecto.
 * - Compatible con MySQL 8.0+ (usa caching_sha2_password).
 */
public class ConexionBD {

    private static String URL;
    private static String USUARIO;
    private static String PASSWORD;
    private static Connection conexion = null;

    // Carga las propiedades al iniciar la clase
    static {
        try {
            cargarPropiedades();
        } catch (IOException e) {
            System.err.println("⚠️ No se encontró db.properties. Usando configuración por defecto.");
            URL = "jdbc:mysql://localhost:3306/gestion_tareas?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            USUARIO = "root";
            PASSWORD = "admin";
        }
    }

    /** Constructor privado: evita instanciación */
    private ConexionBD() {}

    /**
     * Carga la configuración desde el archivo db.properties.
     */
    private static void cargarPropiedades() throws IOException {
        Properties props = new Properties();

        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IOException("Archivo db.properties no encontrado en resources/");
            }

            props.load(input);
            URL = props.getProperty("db.url");
            USUARIO = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            if (URL == null || USUARIO == null || PASSWORD == null) {
                throw new IOException("Propiedades incompletas en db.properties");
            }
        }
    }

    /**
     * Devuelve una conexión activa a la base de datos.
     */
    public static Connection obtenerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("✅ Conexión a la base de datos establecida correctamente.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("❌ No se encontró el driver de MySQL.", e);
            } catch (SQLException e) {
                throw new SQLException("❌ Error al conectar con la base de datos: " + e.getMessage(), e);
            }
        }
        return conexion;
    }

    /**
     * Cierra la conexión si está abierta.
     */
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("🔒 Conexión a la base de datos cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}

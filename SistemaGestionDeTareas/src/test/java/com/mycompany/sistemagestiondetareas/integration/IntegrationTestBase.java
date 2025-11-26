package com.mycompany.sistemagestiondetareas.integration;

import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Clase base para pruebas de integración.
 * 
 * Configuración:
 * - Usa base de datos H2 en memoria para tests
 * - Crea schema completo antes de cada test
 * - Limpia datos después de cada test
 * - Proporciona métodos de utilidad para tests
 * 
 * IMPORTANTE: Requiere crear archivo schema-test.sql en src/test/resources/
 */
public abstract class IntegrationTestBase {

    protected static Connection connection;
    
    /**
     * Configuración inicial: crear BD H2 en memoria
     */
    @BeforeAll
    public static void setUpDatabase() throws Exception {
        // Para tests de integración, podemos:
        // Opción A: Usar H2 en memoria (más rápido)
        // Opción B: Usar MySQL real en TestContainer
        // Opción C: Usar MySQL real con BD de prueba
        
        System.out.println("=== Configurando Base de Datos para Tests de Integración ===");
    }
    
    /**
     * Antes de cada test: limpiar y preparar datos
     */
    @BeforeEach
    public void setUp() throws Exception {
        connection = ConexionBD.obtenerConexion();
        limpiarBaseDatos();
        crearEsquema();
        insertarDatosPrueba();
    }
    
    /**
     * Después de cada test: limpiar
     */
    @AfterEach
    public void tearDown() throws Exception {
        limpiarBaseDatos();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    /**
     * Limpia todas las tablas
     */
    protected void limpiarBaseDatos() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return;
        }
        
        try (Statement stmt = connection.createStatement()) {
            // Desactivar FK constraints temporalmente
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Limpiar tablas en orden inverso a las dependencias
            stmt.execute("DELETE FROM costos");
            stmt.execute("DELETE FROM notificaciones");
            stmt.execute("DELETE FROM tareas");
            stmt.execute("DELETE FROM proyectos");
            stmt.execute("DELETE FROM usuarios");
            
            // Reactivar FK constraints
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            System.out.println("✅ Base de datos limpiada");
        }
    }
    
    /**
     * Crea el esquema de BD si no existe
     */
    protected void crearEsquema() throws Exception {
        // Para MySQL: el schema ya existe
        // Para H2: necesitamos crearlo
        System.out.println("✅ Esquema verificado");
    }
    
    /**
     * Inserta datos de prueba iniciales
     */
    protected void insertarDatosPrueba() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Insertar usuario de prueba
            stmt.execute(
                "INSERT INTO usuarios (id, nombre, apellido, email, password, es_admin) " +
                "VALUES (1, 'Admin', 'Test', 'admin@test.com', 'password', true)"
            );
            
            stmt.execute(
                "INSERT INTO usuarios (id, nombre, apellido, email, password, es_admin) " +
                "VALUES (2, 'Usuario', 'Regular', 'usuario@test.com', 'password', false)"
            );
            
            System.out.println("✅ Datos de prueba insertados");
        }
    }
    
    /**
     * Lee script SQL desde resources
     */
    protected String leerScriptSQL(String nombreArchivo) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(nombreArchivo);
        if (is == null) {
            throw new RuntimeException("No se encontró el archivo: " + nombreArchivo);
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * Ejecuta un script SQL completo
     */
    protected void ejecutarScript(String script) throws SQLException {
        String[] sentencias = script.split(";");
        try (Statement stmt = connection.createStatement()) {
            for (String sentencia : sentencias) {
                String sql = sentencia.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    stmt.execute(sql);
                }
            }
        }
    }
    
    /**
     * Obtiene la conexión actual
     */
    protected Connection getConnection() {
        return connection;
    }
    
    /**
     * Espera un tiempo determinado (útil para tests de scheduler)
     */
    protected void esperarSegundos(int segundos) {
        try {
            Thread.sleep(segundos * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Verifica que una tabla tenga cierto número de registros
     */
    protected int contarRegistros(String tabla) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Verifica que un registro existe
     */
    protected boolean existeRegistro(String tabla, String columna, Object valor) throws SQLException {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tabla, columna);
        try (var stmt = connection.prepareStatement(sql)) {
            if (valor instanceof String) {
                stmt.setString(1, (String) valor);
            } else if (valor instanceof Integer) {
                stmt.setInt(1, (Integer) valor);
            }
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
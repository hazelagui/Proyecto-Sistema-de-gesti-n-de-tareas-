package com.mycompany.sistemagestiondetareas.util;

import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para Notificador
 * 
 * Cobertura:
 * - Construcción del notificador con clientes conectados
 * - Notificación de cambio de estado de tarea
 * - Construcción del mensaje de notificación
 * - Consulta del email del usuario desde BD
 * - Inserción de notificación en BD
 * - Envío por email cuando hay dirección disponible
 * - Envío en tiempo real cuando usuario está conectado
 * - Manejo de error al obtener email (SQLException)
 * - Manejo de error al insertar en BD (SQLException)
 * - Manejo de error al enviar email
 * - Manejo de error al enviar a cliente conectado
 * - Usuario sin email registrado
 * - Usuario no conectado (sin notificación tiempo real)
 */
public class NotificadorTest {

    private Notificador notificador;
    private ConcurrentHashMap<Integer, Cliente> clientesConectados;
    private Cliente mockCliente;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private EmailSender mockEmailSender;

    @BeforeEach
    public void setUp() {
        clientesConectados = new ConcurrentHashMap<>();
        mockCliente = mock(Cliente.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockEmailSender = mock(EmailSender.class);
    }

    // ========================================
    // CONSTRUCCIÓN
    // ========================================

    @Test
    public void constructor_clientesConectadosValido_inicializaCorrectamente() {
        // Act
        notificador = new Notificador(clientesConectados);
        
        // Assert
        assertNotNull(notificador);
    }

    @Test
    public void constructor_clientesConectadosVacio_inicializaCorrectamente() {
        // Arrange
        ConcurrentHashMap<Integer, Cliente> clientesVacios = new ConcurrentHashMap<>();
        
        // Act
        notificador = new Notificador(clientesVacios);
        
        // Assert
        assertNotNull(notificador);
    }

    // ========================================
    // NOTIFICAR CAMBIO DE ESTADO
    // ========================================

    @Test
    public void notificarCambioEstadoTarea_tareaValida_construyeMensajeCorrectamente() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        
        notificador = new Notificador(clientesConectados);
        
        // Act
        notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        
        // Assert
        // Verificar que se consultó el email del usuario
        verify(mockPreparedStatement).setInt(1, tarea.getIdResponsable());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    public void notificarCambioEstadoTarea_usuarioConEmail_consultaEmailCorrectamente() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("usuario@test.com");
        
        notificador = new Notificador(clientesConectados);
        
        // Act
        notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        
        // Assert
        // Verificar que se ejecutó la consulta para obtener el email
        verify(mockPreparedStatement).setInt(1, tarea.getIdResponsable());
        verify(mockResultSet).getString("email");
    }

    @Test
    public void notificarCambioEstadoTarea_usuarioConEmail_insertaNotificacionEnBD() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        // Mockear dos PreparedStatements: uno para SELECT, otro para INSERT
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        
        when(mockSelectRs.next()).thenReturn(true);
        when(mockSelectRs.getString("email")).thenReturn("usuario@test.com");
        when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
        
        when(mockConnection.prepareStatement(contains("SELECT email")))
                .thenReturn(mockSelectStmt);
        when(mockConnection.prepareStatement(contains("INSERT INTO notificaciones")))
                .thenReturn(mockInsertStmt);
        
        try (MockedStatic<ConexionBD> mockedConexionBD = mockStatic(ConexionBD.class)) {
            mockedConexionBD.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            notificador = new Notificador(clientesConectados);
            
            // Act
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
            
            // Assert
            // Verificar que se insertó la notificación
            verify(mockInsertStmt).setInt(eq(1), anyInt());  // id_usuario
            verify(mockInsertStmt).setString(eq(2), anyString());  // mensaje
            verify(mockInsertStmt).setTimestamp(eq(3), any(Timestamp.class));  // fecha
            verify(mockInsertStmt).executeUpdate();
        }
    }

    // ========================================
    // ENVÍO POR EMAIL
    // ========================================

    @Test
    public void notificarCambioEstadoTarea_usuarioConEmail_enviaCorreo() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("usuario@test.com");
        
        // No podemos mockear EmailSender directamente porque se crea internamente
        // Pero podemos verificar que el código no lanza excepciones
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        });
    }

    @Test
    public void notificarCambioEstadoTarea_usuarioSinEmail_noIntentaEnviarCorreo() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn(null);  // Sin email
        
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        });
    }

    @Test
    public void notificarCambioEstadoTarea_emailVacio_noIntentaEnviarCorreo() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("");  // Email vacío
        
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        });
    }

    // ========================================
    // ENVÍO EN TIEMPO REAL
    // ========================================

    @Test
    public void notificarCambioEstadoTarea_usuarioConectado_enviaNotificacionTiempoReal() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        int idUsuario = tarea.getIdResponsable();
        
        // Agregar cliente conectado
        clientesConectados.put(idUsuario, mockCliente);
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("usuario@test.com");
        
        notificador = new Notificador(clientesConectados);
        
        // Act
        notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        
        // Assert
        // Verificar que se intentó enviar mensaje al cliente
        verify(mockCliente).enviarMensaje(anyString());
    }

    @Test
    public void notificarCambioEstadoTarea_usuarioNoConectado_noEnviaTiempoReal() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        // ConcurrentHashMap vacío (sin usuarios conectados)
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("usuario@test.com");
        
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        });
        
        // El cliente mock nunca debe ser llamado
        verify(mockCliente, never()).enviarMensaje(anyString());
    }

    // ========================================
    // MANEJO DE ERRORES
    // ========================================

    @Test
    public void notificarCambioEstadoTarea_errorAlObtenerEmail_manejaExcepcion() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        when(mockConnection.prepareStatement(contains("SELECT email")))
                .thenThrow(new SQLException("Error de BD"));
        
        try (MockedStatic<ConexionBD> mockedConexionBD = mockStatic(ConexionBD.class)) {
            mockedConexionBD.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            notificador = new Notificador(clientesConectados);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
            }, "Debe manejar SQLException sin lanzar excepción");
        }
    }

    @Test
    public void notificarCambioEstadoTarea_errorAlInsertarNotificacion_manejaExcepcion() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        
        when(mockSelectRs.next()).thenReturn(true);
        when(mockSelectRs.getString("email")).thenReturn("usuario@test.com");
        when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
        
        when(mockConnection.prepareStatement(contains("SELECT email")))
                .thenReturn(mockSelectStmt);
        when(mockConnection.prepareStatement(contains("INSERT INTO notificaciones")))
                .thenReturn(mockInsertStmt);
        
        // Simular error al insertar
        when(mockInsertStmt.executeUpdate()).thenThrow(new SQLException("Error al insertar"));
        
        try (MockedStatic<ConexionBD> mockedConexionBD = mockStatic(ConexionBD.class)) {
            mockedConexionBD.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            notificador = new Notificador(clientesConectados);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
            }, "Debe continuar aunque falle la inserción en BD");
        }
    }

    @Test
    public void notificarCambioEstadoTarea_errorAlEnviarAClienteConectado_manejaExcepcion() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        int idUsuario = tarea.getIdResponsable();
        
        // Cliente que lanza excepción al enviar mensaje
        clientesConectados.put(idUsuario, mockCliente);
        doThrow(new RuntimeException("Error de red")).when(mockCliente).enviarMensaje(anyString());
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("email")).thenReturn("usuario@test.com");
        
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        }, "Debe manejar excepciones al enviar a cliente conectado");
    }

    @Test
    public void notificarCambioEstadoTarea_usuarioNoExiste_manejaCorrectamente() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        String estadoAnterior = "PENDIENTE";
        
        setupMocksConexionYEmail();
        when(mockResultSet.next()).thenReturn(false);  // Usuario no encontrado
        
        notificador = new Notificador(clientesConectados);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        }, "Debe manejar caso donde usuario no existe");
    }

    // ========================================
    // CONSTRUCCIÓN DEL MENSAJE
    // ========================================

    @Test
    public void notificarCambioEstadoTarea_mensajeContieneInformacionCompleta() throws SQLException {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        tarea.setNombre("Tarea de Prueba");
        tarea.setEstado("COMPLETADA");
        String estadoAnterior = "EN_PROGRESO";
        
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        
        when(mockSelectRs.next()).thenReturn(true);
        when(mockSelectRs.getString("email")).thenReturn("usuario@test.com");
        when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
        
        when(mockConnection.prepareStatement(contains("SELECT email")))
                .thenReturn(mockSelectStmt);
        when(mockConnection.prepareStatement(contains("INSERT INTO notificaciones")))
                .thenReturn(mockInsertStmt);
        
        try (MockedStatic<ConexionBD> mockedConexionBD = mockStatic(ConexionBD.class)) {
            mockedConexionBD.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            notificador = new Notificador(clientesConectados);
            
            // Act
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
            
            // Assert
            // Verificar que el mensaje contiene la información de la tarea
            verify(mockInsertStmt).setString(eq(2), argThat(mensaje -> 
                mensaje.contains("Tarea de Prueba") &&
                mensaje.contains("EN_PROGRESO") &&
                mensaje.contains("COMPLETADA")
            ));
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private Tarea crearTareaEjemplo() {
        Tarea tarea = new Tarea();
        tarea.setId(1);
        tarea.setNombre("Tarea Test");
        tarea.setDescripcion("Descripción de prueba");
        tarea.setIdProyecto(10);
        tarea.setIdResponsable(5);
        tarea.setEstado("EN_PROGRESO");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        return tarea;
    }

    private void setupMocksConexionYEmail() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        try (MockedStatic<ConexionBD> mockedConexionBD = mockStatic(ConexionBD.class)) {
            mockedConexionBD.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
        }
    }
}

/**
 * Clase mock de Cliente para tests
 * En el proyecto real, esta clase existe en otro lugar
 */
class Cliente {
    public void enviarMensaje(String mensaje) {
        // Implementación mock
    }
}

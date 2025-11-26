package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para TareaDAO - Versión corregida
 */
public class TareaDAOTest {

    private TareaDAO tareaDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        tareaDAO = new TareaDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
    }

    // ========================================
    // INSERTAR
    // ========================================

    @Test
    public void insertar_tareaValida_generaIDCorrectamente() throws SQLException {
        // Arrange
        Tarea tarea = new Tarea();
        tarea.setNombre("Tarea de prueba");
        tarea.setDescripcion("Descripción detallada");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(5);
        tarea.setIdResponsable(3);
        tarea.setEstado("PENDIENTE");
        tarea.setComentarios("Comentario inicial");
        
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(100);
        
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Tarea resultado = tareaDAO.insertar(tarea);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(100, resultado.getId());
            
            verify(mockPreparedStatement).setString(1, "Tarea de prueba");
            verify(mockPreparedStatement).setString(2, "Descripción detallada");
            verify(mockPreparedStatement).setTimestamp(eq(3), any(Timestamp.class));
            verify(mockPreparedStatement).setTimestamp(eq(4), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(5, 5);
            verify(mockPreparedStatement).setInt(6, 3);
            verify(mockPreparedStatement).setString(7, "PENDIENTE");
            verify(mockPreparedStatement).setString(8, "Comentario inicial");
        }
    }

    @Test
    public void insertar_errorSQL_retornaNull() throws SQLException {
        // Arrange
        Tarea tarea = new Tarea();
        tarea.setNombre("Test");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(9999);
        tarea.setIdResponsable(8888);
        
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("FK violation"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Tarea resultado = tareaDAO.insertar(tarea);
            
            // Assert
            assertNull(resultado);
        }
    }

    // ========================================
    // ACTUALIZAR
    // ========================================

    @Test
    public void actualizar_tareaValida_retornaTrue() throws SQLException {
        // Arrange
        Tarea tarea = new Tarea();
        tarea.setId(25);
        tarea.setNombre("Tarea actualizada");
        tarea.setDescripcion("Nueva descripción");
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(10);
        tarea.setIdResponsable(15);
        tarea.setEstado("EN_PROGRESO");
        tarea.setComentarios("Comentarios actualizados");
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizar(tarea);
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setString(1, "Tarea actualizada");
            verify(mockPreparedStatement).setInt(8, 25);
        }
    }

    @Test
    public void actualizar_tareaNoExiste_retornaFalse() throws SQLException {
        // Arrange
        Tarea tarea = new Tarea();
        tarea.setId(9999);
        tarea.setNombre("Test");
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(1);
        tarea.setIdResponsable(1);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizar(tarea);
            
            // Assert
            assertFalse(resultado);
        }
    }

    // ========================================
    // ACTUALIZAR ESTADO CON CONCATENACIÓN
    // ========================================

    @Test
    public void actualizarEstado_sinComentario_actualizaSoloEstado() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizarEstado(10, "COMPLETADA", null);
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setString(1, "COMPLETADA");
            verify(mockPreparedStatement).setString(2, "");
            verify(mockPreparedStatement).setInt(3, 10);
        }
    }

    @Test
    public void actualizarEstado_conComentario_concatenaConSaltoLinea() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizarEstado(10, "EN_PROGRESO", "Iniciando desarrollo");
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setString(1, "EN_PROGRESO");
            verify(mockPreparedStatement).setString(2, "\nIniciando desarrollo");
            verify(mockPreparedStatement).setInt(3, 10);
        }
    }

    @Test
    public void actualizarEstado_comentarioVacio_noAgregaSaltoLinea() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizarEstado(10, "EN_PROGRESO", "");
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setString(2, "");
        }
    }

    @Test
    public void actualizarEstado_errorSQL_retornaFalse() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Update failed"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.actualizarEstado(10, "COMPLETADA", "Test");
            
            // Assert
            assertFalse(resultado);
        }
    }

    // ========================================
    // ELIMINAR
    // ========================================

    @Test
    public void eliminar_tareaExiste_retornaTrue() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.eliminar(15);
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setInt(1, 15);
        }
    }

    @Test
    public void eliminar_tareaNoExiste_retornaFalse() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = tareaDAO.eliminar(9999);
            
            // Assert
            assertFalse(resultado);
        }
    }

    // ========================================
    // BUSCAR POR ID
    // ========================================

    @Test
    public void buscarPorId_tareaExiste_retornaTareaCompleta() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(42);
        when(mockResultSet.getString("nombre")).thenReturn("Tarea Importante");
        when(mockResultSet.getString("descripcion")).thenReturn("Descripción detallada");
        when(mockResultSet.getTimestamp("fecha_creacion")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_vencimiento")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_proyecto")).thenReturn(10);
        when(mockResultSet.getInt("id_responsable")).thenReturn(5);
        when(mockResultSet.getString("estado")).thenReturn("EN_PROGRESO");
        when(mockResultSet.getString("comentarios")).thenReturn("Comentarios de la tarea");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Tarea resultado = tareaDAO.buscarPorId(42);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(42, resultado.getId());
            assertEquals("Tarea Importante", resultado.getNombre());
            assertEquals("EN_PROGRESO", resultado.getEstado());
        }
    }

    @Test
    public void buscarPorId_tareaNoExiste_retornaNull() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Tarea resultado = tareaDAO.buscarPorId(9999);
            
            // Assert
            assertNull(resultado);
        }
    }

    // ========================================
    // LISTAR POR PROYECTO
    // ========================================

    @Test
    public void listarPorProyecto_proyectoConTareas_retornaListaFiltrada() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("nombre")).thenReturn("Tarea 1", "Tarea 2");
        when(mockResultSet.getString("descripcion")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getTimestamp("fecha_creacion"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_vencimiento"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_proyecto")).thenReturn(5);
        when(mockResultSet.getInt("id_responsable")).thenReturn(3, 4);
        when(mockResultSet.getString("estado")).thenReturn("PENDIENTE", "EN_PROGRESO");
        when(mockResultSet.getString("comentarios")).thenReturn("", "");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = tareaDAO.listarPorProyecto(5);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(mockPreparedStatement).setInt(1, 5);
        }
    }

    @Test
    public void listarPorProyecto_proyectoSinTareas_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = tareaDAO.listarPorProyecto(999);
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    // ========================================
    // LISTAR POR RESPONSABLE
    // ========================================

    @Test
    public void listarPorResponsable_responsableConTareas_retornaListaFiltrada() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(10, 11);
        when(mockResultSet.getString("nombre")).thenReturn("Tarea A", "Tarea B");
        when(mockResultSet.getString("descripcion")).thenReturn("Desc A", "Desc B");
        when(mockResultSet.getTimestamp("fecha_creacion"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_vencimiento"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_proyecto")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_responsable")).thenReturn(7);
        when(mockResultSet.getString("estado")).thenReturn("PENDIENTE", "COMPLETADA");
        when(mockResultSet.getString("comentarios")).thenReturn("", "");
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = tareaDAO.listarPorResponsable(7);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(mockPreparedStatement).setInt(1, 7);
        }
    }

    // ========================================
    // LISTAR TODAS
    // ========================================

    @Test
    public void listarTodas_variusTareas_retornaListaCompleta() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("nombre")).thenReturn("Tarea 1", "Tarea 2");
        when(mockResultSet.getString("descripcion")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getTimestamp("fecha_creacion"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_vencimiento"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_proyecto")).thenReturn(1, 2);
        when(mockResultSet.getInt("id_responsable")).thenReturn(3, 4);
        when(mockResultSet.getString("estado")).thenReturn("PENDIENTE", "COMPLETADA");
        when(mockResultSet.getString("comentarios")).thenReturn("", "Comentarios");
        
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = tareaDAO.listarTodas();
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
        }
    }

    @Test
    public void listarTodas_bdVacia_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = tareaDAO.listarTodas();
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }
}
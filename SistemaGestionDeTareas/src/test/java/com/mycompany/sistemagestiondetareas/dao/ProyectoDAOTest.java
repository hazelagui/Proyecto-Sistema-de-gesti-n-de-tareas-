package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
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
 * Pruebas unitarias para ProyectoDAO - Versión corregida
 */
public class ProyectoDAOTest {

    private ProyectoDAO proyectoDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        proyectoDAO = new ProyectoDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
    }

    // ========================================
    // INSERTAR
    // ========================================

    @Test
    public void insertar_proyectoValido_generaIDCorrectamente() throws SQLException {
        // Arrange
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Test");
        proyecto.setDescripcion("Descripción del proyecto");
        proyecto.setFechaInicio(new Date());
        proyecto.setFechaFin(new Date());
        proyecto.setIdResponsable(5);
        proyecto.setNivelRiesgo("MEDIO");
        proyecto.setPresupuestoTotal(50000.0);
        
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
            
            Proyecto resultado = proyectoDAO.insertar(proyecto);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(100, resultado.getId());
            
            // Verificar parámetros
            verify(mockPreparedStatement).setString(1, "Proyecto Test");
            verify(mockPreparedStatement).setString(2, "Descripción del proyecto");
            verify(mockPreparedStatement).setTimestamp(eq(3), any(Timestamp.class));
            verify(mockPreparedStatement).setTimestamp(eq(4), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(5, 5);
            verify(mockPreparedStatement).setString(6, "MEDIO");
            verify(mockPreparedStatement).setDouble(7, 50000.0);
        }
    }

    @Test
    public void insertar_proyectoConFechaFinNull_guardaNull() throws SQLException {
        // Arrange
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Sin Fecha Fin");
        proyecto.setDescripcion("Descripción");
        proyecto.setFechaInicio(new Date());
        proyecto.setFechaFin(null);
        proyecto.setIdResponsable(3);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(10000.0);
        
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(50);
        
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Proyecto resultado = proyectoDAO.insertar(proyecto);
            
            // Assert
            assertNotNull(resultado);
            verify(mockPreparedStatement).setTimestamp(4, null);
        }
    }

    @Test
    public void insertar_errorSQL_retornaNull() throws SQLException {
        // Arrange
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Test");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(1000.0);
        
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Error de conexión"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Proyecto resultado = proyectoDAO.insertar(proyecto);
            
            // Assert
            assertNull(resultado);
        }
    }

    // ========================================
    // ACTUALIZAR
    // ========================================

    @Test
    public void actualizar_proyectoValido_retornaTrue() throws SQLException {
        // Arrange
        Proyecto proyecto = new Proyecto();
        proyecto.setId(25);
        proyecto.setNombre("Proyecto Actualizado");
        proyecto.setDescripcion("Nueva descripción");
        proyecto.setFechaInicio(new Date());
        proyecto.setFechaFin(new Date());
        proyecto.setIdResponsable(7);
        proyecto.setNivelRiesgo("ALTO");
        proyecto.setPresupuestoTotal(75000.0);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = proyectoDAO.actualizar(proyecto);
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setString(1, "Proyecto Actualizado");
            verify(mockPreparedStatement).setInt(8, 25);
        }
    }

    @Test
    public void actualizar_proyectoNoExiste_retornaFalse() throws SQLException {
        // Arrange
        Proyecto proyecto = new Proyecto();
        proyecto.setId(9999);
        proyecto.setNombre("Test");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(1000.0);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = proyectoDAO.actualizar(proyecto);
            
            // Assert
            assertFalse(resultado);
        }
    }

    // ========================================
    // ELIMINAR
    // ========================================

    @Test
    public void eliminar_proyectoExiste_retornaTrue() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = proyectoDAO.eliminar(15);
            
            // Assert
            assertTrue(resultado);
            verify(mockPreparedStatement).setInt(1, 15);
        }
    }

    @Test
    public void eliminar_proyectoNoExiste_retornaFalse() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = proyectoDAO.eliminar(9999);
            
            // Assert
            assertFalse(resultado);
        }
    }

    // ========================================
    // BUSCAR POR ID
    // ========================================

    @Test
    public void buscarPorId_proyectoExiste_retornaProyectoCompleto() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(10);
        when(mockResultSet.getString("nombre")).thenReturn("Proyecto Alpha");
        when(mockResultSet.getString("descripcion")).thenReturn("Descripción completa");
        when(mockResultSet.getTimestamp("fecha_inicio")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_fin")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_responsable")).thenReturn(3);
        when(mockResultSet.getString("nivel_riesgo")).thenReturn("MEDIO");
        when(mockResultSet.getDouble("presupuesto_total")).thenReturn(50000.0);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Proyecto resultado = proyectoDAO.buscarPorId(10);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(10, resultado.getId());
            assertEquals("Proyecto Alpha", resultado.getNombre());
            assertEquals("MEDIO", resultado.getNivelRiesgo());
            assertEquals(50000.0, resultado.getPresupuestoTotal());
        }
    }

    @Test
    public void buscarPorId_proyectoNoExiste_retornaNull() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Proyecto resultado = proyectoDAO.buscarPorId(9999);
            
            // Assert
            assertNull(resultado);
        }
    }

    // ========================================
    // LISTAR POR RESPONSABLE
    // ========================================

    @Test
    public void listarPorResponsable_responsableConProyectos_retornaLista() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("nombre")).thenReturn("Proyecto 1", "Proyecto 2");
        when(mockResultSet.getString("descripcion")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getTimestamp("fecha_inicio"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_fin"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_responsable")).thenReturn(5);
        when(mockResultSet.getString("nivel_riesgo")).thenReturn("BAJO", "MEDIO");
        when(mockResultSet.getDouble("presupuesto_total")).thenReturn(10000.0, 20000.0);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = proyectoDAO.listarPorResponsable(5);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(mockPreparedStatement).setInt(1, 5);
        }
    }

    @Test
    public void listarPorResponsable_responsableSinProyectos_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = proyectoDAO.listarPorResponsable(999);
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    // ========================================
    // LISTAR TODOS
    // ========================================

    @Test
    public void listarTodos_variusProyectos_retornaListaCompleta() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("nombre")).thenReturn("Proyecto A", "Proyecto B");
        when(mockResultSet.getString("descripcion")).thenReturn("Desc A", "Desc B");
        when(mockResultSet.getTimestamp("fecha_inicio"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("fecha_fin"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_responsable")).thenReturn(1, 2);
        when(mockResultSet.getString("nivel_riesgo")).thenReturn("BAJO", "ALTO");
        when(mockResultSet.getDouble("presupuesto_total")).thenReturn(5000.0, 15000.0);
        
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = proyectoDAO.listarTodos();
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
        }
    }

    @Test
    public void listarTodos_bdVacia_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = proyectoDAO.listarTodos();
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }
}
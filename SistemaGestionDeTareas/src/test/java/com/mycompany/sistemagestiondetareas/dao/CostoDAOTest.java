package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Costo;
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
 * Pruebas unitarias para CostoDAO - Versión corregida
 */
public class CostoDAOTest {

    private CostoDAO costoDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        costoDAO = new CostoDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
    }

    // ========================================
    // INSERTAR
    // ========================================

    @Test
    public void insertar_costoValido_generaIDCorrectamente() throws SQLException {
        // Arrange
        Costo costo = new Costo();
        costo.setTipo("PROYECTO");
        costo.setIdReferencia(10);
        costo.setDescripcion("Compra de licencias");
        costo.setMonto(1500.50);
        costo.setTipoCosto("GASTO_PLANIFICADO");
        costo.setFechaRegistro(new Date());
        costo.setIdUsuarioRegistro(5);
        
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
            
            Costo resultado = costoDAO.insertar(costo);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(100, resultado.getId());
            
            // Verificar parámetros en orden
            verify(mockPreparedStatement).setString(1, "PROYECTO");
            verify(mockPreparedStatement).setInt(2, 10);
            verify(mockPreparedStatement).setString(3, "Compra de licencias");
            verify(mockPreparedStatement).setDouble(4, 1500.50);
            verify(mockPreparedStatement).setString(5, "GASTO_PLANIFICADO");
            verify(mockPreparedStatement).setTimestamp(eq(6), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(7, 5);
        }
    }

    @Test
    public void insertar_costoTipoTarea_guardaCorrectamente() throws SQLException {
        // Arrange
        Costo costo = new Costo();
        costo.setTipo("TAREA");
        costo.setIdReferencia(25);
        costo.setDescripcion("Materiales");
        costo.setMonto(350.00);
        costo.setTipoCosto("RETRASO");
        costo.setFechaRegistro(new Date());
        costo.setIdUsuarioRegistro(3);
        
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
            
            Costo resultado = costoDAO.insertar(costo);
            
            // Assert
            assertNotNull(resultado);
            verify(mockPreparedStatement).setString(1, "TAREA");
            verify(mockPreparedStatement).setString(5, "RETRASO");
        }
    }

    @Test
    public void insertar_errorSQL_retornaNull() throws SQLException {
        // Arrange
        Costo costo = new Costo();
        costo.setTipo("PROYECTO");
        costo.setIdReferencia(1);
        costo.setMonto(100.0);
        costo.setFechaRegistro(new Date());
        costo.setIdUsuarioRegistro(1);
        
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Insert failed"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Costo resultado = costoDAO.insertar(costo);
            
            // Assert
            assertNull(resultado);
        }
    }

    // ========================================
    // LISTAR POR REFERENCIA
    // ========================================

    @Test
    public void listarPorReferencia_proyectoConCostos_retornaListaFiltrada() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("tipo")).thenReturn("PROYECTO");
        when(mockResultSet.getInt("id_referencia")).thenReturn(10);
        when(mockResultSet.getString("descripcion")).thenReturn("Costo 1", "Costo 2");
        when(mockResultSet.getDouble("monto")).thenReturn(1000.00, 500.00);
        when(mockResultSet.getString("tipo_costo"))
                .thenReturn("GASTO_PLANIFICADO", "ADELANTO");
        when(mockResultSet.getTimestamp("fecha_registro"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_usuario_registro")).thenReturn(5);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = costoDAO.listarPorReferencia("PROYECTO", 10);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(mockPreparedStatement).setString(1, "PROYECTO");
            verify(mockPreparedStatement).setInt(2, 10);
        }
    }

    @Test
    public void listarPorReferencia_tareaConCostos_retornaListaFiltrada() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(5);
        when(mockResultSet.getString("tipo")).thenReturn("TAREA");
        when(mockResultSet.getInt("id_referencia")).thenReturn(25);
        when(mockResultSet.getString("descripcion")).thenReturn("Materiales tarea");
        when(mockResultSet.getDouble("monto")).thenReturn(750.50);
        when(mockResultSet.getString("tipo_costo")).thenReturn("RETRASO");
        when(mockResultSet.getTimestamp("fecha_registro"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_usuario_registro")).thenReturn(3);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = costoDAO.listarPorReferencia("TAREA", 25);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("TAREA", resultado.get(0).getTipo());
        }
    }

    @Test
    public void listarPorReferencia_sinCostos_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = costoDAO.listarPorReferencia("PROYECTO", 9999);
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    // ========================================
    // LISTAR POR USUARIO
    // ========================================

    @Test
    public void listarPorUsuario_usuarioConCostos_retornaListaFiltrada() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        when(mockResultSet.getInt("id")).thenReturn(10, 11);
        when(mockResultSet.getString("tipo")).thenReturn("PROYECTO", "TAREA");
        when(mockResultSet.getInt("id_referencia")).thenReturn(1, 2);
        when(mockResultSet.getString("descripcion")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getDouble("monto")).thenReturn(100.00, 200.00);
        when(mockResultSet.getString("tipo_costo"))
                .thenReturn("GASTO_PLANIFICADO", "RETRASO");
        when(mockResultSet.getTimestamp("fecha_registro"))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("id_usuario_registro")).thenReturn(7);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = costoDAO.listarPorUsuario(7);
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(mockPreparedStatement).setInt(1, 7);
        }
    }

    @Test
    public void listarPorUsuario_usuarioSinCostos_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = costoDAO.listarPorUsuario(999);
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    // ========================================
    // CALCULAR TOTAL POR TIPO (SUM)
    // ========================================

    @Test
    public void calcularTotalPorTipo_conCostos_retornaSumaCorrecta() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total")).thenReturn(2500.75);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            double resultado = costoDAO.calcularTotalPorTipo("PROYECTO", 10, "RETRASO");
            
            // Assert
            assertEquals(2500.75, resultado, 0.01);
            verify(mockPreparedStatement).setString(1, "PROYECTO");
            verify(mockPreparedStatement).setInt(2, 10);
            verify(mockPreparedStatement).setString(3, "RETRASO");
        }
    }

    @Test
    public void calcularTotalPorTipo_sinCostos_retornaCero() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total")).thenReturn(0.0);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            double resultado = costoDAO.calcularTotalPorTipo("PROYECTO", 999, "RETRASO");
            
            // Assert
            assertEquals(0.0, resultado, 0.01);
        }
    }

    @Test
    public void calcularTotalPorTipo_errorSQL_retornaCero() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("SUM query failed"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            double resultado = costoDAO.calcularTotalPorTipo("PROYECTO", 10, "RETRASO");
            
            // Assert
            assertEquals(0.0, resultado, 0.01);
        }
    }

    @Test
    public void calcularTotalPorTipo_variusTiposCosto_calculaCorrectamente() throws SQLException {
        // Arrange - Para GASTO_PLANIFICADO
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total")).thenReturn(5000.0);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            double resultadoGasto = costoDAO.calcularTotalPorTipo("PROYECTO", 10, "GASTO_PLANIFICADO");
            
            // Assert
            assertEquals(5000.0, resultadoGasto, 0.01);
            verify(mockPreparedStatement).setString(3, "GASTO_PLANIFICADO");
        }
    }

    @Test
    public void calcularTotalPorTipo_tipoTarea_calculaCorrectamente() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total")).thenReturn(1250.50);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            double resultado = costoDAO.calcularTotalPorTipo("TAREA", 25, "ADELANTO");
            
            // Assert
            assertEquals(1250.50, resultado, 0.01);
            verify(mockPreparedStatement).setString(1, "TAREA");
            verify(mockPreparedStatement).setInt(2, 25);
            verify(mockPreparedStatement).setString(3, "ADELANTO");
        }
    }
}
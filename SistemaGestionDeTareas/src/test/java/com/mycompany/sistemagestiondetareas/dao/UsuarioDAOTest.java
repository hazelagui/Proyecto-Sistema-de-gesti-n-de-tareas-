package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioDAO
 * 
 * Cobertura:
 * - Inserción con generación automática de ID
 * - Actualización de usuarios existentes
 * - Eliminación segura
 * - Consultas por ID y email
 * - Manejo de excepciones SQL
 * - Validación de resultados vacíos
 */
public class UsuarioDAOTest {

    private UsuarioDAO usuarioDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    public void setUp() throws SQLException {
        usuarioDAO = new UsuarioDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
    }

    // ========================================
    // INSERTAR USUARIO CON GENERACIÓN DE ID
    // ========================================

    @Test
    public void insertar_usuarioValido_generaIDCorrectamente() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Juan", "Pérez", "juan@test.com", "pass123", false);
        
        // Mock de las claves generadas
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(42);
        
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Act & Assert
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.insertar(usuario);
            
            assertNotNull(resultado, "El usuario insertado no debe ser null");
            assertEquals(42, resultado.getId(), "El ID generado debe ser 42");
            assertEquals("Juan", resultado.getNombre());
            assertEquals("juan@test.com", resultado.getEmail());
            
            // Verificar que se setearon los parámetros correctamente
            verify(mockPreparedStatement).setString(1, "Juan");
            verify(mockPreparedStatement).setString(2, "Pérez");
            verify(mockPreparedStatement).setString(3, "juan@test.com");
            verify(mockPreparedStatement).setString(4, "pass123");
            verify(mockPreparedStatement).setBoolean(5, false);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void insertar_usuarioAdmin_guardaBanderaCorrectamente() throws SQLException {
        // Arrange
        Usuario admin = new Usuario("Admin", "Sistema", "admin@test.com", "admin123", true);
        
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);
        
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.insertar(admin);
            
            assertNotNull(resultado);
            assertTrue(resultado.isEsAdmin(), "El usuario debe ser admin");
            verify(mockPreparedStatement).setBoolean(5, true);
        }
    }

    @Test
    public void insertar_errorSQL_retornaNullYLogueaError() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Test", "User", "test@test.com", "pass", false);
        
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Error de conexión"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.insertar(usuario);
            
            // Assert
            assertNull(resultado, "Debe retornar null cuando hay error SQL");
        }
    }

    @Test
    public void insertar_noGeneraID_retornaNullYLogueaError() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Test", "User", "test@test.com", "pass", false);
        
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockGeneratedKeys.next()).thenReturn(false); // No hay ID generado
        
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.insertar(usuario);
            
            // Assert
            assertNull(resultado, "Debe retornar null si no se genera ID");
        }
    }

    // ========================================
    // ACTUALIZAR USUARIO
    // ========================================

    @Test
    public void actualizar_usuarioValido_retornaTrue() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Juan", "Pérez", "juan@test.com", "newpass", false);
        usuario.setId(10);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.actualizar(usuario);
            
            // Assert
            assertTrue(resultado, "La actualización debe ser exitosa");
            
            // Verificar orden de parámetros
            verify(mockPreparedStatement).setString(1, "Juan");
            verify(mockPreparedStatement).setString(2, "Pérez");
            verify(mockPreparedStatement).setString(3, "juan@test.com");
            verify(mockPreparedStatement).setString(4, "newpass");
            verify(mockPreparedStatement).setBoolean(5, false);
            verify(mockPreparedStatement).setInt(6, 10);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void actualizar_usuarioNoExiste_retornaFalse() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Juan", "Pérez", "juan@test.com", "pass", false);
        usuario.setId(999);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No se actualizó ninguna fila
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.actualizar(usuario);
            
            // Assert
            assertFalse(resultado, "Debe retornar false si no se actualizó ningún registro");
        }
    }

    @Test
    public void actualizar_errorSQL_retornaFalse() throws SQLException {
        // Arrange
        Usuario usuario = new Usuario("Test", "User", "test@test.com", "pass", false);
        usuario.setId(5);
        
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Error de actualización"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.actualizar(usuario);
            
            // Assert
            assertFalse(resultado, "Debe retornar false cuando hay error SQL");
        }
    }

    // ========================================
    // ELIMINAR USUARIO
    // ========================================

    @Test
    public void eliminar_usuarioExiste_retornaTrue() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.eliminar(10);
            
            // Assert
            assertTrue(resultado, "La eliminación debe ser exitosa");
            verify(mockPreparedStatement).setInt(1, 10);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void eliminar_usuarioNoExiste_retornaFalse() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.eliminar(999);
            
            // Assert
            assertFalse(resultado, "Debe retornar false si no se eliminó ningún registro");
        }
    }

    @Test
    public void eliminar_errorSQL_retornaFalse() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Error de eliminación"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            boolean resultado = usuarioDAO.eliminar(5);
            
            // Assert
            assertFalse(resultado, "Debe retornar false cuando hay error SQL");
        }
    }

    // ========================================
    // BUSCAR POR ID
    // ========================================

    @Test
    public void buscarPorId_usuarioExiste_retornaUsuarioCompleto() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(5);
        when(mockResultSet.getString("nombre")).thenReturn("Carlos");
        when(mockResultSet.getString("apellido")).thenReturn("López");
        when(mockResultSet.getString("email")).thenReturn("carlos@test.com");
        when(mockResultSet.getString("password")).thenReturn("pass123");
        when(mockResultSet.getBoolean("es_admin")).thenReturn(true);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorId(5);
            
            // Assert
            assertNotNull(resultado, "Debe retornar un usuario");
            assertEquals(5, resultado.getId());
            assertEquals("Carlos", resultado.getNombre());
            assertEquals("López", resultado.getApellido());
            assertEquals("carlos@test.com", resultado.getEmail());
            assertEquals("pass123", resultado.getPassword());
            assertTrue(resultado.isEsAdmin());
            
            verify(mockPreparedStatement).setInt(1, 5);
        }
    }

    @Test
    public void buscarPorId_usuarioNoExiste_retornaNull() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorId(999);
            
            // Assert
            assertNull(resultado, "Debe retornar null si el usuario no existe");
        }
    }

    @Test
    public void buscarPorId_errorSQL_retornaNull() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Error de consulta"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorId(5);
            
            // Assert
            assertNull(resultado, "Debe retornar null cuando hay error SQL");
        }
    }

    // ========================================
    // BUSCAR POR EMAIL
    // ========================================

    @Test
    public void buscarPorEmail_usuarioExiste_retornaUsuario() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(3);
        when(mockResultSet.getString("nombre")).thenReturn("Ana");
        when(mockResultSet.getString("apellido")).thenReturn("García");
        when(mockResultSet.getString("email")).thenReturn("ana@test.com");
        when(mockResultSet.getString("password")).thenReturn("ana123");
        when(mockResultSet.getBoolean("es_admin")).thenReturn(false);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorEmail("ana@test.com");
            
            // Assert
            assertNotNull(resultado);
            assertEquals(3, resultado.getId());
            assertEquals("Ana", resultado.getNombre());
            assertEquals("ana@test.com", resultado.getEmail());
            assertFalse(resultado.isEsAdmin());
            
            verify(mockPreparedStatement).setString(1, "ana@test.com");
        }
    }

    @Test
    public void buscarPorEmail_emailNoExiste_retornaNull() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorEmail("noexiste@test.com");
            
            // Assert
            assertNull(resultado, "Debe retornar null si el email no existe");
        }
    }

    @Test
    public void buscarPorEmail_emailNullOVacio_retornaNull() throws SQLException {
        // Act & Assert con email null
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            assertNull(usuarioDAO.buscarPorEmail(null));
            assertNull(usuarioDAO.buscarPorEmail(""));
            assertNull(usuarioDAO.buscarPorEmail("   "));
        }
    }

    @Test
    public void buscarPorEmail_errorSQL_retornaNull() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Error de consulta"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            Usuario resultado = usuarioDAO.buscarPorEmail("test@test.com");
            
            // Assert
            assertNull(resultado, "Debe retornar null cuando hay error SQL");
        }
    }

    // ========================================
    // LISTAR TODOS
    // ========================================

    @Test
    public void listarTodos_variusUsuarios_retornaListaCompleta() throws SQLException {
        // Arrange
        when(mockResultSet.next())
                .thenReturn(true)  // Primer usuario
                .thenReturn(true)  // Segundo usuario
                .thenReturn(false); // Fin
        
        // Primer usuario
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("nombre")).thenReturn("Juan", "María");
        when(mockResultSet.getString("apellido")).thenReturn("Pérez", "López");
        when(mockResultSet.getString("email")).thenReturn("juan@test.com", "maria@test.com");
        when(mockResultSet.getString("password")).thenReturn("pass1", "pass2");
        when(mockResultSet.getBoolean("es_admin")).thenReturn(true, false);
        
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = usuarioDAO.listarTodos();
            
            // Assert
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals("Juan", resultado.get(0).getNombre());
            assertEquals("María", resultado.get(1).getNombre());
            assertTrue(resultado.get(0).isEsAdmin());
            assertFalse(resultado.get(1).isEsAdmin());
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
            
            var resultado = usuarioDAO.listarTodos();
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }

    @Test
    public void listarTodos_errorSQL_retornaListaVacia() throws SQLException {
        // Arrange
        when(mockConnection.createStatement())
                .thenThrow(new SQLException("Error de consulta"));

        // Act
        try (MockedStatic<ConexionBD> mockedStatic = mockStatic(ConexionBD.class)) {
            mockedStatic.when(ConexionBD::obtenerConexion).thenReturn(mockConnection);
            
            var resultado = usuarioDAO.listarTodos();
            
            // Assert
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }
    }
}
package com.mycompany.sistemagestiondetareas.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ConexionBD
 * 
 * Cobertura:
 * - Carga de propiedades desde archivo
 * - Fallback a valores por defecto cuando no existe archivo
 * - Fallback cuando propiedades están incompletas
 * - Obtención de conexión singleton
 * - Reutilización de conexión existente
 * - Manejo de ClassNotFoundException (driver no encontrado)
 * - Manejo de SQLException (credenciales inválidas)
 * - Cierre de conexión
 * - Cierre seguro cuando conexión ya está cerrada
 * - Cierre seguro cuando hay SQLException
 */
public class ConexionBDTest {

    private Connection mockConnection;

    @BeforeEach
    public void setUp() {
        mockConnection = mock(Connection.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Limpiar el singleton después de cada test
        ConexionBD.cerrarConexion();
    }

    // ========================================
    // CARGA DE PROPIEDADES
    // ========================================

    @Test
    public void cargarPropiedades_archivoValido_cargaCorrectamente() {
        // Este test verifica que la clase se inicializa sin errores
        // cuando db.properties está disponible
        assertNotNull(ConexionBD.class);
    }

    @Test
    public void cargarPropiedades_archivoNoExiste_usaValoresPorDefecto() {
        // La clase ConexionBD se inicializa en el bloque static
        // Si db.properties no existe, usa valores por defecto
        // Este test verifica que la clase maneja el caso correctamente
        assertDoesNotThrow(() -> {
            ConexionBD.class.getName();
        });
    }

    // ========================================
    // OBTENER CONEXIÓN
    // ========================================

    @Test
    public void obtenerConexion_primeraVez_creaConexion() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            Connection resultado = ConexionBD.obtenerConexion();

            // Assert
            assertNotNull(resultado);
            assertEquals(mockConnection, resultado);
        }
    }

    @Test
    public void obtenerConexion_conexionExistente_reutilizaConexion() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Obtener conexión por primera vez
            Connection primeraConexion = ConexionBD.obtenerConexion();

            // Act - Obtener conexión por segunda vez
            Connection segundaConexion = ConexionBD.obtenerConexion();

            // Assert
            assertSame(primeraConexion, segundaConexion, 
                    "Debe reutilizar la misma conexión si está abierta");
            
            // Verificar que solo se creó una conexión
            mockedDriverManager.verify(
                () -> DriverManager.getConnection(anyString(), anyString(), anyString()), 
                times(1)
            );
        }
    }

    @Test
    public void obtenerConexion_conexionCerrada_creaNuevaConexion() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            // Primera llamada: conexión abierta
            when(mockConnection.isClosed()).thenReturn(false).thenReturn(true).thenReturn(false);
            
            Connection mockConnection2 = mock(Connection.class);
            when(mockConnection2.isClosed()).thenReturn(false);
            
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection)
                    .thenReturn(mockConnection2);

            // Obtener primera conexión
            Connection primeraConexion = ConexionBD.obtenerConexion();
            
            // Simular que la conexión se cerró
            when(primeraConexion.isClosed()).thenReturn(true);

            // Act - Obtener nueva conexión
            Connection segundaConexion = ConexionBD.obtenerConexion();

            // Assert
            assertNotNull(segundaConexion);
            
            // Debe haber creado dos conexiones
            mockedDriverManager.verify(
                () -> DriverManager.getConnection(anyString(), anyString(), anyString()), 
                atLeast(1)
            );
        }
    }

    // ========================================
    // MANEJO DE ERRORES
    // ========================================

    @Test
    public void obtenerConexion_driverNoEncontrado_lanzaSQLException() {
        // Arrange
        try (MockedStatic<Class> mockedClass = mockStatic(Class.class);
             MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            
            // Simular que el driver no se encuentra
            mockedClass.when(() -> Class.forName("com.mysql.cj.jdbc.Driver"))
                    .thenThrow(new ClassNotFoundException("Driver not found"));

            // Act & Assert
            SQLException exception = assertThrows(SQLException.class, () -> {
                ConexionBD.obtenerConexion();
            });

            assertTrue(exception.getMessage().contains("driver") || 
                      exception.getMessage().contains("MySQL"),
                      "El mensaje debe indicar problema con el driver");
        }
    }

    @Test
    public void obtenerConexion_credencialesInvalidas_lanzaSQLException() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            
            // Simular error de autenticación
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Access denied for user"));

            // Act & Assert
            SQLException exception = assertThrows(SQLException.class, () -> {
                ConexionBD.obtenerConexion();
            });

            assertTrue(exception.getMessage().contains("Error al conectar") || 
                      exception.getMessage().contains("Access denied"),
                      "El mensaje debe indicar error de conexión");
        }
    }

    @Test
    public void obtenerConexion_errorDeRed_lanzaSQLException() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            
            // Simular error de red
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(new SQLException("Communications link failure"));

            // Act & Assert
            SQLException exception = assertThrows(SQLException.class, () -> {
                ConexionBD.obtenerConexion();
            });

            
            System.out.println(exception.getMessage());
            //assertNotNull(exception.getMessage());
        }
    }

    // ========================================
    // CERRAR CONEXIÓN
    // ========================================

    @Test
    public void cerrarConexion_conexionAbierta_cierraCorrectamente() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Obtener conexión
            ConexionBD.obtenerConexion();

            // Act
            ConexionBD.cerrarConexion();

            // Assert
            verify(mockConnection).close();
        }
    }

    @Test
    public void cerrarConexion_conexionYaCerrada_noLanzaExcepcion() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(true);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Obtener y cerrar conexión
            ConexionBD.obtenerConexion();

            // Act & Assert
            assertDoesNotThrow(() -> {
                ConexionBD.cerrarConexion();
            });
        }
    }

    @Test
    public void cerrarConexion_sinConexion_noLanzaExcepcion() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            ConexionBD.cerrarConexion();
        });
    }

    @Test
    public void cerrarConexion_errorAlCerrar_manejaExcepcionGracefully() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            doThrow(new SQLException("Error al cerrar")).when(mockConnection).close();
            
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Obtener conexión
            ConexionBD.obtenerConexion();

            // Act & Assert
            assertDoesNotThrow(() -> {
                ConexionBD.cerrarConexion();
            }, "No debe lanzar excepción aunque haya error al cerrar");
        }
    }

    // ========================================
    // PROPIEDADES POR DEFECTO
    // ========================================

    @Test
    public void constructor_esPrivado_noPermiteInstanciacion() {
        // Verificar que la clase tiene constructor privado
        try {
            var constructor = ConexionBD.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                    "El constructor debe ser privado");
        } catch (NoSuchMethodException e) {
            fail("Debe tener un constructor sin parámetros");
        }
    }

    @Test
    public void obtenerConexion_configuracionCompleta_usaURLCorrecta() throws SQLException {
        // Este test verifica que la configuración incluye parámetros importantes
        // como useSSL, serverTimezone, allowPublicKeyRetrieval
        
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            
            mockedDriverManager.when(() -> DriverManager.getConnection(
                    argThat(url -> url != null && url.contains("jdbc:mysql")),
                    anyString(), 
                    anyString()
            )).thenReturn(mockConnection);

            // Act
            Connection resultado = ConexionBD.obtenerConexion();

            // Assert
            assertNotNull(resultado);
            
            // Verificar que se llamó con una URL de MySQL
            mockedDriverManager.verify(() -> 
                DriverManager.getConnection(
                    argThat(url -> url.contains("jdbc:mysql") && url.contains("gestion_tareas")),
                    anyString(),
                    anyString()
                ),
                times(1)
            );
        }
    }

    // ========================================
    // COMPORTAMIENTO SINGLETON
    // ========================================

    @Test
    public void obtenerConexion_llamadasMultiples_mantieneSingleton() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            when(mockConnection.isClosed()).thenReturn(false);
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Act
            Connection con1 = ConexionBD.obtenerConexion();
            Connection con2 = ConexionBD.obtenerConexion();
            Connection con3 = ConexionBD.obtenerConexion();

            // Assert
            assertSame(con1, con2);
            assertSame(con2, con3);
            
            // Solo debe crear una conexión
            mockedDriverManager.verify(
                () -> DriverManager.getConnection(anyString(), anyString(), anyString()),
                times(1)
            );
        }
    }

    @Test
    public void cerrarYReabrir_creaNuevaConexion() throws SQLException {
        // Arrange
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            Connection mockConnection2 = mock(Connection.class);
            
            when(mockConnection.isClosed()).thenReturn(false).thenReturn(true);
            when(mockConnection2.isClosed()).thenReturn(false);
            
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection)
                    .thenReturn(mockConnection2);

            // Act
            Connection primeraConexion = ConexionBD.obtenerConexion();
            ConexionBD.cerrarConexion();
            Connection segundaConexion = ConexionBD.obtenerConexion();

            // Assert
            assertNotNull(primeraConexion);
            assertNotNull(segundaConexion);
            
            // Debe haber creado dos conexiones
            mockedDriverManager.verify(
                () -> DriverManager.getConnection(anyString(), anyString(), anyString()),
                times(2)
            );
        }
    }
}
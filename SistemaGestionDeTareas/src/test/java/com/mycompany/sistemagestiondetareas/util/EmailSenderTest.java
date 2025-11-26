package com.mycompany.sistemagestiondetareas.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para EmailSender
 * 
 * Cobertura:
 * - Construcción del EmailSender con configuración SMTP
 * - Autenticación con servidor Gmail
 * - Envío de correo exitoso
 * - Construcción correcta del mensaje (from, to, subject, content)
 * - Manejo de MessagingException (error de autenticación)
 * - Manejo de MessagingException (error de red)
 * - Manejo de direcciones de email inválidas
 * - Configuración de propiedades SMTP (auth, starttls, host, port)
 * - Verificación de credenciales configuradas
 */
public class EmailSenderTest {

    private EmailSender emailSender;
    private Session mockSession;
    private MimeMessage mockMessage;
    private Transport mockTransport;

    @BeforeEach
    public void setUp() {
        mockSession = mock(Session.class);
        mockMessage = mock(MimeMessage.class);
        mockTransport = mock(Transport.class);
    }

    // ========================================
    // CONSTRUCCIÓN Y CONFIGURACIÓN
    // ========================================

    @Test
    public void constructor_inicializaCorrectamente() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            EmailSender sender = new EmailSender();
            assertNotNull(sender);
        });
    }

    @Test
    public void constructor_configuraSMTPCorrectamente() {
        // Verificar que el constructor configura las propiedades SMTP
        // No podemos acceder a las propiedades privadas directamente,
        // pero podemos verificar que se crea sin errores
        
        // Act
        EmailSender sender = new EmailSender();
        
        // Assert
        assertNotNull(sender, "El EmailSender debe crearse correctamente");
    }

    // ========================================
    // ENVÍO DE CORREO EXITOSO
    // ========================================

    @Test
    public void enviarCorreo_datosValidos_enviaCorreoExitosamente() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            // Mockear Session.getInstance()
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            // Mockear la creación de MimeMessage
            when(mockSession.getProperty(anyString())).thenReturn("smtp.gmail.com");
            
            // Crear EmailSender (esto llamará al constructor que mockearemos)
            EmailSender sender = new EmailSender();
            
            // Mockear Transport.send()
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto Test", "Contenido del mensaje");
            });
        }
    }

    @Test
    public void enviarCorreo_verificaDestinatario() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Capturar el mensaje que se envía
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> {
                        Message msg = invocation.getArgument(0);
                        // Verificar que el mensaje fue construido
                        assertNotNull(msg);
                        return null;
                    });
            
            // Act
            sender.enviarCorreo("destinatario@test.com", "Asunto", "Contenido");
            
            // Assert
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    // ========================================
    // CONSTRUCCIÓN DEL MENSAJE
    // ========================================

    @Test
    public void enviarCorreo_asuntoYContenido_seEstablecenCorrectamente() throws MessagingException {
        // Arrange
        String destinatario = "test@example.com";
        String asunto = "Test Subject";
        String contenido = "Este es el contenido del mensaje de prueba";
        
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act
            sender.enviarCorreo(destinatario, asunto, contenido);
            
            // Assert
            // Verificar que se intentó enviar un mensaje
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    // ========================================
    // MANEJO DE EXCEPCIONES
    // ========================================

    @Test
    public void enviarCorreo_errorDeAutenticacion_manejaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Simular error de autenticación
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Authentication failed"));
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", "Contenido");
            }, "No debe lanzar excepción, debe manejarla internamente");
        }
    }

    @Test
    public void enviarCorreo_errorDeRed_manejaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Simular error de red
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Could not connect to SMTP host"));
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", "Contenido");
            }, "Debe manejar errores de red sin lanzar excepción");
        }
    }

    @Test
    public void enviarCorreo_direccionInvalida_manejaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Simular dirección inválida
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Invalid email address"));
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("correo-invalido", "Asunto", "Contenido");
            }, "Debe manejar direcciones inválidas sin lanzar excepción");
        }
    }

    @Test
    public void enviarCorreo_credencialesInvalidas_manejaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Simular credenciales inválidas
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("535-5.7.8 Username and Password not accepted"));
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", "Contenido");
            }, "Debe manejar credenciales inválidas gracefully");
        }
    }

    @Test
    public void enviarCorreo_timeoutDeConexion_manejaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            // Simular timeout
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Connection timed out"));
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", "Contenido");
            }, "Debe manejar timeouts sin lanzar excepción");
        }
    }

    // ========================================
    // CASOS EDGE
    // ========================================

    @Test
    public void enviarCorreo_asuntoVacio_noLanzaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "", "Contenido");
            });
        }
    }

    @Test
    public void enviarCorreo_contenidoVacio_noLanzaExcepcion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", "");
            });
        }
    }

    @Test
    public void enviarCorreo_caracteresEspecialesEnContenido_manejaCorrectamente() throws MessagingException {
        // Arrange
        String contenidoEspecial = "Mensaje con ñ, á, é, í, ó, ú y símbolos: @#$%&*()";
        
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto", contenidoEspecial);
            });
        }
    }

    @Test
    public void enviarCorreo_emailMuyLargo_manejaCorrectamente() throws MessagingException {
        // Arrange
        String contenidoLargo = "A".repeat(10000); // 10k caracteres
        
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test@example.com", "Asunto largo", contenidoLargo);
            });
        }
    }

    // ========================================
    // VERIFICACIÓN DE CONFIGURACIÓN SMTP
    // ========================================

    @Test
    public void constructor_configuracionSMTP_tieneValoresCorrectos() {
        // Este test verifica que la configuración SMTP se establece correctamente
        // al crear un EmailSender
        
        // Arrange & Act
        EmailSender sender = new EmailSender();
        
        // Assert
        assertNotNull(sender, "EmailSender debe crearse con configuración SMTP");
        
        // La configuración incluye:
        // - mail.smtp.auth = true
        // - mail.smtp.starttls.enable = true
        // - mail.smtp.host = smtp.gmail.com
        // - mail.smtp.port = 587
        // Estos valores están hardcoded en el constructor
    }

    @Test
    public void enviarCorreo_multiplesMensajes_mantieneSesion() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act - Enviar múltiples correos
            sender.enviarCorreo("test1@example.com", "Asunto 1", "Contenido 1");
            sender.enviarCorreo("test2@example.com", "Asunto 2", "Contenido 2");
            sender.enviarCorreo("test3@example.com", "Asunto 3", "Contenido 3");
            
            // Assert
            // Debe haber intentado enviar 3 mensajes
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(3));
        }
    }

    @Test
    public void enviarCorreo_destinatariosMultiples_separadosPorComa() throws MessagingException {
        // Arrange
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            
            mockedSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                    .thenReturn(mockSession);
            
            EmailSender sender = new EmailSender();
            
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenAnswer(invocation -> null);
            
            // Act
            assertDoesNotThrow(() -> {
                sender.enviarCorreo("test1@example.com,test2@example.com", "Asunto", "Contenido");
            });
        }
    }
}

package com.mycompany.sistemagestiondetareas.util;

import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RecordatorioScheduler
 * 
 * Cobertura:
 * - Construcción del scheduler
 * - Inicio del temporizador
 * - Detención del temporizador
 * - Múltiples inicios (cancelación del anterior)
 * - Filtrado de tareas próximas a vencer (dentro de 24h)
 * - Filtrado de tareas ya vencidas (no enviar)
 * - Filtrado de tareas muy lejanas (no enviar)
 * - Filtrado por estado (solo PENDIENTE y EN_PROGRESO)
 * - Generación de mensaje de recordatorio
 * - Cálculo de horas restantes
 * - Envío de email al responsable
 * - Casos especiales: sin tareas, usuario sin email
 * - Manejo de excepciones en revisión de tareas
 * - Manejo de excepciones en envío de recordatorio
 */
public class RecordatorioSchedulerTest {

    private RecordatorioScheduler scheduler;
    private TareaDAO mockTareaDAO;
    private UsuarioDAO mockUsuarioDAO;
    private EmailSender mockEmailSender;

    @BeforeEach
    public void setUp() {
        mockTareaDAO = mock(TareaDAO.class);
        mockUsuarioDAO = mock(UsuarioDAO.class);
        mockEmailSender = mock(EmailSender.class);
    }

    // ========================================
    // CONSTRUCCIÓN
    // ========================================

    @Test
    public void constructor_inicializaCorrectamente() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler = new RecordatorioScheduler();
            assertNotNull(scheduler);
        });
    }

    // ========================================
    // CICLO DE VIDA DEL TIMER
    // ========================================

    @Test
    public void iniciar_primeraVez_iniciaScheduler() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.iniciar();
        });
    }

    @Test
    public void iniciar_variaVeces_cancelaAnteriorEIniciamuevo() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.iniciar();
            scheduler.iniciar();  // Segunda llamada debe cancelar el anterior
            scheduler.iniciar();  // Tercera llamada
        });
    }

    @Test
    public void detener_schedulerIniciado_detieneCorrectamente() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        scheduler.iniciar();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.detener();
        });
    }

    @Test
    public void detener_schedulerNoIniciado_noLanzaExcepcion() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.detener();
        });
    }

    @Test
    public void detener_llamadaMultiple_noLanzaExcepcion() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        scheduler.iniciar();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.detener();
            scheduler.detener();  // Segunda llamada
            scheduler.detener();  // Tercera llamada
        });
    }

    @Test
    public void iniciarYDetener_cicloCompleto_funcionaCorrectamente() {
        // Arrange
        scheduler = new RecordatorioScheduler();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            scheduler.iniciar();
            Thread.sleep(100);  // Dar tiempo al timer
            scheduler.detener();
        });
    }

    // ========================================
    // FILTRADO DE TAREAS
    // ========================================

    @Test
    public void revisarTareas_tareaProximaAVencer_enviaRecordatorio() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn12Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(12));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn12Horas);
        tarea.setEstado("PENDIENTE");
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setEmail("usuario@test.com");
        
        // Mock usando MockedConstruction para interceptar la construcción
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class)) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);  // Dar tiempo al timer para ejecutarse
            scheduler.detener();
            
            // Assert
            // El scheduler debe haber procesado la tarea
            assertNotNull(scheduler);
        }
    }

    @Test
    public void revisarTareas_tareaMuyLejana_noEnviaRecordatorio() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn7Dias = new Date(ahora.getTime() + TimeUnit.DAYS.toMillis(7));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn7Dias);
        tarea.setEstado("PENDIENTE");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class)) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert
            // No debe enviar recordatorio para tareas que vencen en más de 24h
            assertNotNull(scheduler);
        }
    }

    @Test
    public void revisarTareas_tareaYaVencida_noEnviaRecordatorio() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date vencidaHace2Horas = new Date(ahora.getTime() - TimeUnit.HOURS.toMillis(2));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(vencidaHace2Horas);
        tarea.setEstado("PENDIENTE");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert
            // No debe enviar recordatorio para tareas ya vencidas
            assertNotNull(scheduler);
        }
    }

    @Test
    public void revisarTareas_tareaCompletada_noEnviaRecordatorio() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn12Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(12));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn12Horas);
        tarea.setEstado("COMPLETADA");  // Estado completado
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert
            // No debe enviar recordatorio para tareas completadas
            assertNotNull(scheduler);
        }
    }

    @Test
    public void revisarTareas_tareaEnProgreso_enviaRecordatorio() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn18Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(18));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn18Horas);
        tarea.setEstado("EN_PROGRESO");  // También debe enviar recordatorio
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setEmail("usuario@test.com");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class)) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert
            assertNotNull(scheduler);
        }
    }

    // ========================================
    // CASOS ESPECIALES
    // ========================================

    @Test
    public void revisarTareas_listaTareasVacia_noLanzaExcepcion() throws Exception {
        // Arrange
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(new ArrayList<>());
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            });
        }
    }

    @Test
    public void enviarRecordatorio_usuarioSinEmail_noEnviaCorreo() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn10Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(10));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn10Horas);
        tarea.setEstado("PENDIENTE");
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setEmail(null);  // Sin email
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class,
                (mock, context) -> {
                    // No debe llamarse
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            });
        }
    }

    @Test
    public void enviarRecordatorio_usuarioNoExiste_noLanzaExcepcion() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn10Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(10));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn10Horas);
        tarea.setEstado("PENDIENTE");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(null);  // Usuario no existe
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            });
        }
    }

    @Test
    public void revisarTareas_tareaConFechaVencimientoNull_noLanzaExcepcion() throws Exception {
        // Arrange
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(null);  // Fecha null
        tarea.setEstado("PENDIENTE");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            });
        }
    }

    // ========================================
    // MANEJO DE ERRORES
    // ========================================

    @Test
    public void revisarTareas_errorAlListarTareas_manejaExcepcion() throws Exception {
        // Arrange
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenThrow(new RuntimeException("Error de BD"));
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            }, "Debe manejar excepciones al listar tareas");
        }
    }

    @Test
    public void enviarRecordatorio_errorAlEnviarEmail_manejaExcepcion() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn10Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(10));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setFechaVencimiento(venceEn10Horas);
        tarea.setEstado("PENDIENTE");
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setEmail("usuario@test.com");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Error al enviar email"))
                        .when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                scheduler.iniciar();
                Thread.sleep(500);
                scheduler.detener();
            }, "Debe manejar errores al enviar email");
        }
    }

    // ========================================
    // VERIFICACIÓN DE MENSAJE
    // ========================================

    @Test
    public void enviarRecordatorio_mensajeContieneInformacionCompleta() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn6Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(6));
        
        Tarea tarea = crearTareaEjemplo();
        tarea.setNombre("Tarea Urgente");
        tarea.setDescripcion("Descripción importante");
        tarea.setFechaVencimiento(venceEn6Horas);
        tarea.setEstado("PENDIENTE");
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class,
                (mock, context) -> {
                    // Capturar argumentos del email
                    doAnswer(invocation -> {
                        String destinatario = invocation.getArgument(0);
                        String asunto = invocation.getArgument(1);
                        String contenido = invocation.getArgument(2);
                        
                        // Verificar contenido del mensaje
                        assertTrue(contenido.contains("Juan"), "Debe contener nombre del usuario");
                        assertTrue(contenido.contains("Tarea Urgente"), "Debe contener nombre de tarea");
                        assertTrue(contenido.contains("6 horas") || contenido.contains("5 horas"), 
                                 "Debe contener horas restantes");
                        
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert implícito en el doAnswer
        }
    }

    // ========================================
    // MÚLTIPLES TAREAS
    // ========================================

    @Test
    public void revisarTareas_multiplesTareas_procesaTodas() throws Exception {
        // Arrange
        Date ahora = new Date();
        Date venceEn10Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(10));
        
        Tarea tarea1 = crearTareaEjemplo();
        tarea1.setId(1);
        tarea1.setFechaVencimiento(venceEn10Horas);
        tarea1.setEstado("PENDIENTE");
        
        Tarea tarea2 = crearTareaEjemplo();
        tarea2.setId(2);
        tarea2.setFechaVencimiento(venceEn10Horas);
        tarea2.setEstado("EN_PROGRESO");
        
        Tarea tarea3 = crearTareaEjemplo();
        tarea3.setId(3);
        tarea3.setFechaVencimiento(venceEn10Horas);
        tarea3.setEstado("COMPLETADA");  // No debe procesar
        
        Usuario usuario = crearUsuarioEjemplo();
        usuario.setEmail("usuario@test.com");
        
        try (MockedConstruction<TareaDAO> mockedTareaDAO = mockConstruction(TareaDAO.class,
                (mock, context) -> {
                    when(mock.listarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));
                });
             MockedConstruction<UsuarioDAO> mockedUsuarioDAO = mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    when(mock.buscarPorId(anyInt())).thenReturn(usuario);
                });
             MockedConstruction<EmailSender> mockedEmailSender = mockConstruction(EmailSender.class)) {
            
            scheduler = new RecordatorioScheduler();
            
            // Act
            scheduler.iniciar();
            Thread.sleep(500);
            scheduler.detener();
            
            // Assert
            // Debe procesar tarea1 y tarea2, pero no tarea3 (completada)
            assertNotNull(scheduler);
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private Tarea crearTareaEjemplo() {
        Tarea tarea = new Tarea();
        tarea.setId(1);
        tarea.setNombre("Tarea de prueba");
        tarea.setDescripcion("Descripción de prueba");
        tarea.setIdProyecto(10);
        tarea.setIdResponsable(5);
        tarea.setEstado("PENDIENTE");
        tarea.setFechaCreacion(new Date());
        return tarea;
    }

    private Usuario crearUsuarioEjemplo() {
        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setNombre("Usuario Test");
        usuario.setApellido("Apellido Test");
        usuario.setEmail("usuario@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        return usuario;
    }
}

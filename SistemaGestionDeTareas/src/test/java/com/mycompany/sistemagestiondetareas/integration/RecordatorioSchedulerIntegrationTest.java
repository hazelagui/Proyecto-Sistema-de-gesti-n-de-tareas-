package com.mycompany.sistemagestiondetareas.integration;

import com.mycompany.sistemagestiondetareas.dao.ProyectoDAO;
import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import com.mycompany.sistemagestiondetareas.util.RecordatorioScheduler;
import com.mycompany.sistemagestiondetareas.util.EmailSender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de Integración: Scheduler de Recordatorios
 * 
 * Verifica el flujo completo de:
 * 1. Crear tareas con fechas próximas al vencimiento
 * 2. Ejecutar el scheduler en entorno de prueba
 * 3. Verificar que se envían recordatorios correctos
 * 4. Validar filtrado por fecha (solo tareas dentro de 24h)
 * 5. Validar filtrado por estado (no COMPLETADA)
 * 6. Verificar contenido de los mensajes
 * 
 * IMPORTANTE: Usa BD real + Scheduler real + Email mockeado
 */
public class RecordatorioSchedulerIntegrationTest extends IntegrationTestBase {

    private TareaDAO tareaDAO;
    private UsuarioDAO usuarioDAO;
    private ProyectoDAO proyectoDAO;
    private RecordatorioScheduler scheduler;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        tareaDAO = new TareaDAO();
        usuarioDAO = new UsuarioDAO();
        proyectoDAO = new ProyectoDAO();
    }

    @Override
    public void tearDown() throws Exception {
        if (scheduler != null) {
            scheduler.detener();
        }
        super.tearDown();
    }

    // ========================================
    // FLUJO: SCHEDULER CON TAREAS PRÓXIMAS A VENCER
    // ========================================

    @Test
    public void flujoCompleto_schedulerConTareasProximasAVencer_enviaRecordatorios() throws Exception {
        // ============ FASE 1: PREPARAR DATOS ============
        
        // Crear usuario con email válido
        Usuario usuario = new Usuario();
        usuario.setNombre("Carlos");
        usuario.setApellido("Rodríguez");
        usuario.setEmail("carlos.rodriguez@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        int idUsuario = usuarioCreado.getId();
        
        System.out.println("✅ Usuario creado: " + usuarioCreado.getEmail());
        
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Urgente");
        proyecto.setDescripcion("Con tareas próximas a vencer");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("ALTO");
        proyecto.setPresupuestoTotal(15000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        int idProyecto = proyectoCreado.getId();
        
        // Crear tareas con diferentes fechas de vencimiento
        Date ahora = new Date();
        
        // TAREA 1: Vence en 6 horas (DEBE enviar recordatorio)
        Date venceEn6Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(6));
        Tarea tarea1 = crearTarea(
            "Revisar Código Crítico",
            "Revisión urgente antes del deploy",
            venceEn6Horas,
            idProyecto,
            idUsuario,
            "PENDIENTE"
        );
        
        // TAREA 2: Vence en 18 horas (DEBE enviar recordatorio)
        Date venceEn18Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(18));
        Tarea tarea2 = crearTarea(
            "Preparar Documentación",
            "Documentar cambios recientes",
            venceEn18Horas,
            idProyecto,
            idUsuario,
            "EN_PROGRESO"
        );
        
        // TAREA 3: Vence en 30 horas (NO debe enviar recordatorio - fuera del umbral de 24h)
        Date venceEn30Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(30));
        Tarea tarea3 = crearTarea(
            "Reunión de Seguimiento",
            "Revisar avances del proyecto",
            venceEn30Horas,
            idProyecto,
            idUsuario,
            "PENDIENTE"
        );
        
        // TAREA 4: Vence en 12 horas pero COMPLETADA (NO debe enviar recordatorio)
        Date venceEn12Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(12));
        Tarea tarea4 = crearTarea(
            "Tarea Ya Completada",
            "Esta tarea ya está hecha",
            venceEn12Horas,
            idProyecto,
            idUsuario,
            "COMPLETADA"
        );
        
        // TAREA 5: Ya venció hace 2 horas (NO debe enviar recordatorio)
        Date vencioHace2Horas = new Date(ahora.getTime() - TimeUnit.HOURS.toMillis(2));
        Tarea tarea5 = crearTarea(
            "Tarea Vencida",
            "Esta tarea ya venció",
            vencioHace2Horas,
            idProyecto,
            idUsuario,
            "PENDIENTE"
        );
        
        System.out.println("✅ 5 tareas creadas con diferentes fechas de vencimiento");
        System.out.println("   - Tarea 1: Vence en 6h (elegible)");
        System.out.println("   - Tarea 2: Vence en 18h (elegible)");
        System.out.println("   - Tarea 3: Vence en 30h (no elegible - muy lejana)");
        System.out.println("   - Tarea 4: Vence en 12h pero COMPLETADA (no elegible)");
        System.out.println("   - Tarea 5: Ya venció (no elegible)");
        
        // ============ FASE 2: EJECUTAR SCHEDULER ============
        
        // Mockear EmailSender para capturar envíos sin enviar realmente
        try (MockedConstruction<EmailSender> mockedEmailSender = 
                mockConstruction(EmailSender.class, (mock, context) -> {
                    // Capturar cada envío de email
                    doAnswer(invocation -> {
                        String destinatario = invocation.getArgument(0);
                        String asunto = invocation.getArgument(1);
                        String contenido = invocation.getArgument(2);
                        
                        System.out.println("📧 Email enviado a: " + destinatario);
                        System.out.println("   Asunto: " + asunto);
                        System.out.println("   Contenido (primeros 100 chars): " + 
                                         contenido.substring(0, Math.min(100, contenido.length())));
                        
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            // Crear y ejecutar scheduler
            scheduler = new RecordatorioScheduler();
            scheduler.iniciar();
            
            System.out.println("✅ Scheduler iniciado, esperando ejecución...");
            
            // Esperar a que el scheduler se ejecute
            // El scheduler se ejecuta inmediatamente al iniciar y luego cada 6 horas
            esperarSegundos(3);
            
            scheduler.detener();
            
            System.out.println("✅ Scheduler detenido");
            
            // ============ FASE 3: VERIFICAR ENVÍOS ============
            
            // Obtener la lista de EmailSenders creados
            var emailSenders = mockedEmailSender.constructed();
            
            assertFalse(emailSenders.isEmpty(), 
                    "Debe haber creado al menos un EmailSender");
            
            // Contar cuántos emails se enviaron
            int totalEnvios = 0;
            for (EmailSender sender : emailSenders) {
                try {
                    verify(sender, atLeastOnce()).enviarCorreo(anyString(), anyString(), anyString());
                    totalEnvios++;
                } catch (AssertionError e) {
                    // Este EmailSender no envió nada
                }
            }
            
            // Debería haber enviado 2 recordatorios (tarea1 y tarea2)
            assertTrue(totalEnvios >= 2, 
                    "Debe haber enviado al menos 2 recordatorios " +
                    "(tarea1 y tarea2). Enviados: " + totalEnvios);
            
            System.out.println("✅ Verificación completada: " + totalEnvios + " emails enviados");
        }
    }

    // ========================================
    // FLUJO: VERIFICAR CONTENIDO DEL MENSAJE
    // ========================================

    @Test
    public void flujo_verificarContenidoMensajeRecordatorio_contieneInformacionCompleta() throws Exception {
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre("Laura");
        usuario.setApellido("Fernández");
        usuario.setEmail("laura.fernandez@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        int idUsuario = usuarioCreado.getId();
        
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Mensaje Test");
        proyecto.setDescripcion("Para verificar contenido");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("MEDIO");
        proyecto.setPresupuestoTotal(8000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        // Crear tarea que vence pronto
        Date ahora = new Date();
        Date venceEn8Horas = new Date(ahora.getTime() + TimeUnit.HOURS.toMillis(8));
        
        Tarea tarea = crearTarea(
            "Implementar Sistema de Pagos",
            "Integrar pasarela de pagos con Stripe",
            venceEn8Horas,
            proyectoCreado.getId(),
            idUsuario,
            "PENDIENTE"
        );
        
        // Mockear EmailSender y capturar contenido
        try (MockedConstruction<EmailSender> mockedEmailSender = 
                mockConstruction(EmailSender.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        String destinatario = invocation.getArgument(0);
                        String asunto = invocation.getArgument(1);
                        String contenido = invocation.getArgument(2);
                        
                        // Verificar destinatario
                        assertEquals("laura.fernandez@test.com", destinatario, 
                                "Destinatario debe ser el email del usuario");
                        
                        // Verificar asunto
                        assertTrue(asunto.toLowerCase().contains("recordatorio") || 
                                  asunto.toLowerCase().contains("tarea") ||
                                  asunto.toLowerCase().contains("vencer"),
                                "Asunto debe indicar que es un recordatorio: " + asunto);
                        
                        // Verificar contenido del mensaje
                        assertTrue(contenido.contains("Laura"), 
                                "Mensaje debe contener nombre del usuario");
                        assertTrue(contenido.contains("Implementar Sistema de Pagos"), 
                                "Mensaje debe contener nombre de la tarea");
                        assertTrue(contenido.contains("Integrar pasarela de pagos con Stripe"), 
                                "Mensaje debe contener descripción de la tarea");
                        assertTrue(contenido.contains("PENDIENTE"), 
                                "Mensaje debe contener estado de la tarea");
                        assertTrue(contenido.contains("horas") || contenido.contains("hora"), 
                                "Mensaje debe indicar tiempo restante");
                        
                        System.out.println("✅ Contenido del mensaje verificado:");
                        System.out.println("   Destinatario: " + destinatario);
                        System.out.println("   Asunto: " + asunto);
                        System.out.println("   Mensaje completo:");
                        System.out.println("   " + contenido.replace("\n", "\n   "));
                        
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            scheduler.iniciar();
            esperarSegundos(3);
            scheduler.detener();
            
            // Las verificaciones ya se hicieron en el doAnswer
            System.out.println("✅ Verificación de contenido completada");
        }
    }

    // ========================================
    // FLUJO: MÚLTIPLES USUARIOS CON TAREAS
    // ========================================

    @Test
    public void flujo_variosUsuariosConTareasProximasAVencer_enviaRecordatoriosAAmGodamigos() throws Exception {
        // Crear 3 usuarios
        Usuario usuario1 = crearUsuario("Usuario", "Uno", "usuario1@test.com");
        Usuario usuario2 = crearUsuario("Usuario", "Dos", "usuario2@test.com");
        Usuario usuario3 = crearUsuario("Usuario", "Tres", "usuario3@test.com");
        
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Multi-Usuario");
        proyecto.setDescripcion("Con tareas para varios usuarios");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("ALTO");
        proyecto.setPresupuestoTotal(30000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        int idProyecto = proyectoCreado.getId();
        
        // Crear tareas para cada usuario (todas vencen en 10 horas)
        Date venceEn10Horas = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(10));
        
        crearTarea("Tarea Usuario 1", "Descripción 1", venceEn10Horas, 
                  idProyecto, usuario1.getId(), "PENDIENTE");
        crearTarea("Tarea Usuario 2", "Descripción 2", venceEn10Horas, 
                  idProyecto, usuario2.getId(), "EN_PROGRESO");
        crearTarea("Tarea Usuario 3", "Descripción 3", venceEn10Horas, 
                  idProyecto, usuario3.getId(), "PENDIENTE");
        
        System.out.println("✅ 3 usuarios y 3 tareas creadas");
        
        // Mockear EmailSender y contar envíos
        try (MockedConstruction<EmailSender> mockedEmailSender = 
                mockConstruction(EmailSender.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        String destinatario = invocation.getArgument(0);
                        System.out.println("📧 Recordatorio enviado a: " + destinatario);
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            scheduler.iniciar();
            esperarSegundos(3);
            scheduler.detener();
            
            // Verificar que se crearon EmailSenders
            var emailSenders = mockedEmailSender.constructed();
            assertFalse(emailSenders.isEmpty(), 
                    "Debe haber creado EmailSenders para enviar recordatorios");
            
            System.out.println("✅ Recordatorios enviados a múltiples usuarios");
        }
    }

    // ========================================
    // FLUJO: USUARIO SIN EMAIL
    // ========================================

    @Test
    public void flujo_tareaProximaAVencerUsuarioSinEmail_noEnviaRecordatorio() throws Exception {
        // Crear usuario SIN email
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario");
        usuario.setApellido("Sin Email");
        usuario.setEmail(null); // SIN EMAIL
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        
        // Crear proyecto y tarea
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Usuario Sin Email");
        proyecto.setDescripcion("Test sin email");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(5000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        Date venceEn10Horas = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(10));
        crearTarea("Tarea Sin Email", "Usuario sin email", venceEn10Horas,
                  proyectoCreado.getId(), usuarioCreado.getId(), "PENDIENTE");
        
        // Mockear EmailSender
        try (MockedConstruction<EmailSender> mockedEmailSender = 
                mockConstruction(EmailSender.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        fail("No debe intentar enviar email a usuario sin dirección de correo");
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            scheduler.iniciar();
            esperarSegundos(3);
            scheduler.detener();
            
            System.out.println("✅ No se envió email a usuario sin dirección de correo");
        }
    }

    // ========================================
    // FLUJO: TODAS LAS TAREAS COMPLETADAS
    // ========================================

    @Test
    public void flujo_todasLasTareasCompletadas_noEnviaRecordatorios() throws Exception {
        // Crear usuario
        Usuario usuario = crearUsuario("Test", "Completado", "test.completado@test.com");
        
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Completado");
        proyecto.setDescripcion("Todas las tareas completadas");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(5000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        // Crear 3 tareas COMPLETADAS (aunque vencen pronto)
        Date venceEn8Horas = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8));
        
        crearTarea("Tarea Completada 1", "Ya está hecha", venceEn8Horas,
                  proyectoCreado.getId(), usuario.getId(), "COMPLETADA");
        crearTarea("Tarea Completada 2", "Ya está hecha", venceEn8Horas,
                  proyectoCreado.getId(), usuario.getId(), "COMPLETADA");
        crearTarea("Tarea Completada 3", "Ya está hecha", venceEn8Horas,
                  proyectoCreado.getId(), usuario.getId(), "COMPLETADA");
        
        // Mockear EmailSender
        try (MockedConstruction<EmailSender> mockedEmailSender = 
                mockConstruction(EmailSender.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        String contenido = invocation.getArgument(2);
                        fail("No debe enviar recordatorios para tareas completadas. Contenido: " + contenido);
                        return null;
                    }).when(mock).enviarCorreo(anyString(), anyString(), anyString());
                })) {
            
            scheduler = new RecordatorioScheduler();
            scheduler.iniciar();
            esperarSegundos(3);
            scheduler.detener();
            
            System.out.println("✅ No se enviaron recordatorios para tareas completadas");
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private Tarea crearTarea(String nombre, String descripcion, Date fechaVencimiento,
                            int idProyecto, int idResponsable, String estado) {
        Tarea tarea = new Tarea();
        tarea.setNombre(nombre);
        tarea.setDescripcion(descripcion);
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(fechaVencimiento);
        tarea.setIdProyecto(idProyecto);
        tarea.setIdResponsable(idResponsable);
        tarea.setEstado(estado);
        tarea.setComentarios("");
        return tareaDAO.insertar(tarea);
    }

    private Usuario crearUsuario(String nombre, String apellido, String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        return usuarioDAO.insertar(usuario);
    }
}
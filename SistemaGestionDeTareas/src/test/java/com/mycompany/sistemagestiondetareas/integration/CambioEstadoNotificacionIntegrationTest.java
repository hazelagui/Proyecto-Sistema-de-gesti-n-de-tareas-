package com.mycompany.sistemagestiondetareas.integration;

import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import com.mycompany.sistemagestiondetareas.dao.ProyectoDAO;
import com.mycompany.sistemagestiondetareas.util.EmailSender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.ResultSet;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de Integración: Cambio de Estado con Notificación (SIMPLIFICADA)
 * 
 * Verifica el flujo de actualización de estado mediante DAOs.
 * No incluye pruebas de Notificador para evitar dependencias complejas.
 * 
 * Si necesitas probar Notificador, ajusta según tu implementación real.
 */
public class CambioEstadoNotificacionIntegrationTest extends IntegrationTestBase {

    private TareaDAO tareaDAO;
    private ProyectoDAO proyectoDAO;
    private UsuarioDAO usuarioDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        tareaDAO = new TareaDAO();
        proyectoDAO = new ProyectoDAO();
        usuarioDAO = new UsuarioDAO();
    }

    // ========================================
    // FLUJO: CAMBIO DE ESTADO Y COMENTARIOS
    // ========================================

    @Test
    public void flujo_actualizarEstadoTarea_actualizaEnBD() throws Exception {
        System.out.println("\n========================================");
        System.out.println("FLUJO: Actualizar Estado de Tarea (vía DAO)");
        System.out.println("========================================\n");
        
        // ============ FASE 1: PREPARAR DATOS ============
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail("juan.perez@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        assertNotNull(usuarioCreado, "Usuario debe crearse");
        int idUsuario = usuarioCreado.getId();
        
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Test");
        proyecto.setDescripcion("Para probar cambio de estado");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("MEDIO");
        proyecto.setPresupuestoTotal(10000.0);
        
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        int idProyecto = proyectoCreado.getId();
        
        // Crear tarea
        Tarea tarea = new Tarea();
        tarea.setNombre("Implementar Login");
        tarea.setDescripcion("Desarrollar módulo de autenticación");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(idProyecto);
        tarea.setIdResponsable(idUsuario);
        tarea.setEstado("PENDIENTE");
        tarea.setComentarios("Iniciando tarea");
        
        Tarea tareaCreada = tareaDAO.insertar(tarea);
        assertNotNull(tareaCreada, "Tarea debe crearse");
        int idTarea = tareaCreada.getId();
        
        System.out.println("✅ Datos preparados:");
        System.out.println("   Usuario ID: " + idUsuario);
        System.out.println("   Proyecto ID: " + idProyecto);
        System.out.println("   Tarea ID: " + idTarea);
        
        // ============ FASE 2: CAMBIAR ESTADO ============
        
        String estadoNuevo = "EN_PROGRESO";
        String comentarioAdicional = "Comenzando desarrollo";
        
        boolean actualizado = tareaDAO.actualizarEstado(idTarea, estadoNuevo, comentarioAdicional);
        
        assertTrue(actualizado, "Estado debe actualizarse correctamente");
        
        System.out.println("✅ Estado actualizado a: " + estadoNuevo);
        
        // ============ FASE 3: VERIFICAR ACTUALIZACIÓN ============
        
        Tarea tareaActualizada = tareaDAO.buscarPorId(idTarea);
        assertNotNull(tareaActualizada, "Tarea debe existir");
        assertEquals(estadoNuevo, tareaActualizada.getEstado(), "Estado debe estar actualizado");
        
        // Verificar concatenación de comentarios
        String comentariosFinales = tareaActualizada.getComentarios();
        assertNotNull(comentariosFinales, "Comentarios no deben ser null");
        assertTrue(comentariosFinales.contains("Iniciando tarea"),
                "Debe mantener comentario original");
        assertTrue(comentariosFinales.contains("Comenzando desarrollo"),
                "Debe incluir comentario nuevo");
        
        System.out.println("✅ Comentarios concatenados correctamente:");
        System.out.println("   " + comentariosFinales);
        System.out.println("\n========================================");
        System.out.println("✅ FLUJO VERIFICADO CON ÉXITO");
        System.out.println("========================================\n");
    }

    // ========================================
    // FLUJO: MÚLTIPLES CAMBIOS DE ESTADO
    // ========================================

    @Test
    public void flujo_multiplescambiosDeEstado_concatenaComentarios() throws Exception {
        System.out.println("\n========================================");
        System.out.println("FLUJO: Múltiples Cambios de Estado");
        System.out.println("========================================\n");
        
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setNombre("María");
        usuario.setApellido("López");
        usuario.setEmail("maria.lopez@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        int idUsuario = usuarioCreado.getId();
        
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Multi-Estado");
        proyecto.setDescripcion("Para probar múltiples cambios");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(5000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        Tarea tarea = new Tarea();
        tarea.setNombre("Tarea Multi-Estado");
        tarea.setDescripcion("Pasará por varios estados");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(proyectoCreado.getId());
        tarea.setIdResponsable(idUsuario);
        tarea.setEstado("PENDIENTE");
        tarea.setComentarios("Comentario inicial");
        Tarea tareaCreada = tareaDAO.insertar(tarea);
        int idTarea = tareaCreada.getId();
        
        System.out.println("✅ Tarea creada con ID: " + idTarea);
        
        // CAMBIO 1: PENDIENTE → EN_PROGRESO
        System.out.println("\n📝 Cambio 1: PENDIENTE → EN_PROGRESO");
        boolean cambio1 = tareaDAO.actualizarEstado(idTarea, "EN_PROGRESO", "Iniciando trabajo");
        assertTrue(cambio1);
        
        // CAMBIO 2: EN_PROGRESO → COMPLETADA
        System.out.println("📝 Cambio 2: EN_PROGRESO → COMPLETADA");
        boolean cambio2 = tareaDAO.actualizarEstado(idTarea, "COMPLETADA", "Trabajo finalizado");
        assertTrue(cambio2);
        
        // Verificar comentarios concatenados
        Tarea tareaFinal = tareaDAO.buscarPorId(idTarea);
        String comentarios = tareaFinal.getComentarios();
        
        assertTrue(comentarios.contains("Comentario inicial"));
        assertTrue(comentarios.contains("Iniciando trabajo"));
        assertTrue(comentarios.contains("Trabajo finalizado"));
        
        System.out.println("\n✅ Comentarios concatenados correctamente:");
        System.out.println("   " + comentarios.replace("\n", "\n   "));
        System.out.println("\n========================================");
        System.out.println("✅ TEST COMPLETADO");
        System.out.println("========================================\n");
    }

    // ========================================
    // FLUJO: VERIFICAR ESTADO EN BD
    // ========================================

    @Test
    public void flujo_actualizarEstado_persisteEnBaseDeDatos() throws Exception {
        System.out.println("\n========================================");
        System.out.println("FLUJO: Verificar Persistencia en BD");
        System.out.println("========================================\n");
        
        // Crear datos
        Usuario usuario = new Usuario();
        usuario.setNombre("Pedro");
        usuario.setApellido("Gómez");
        usuario.setEmail("pedro.gomez@test.com");
        usuario.setPassword("password");
        usuario.setEsAdmin(false);
        Usuario usuarioCreado = usuarioDAO.insertar(usuario);
        
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Persistencia");
        proyecto.setDescripcion("Test de persistencia");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(1000.0);
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        Tarea tarea = new Tarea();
        tarea.setNombre("Tarea Persistencia");
        tarea.setDescripcion("Test de BD");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(proyectoCreado.getId());
        tarea.setIdResponsable(usuarioCreado.getId());
        tarea.setEstado("PENDIENTE");
        tarea.setComentarios("Inicial");
        Tarea tareaCreada = tareaDAO.insertar(tarea);
        int idTarea = tareaCreada.getId();
        
        // Actualizar estado
        tareaDAO.actualizarEstado(idTarea, "COMPLETADA", "Finalizado");
        
        // Verificar DIRECTAMENTE en BD usando SQL
        String sql = "SELECT estado, comentarios FROM tareas WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idTarea);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Tarea debe existir en BD");
                
                String estadoBD = rs.getString("estado");
                String comentariosBD = rs.getString("comentarios");
                
                assertEquals("COMPLETADA", estadoBD, "Estado debe ser COMPLETADA en BD");
                assertTrue(comentariosBD.contains("Inicial"), "Debe contener comentario inicial");
                assertTrue(comentariosBD.contains("Finalizado"), "Debe contener comentario nuevo");
                
                System.out.println("✅ Verificación directa en BD:");
                System.out.println("   Estado en BD: " + estadoBD);
                System.out.println("   Comentarios en BD: " + comentariosBD);
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("✅ PERSISTENCIA VERIFICADA");
        System.out.println("========================================\n");
    }
}

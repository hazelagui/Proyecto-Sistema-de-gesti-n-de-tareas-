package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.util.Notificador;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador para la gestión de tareas.
 */
public class ControladorTarea {
    // DAO para acceso a la base de datos
    private final TareaDAO tareaDAO;
    private final Notificador notificador;
    
    // Constructor que inicializa el DAO y verifica datos iniciales
    public ControladorTarea() {
        this.tareaDAO = new TareaDAO();
        this.notificador = new Notificador(new ConcurrentHashMap<>());
        verificarDatosIniciales();
    }
    
    /**
     * Verifica si existen datos iniciales y los crea si es necesario.
     */
    private void verificarDatosIniciales() {
        List<Tarea> tareas = obtenerTodasLasTareas();
        
        // Si no hay tareas, crear las tareas por defecto
        if (tareas.isEmpty()) {
            Date ahora = new Date();
            Date futuro = new Date(ahora.getTime() + 7L * 24 * 60 * 60 * 1000); // 7 días después
            
            crearTarea("Diseñar interfaz", "Diseñar la interfaz de usuario del sistema", 
                      ahora, futuro, 1, 1, "PENDIENTE", "");
            crearTarea("Implementar base de datos", "Crear el esquema de la base de datos", 
                      ahora, futuro, 1, 2, "EN PROCESO", "Esquema inicial creado");
        }
    }
    
    /**
     * Crea una nueva tarea.
     * @param nombre Nombre de la tarea.
     * @param descripcion Descripción de la tarea.
     * @param fechaCreacion Fecha de creación de la tarea.
     * @param fechaVencimiento Fecha de vencimiento de la tarea.
     * @param idProyecto ID del proyecto al que pertenece la tarea.
     * @param idResponsable ID del usuario responsable.
     * @param estado Estado de la tarea (Pendiente, En proceso, Completada).
     * @param comentarios Comentarios adicionales.
     * @return Tarea creada o null si no se pudo crear.
     */
    public Tarea crearTarea(String nombre, String descripcion, Date fechaCreacion, Date fechaVencimiento,
                          int idProyecto, int idResponsable, String estado, String comentarios) {
        // Validar datos
        if (nombre == null || nombre.trim().isEmpty() ||
            descripcion == null || descripcion.trim().isEmpty() ||
            fechaCreacion == null || fechaVencimiento == null ||
            idProyecto <= 0 || idResponsable <= 0 ||
            estado == null || estado.trim().isEmpty()) {
            return null;
        }
        
        // Validar estado
        estado = validarEstado(estado);
        
        // Crear la tarea
        Tarea tarea = new Tarea(nombre, descripcion, fechaCreacion, fechaVencimiento,
                               idProyecto, idResponsable, estado, comentarios != null ? comentarios : "");
        
        // Insertar en la base de datos
        return tareaDAO.insertar(tarea);
    }
    
    /**
     * Actualiza una tarea existente.
     * @param id ID de la tarea.
     * @param nombre Nombre de la tarea.
     * @param descripcion Descripción de la tarea.
     * @param fechaVencimiento Fecha de vencimiento de la tarea.
     * @param idProyecto ID del proyecto al que pertenece la tarea.
     * @param idResponsable ID del usuario responsable.
     * @param estado Estado de la tarea (Pendiente, En proceso, Completada).
     * @param comentarios Comentarios adicionales.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarTarea(int id, String nombre, String descripcion, Date fechaVencimiento,
                                 int idProyecto, int idResponsable, String estado, String comentarios) {
        // Validar datos
        if (id <= 0 || nombre == null || nombre.trim().isEmpty() ||
            descripcion == null || descripcion.trim().isEmpty() ||
            fechaVencimiento == null || idProyecto <= 0 || idResponsable <= 0 ||
            estado == null || estado.trim().isEmpty()) {
            return false;
        }
        
        // Validar estado
        estado = validarEstado(estado);
        
        // Obtener la tarea existente para preservar la fecha de creación
        Tarea tareaExistente = obtenerTareaPorId(id);
        
        // Verificar si la tarea existe
        if (tareaExistente == null) {
            return false;
        }
        
        // Crear la tarea actualizada
        Tarea tarea = new Tarea(id, nombre, descripcion, tareaExistente.getFechaCreacion(), 
                               fechaVencimiento, idProyecto, idResponsable, estado, 
                               comentarios != null ? comentarios : "");
        
        // Actualizar en la base de datos
        return tareaDAO.actualizar(tarea);
    }
    
    /**
     * Actualiza el estado de una tarea.
     * @param id ID de la tarea.
     * @param nuevoEstado Nuevo estado de la tarea.
     * @param comentarioAdicional Comentario adicional (opcional).
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarEstadoTarea(int id, String nuevoEstado, String comentarioAdicional) {
        // Validar datos
        if (id <= 0 || nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return false;
        }
        
        // Validar estado
        nuevoEstado = validarEstado(nuevoEstado);
        
        // Obtener tarea actual para comparar estados
        Tarea tarea = obtenerTareaPorId(id);
        if (tarea == null) {
            return false;
        }
        
        String estadoAnterior = tarea.getEstado();
        
        // Actualizar estado en la base de datos
        boolean exito = tareaDAO.actualizarEstado(id, nuevoEstado, comentarioAdicional);
        
        if (exito) {
            // Enviar notificación del cambio de estado
            tarea.setEstado(nuevoEstado);
            notificador.notificarCambioEstadoTarea(tarea, estadoAnterior);
        }
        
        return exito;
    }
    
    /**
     * Obtiene una tarea por su ID.
     * @param id ID de la tarea.
     * @return Tarea encontrada o null si no existe.
     */
    public Tarea obtenerTareaPorId(int id) {
        if (id <= 0) {
            return null;
        }
        
        return tareaDAO.buscarPorId(id);
    }
    
    /**
     * Obtiene todas las tareas.
     * @return Lista de todas las tareas.
     */
    public List<Tarea> obtenerTodasLasTareas() {
        return tareaDAO.listarTodas();
    }
    
    /**
     * Obtiene las tareas de un proyecto específico.
     * @param idProyecto ID del proyecto.
     * @return Lista de tareas del proyecto.
     */
    public List<Tarea> obtenerTareasPorProyecto(int idProyecto) {
        if (idProyecto <= 0) {
            return new ArrayList<>();
        }
        
        return tareaDAO.listarPorProyecto(idProyecto);
    }
    
    /**
     * Obtiene las tareas asignadas a un responsable.
     * @param idResponsable ID del responsable.
     * @return Lista de tareas asignadas al responsable.
     */
    public List<Tarea> obtenerTareasPorResponsable(int idResponsable) {
        if (idResponsable <= 0) {
            return new ArrayList<>();
        }
        
        return tareaDAO.listarPorResponsable(idResponsable);
    }
    
    /**
     * Elimina una tarea.
     * @param id ID de la tarea a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarTarea(int id) {
        if (id <= 0) {
            return false;
        }
        
        return tareaDAO.eliminar(id);
    }
    
    /**
     * Valida y normaliza el estado de una tarea.
     * @param estado Estado a validar.
     * @return Estado validado y normalizado.
     */
    private String validarEstado(String estado) {
        estado = estado.trim().toUpperCase();
        
        switch (estado) {
            case "PENDIENTE":
            case "EN PROCESO":
            case "COMPLETADA":
                return estado;
            default:
                return "PENDIENTE"; // Valor predeterminado
        }
    }
} 
package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.ProyectoDAO;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controlador para la gestión de proyectos.
 */
public class ControladorProyecto {
    // DAO para acceso a la base de datos
    private final ProyectoDAO proyectoDAO;
    
    // Constructor que inicializa el DAO y verifica datos iniciales
    public ControladorProyecto() {
        this.proyectoDAO = new ProyectoDAO();
        verificarDatosIniciales();
    }
    
    /**
     * Verifica si existen datos iniciales y los crea si es necesario.
     */
    private void verificarDatosIniciales() {
        List<Proyecto> proyectos = obtenerTodosLosProyectos();
        
        // Si no hay proyectos, crear los proyectos por defecto
        if (proyectos.isEmpty()) {
            Date ahora = new Date();
            Date futuro = new Date(ahora.getTime() + 30L * 24 * 60 * 60 * 1000); // 30 días después
            
            crearProyecto("Sistema de Gestión", "Desarrollo de un sistema de gestión de tareas",
                         ahora, futuro, 1, "VERDE", 0.0);
            crearProyecto("Migración de Datos", "Migración de datos de sistema antiguo",
                         ahora, futuro, 2, "AMARILLO", 0.0);
        }
    }
    
    /**
     * Crea un nuevo proyecto.
     * @param nombre Nombre del proyecto.
     * @param descripcion Descripción del proyecto.
     * @param fechaInicio Fecha de inicio del proyecto.
     * @param fechaFin Fecha de finalización del proyecto (puede ser null).
     * @param idResponsable ID del usuario responsable.
     * @param nivelRiesgo Nivel de riesgo (Verde, Amarillo, Rojo).
     * @param presupuestoTotal Presupuesto total del proyecto.
     * @return Proyecto creado o null si no se pudo crear.
     */
    public Proyecto crearProyecto(String nombre, String descripcion, Date fechaInicio, Date fechaFin,
                                int idResponsable, String nivelRiesgo, double presupuestoTotal) {
        // Validar datos
        if (nombre == null || nombre.trim().isEmpty() ||
            descripcion == null || descripcion.trim().isEmpty() ||
            fechaInicio == null || idResponsable <= 0 ||
            nivelRiesgo == null || nivelRiesgo.trim().isEmpty()) {
            return null;
        }
        
        // Validar nivel de riesgo
        nivelRiesgo = nivelRiesgo.toUpperCase();
        if (!nivelRiesgo.equals("VERDE") && !nivelRiesgo.equals("AMARILLO") && !nivelRiesgo.equals("ROJO")) {
            nivelRiesgo = "VERDE"; // Valor predeterminado
        }
        
        // Crear el proyecto
        Proyecto proyecto = new Proyecto(nombre, descripcion, fechaInicio, fechaFin, idResponsable, nivelRiesgo, presupuestoTotal);
        
        // Insertar en la base de datos
        return proyectoDAO.insertar(proyecto);
    }
    
    /**
     * Actualiza un proyecto existente.
     * @param proyecto Proyecto con los datos actualizados.
     * @return true si se actualizó correctamente, false en caso contrario.
     */
    public boolean actualizarProyecto(Proyecto proyecto) {
        return proyectoDAO.actualizar(proyecto);
    }
    
    /**
     * Obtiene un proyecto por su ID.
     * @param id ID del proyecto.
     * @return Proyecto encontrado o null si no existe.
     */
    public Proyecto obtenerProyectoPorId(int id) {
        if (id <= 0) {
            return null;
        }
        
        return proyectoDAO.buscarPorId(id);
    }
    
    /**
     * Obtiene todos los proyectos.
     * @return Lista de todos los proyectos.
     */
    public List<Proyecto> obtenerTodosLosProyectos() {
        return proyectoDAO.listarTodos();
    }
    
    /**
     * Obtiene los proyectos asignados a un responsable.
     * @param idResponsable ID del responsable.
     * @return Lista de proyectos asignados al responsable.
     */
    public List<Proyecto> obtenerProyectosPorResponsable(int idResponsable) {
        if (idResponsable <= 0) {
            return new ArrayList<>();
        }
        
        return proyectoDAO.listarPorResponsable(idResponsable);
    }
    
    /**
     * Elimina un proyecto.
     * @param id ID del proyecto a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarProyecto(int id) {
        if (id <= 0) {
            return false;
        }
        
        return proyectoDAO.eliminar(id);
    }
} 
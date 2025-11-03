package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase DAO para operaciones de la entidad Tarea en la base de datos.
 */
public class TareaDAO {
    
    /**
     * Inserta una nueva tarea en la base de datos.
     * @param tarea Tarea a insertar.
     * @return Tarea con ID generado o null si hubo error.
     */
    public Tarea insertar(Tarea tarea) {
        String sql = "INSERT INTO tareas (nombre, descripcion, fecha_creacion, fecha_vencimiento, id_proyecto, id_responsable, estado, comentarios) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, tarea.getNombre());
            stmt.setString(2, tarea.getDescripcion());
            stmt.setTimestamp(3, new Timestamp(tarea.getFechaCreacion().getTime()));
            stmt.setTimestamp(4, new Timestamp(tarea.getFechaVencimiento().getTime()));
            stmt.setInt(5, tarea.getIdProyecto());
            stmt.setInt(6, tarea.getIdResponsable());
            stmt.setString(7, tarea.getEstado());
            stmt.setString(8, tarea.getComentarios());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas == 0) {
                return null;
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    tarea.setId(rs.getInt(1));
                    return tarea;
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al insertar tarea: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Actualiza una tarea existente en la base de datos.
     * @param tarea Tarea a actualizar.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizar(Tarea tarea) {
        String sql = "UPDATE tareas SET nombre = ?, descripcion = ?, fecha_vencimiento = ?, id_proyecto = ?, id_responsable = ?, estado = ?, comentarios = ? WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tarea.getNombre());
            stmt.setString(2, tarea.getDescripcion());
            stmt.setTimestamp(3, new Timestamp(tarea.getFechaVencimiento().getTime()));
            stmt.setInt(4, tarea.getIdProyecto());
            stmt.setInt(5, tarea.getIdResponsable());
            stmt.setString(6, tarea.getEstado());
            stmt.setString(7, tarea.getComentarios());
            stmt.setInt(8, tarea.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar tarea: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza el estado de una tarea.
     * @param id ID de la tarea.
     * @param nuevoEstado Nuevo estado.
     * @param comentario Comentario adicional.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarEstado(int id, String nuevoEstado, String comentario) {
        String sql = "UPDATE tareas SET estado = ?, comentarios = CONCAT(IFNULL(comentarios, ''), ?) WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevoEstado);
            
            // Si hay comentario, agregar salto de línea
            if (comentario != null && !comentario.trim().isEmpty()) {
                stmt.setString(2, "\n" + comentario);
            } else {
                stmt.setString(2, "");
            }
            
            stmt.setInt(3, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de tarea: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina una tarea de la base de datos.
     * @param id ID de la tarea a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM tareas WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar tarea: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca una tarea por su ID.
     * @param id ID de la tarea a buscar.
     * @return Tarea encontrada o null si no existe.
     */
    public Tarea buscarPorId(int id) {
        String sql = "SELECT * FROM tareas WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extraerTareaDeResultSet(rs);
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar tarea por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene todas las tareas de la base de datos.
     * @return Lista de todas las tareas.
     */
    public List<Tarea> listarTodas() {
        String sql = "SELECT * FROM tareas";
        List<Tarea> tareas = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Tarea tarea = extraerTareaDeResultSet(rs);
                tareas.add(tarea);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar tareas: " + e.getMessage());
        }
        
        return tareas;
    }
    
    /**
     * Obtiene las tareas de un proyecto específico.
     * @param idProyecto ID del proyecto.
     * @return Lista de tareas del proyecto.
     */
    public List<Tarea> listarPorProyecto(int idProyecto) {
        String sql = "SELECT * FROM tareas WHERE id_proyecto = ?";
        List<Tarea> tareas = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idProyecto);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tarea tarea = extraerTareaDeResultSet(rs);
                    tareas.add(tarea);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar tareas por proyecto: " + e.getMessage());
        }
        
        return tareas;
    }
    
    /**
     * Obtiene las tareas asignadas a un responsable.
     * @param idResponsable ID del responsable.
     * @return Lista de tareas asignadas al responsable.
     */
    public List<Tarea> listarPorResponsable(int idResponsable) {
        String sql = "SELECT * FROM tareas WHERE id_responsable = ?";
        List<Tarea> tareas = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idResponsable);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tarea tarea = extraerTareaDeResultSet(rs);
                    tareas.add(tarea);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar tareas por responsable: " + e.getMessage());
        }
        
        return tareas;
    }
    
    /**
     * Extrae un objeto Tarea de un ResultSet.
     * @param rs ResultSet con datos de tarea.
     * @return Objeto Tarea.
     * @throws SQLException Si ocurre un error al acceder a los datos.
     */
    private Tarea extraerTareaDeResultSet(ResultSet rs) throws SQLException {
        Tarea tarea = new Tarea();
        tarea.setId(rs.getInt("id"));
        tarea.setNombre(rs.getString("nombre"));
        tarea.setDescripcion(rs.getString("descripcion"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            tarea.setFechaCreacion(new Date(fechaCreacion.getTime()));
        }
        
        Timestamp fechaVencimiento = rs.getTimestamp("fecha_vencimiento");
        if (fechaVencimiento != null) {
            tarea.setFechaVencimiento(new Date(fechaVencimiento.getTime()));
        }
        
        tarea.setIdProyecto(rs.getInt("id_proyecto"));
        tarea.setIdResponsable(rs.getInt("id_responsable"));
        tarea.setEstado(rs.getString("estado"));
        tarea.setComentarios(rs.getString("comentarios"));
        
        return tarea;
    }
} 
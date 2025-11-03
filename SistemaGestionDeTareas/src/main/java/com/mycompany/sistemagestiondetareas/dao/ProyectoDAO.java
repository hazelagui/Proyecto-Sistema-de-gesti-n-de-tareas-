package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para operaciones de la entidad Proyecto en la base de datos.
 */
public class ProyectoDAO {
    
    private static final String SQL_INSERT = "INSERT INTO proyectos (nombre, descripcion, fecha_inicio, fecha_fin, id_responsable, nivel_riesgo, presupuesto_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE proyectos SET nombre = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?, id_responsable = ?, nivel_riesgo = ?, presupuesto_total = ? WHERE id = ?";
    
    /**
     * Inserta un nuevo proyecto en la base de datos.
     * @param proyecto Proyecto a insertar.
     * @return Proyecto con ID generado o null si hubo error.
     */
    public Proyecto insertar(Proyecto proyecto) {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getDescripcion());
            stmt.setTimestamp(3, new Timestamp(proyecto.getFechaInicio().getTime()));
            stmt.setTimestamp(4, proyecto.getFechaFin() != null ? new Timestamp(proyecto.getFechaFin().getTime()) : null);
            stmt.setInt(5, proyecto.getIdResponsable());
            stmt.setString(6, proyecto.getNivelRiesgo());
            stmt.setDouble(7, proyecto.getPresupuestoTotal());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return null;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    proyecto.setId(generatedKeys.getInt(1));
                    return proyecto;
                }
            }
            
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza un proyecto existente en la base de datos.
     * @param proyecto Proyecto a actualizar.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizar(Proyecto proyecto) {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            stmt.setString(1, proyecto.getNombre());
            stmt.setString(2, proyecto.getDescripcion());
            stmt.setTimestamp(3, new Timestamp(proyecto.getFechaInicio().getTime()));
            stmt.setTimestamp(4, proyecto.getFechaFin() != null ? new Timestamp(proyecto.getFechaFin().getTime()) : null);
            stmt.setInt(5, proyecto.getIdResponsable());
            stmt.setString(6, proyecto.getNivelRiesgo());
            stmt.setDouble(7, proyecto.getPresupuestoTotal());
            stmt.setInt(8, proyecto.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Elimina un proyecto de la base de datos.
     * @param id ID del proyecto a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proyectos WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar proyecto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca un proyecto por su ID.
     * @param id ID del proyecto a buscar.
     * @return Proyecto encontrado o null si no existe.
     */
    public Proyecto buscarPorId(int id) {
        String sql = "SELECT * FROM proyectos WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extraerProyectoDeResultSet(rs);
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar proyecto por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene todos los proyectos de la base de datos.
     * @return Lista de todos los proyectos.
     */
    public List<Proyecto> listarTodos() {
        String sql = "SELECT * FROM proyectos";
        List<Proyecto> proyectos = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Proyecto proyecto = extraerProyectoDeResultSet(rs);
                proyectos.add(proyecto);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar proyectos: " + e.getMessage());
        }
        
        return proyectos;
    }
    
    /**
     * Obtiene los proyectos asignados a un responsable.
     * @param idResponsable ID del responsable.
     * @return Lista de proyectos asignados al responsable.
     */
    public List<Proyecto> listarPorResponsable(int idResponsable) {
        String sql = "SELECT * FROM proyectos WHERE id_responsable = ?";
        List<Proyecto> proyectos = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idResponsable);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Proyecto proyecto = extraerProyectoDeResultSet(rs);
                    proyectos.add(proyecto);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar proyectos por responsable: " + e.getMessage());
        }
        
        return proyectos;
    }
    
    /**
     * Extrae un objeto Proyecto de un ResultSet.
     * @param rs ResultSet con datos de proyecto.
     * @return Objeto Proyecto.
     * @throws SQLException Si ocurre un error al acceder a los datos.
     */
    private Proyecto extraerProyectoDeResultSet(ResultSet rs) throws SQLException {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(rs.getInt("id"));
        proyecto.setNombre(rs.getString("nombre"));
        proyecto.setDescripcion(rs.getString("descripcion"));
        proyecto.setFechaInicio(rs.getTimestamp("fecha_inicio"));
        proyecto.setFechaFin(rs.getTimestamp("fecha_fin"));
        proyecto.setIdResponsable(rs.getInt("id_responsable"));
        proyecto.setNivelRiesgo(rs.getString("nivel_riesgo"));
        proyecto.setPresupuestoTotal(rs.getDouble("presupuesto_total"));
        return proyecto;
    }
} 
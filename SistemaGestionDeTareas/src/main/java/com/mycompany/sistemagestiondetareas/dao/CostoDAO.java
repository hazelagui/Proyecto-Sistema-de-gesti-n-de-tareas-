package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Costo;
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
 * Clase DAO para operaciones de la entidad Costo en la base de datos.
 */
public class CostoDAO {
    
    /**
     * Inserta un nuevo costo en la base de datos.
     * @param costo Costo a insertar.
     * @return Costo con ID generado o null si hubo error.
     */
    public Costo insertar(Costo costo) {
        String sql = "INSERT INTO costos (tipo, id_referencia, descripcion, monto, tipo_costo, fecha_registro, id_usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, costo.getTipo());
            stmt.setInt(2, costo.getIdReferencia());
            stmt.setString(3, costo.getDescripcion());
            stmt.setDouble(4, costo.getMonto());
            stmt.setString(5, costo.getTipoCosto());
            stmt.setTimestamp(6, new Timestamp(costo.getFechaRegistro().getTime()));
            stmt.setInt(7, costo.getIdUsuarioRegistro());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas == 0) {
                return null;
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    costo.setId(rs.getInt(1));
                    return costo;
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al insertar costo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene los costos de un proyecto o tarea.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @return Lista de costos asociados.
     */
    public List<Costo> listarPorReferencia(String tipo, int idReferencia) {
        String sql = "SELECT * FROM costos WHERE tipo = ? AND id_referencia = ?";
        List<Costo> costos = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo);
            stmt.setInt(2, idReferencia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Costo costo = extraerCostoDeResultSet(rs);
                    costos.add(costo);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar costos por referencia: " + e.getMessage());
        }
        
        return costos;
    }
    
    /**
     * Obtiene los costos registrados por un usuario.
     * @param idUsuario ID del usuario.
     * @return Lista de costos registrados por el usuario.
     */
    public List<Costo> listarPorUsuario(int idUsuario) {
        String sql = "SELECT * FROM costos WHERE id_usuario_registro = ?";
        List<Costo> costos = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Costo costo = extraerCostoDeResultSet(rs);
                    costos.add(costo);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar costos por usuario: " + e.getMessage());
        }
        
        return costos;
    }
    
    /**
     * Calcula el total de costos por tipo para un proyecto o tarea.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @param tipoCosto Tipo de costo a sumar (RETRASO, ADELANTO, GASTO_PLANIFICADO).
     * @return Suma total de los costos.
     */
    public double calcularTotalPorTipo(String tipo, int idReferencia, String tipoCosto) {
        String sql = "SELECT SUM(monto) as total FROM costos WHERE tipo = ? AND id_referencia = ? AND tipo_costo = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo);
            stmt.setInt(2, idReferencia);
            stmt.setString(3, tipoCosto);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al calcular total de costos: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * Extrae un objeto Costo de un ResultSet.
     * @param rs ResultSet con datos de costo.
     * @return Objeto Costo.
     * @throws SQLException Si ocurre un error al acceder a los datos.
     */
    private Costo extraerCostoDeResultSet(ResultSet rs) throws SQLException {
        Costo costo = new Costo();
        costo.setId(rs.getInt("id"));
        costo.setTipo(rs.getString("tipo"));
        costo.setIdReferencia(rs.getInt("id_referencia"));
        costo.setDescripcion(rs.getString("descripcion"));
        costo.setMonto(rs.getDouble("monto"));
        costo.setTipoCosto(rs.getString("tipo_costo"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            costo.setFechaRegistro(new Date(fechaRegistro.getTime()));
        }
        
        costo.setIdUsuarioRegistro(rs.getInt("id_usuario_registro"));
        
        return costo;
    }
} 
package com.mycompany.sistemagestiondetareas.dao;

import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import com.mycompany.sistemagestiondetareas.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para operaciones de la entidad Usuario en la base de datos.
 */
public class UsuarioDAO {
    
    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario Usuario a insertar.
     * @return Usuario con ID generado o null si hubo error.
     */
    public Usuario insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, password, es_admin) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getPassword());
            stmt.setBoolean(5, usuario.isEsAdmin());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas == 0) {
                return null;
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                    return usuario;
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Actualiza un usuario existente en la base de datos.
     * @param usuario Usuario a actualizar.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, password = ?, es_admin = ? WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getPassword());
            stmt.setBoolean(5, usuario.isEsAdmin());
            stmt.setInt(6, usuario.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina un usuario de la base de datos.
     * @param id ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca un usuario por su ID.
     * @param id ID del usuario a buscar.
     * @return Usuario encontrado o null si no existe.
     */
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extraerUsuarioDeResultSet(rs);
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca un usuario por su email.
     * @param email Email del usuario a buscar.
     * @return Usuario encontrado o null si no existe.
     */
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extraerUsuarioDeResultSet(rs);
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene todos los usuarios de la base de datos.
     * @return Lista de todos los usuarios.
     */
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario usuario = extraerUsuarioDeResultSet(rs);
                usuarios.add(usuario);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    /**
     * Extrae un objeto Usuario de un ResultSet.
     * @param rs ResultSet con datos de usuario.
     * @return Objeto Usuario.
     * @throws SQLException Si ocurre un error al acceder a los datos.
     */
    private Usuario extraerUsuarioDeResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEsAdmin(rs.getBoolean("es_admin"));
        return usuario;
    }
} 
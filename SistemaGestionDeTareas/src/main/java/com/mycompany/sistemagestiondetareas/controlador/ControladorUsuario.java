package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.util.List;

/**
 * Controlador para la gestión de usuarios.
 */
public class ControladorUsuario {
    // DAO para acceso a la base de datos
    private final UsuarioDAO usuarioDAO;
    
    // Constructor que inicializa el DAO y verifica datos iniciales
    public ControladorUsuario() {
        this.usuarioDAO = new UsuarioDAO();
        verificarDatosIniciales();
    }
    
    /**
     * Verifica si existen datos iniciales y los crea si es necesario.
     */
    private void verificarDatosIniciales() {
        List<Usuario> usuarios = obtenerTodosLosUsuarios();
        
        // Si no hay usuarios, crear los usuarios por defecto
        if (usuarios.isEmpty()) {
            registrarUsuario("Admin", "Sistema", "admin@sistema.com", "admin123", true);
            registrarUsuario("Usuario", "Normal", "usuario@sistema.com", "user123", false);
        }
    }
    
    /**
     * Registra un nuevo usuario.
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @param esAdmin Indica si el usuario es administrador.
     * @return El usuario registrado o null si no se pudo registrar.
     */
    public Usuario registrarUsuario(String nombre, String apellido, String email, String password, boolean esAdmin) {
        // Validar datos
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }
        
        // Verificar si ya existe un usuario con ese email
        if (obtenerPorEmail(email) != null) {
            return null;
        }
        
        // Crear el usuario
        Usuario usuario = new Usuario(nombre, apellido, email, password, esAdmin);
        
        // Insertar en la base de datos
        return usuarioDAO.insertar(usuario);
    }
    
    /**
     * Autentica un usuario con sus credenciales.
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @return Usuario autenticado o null si las credenciales son inválidas.
     */
    public Usuario autenticarUsuario(String email, String password) {
        Usuario usuario = obtenerPorEmail(email);
        
        if (usuario != null && usuario.getPassword().equals(password)) {
            return usuario;
        }
        
        return null;
    }
    
    /**
     * Busca un usuario por su email.
     * @param email Email del usuario a buscar.
     * @return Usuario encontrado o null si no existe.
     */
    private Usuario obtenerPorEmail(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }
    
    /**
     * Obtiene un usuario por su ID.
     * @param id ID del usuario a buscar.
     * @return Usuario encontrado o null si no existe.
     */
    public Usuario obtenerUsuarioPorId(int id) {
        return usuarioDAO.buscarPorId(id);
    }
    
    /**
     * Obtiene todos los usuarios registrados.
     * @return Lista de todos los usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.listarTodos();
    }
    
    /**
     * Actualiza los datos de un usuario.
     * @param usuario Usuario con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            return false;
        }
        
        return usuarioDAO.actualizar(usuario);
    }
    
    /**
     * Actualiza la contraseña de un usuario.
     * @param idUsuario ID del usuario.
     * @param nuevaPassword Nueva contraseña.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public boolean actualizarPassword(int idUsuario, String nuevaPassword) {
        Usuario usuario = obtenerUsuarioPorId(idUsuario);
        
        if (usuario == null || nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            return false;
        }
        
        usuario.setPassword(nuevaPassword);
        return usuarioDAO.actualizar(usuario);
    }
    
    /**
     * Elimina un usuario.
     * @param id ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarUsuario(int id) {
        if (id <= 0) {
            return false;
        }
        
        return usuarioDAO.eliminar(id);
    }
} 
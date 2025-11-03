package com.mycompany.sistemagestiondetareas.modelo;

/**
 * Clase que representa un usuario del sistema.
 */
public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private boolean esAdmin;
    
    // Constructor vacío
    public Usuario() {
    }
    
    // Constructor con todos los campos excepto id
    public Usuario(String nombre, String apellido, String email, String password, boolean esAdmin) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.esAdmin = esAdmin;
    }
    
    // Constructor completo
    public Usuario(int id, String nombre, String apellido, String email, String password, boolean esAdmin) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.esAdmin = esAdmin;
    }
    
    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }
    
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
} 
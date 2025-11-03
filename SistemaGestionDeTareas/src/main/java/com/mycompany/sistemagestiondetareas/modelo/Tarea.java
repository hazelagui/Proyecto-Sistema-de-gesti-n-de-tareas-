package com.mycompany.sistemagestiondetareas.modelo;

import java.util.Date;

/**
 * Clase que representa una tarea dentro de un proyecto.
 */
public class Tarea {
    private int id;
    private String nombre;
    private String descripcion;
    private Date fechaCreacion;
    private Date fechaVencimiento;
    private int idProyecto;
    private int idResponsable;
    private String estado; // Pendiente, En proceso, Completada
    private String comentarios;
    
    // Constructor vacío
    public Tarea() {
    }
    
    // Constructor sin ID
    public Tarea(String nombre, String descripcion, Date fechaCreacion, Date fechaVencimiento, 
                int idProyecto, int idResponsable, String estado, String comentarios) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.idProyecto = idProyecto;
        this.idResponsable = idResponsable;
        this.estado = estado;
        this.comentarios = comentarios;
    }
    
    // Constructor completo
    public Tarea(int id, String nombre, String descripcion, Date fechaCreacion, Date fechaVencimiento, 
                int idProyecto, int idResponsable, String estado, String comentarios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.idProyecto = idProyecto;
        this.idResponsable = idResponsable;
        this.estado = estado;
        this.comentarios = comentarios;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public int getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(int idResponsable) {
        this.idResponsable = idResponsable;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
} 
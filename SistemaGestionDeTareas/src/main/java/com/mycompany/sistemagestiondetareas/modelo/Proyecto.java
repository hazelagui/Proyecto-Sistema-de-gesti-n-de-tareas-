package com.mycompany.sistemagestiondetareas.modelo;

import java.util.Date;

/**
 * Clase que representa un proyecto en el sistema.
 */
public class Proyecto {
    private int id;
    private String nombre;
    private String descripcion;
    private Date fechaInicio;
    private Date fechaFin;
    private int idResponsable;
    private String nivelRiesgo; // Verde (bajo), Amarillo (medio), Rojo (alto)
    private double presupuestoTotal;
    
    // Constructor vacío
    public Proyecto() {
    }
    
    // Constructor sin ID
    public Proyecto(String nombre, String descripcion, Date fechaInicio, Date fechaFin, 
                   int idResponsable, String nivelRiesgo, double presupuestoTotal) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.idResponsable = idResponsable;
        this.nivelRiesgo = nivelRiesgo;
        this.presupuestoTotal = presupuestoTotal;
    }
    
    // Constructor completo
    public Proyecto(int id, String nombre, String descripcion, Date fechaInicio, Date fechaFin, 
                   int idResponsable, String nivelRiesgo, double presupuestoTotal) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.idResponsable = idResponsable;
        this.nivelRiesgo = nivelRiesgo;
        this.presupuestoTotal = presupuestoTotal;
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

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(int idResponsable) {
        this.idResponsable = idResponsable;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public double getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(double presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
} 
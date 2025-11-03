package com.mycompany.sistemagestiondetareas.modelo;

import java.util.Date;

/**
 * Clase que representa los costos asociados a proyectos y tareas.
 */
public class Costo {
    private int id;
    private String tipo; // PROYECTO o TAREA
    private int idReferencia; // ID del proyecto o tarea
    private String descripcion;
    private double monto;
    private String tipoCosto; // RETRASO, ADELANTO, GASTO_PLANIFICADO
    private Date fechaRegistro;
    private int idUsuarioRegistro;
    
    // Constructor vacío
    public Costo() {
    }
    
    // Constructor sin ID
    public Costo(String tipo, int idReferencia, String descripcion, double monto, 
                String tipoCosto, Date fechaRegistro, int idUsuarioRegistro) {
        this.tipo = tipo;
        this.idReferencia = idReferencia;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipoCosto = tipoCosto;
        this.fechaRegistro = fechaRegistro;
        this.idUsuarioRegistro = idUsuarioRegistro;
    }
    
    // Constructor completo
    public Costo(int id, String tipo, int idReferencia, String descripcion, double monto, 
                String tipoCosto, Date fechaRegistro, int idUsuarioRegistro) {
        this.id = id;
        this.tipo = tipo;
        this.idReferencia = idReferencia;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipoCosto = tipoCosto;
        this.fechaRegistro = fechaRegistro;
        this.idUsuarioRegistro = idUsuarioRegistro;
    }
    
    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipoCosto() {
        return tipoCosto;
    }

    public void setTipoCosto(String tipoCosto) {
        this.tipoCosto = tipoCosto;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdUsuarioRegistro() {
        return idUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(int idUsuarioRegistro) {
        this.idUsuarioRegistro = idUsuarioRegistro;
    }
} 
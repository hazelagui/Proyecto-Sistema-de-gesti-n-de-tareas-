package com.mycompany.sistemagestiondetareas.modelo;

import java.io.Serializable;
import java.util.Date;

/**
 * Clase que representa un reporte de proyecto o tarea.
 */
public class Reporte implements Serializable {
    private int id;
    private String tipo; // Proyecto o Tarea
    private int idReferencia; // ID del proyecto o tarea
    private String titulo;
    private String contenido;
    private Date fechaGeneracion;
    private int idGenerador; // ID del usuario que generó el reporte
    
    // Constructor vacío
    public Reporte() {
    }
    
    // Constructor sin ID
    public Reporte(String tipo, int idReferencia, String titulo, String contenido, 
                  Date fechaGeneracion, int idGenerador) {
        this.tipo = tipo;
        this.idReferencia = idReferencia;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaGeneracion = fechaGeneracion;
        this.idGenerador = idGenerador;
    }
    
    // Constructor completo
    public Reporte(int id, String tipo, int idReferencia, String titulo, String contenido, 
                  Date fechaGeneracion, int idGenerador) {
        this.id = id;
        this.tipo = tipo;
        this.idReferencia = idReferencia;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaGeneracion = fechaGeneracion;
        this.idGenerador = idGenerador;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getIdGenerador() {
        return idGenerador;
    }

    public void setIdGenerador(int idGenerador) {
        this.idGenerador = idGenerador;
    }
    
    @Override
    public String toString() {
        return titulo;
    }
} 
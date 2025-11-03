package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.CostoDAO;
import com.mycompany.sistemagestiondetareas.modelo.Costo;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestionar los costos del sistema.
 */
public class ControladorCosto {
    private final CostoDAO costoDAO;
    
    public ControladorCosto() {
        this.costoDAO = new CostoDAO();
    }
    
    /**
     * Registra un nuevo costo en el sistema.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @param descripcion Descripción del costo.
     * @param monto Monto del costo.
     * @param tipoCosto Tipo de costo (RETRASO, ADELANTO, GASTO_PLANIFICADO).
     * @param idUsuarioRegistro ID del usuario que registra el costo.
     * @return Costo registrado o null si hubo error.
     */
    public Costo registrarCosto(String tipo, int idReferencia, String descripcion, 
                              double monto, String tipoCosto, int idUsuarioRegistro) {
        Costo costo = new Costo(tipo, idReferencia, descripcion, monto, tipoCosto, 
                              new Date(), idUsuarioRegistro);
        return costoDAO.insertar(costo);
    }
    
    /**
     * Obtiene los costos de un proyecto o tarea.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @return Lista de costos asociados.
     */
    public List<Costo> obtenerCostosPorReferencia(String tipo, int idReferencia) {
        return costoDAO.listarPorReferencia(tipo, idReferencia);
    }
    
    /**
     * Obtiene los costos registrados por un usuario.
     * @param idUsuario ID del usuario.
     * @return Lista de costos registrados por el usuario.
     */
    public List<Costo> obtenerCostosPorUsuario(int idUsuario) {
        return costoDAO.listarPorUsuario(idUsuario);
    }
    
    /**
     * Calcula el balance total de costos para un proyecto o tarea.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @return Balance total (adelantos - retrasos - gastos planificados).
     */
    public double calcularBalanceTotal(String tipo, int idReferencia) {
        double adelantos = costoDAO.calcularTotalPorTipo(tipo, idReferencia, "ADELANTO");
        double retrasos = costoDAO.calcularTotalPorTipo(tipo, idReferencia, "RETRASO");
        double gastos = costoDAO.calcularTotalPorTipo(tipo, idReferencia, "GASTO_PLANIFICADO");
        
        return adelantos - retrasos - gastos;
    }
    
    /**
     * Calcula el total de costos por tipo para un proyecto o tarea.
     * @param tipo Tipo de referencia (PROYECTO o TAREA).
     * @param idReferencia ID del proyecto o tarea.
     * @param tipoCosto Tipo de costo (RETRASO, ADELANTO, GASTO_PLANIFICADO).
     * @return Suma total de los costos.
     */
    public double calcularTotalPorTipo(String tipo, int idReferencia, String tipoCosto) {
        return costoDAO.calcularTotalPorTipo(tipo, idReferencia, tipoCosto);
    }
} 
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemagestiondetareas.util;

import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler para enviar recordatorios automáticos de tareas próximas a vencer.
 * Revisa cada 6 horas las tareas que vencen en las próximas 24 horas.
 */
public class RecordatorioScheduler {
    
    private static final long INTERVALO_REVISION = TimeUnit.HOURS.toMillis(6); // Cada 6 horas
    private static final long UMBRAL_DIAS_VENCIMIENTO = TimeUnit.DAYS.toMillis(1); // 24 horas
    
    private final TareaDAO tareaDAO;
    private final UsuarioDAO usuarioDAO;
    private final EmailSender emailSender;
    private Timer timer;
    
    public RecordatorioScheduler() {
        this.tareaDAO = new TareaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.emailSender = new EmailSender();
    }
    
    /**
     * Inicia el scheduler de recordatorios.
     */
    public void iniciar() {
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new Timer("RecordatorioScheduler", true);
        
        // Ejecutar inmediatamente y luego cada INTERVALO_REVISION
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                revisarTareasProximasAVencer();
            }
        }, 0, INTERVALO_REVISION);
        
        System.out.println("✅ Scheduler de recordatorios iniciado. Revisión cada 6 horas.");
    }
    
    /**
     * Detiene el scheduler.
     */
    public void detener() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("🛑 Scheduler de recordatorios detenido.");
        }
    }
    
    /**
     * Revisa todas las tareas y envía recordatorios para las próximas a vencer.
     */
    private void revisarTareasProximasAVencer() {
        try {
            List<Tarea> todasLasTareas = tareaDAO.listarTodas();
            Date ahora = new Date();
            Date limiteVencimiento = new Date(ahora.getTime() + UMBRAL_DIAS_VENCIMIENTO);
            
            int recordatoriosEnviados = 0;
            
            for (Tarea tarea : todasLasTareas) {
                // Solo enviar recordatorios para tareas pendientes o en proceso
                if (!tarea.getEstado().equals("COMPLETADA")) {
                    Date fechaVencimiento = tarea.getFechaVencimiento();
                    
                    // Verificar si vence en las próximas 24 horas
                    if (fechaVencimiento != null && 
                        fechaVencimiento.after(ahora) && 
                        fechaVencimiento.before(limiteVencimiento)) {
                        
                        enviarRecordatorio(tarea);
                        recordatoriosEnviados++;
                    }
                }
            }
            
            if (recordatoriosEnviados > 0) {
                System.out.println("📧 " + recordatoriosEnviados + " recordatorios enviados.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al revisar tareas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Envía un recordatorio al responsable de la tarea.
     */
    private void enviarRecordatorio(Tarea tarea) {
        try {
            Usuario responsable = usuarioDAO.buscarPorId(tarea.getIdResponsable());
            
            if (responsable == null || responsable.getEmail() == null) {
                return;
            }
            
            // Calcular horas restantes
            long horasRestantes = TimeUnit.MILLISECONDS.toHours(
                tarea.getFechaVencimiento().getTime() - System.currentTimeMillis()
            );
            
            // Construir mensaje
            String asunto = "⏰ Recordatorio: Tarea próxima a vencer";
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Hola ").append(responsable.getNombre()).append(",\n\n");
            mensaje.append("Te recordamos que tienes una tarea próxima a vencer:\n\n");
            mensaje.append("📋 Tarea: ").append(tarea.getNombre()).append("\n");
            mensaje.append("📝 Descripción: ").append(tarea.getDescripcion()).append("\n");
            mensaje.append("📅 Fecha de vencimiento: ").append(tarea.getFechaVencimiento()).append("\n");
            mensaje.append("⏱️ Tiempo restante: ").append(horasRestantes).append(" horas\n");
            mensaje.append("🔄 Estado actual: ").append(tarea.getEstado()).append("\n\n");
            mensaje.append("Por favor, asegúrate de completar esta tarea a tiempo.\n\n");
            mensaje.append("Saludos,\nSistema de Gestión de Tareas");
            
            // Enviar email
            emailSender.enviarCorreo(
                responsable.getEmail(),
                asunto,
                mensaje.toString()
            );
            
            System.out.println("✉️ Recordatorio enviado a " + responsable.getEmail() + 
                             " para tarea: " + tarea.getNombre());
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar recordatorio: " + e.getMessage());
        }
    }
    
    /**
     * Método para testing manual.
     */
    public static void main(String[] args) {
        RecordatorioScheduler scheduler = new RecordatorioScheduler();
        scheduler.iniciar();
        
        // Mantener el programa corriendo
        System.out.println("Scheduler iniciado. Presiona Ctrl+C para detener.");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            scheduler.detener();
        }
    }
}
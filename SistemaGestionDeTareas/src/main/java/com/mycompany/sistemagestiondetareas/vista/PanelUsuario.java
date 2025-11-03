package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorTarea;
import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel principal para usuarios regulares.
 */
public class PanelUsuario extends JFrame implements ActionListener {
    private final Usuario usuario;
    private final ControladorProyecto controladorProyecto;
    private final ControladorTarea controladorTarea;
    private final ControladorUsuario controladorUsuario;
    
    private JTabbedPane tabbedPane;
    private JTable tablaProyectos;
    private JTable tablaTareas;
    private DefaultTableModel modeloProyectos;
    private DefaultTableModel modeloTareas;
    private JButton btnActualizarEstadoTarea;
    private JButton btnVerDetallesTarea;
    private JButton btnCerrarSesion;
    
    /**
     * Constructor del panel de usuario.
     * @param usuario Usuario regular.
     */
    public PanelUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.controladorProyecto = new ControladorProyecto();
        this.controladorTarea = new ControladorTarea();
        this.controladorUsuario = new ControladorUsuario();
        
        configurarVentana();
        inicializarComponentes();
        cargarDatos();
    }
    
    /**
     * Configura las propiedades básicas de la ventana.
     */
    private void configurarVentana() {
        setTitle("Sistema de Gestión de Tareas - Usuario: " + usuario.getNombre());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }
    
    /**
     * Inicializa y configura los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Panel superior con información del usuario
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(this);
        
        panelSuperior.add(lblBienvenida, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);
        
        // Panel con pestañas
        tabbedPane = new JTabbedPane();
        
        // Pestaña de Proyectos Asignados
        JPanel panelProyectos = crearPanelProyectos();
        tabbedPane.addTab("Mis Proyectos", panelProyectos);
        
        // Pestaña de Tareas Asignadas
        JPanel panelTareas = crearPanelTareas();
        tabbedPane.addTab("Mis Tareas", panelTareas);
        
        // Agregar componentes a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Crea el panel de proyectos asignados.
     * @return Panel configurado.
     */
    private JPanel crearPanelProyectos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla para proyectos
        modeloProyectos = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloProyectos.addColumn("ID");
        modeloProyectos.addColumn("Nombre");
        modeloProyectos.addColumn("Descripción");
        modeloProyectos.addColumn("Fecha Inicio");
        modeloProyectos.addColumn("Fecha Fin");
        modeloProyectos.addColumn("Nivel Riesgo");
        
        tablaProyectos = new JTable(modeloProyectos);
        JScrollPane scrollPane = new JScrollPane(tablaProyectos);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCrearTarea = new JButton("Crear Tarea");
        btnCrearTarea.addActionListener(e -> crearTarea());
        
        panelBotones.add(btnCrearTarea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de tareas asignadas.
     * @return Panel configurado.
     */
    private JPanel crearPanelTareas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla para tareas
        modeloTareas = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa
            }
        };
        
        modeloTareas.addColumn("ID");
        modeloTareas.addColumn("Nombre");
        modeloTareas.addColumn("Descripción");
        modeloTareas.addColumn("Fecha Vencimiento");
        modeloTareas.addColumn("Proyecto");
        modeloTareas.addColumn("Estado");
        
        tablaTareas = new JTable(modeloTareas);
        JScrollPane scrollPane = new JScrollPane(tablaTareas);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnActualizarEstadoTarea = new JButton("Actualizar Estado");
        btnVerDetallesTarea = new JButton("Ver Detalles");
        
        btnActualizarEstadoTarea.addActionListener(this);
        btnVerDetallesTarea.addActionListener(this);
        
        panelBotones.add(btnActualizarEstadoTarea);
        panelBotones.add(btnVerDetallesTarea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Carga los datos en las tablas.
     */
    private void cargarDatos() {
        cargarProyectos();
        cargarTareas();
    }
    
    /**
     * Carga la lista de proyectos asignados en la tabla.
     */
    private void cargarProyectos() {
        // Limpiar tabla
        modeloProyectos.setRowCount(0);
        
        // Obtener proyectos asignados al usuario
        List<Proyecto> proyectos = controladorProyecto.obtenerProyectosPorResponsable(usuario.getId());
        
        // Llenar tabla
        for (Proyecto proyecto : proyectos) {
            Object[] fila = {
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getFechaInicio(),
                proyecto.getFechaFin(),
                proyecto.getNivelRiesgo()
            };
            
            modeloProyectos.addRow(fila);
        }
    }
    
    /**
     * Carga la lista de tareas asignadas en la tabla.
     */
    private void cargarTareas() {
        // Limpiar tabla
        modeloTareas.setRowCount(0);
        
        // Obtener tareas asignadas al usuario
        List<Tarea> tareas = controladorTarea.obtenerTareasPorResponsable(usuario.getId());
        
        // Llenar tabla
        for (Tarea tarea : tareas) {
            Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(tarea.getIdProyecto());
            
            Object[] fila = {
                tarea.getId(),
                tarea.getNombre(),
                tarea.getDescripcion(),
                tarea.getFechaVencimiento(),
                proyecto != null ? proyecto.getNombre() : "N/A",
                tarea.getEstado()
            };
            
            modeloTareas.addRow(fila);
        }
    }
    
    /**
     * Maneja eventos de acción de los componentes.
     * @param e Evento de acción.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCerrarSesion) {
            cerrarSesion();
        } else if (e.getSource() == btnActualizarEstadoTarea) {
            actualizarEstadoTarea();
        } else if (e.getSource() == btnVerDetallesTarea) {
            verDetallesTarea();
        }
    }
    
    /**
     * Actualiza el estado de una tarea seleccionada.
     */
    private void actualizarEstadoTarea() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Por favor, seleccione una tarea para actualizar su estado.", 
                    "Ninguna tarea seleccionada", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idTarea = (int) tablaTareas.getValueAt(filaSeleccionada, 0);
        String estadoActual = (String) tablaTareas.getValueAt(filaSeleccionada, 5);
        
        // Opciones de estado
        String[] opciones = {"PENDIENTE", "EN PROCESO", "COMPLETADA"};
        String nuevoEstado = (String) JOptionPane.showInputDialog(this,
                "Seleccione el nuevo estado:",
                "Actualizar Estado de Tarea",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                estadoActual);
        
        if (nuevoEstado != null && !nuevoEstado.equals(estadoActual)) {
            String comentario = JOptionPane.showInputDialog(this,
                    "Añadir comentario (opcional):",
                    "Comentario de Actualización",
                    JOptionPane.PLAIN_MESSAGE);
            
            // Actualizar estado en el controlador
            if (controladorTarea.actualizarEstadoTarea(idTarea, nuevoEstado, comentario)) {
                JOptionPane.showMessageDialog(this,
                        "Estado actualizado correctamente.",
                        "Actualización Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar tareas
                cargarTareas();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar el estado de la tarea.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Muestra los detalles de una tarea seleccionada.
     */
    private void verDetallesTarea() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Por favor, seleccione una tarea para ver sus detalles.", 
                    "Ninguna tarea seleccionada", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idTarea = (int) tablaTareas.getValueAt(filaSeleccionada, 0);
        Tarea tarea = controladorTarea.obtenerTareaPorId(idTarea);
        
        if (tarea != null) {
            StringBuilder detalles = new StringBuilder();
            detalles.append("ID: ").append(tarea.getId()).append("\n");
            detalles.append("Nombre: ").append(tarea.getNombre()).append("\n");
            detalles.append("Descripción: ").append(tarea.getDescripcion()).append("\n");
            detalles.append("Fecha Creación: ").append(tarea.getFechaCreacion()).append("\n");
            detalles.append("Fecha Vencimiento: ").append(tarea.getFechaVencimiento()).append("\n");
            detalles.append("Estado: ").append(tarea.getEstado()).append("\n");
            detalles.append("Comentarios: ").append(tarea.getComentarios().isEmpty() ? "Ninguno" : tarea.getComentarios());
            
            JOptionPane.showMessageDialog(this,
                    detalles.toString(),
                    "Detalles de la Tarea",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo obtener la información de la tarea.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     */
    private void cerrarSesion() {
        dispose();
        new Login().setVisible(true);
    }
    
    private void crearTarea() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proyecto para crear una tarea.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idProyecto = (int) tablaProyectos.getValueAt(filaSeleccionada, 0);
        Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
        
        if (proyecto != null) {
            DialogoTarea dialogo = new DialogoTarea(this, controladorTarea, controladorProyecto, controladorUsuario, proyecto, false, usuario);
            dialogo.setVisible(true);
            if (dialogo.isTareaGuardada()) {
                cargarTareas();
            }
        }
    }
} 
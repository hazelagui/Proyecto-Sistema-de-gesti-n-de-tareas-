package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorTarea;
import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.controlador.ControladorCosto;
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
 * Panel principal para administradores.
 */
public class PanelAdmin extends JFrame implements ActionListener {
    private final Usuario usuario;
    private final ControladorProyecto controladorProyecto;
    private final ControladorTarea controladorTarea;
    private final ControladorUsuario controladorUsuario;
    private final ControladorCosto controladorCosto;
    
    private JTabbedPane tabbedPane;
    private JTable tablaProyectos;
    private JTable tablaTareas;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloProyectos;
    private DefaultTableModel modeloTareas;
    private DefaultTableModel modeloUsuarios;
    private JButton btnCrearProyecto;
    private JButton btnEditarProyecto;
    private JButton btnEliminarProyecto;
    private JButton btnCrearTarea;
    private JButton btnEditarTarea;
    private JButton btnEliminarTarea;
    private JButton btnCrearUsuario;
    private JButton btnEditarUsuario;
    private JButton btnEliminarUsuario;
    private JButton btnCerrarSesion;
    
    /**
     * Constructor del panel de administrador.
     * @param usuario Usuario administrador.
     */
    public PanelAdmin(Usuario usuario) {
        this.usuario = usuario;
        this.controladorProyecto = new ControladorProyecto();
        this.controladorTarea = new ControladorTarea();
        this.controladorUsuario = new ControladorUsuario();
        this.controladorCosto = new ControladorCosto();
        
        configurarVentana();
        inicializarComponentes();
        cargarDatos();
    }
    
    /**
     * Configura las propiedades básicas de la ventana.
     */
    private void configurarVentana() {
        setTitle("Sistema de Gestión de Tareas - Administrador: " + usuario.getNombre());
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
        
        // Pestaña de Proyectos
        JPanel panelProyectos = crearPanelProyectos();
        tabbedPane.addTab("Proyectos", panelProyectos);
        
        // Pestaña de Tareas
        JPanel panelTareas = crearPanelTareas();
        tabbedPane.addTab("Tareas", panelTareas);
        
        // Pestaña de Costos (solo para admin)
        JPanel panelCostos = new PanelCostos(true, usuario);
        tabbedPane.addTab("Costos", panelCostos);
        
        // Pestaña de Usuarios
        JPanel panelUsuarios = crearPanelUsuarios();
        tabbedPane.addTab("Usuarios", panelUsuarios);
        
        // Agregar componentes a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Crea el panel de proyectos.
     * @return Panel configurado.
     */
    private JPanel crearPanelProyectos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla para proyectos
        modeloProyectos = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa
            }
        };
        
        modeloProyectos.addColumn("ID");
        modeloProyectos.addColumn("Nombre");
        modeloProyectos.addColumn("Descripción");
        modeloProyectos.addColumn("Fecha Inicio");
        modeloProyectos.addColumn("Fecha Fin");
        modeloProyectos.addColumn("Responsable");
        modeloProyectos.addColumn("Nivel Riesgo");
        modeloProyectos.addColumn("Presupuesto Total");
        modeloProyectos.addColumn("Costos Retraso");
        modeloProyectos.addColumn("Costos Adelanto");
        modeloProyectos.addColumn("Gastos Planificados");
        modeloProyectos.addColumn("Balance Total");
        
        tablaProyectos = new JTable(modeloProyectos);
        JScrollPane scrollPane = new JScrollPane(tablaProyectos);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCrearProyecto = new JButton("Crear Proyecto");
        btnEditarProyecto = new JButton("Editar Proyecto");
        btnEliminarProyecto = new JButton("Eliminar Proyecto");
        JButton btnVerCostos = new JButton("Ver Costos");
        
        btnCrearProyecto.addActionListener(this);
        btnEditarProyecto.addActionListener(this);
        btnEliminarProyecto.addActionListener(this);
        btnVerCostos.addActionListener(e -> mostrarCostosProyecto());
        
        panelBotones.add(btnCrearProyecto);
        panelBotones.add(btnEditarProyecto);
        panelBotones.add(btnEliminarProyecto);
        panelBotones.add(btnVerCostos);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de tareas.
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
        modeloTareas.addColumn("Fecha Creación");
        modeloTareas.addColumn("Fecha Vencimiento");
        modeloTareas.addColumn("Proyecto");
        modeloTareas.addColumn("Responsable");
        modeloTareas.addColumn("Estado");
        
        tablaTareas = new JTable(modeloTareas);
        JScrollPane scrollPane = new JScrollPane(tablaTareas);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCrearTarea = new JButton("Crear Tarea");
        btnEditarTarea = new JButton("Editar Tarea");
        btnEliminarTarea = new JButton("Eliminar Tarea");
        
        btnCrearTarea.addActionListener(this);
        btnEditarTarea.addActionListener(this);
        btnEliminarTarea.addActionListener(this);
        
        panelBotones.add(btnCrearTarea);
        panelBotones.add(btnEditarTarea);
        panelBotones.add(btnEliminarTarea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crea el panel de usuarios.
     * @return Panel configurado.
     */
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla para usuarios
        modeloUsuarios = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa
            }
        };
        
        modeloUsuarios.addColumn("ID");
        modeloUsuarios.addColumn("Nombre");
        modeloUsuarios.addColumn("Apellido");
        modeloUsuarios.addColumn("Email");
        modeloUsuarios.addColumn("Rol");
        
        tablaUsuarios = new JTable(modeloUsuarios);
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCrearUsuario = new JButton("Crear Usuario");
        btnEditarUsuario = new JButton("Editar Usuario");
        btnEliminarUsuario = new JButton("Eliminar Usuario");
        
        btnCrearUsuario.addActionListener(this);
        btnEditarUsuario.addActionListener(this);
        btnEliminarUsuario.addActionListener(this);
        
        panelBotones.add(btnCrearUsuario);
        panelBotones.add(btnEditarUsuario);
        panelBotones.add(btnEliminarUsuario);
        
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
        cargarUsuarios();
    }
    
    /**
     * Carga la lista de proyectos en la tabla.
     */
    public void cargarProyectos() {
        // Limpiar tabla
        modeloProyectos.setRowCount(0);
        
        // Obtener proyectos
        List<Proyecto> proyectos = controladorProyecto.obtenerTodosLosProyectos();
        
        // Llenar tabla
        for (Proyecto proyecto : proyectos) {
            Usuario responsable = controladorUsuario.obtenerUsuarioPorId(proyecto.getIdResponsable());
            
            double retrasos = controladorCosto.calcularTotalPorTipo("PROYECTO", proyecto.getId(), "RETRASO");
            double adelantos = controladorCosto.calcularTotalPorTipo("PROYECTO", proyecto.getId(), "ADELANTO");
            double gastos = controladorCosto.calcularTotalPorTipo("PROYECTO", proyecto.getId(), "GASTO_PLANIFICADO");
            double balance = proyecto.getPresupuestoTotal() - gastos + adelantos - retrasos;
            
            Object[] fila = {
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getFechaInicio(),
                proyecto.getFechaFin(),
                responsable != null ? responsable.getNombre() + " " + responsable.getApellido() : "N/A",
                proyecto.getNivelRiesgo(),
                String.format("$%.2f", proyecto.getPresupuestoTotal()),
                String.format("$%.2f", retrasos),
                String.format("$%.2f", adelantos),
                String.format("$%.2f", gastos),
                String.format("$%.2f", balance)
            };
            
            modeloProyectos.addRow(fila);
        }
    }
    
    /**
     * Carga la lista de tareas en la tabla.
     */
    private void cargarTareas() {
        // Limpiar tabla
        modeloTareas.setRowCount(0);
        
        // Obtener tareas
        List<Tarea> tareas = controladorTarea.obtenerTodasLasTareas();
        
        // Llenar tabla
        for (Tarea tarea : tareas) {
            Usuario responsable = controladorUsuario.obtenerUsuarioPorId(tarea.getIdResponsable());
            Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(tarea.getIdProyecto());
            
            Object[] fila = {
                tarea.getId(),
                tarea.getNombre(),
                tarea.getDescripcion(),
                tarea.getFechaCreacion(),
                tarea.getFechaVencimiento(),
                proyecto != null ? proyecto.getNombre() : "N/A",
                responsable != null ? responsable.getNombre() + " " + responsable.getApellido() : "N/A",
                tarea.getEstado()
            };
            
            modeloTareas.addRow(fila);
        }
    }
    
    /**
     * Carga la lista de usuarios en la tabla.
     */
    private void cargarUsuarios() {
        // Limpiar tabla
        modeloUsuarios.setRowCount(0);
        
        // Obtener usuarios
        List<Usuario> usuarios = controladorUsuario.obtenerTodosLosUsuarios();
        
        // Llenar tabla
        for (Usuario u : usuarios) {
            Object[] fila = {
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.isEsAdmin() ? "Administrador" : "Usuario"
            };
            
            modeloUsuarios.addRow(fila);
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
        } else if (e.getSource() == btnCrearProyecto) {
            DialogoProyecto dialogo = new DialogoProyecto(this, controladorProyecto, controladorUsuario);
            dialogo.setVisible(true);
            if (dialogo.isProyectoGuardado()) {
                cargarProyectos();
            }
        } else if (e.getSource() == btnEditarProyecto) {
            editarProyectoSeleccionado();
        } else if (e.getSource() == btnEliminarProyecto) {
            eliminarProyectoSeleccionado();
        } else if (e.getSource() == btnCrearTarea) {
            int filaSeleccionada = tablaProyectos.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un proyecto para crear una tarea.", 
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int idProyecto = (int) tablaProyectos.getValueAt(filaSeleccionada, 0);
            Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
            
            if (proyecto != null) {
                DialogoTarea dialogo = new DialogoTarea(this, controladorTarea, controladorProyecto, controladorUsuario, proyecto, true, usuario);
                dialogo.setVisible(true);
                if (dialogo.isTareaGuardada()) {
                    cargarTareas();
                }
            }
        } else if (e.getSource() == btnEditarTarea) {
            editarTareaSeleccionada();
        } else if (e.getSource() == btnEliminarTarea) {
            eliminarTareaSeleccionada();
        } else if (e.getSource() == btnCrearUsuario) {
            DialogoUsuario dialogo = new DialogoUsuario(this, controladorUsuario);
            dialogo.setVisible(true);
            if (dialogo.isUsuarioGuardado()) {
                cargarUsuarios();
            }
        } else if (e.getSource() == btnEditarUsuario) {
            editarUsuarioSeleccionado();
        } else if (e.getSource() == btnEliminarUsuario) {
            eliminarUsuarioSeleccionado();
        }
    }
    
    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     */
    private void cerrarSesion() {
        dispose();
        new Login().setVisible(true);
    }
    
    /**
     * Edita el proyecto seleccionado en la tabla.
     */
    private void editarProyectoSeleccionado() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proyecto para editar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idProyecto = (int) tablaProyectos.getValueAt(filaSeleccionada, 0);
        Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
        
        if (proyecto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo encontrar el proyecto seleccionado.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DialogoProyecto dialogo = new DialogoProyecto(this, controladorProyecto, controladorUsuario, proyecto);
        dialogo.setVisible(true);
        
        if (dialogo.isProyectoGuardado()) {
            cargarProyectos();
        }
    }
    
    /**
     * Elimina el proyecto seleccionado en la tabla.
     */
    private void eliminarProyectoSeleccionado() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proyecto para eliminar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idProyecto = (int) tablaProyectos.getValueAt(filaSeleccionada, 0);
        String nombreProyecto = (String) tablaProyectos.getValueAt(filaSeleccionada, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar el proyecto '" + nombreProyecto + "'?\n" +
                "Esta acción eliminará también todas las tareas asociadas.", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controladorProyecto.eliminarProyecto(idProyecto);
            
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Proyecto eliminado exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarProyectos();
                cargarTareas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el proyecto.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Edita la tarea seleccionada en la tabla.
     */
    private void editarTareaSeleccionada() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tarea para editar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idTarea = (int) tablaTareas.getValueAt(filaSeleccionada, 0);
        Tarea tarea = controladorTarea.obtenerTareaPorId(idTarea);
        
        if (tarea == null) {
            JOptionPane.showMessageDialog(this, "No se pudo encontrar la tarea seleccionada.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DialogoTarea dialogo = new DialogoTarea(this, controladorTarea, controladorProyecto, controladorUsuario, tarea, true, usuario);
        dialogo.setVisible(true);
        
        if (dialogo.isTareaGuardada()) {
            cargarTareas();
        }
    }
    
    /**
     * Elimina la tarea seleccionada en la tabla.
     */
    private void eliminarTareaSeleccionada() {
        int filaSeleccionada = tablaTareas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tarea para eliminar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idTarea = (int) tablaTareas.getValueAt(filaSeleccionada, 0);
        String nombreTarea = (String) tablaTareas.getValueAt(filaSeleccionada, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar la tarea '" + nombreTarea + "'?", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controladorTarea.eliminarTarea(idTarea);
            
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Tarea eliminada exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTareas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar la tarea.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Edita el usuario seleccionado en la tabla.
     */
    private void editarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para editar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        
        // Evitar editar el usuario actual
        if (idUsuario == usuario.getId()) {
            JOptionPane.showMessageDialog(this, "No puede editar su propio usuario desde esta pantalla.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Usuario usuarioSeleccionado = controladorUsuario.obtenerUsuarioPorId(idUsuario);
        
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "No se pudo encontrar el usuario seleccionado.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DialogoUsuario dialogo = new DialogoUsuario(this, controladorUsuario, usuarioSeleccionado);
        dialogo.setVisible(true);
        
        if (dialogo.isUsuarioGuardado()) {
            cargarUsuarios();
        }
    }
    
    /**
     * Elimina el usuario seleccionado en la tabla.
     */
    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para eliminar.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = tablaUsuarios.getValueAt(filaSeleccionada, 1) + " " + 
                             tablaUsuarios.getValueAt(filaSeleccionada, 2);
        
        // Evitar eliminar el usuario actual
        if (idUsuario == usuario.getId()) {
            JOptionPane.showMessageDialog(this, "No puede eliminar su propio usuario.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar el usuario '" + nombreUsuario + "'?\n" +
                "Esta acción no se puede deshacer y eliminará todo el contenido asociado.", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = controladorUsuario.eliminarUsuario(idUsuario);
            
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
                cargarProyectos();
                cargarTareas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el usuario.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void mostrarCostosProyecto() {
        int filaSeleccionada = tablaProyectos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un proyecto para ver sus costos.", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idProyecto = (int) tablaProyectos.getValueAt(filaSeleccionada, 0);
        String nombreProyecto = (String) tablaProyectos.getValueAt(filaSeleccionada, 1);
        Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
        
        // Mostrar diálogo con los costos del proyecto
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Costos del proyecto: ").append(nombreProyecto).append("\n\n");
        
        double retrasos = controladorCosto.calcularTotalPorTipo("PROYECTO", idProyecto, "RETRASO");
        double adelantos = controladorCosto.calcularTotalPorTipo("PROYECTO", idProyecto, "ADELANTO");
        double gastos = controladorCosto.calcularTotalPorTipo("PROYECTO", idProyecto, "GASTO_PLANIFICADO");
        double balance = proyecto.getPresupuestoTotal() - gastos + adelantos - retrasos;
        
        mensaje.append("Presupuesto Total: $").append(String.format("%.2f", proyecto.getPresupuestoTotal())).append("\n");
        mensaje.append("Costos por retrasos: $").append(String.format("%.2f", retrasos)).append("\n");
        mensaje.append("Costos por adelantos: $").append(String.format("%.2f", adelantos)).append("\n");
        mensaje.append("Gastos planificados: $").append(String.format("%.2f", gastos)).append("\n");
        mensaje.append("Balance total: $").append(String.format("%.2f", balance)).append("\n");
        
        JOptionPane.showMessageDialog(this, mensaje.toString(), 
                "Costos del Proyecto", JOptionPane.INFORMATION_MESSAGE);
    }
} 
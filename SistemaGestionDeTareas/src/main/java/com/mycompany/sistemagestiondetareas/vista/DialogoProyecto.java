package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Diálogo para crear o editar proyectos.
 */
public class DialogoProyecto extends JDialog implements ActionListener {
    // Controladores
    private final ControladorProyecto controladorProyecto;
    private final ControladorUsuario controladorUsuario;
    
    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTextField txtPresupuesto;
    private JComboBox<String> comboResponsable;
    private JComboBox<String> comboNivelRiesgo;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Datos
    private List<Usuario> usuarios;
    private Proyecto proyectoEditar;
    private boolean proyectoGuardado = false;
    
    /**
     * Constructor para crear un nuevo proyecto.
     * @param parent Ventana padre.
     * @param controladorProyecto Controlador de proyectos.
     * @param controladorUsuario Controlador de usuarios.
     */
    public DialogoProyecto(JFrame parent, ControladorProyecto controladorProyecto, 
            ControladorUsuario controladorUsuario) {
        super(parent, "Crear Proyecto", true);
        this.controladorProyecto = controladorProyecto;
        this.controladorUsuario = controladorUsuario;
        this.proyectoEditar = null;
        
        inicializar();
    }
    
    /**
     * Constructor para editar un proyecto existente.
     * @param parent Ventana padre.
     * @param controladorProyecto Controlador de proyectos.
     * @param controladorUsuario Controlador de usuarios.
     * @param proyecto Proyecto a editar.
     */
    public DialogoProyecto(JFrame parent, ControladorProyecto controladorProyecto, 
            ControladorUsuario controladorUsuario, Proyecto proyecto) {
        super(parent, "Editar Proyecto", true);
        this.controladorProyecto = controladorProyecto;
        this.controladorUsuario = controladorUsuario;
        this.proyectoEditar = proyecto;
        
        inicializar();
        cargarDatosProyecto();
    }
    
    /**
     * Inicializa los componentes del diálogo.
     */
    private void inicializar() {
        // Cargar usuarios
        cargarUsuarios();
        
        // Panel principal con layout de grid
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Etiquetas y campos
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNombre = new JTextField(20);
        panelFormulario.add(txtNombre, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Descripción:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtDescripcion = new JTextField(20);
        panelFormulario.add(txtDescripcion, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Fecha Inicio (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtFechaInicio = new JTextField(10);
        // Establecer la fecha actual como valor por defecto
        txtFechaInicio.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        panelFormulario.add(txtFechaInicio, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Fecha Fin (YYYY-MM-DD, opcional):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtFechaFin = new JTextField(10);
        panelFormulario.add(txtFechaFin, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Presupuesto Total:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPresupuesto = new JTextField(20);
        panelFormulario.add(txtPresupuesto, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Nivel de Riesgo:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String[] nivelesRiesgo = {"VERDE", "AMARILLO", "ROJO"};
        comboNivelRiesgo = new JComboBox<>(nivelesRiesgo);
        panelFormulario.add(comboNivelRiesgo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Responsable:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboResponsable = new JComboBox<>();
        if (usuarios != null && !usuarios.isEmpty()) {
            String[] nombresUsuarios = new String[usuarios.size()];
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i);
                nombresUsuarios[i] = u.getNombre() + " " + u.getApellido() + " (" + u.getId() + ")";
            }
            comboResponsable.setModel(new DefaultComboBoxModel<>(nombresUsuarios));
        }
        panelFormulario.add(comboResponsable, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(this);
        btnCancelar.addActionListener(this);
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        // Agregar paneles al diálogo
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelFormulario, BorderLayout.CENTER);
        getContentPane().add(panelBotones, BorderLayout.SOUTH);
        
        // Configuración final del diálogo
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    /**
     * Carga la lista de usuarios disponibles.
     */
    private void cargarUsuarios() {
        usuarios = controladorUsuario.obtenerTodosLosUsuarios();
        if (usuarios == null || usuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No hay usuarios disponibles. Debe crear al menos un usuario primero.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    /**
     * Carga los datos del proyecto a editar en el formulario.
     */
    private void cargarDatosProyecto() {
        if (proyectoEditar != null) {
            txtNombre.setText(proyectoEditar.getNombre());
            txtDescripcion.setText(proyectoEditar.getDescripcion());
            txtPresupuesto.setText(String.format("%.2f", proyectoEditar.getPresupuestoTotal()));
            
            // Formatear fechas
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            txtFechaInicio.setText(sdf.format(proyectoEditar.getFechaInicio()));
            
            if (proyectoEditar.getFechaFin() != null) {
                txtFechaFin.setText(sdf.format(proyectoEditar.getFechaFin()));
            }
            
            // Seleccionar nivel de riesgo
            for (int i = 0; i < comboNivelRiesgo.getItemCount(); i++) {
                if (comboNivelRiesgo.getItemAt(i).equals(proyectoEditar.getNivelRiesgo())) {
                    comboNivelRiesgo.setSelectedIndex(i);
                    break;
                }
            }
            
            // Seleccionar responsable
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getId() == proyectoEditar.getIdResponsable()) {
                    comboResponsable.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Guarda el proyecto (nuevo o editado).
     */
    private void guardarProyecto() {
        // Validar campos
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String descripcion = txtDescripcion.getText().trim();
        
        // Validar y parsear fechas
        String fechaInicioStr = txtFechaInicio.getText().trim();
        if (fechaInicioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La fecha de inicio es obligatoria.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String fechaFinStr = txtFechaFin.getText().trim();
        
        try {
            double presupuesto = Double.parseDouble(txtPresupuesto.getText().trim());
            if (presupuesto < 0) {
                JOptionPane.showMessageDialog(this, "El presupuesto no puede ser negativo.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Obtener el nivel de riesgo y el ID del responsable
            String nivelRiesgo = (String) comboNivelRiesgo.getSelectedItem();
            String responsableStr = (String) comboResponsable.getSelectedItem();
            int idResponsable = Integer.parseInt(responsableStr.substring(
                    responsableStr.lastIndexOf("(") + 1, responsableStr.lastIndexOf(")")));
            
            // Convertir fechas
            java.sql.Timestamp fechaInicio = java.sql.Timestamp.valueOf(fechaInicioStr + " 00:00:00");
            java.sql.Timestamp fechaFin = null;
            if (!fechaFinStr.isEmpty()) {
                fechaFin = java.sql.Timestamp.valueOf(fechaFinStr + " 00:00:00");
            }
            
            boolean exito;
            if (proyectoEditar == null) {
                // Crear nuevo proyecto
                Proyecto nuevoProyecto = controladorProyecto.crearProyecto(
                        nombre, descripcion, fechaInicio, fechaFin, idResponsable, nivelRiesgo, presupuesto);
                exito = (nuevoProyecto != null && nuevoProyecto.getId() > 0);
            } else {
                // Actualizar proyecto existente
                proyectoEditar.setNombre(nombre);
                proyectoEditar.setDescripcion(descripcion);
                proyectoEditar.setFechaInicio(fechaInicio);
                proyectoEditar.setFechaFin(fechaFin);
                proyectoEditar.setIdResponsable(idResponsable);
                proyectoEditar.setNivelRiesgo(nivelRiesgo);
                proyectoEditar.setPresupuestoTotal(presupuesto);
                exito = controladorProyecto.actualizarProyecto(proyectoEditar);
            }
            
            if (exito) {
                proyectoGuardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Error al guardar el proyecto. Por favor, inténtelo de nuevo.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error al procesar los datos: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Verifica si el proyecto fue guardado exitosamente.
     * @return true si el proyecto fue guardado, false en caso contrario.
     */
    public boolean isProyectoGuardado() {
        return proyectoGuardado;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGuardar) {
            guardarProyecto();
        } else if (e.getSource() == btnCancelar) {
            dispose();
        }
    }
} 
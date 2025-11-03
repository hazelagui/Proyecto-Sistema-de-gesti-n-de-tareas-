package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorTarea;
import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Diálogo para crear o editar tareas.
 */
public class DialogoTarea extends JDialog implements ActionListener {
    // Controladores
    private final ControladorTarea controladorTarea;
    private final ControladorProyecto controladorProyecto;
    private final ControladorUsuario controladorUsuario;
    private final boolean esAdmin;
    private final Usuario usuario;
    
    // Componentes del formulario
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTextField txtFechaVencimiento;
    private JComboBox<String> comboProyecto;
    private JComboBox<String> comboResponsable;
    private JComboBox<String> comboEstado;
    private JTextArea txtComentarios;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Datos
    private List<Proyecto> proyectos;
    private List<Usuario> usuarios;
    private Tarea tareaEditar;
    private boolean tareaGuardada = false;
    
    /**
     * Constructor para crear una nueva tarea.
     * @param parent Ventana padre.
     * @param controladorTarea Controlador de tareas.
     * @param controladorProyecto Controlador de proyectos.
     * @param controladorUsuario Controlador de usuarios.
     * @param proyecto Proyecto asociado a la tarea.
     * @param esAdmin Indica si el usuario es administrador.
     * @param usuario Usuario asociado a la tarea.
     */
    public DialogoTarea(JFrame parent, ControladorTarea controladorTarea, 
            ControladorProyecto controladorProyecto, ControladorUsuario controladorUsuario, 
            Proyecto proyecto, boolean esAdmin, Usuario usuario) {
        super(parent, "Crear Tarea", true);
        this.controladorTarea = controladorTarea;
        this.controladorProyecto = controladorProyecto;
        this.controladorUsuario = controladorUsuario;
        this.tareaEditar = null;
        this.esAdmin = esAdmin;
        this.usuario = usuario;
        
        inicializar();
    }
    
    /**
     * Constructor para editar una tarea existente.
     * @param parent Ventana padre.
     * @param controladorTarea Controlador de tareas.
     * @param controladorProyecto Controlador de proyectos.
     * @param controladorUsuario Controlador de usuarios.
     * @param tarea Tarea a editar.
     * @param esAdmin Indica si el usuario es administrador.
     * @param usuario Usuario asociado a la tarea.
     */
    public DialogoTarea(JFrame parent, ControladorTarea controladorTarea, 
            ControladorProyecto controladorProyecto, ControladorUsuario controladorUsuario, 
            Tarea tarea, boolean esAdmin, Usuario usuario) {
        super(parent, "Editar Tarea", true);
        this.controladorTarea = controladorTarea;
        this.controladorProyecto = controladorProyecto;
        this.controladorUsuario = controladorUsuario;
        this.tareaEditar = tarea;
        this.esAdmin = esAdmin;
        this.usuario = usuario;
        
        inicializar();
        cargarDatosTarea();
    }
    
    /**
     * Inicializa los componentes del diálogo.
     */
    private void inicializar() {
        // Cargar proyectos y usuarios
        cargarProyectos();
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
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        panelFormulario.add(scrollDesc, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Fecha Vencimiento (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtFechaVencimiento = new JTextField(10);
        // Establecer una fecha futura como valor por defecto
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaVencimiento = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000); // 7 días
        txtFechaVencimiento.setText(sdf.format(fechaVencimiento));
        panelFormulario.add(txtFechaVencimiento, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Proyecto:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboProyecto = new JComboBox<>();
        if (proyectos != null && !proyectos.isEmpty()) {
            String[] nombresProyectos = new String[proyectos.size()];
            for (int i = 0; i < proyectos.size(); i++) {
                Proyecto p = proyectos.get(i);
                nombresProyectos[i] = p.getNombre() + " (" + p.getId() + ")";
            }
            comboProyecto.setModel(new DefaultComboBoxModel<>(nombresProyectos));
        }
        panelFormulario.add(comboProyecto, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
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
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Estado:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String[] estados = {"PENDIENTE", "EN PROCESO", "COMPLETADA"};
        comboEstado = new JComboBox<>(estados);
        panelFormulario.add(comboEstado, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Comentarios:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtComentarios = new JTextArea(3, 20);
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        JScrollPane scrollComm = new JScrollPane(txtComentarios);
        panelFormulario.add(scrollComm, gbc);
        
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
     * Carga la lista de proyectos disponibles.
     */
    private void cargarProyectos() {
        if (esAdmin) {
            proyectos = controladorProyecto.obtenerTodosLosProyectos();
        } else {
            proyectos = controladorProyecto.obtenerProyectosPorResponsable(usuario.getId());
        }
        
        if (proyectos == null || proyectos.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No hay proyectos disponibles.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
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
     * Carga los datos de la tarea a editar en el formulario.
     */
    private void cargarDatosTarea() {
        if (tareaEditar != null) {
            txtNombre.setText(tareaEditar.getNombre());
            txtDescripcion.setText(tareaEditar.getDescripcion());
            
            // Formatear fecha de vencimiento
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            txtFechaVencimiento.setText(sdf.format(tareaEditar.getFechaVencimiento()));
            
            // Seleccionar proyecto
            for (int i = 0; i < proyectos.size(); i++) {
                if (proyectos.get(i).getId() == tareaEditar.getIdProyecto()) {
                    comboProyecto.setSelectedIndex(i);
                    break;
                }
            }
            
            // Seleccionar responsable
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getId() == tareaEditar.getIdResponsable()) {
                    comboResponsable.setSelectedIndex(i);
                    break;
                }
            }
            
            // Seleccionar estado
            String estado = tareaEditar.getEstado();
            for (int i = 0; i < comboEstado.getItemCount(); i++) {
                if (comboEstado.getItemAt(i).equals(estado)) {
                    comboEstado.setSelectedIndex(i);
                    break;
                }
            }
            
            txtComentarios.setText(tareaEditar.getComentarios());
        }
    }
    
    /**
     * Guarda la tarea (nueva o editada).
     */
    private void guardarTarea() {
        // Validar campos
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String descripcion = txtDescripcion.getText().trim();
        String comentarios = txtComentarios.getText().trim();
        
        // Validar y parsear fechas
        String fechaVencimientoStr = txtFechaVencimiento.getText().trim();
        if (fechaVencimientoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La fecha de vencimiento es obligatoria.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Obtener el ID del proyecto, responsable y estado
            String proyectoStr = (String) comboProyecto.getSelectedItem();
            int idProyecto = Integer.parseInt(proyectoStr.substring(
                    proyectoStr.lastIndexOf("(") + 1, proyectoStr.lastIndexOf(")")));
            
            String responsableStr = (String) comboResponsable.getSelectedItem();
            int idResponsable = Integer.parseInt(responsableStr.substring(
                    responsableStr.lastIndexOf("(") + 1, responsableStr.lastIndexOf(")")));
            
            String estado = (String) comboEstado.getSelectedItem();
            
            // Convertir fechas
            java.sql.Timestamp fechaVencimiento = java.sql.Timestamp.valueOf(fechaVencimientoStr + " 00:00:00");
            
            boolean exito;
            if (tareaEditar == null) {
                // Crear nueva tarea
                Date fechaCreacion = new Date(); // Fecha actual
                Tarea nuevaTarea = controladorTarea.crearTarea(
                        nombre, descripcion, fechaCreacion, fechaVencimiento, 
                        idProyecto, idResponsable, estado, comentarios);
                exito = (nuevaTarea != null && nuevaTarea.getId() > 0);
            } else {
                // Actualizar tarea existente
                exito = controladorTarea.actualizarTarea(
                        tareaEditar.getId(), nombre, descripcion, fechaVencimiento, 
                        idProyecto, idResponsable, estado, comentarios);
            }
            
            if (exito) {
                tareaGuardada = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Error al guardar la tarea. Por favor, inténtelo de nuevo.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error al procesar los datos: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Verifica si la tarea fue guardada exitosamente.
     * @return true si la tarea fue guardada, false en caso contrario.
     */
    public boolean isTareaGuardada() {
        return tareaGuardada;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGuardar) {
            guardarTarea();
        } else if (e.getSource() == btnCancelar) {
            dispose();
        }
    }
} 
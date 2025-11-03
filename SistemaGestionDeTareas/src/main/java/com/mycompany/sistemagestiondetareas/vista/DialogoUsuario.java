package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Diálogo para crear o editar usuarios.
 */
public class DialogoUsuario extends JDialog implements ActionListener {
    // Controladores
    private final ControladorUsuario controladorUsuario;
    
    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JRadioButton radioUsuario;
    private JRadioButton radioAdmin;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Datos
    private Usuario usuarioEditar;
    private boolean usuarioGuardado = false;
    
    /**
     * Constructor para crear un nuevo usuario.
     * @param parent Ventana padre.
     * @param controladorUsuario Controlador de usuarios.
     */
    public DialogoUsuario(JFrame parent, ControladorUsuario controladorUsuario) {
        super(parent, "Crear Usuario", true);
        this.controladorUsuario = controladorUsuario;
        this.usuarioEditar = null;
        
        inicializar();
    }
    
    /**
     * Constructor para editar un usuario existente.
     * @param parent Ventana padre.
     * @param controladorUsuario Controlador de usuarios.
     * @param usuario Usuario a editar.
     */
    public DialogoUsuario(JFrame parent, ControladorUsuario controladorUsuario, Usuario usuario) {
        super(parent, "Editar Usuario", true);
        this.controladorUsuario = controladorUsuario;
        this.usuarioEditar = usuario;
        
        inicializar();
        cargarDatosUsuario();
    }
    
    /**
     * Inicializa los componentes del diálogo.
     */
    private void inicializar() {
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
        panelFormulario.add(new JLabel("Apellido:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtApellido = new JTextField(20);
        panelFormulario.add(txtApellido, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        panelFormulario.add(txtEmail, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel lblPassword = new JLabel("Contraseña:");
        if (usuarioEditar != null) {
            lblPassword.setText("Nueva Contraseña (dejar en blanco para no cambiar):");
        }
        panelFormulario.add(lblPassword, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        panelFormulario.add(txtPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Tipo de Usuario:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel panelRadio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioUsuario = new JRadioButton("Usuario Regular");
        radioAdmin = new JRadioButton("Administrador");
        
        ButtonGroup groupTipo = new ButtonGroup();
        groupTipo.add(radioUsuario);
        groupTipo.add(radioAdmin);
        
        radioUsuario.setSelected(true); // Por defecto
        
        panelRadio.add(radioUsuario);
        panelRadio.add(radioAdmin);
        panelFormulario.add(panelRadio, gbc);
        
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
     * Carga los datos del usuario a editar en el formulario.
     */
    private void cargarDatosUsuario() {
        if (usuarioEditar != null) {
            txtNombre.setText(usuarioEditar.getNombre());
            txtApellido.setText(usuarioEditar.getApellido());
            txtEmail.setText(usuarioEditar.getEmail());
            // No cargar contraseña
            
            // Seleccionar tipo de usuario
            if (usuarioEditar.isEsAdmin()) {
                radioAdmin.setSelected(true);
            } else {
                radioUsuario.setSelected(true);
            }
        }
    }
    
    /**
     * Guarda el usuario (nuevo o editado).
     */
    private void guardarUsuario() {
        // Validar campos
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String apellido = txtApellido.getText().trim();
        if (apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El apellido es obligatorio.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El email es obligatorio.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String password = new String(txtPassword.getPassword());
        if (password.isEmpty() && usuarioEditar == null) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria para un nuevo usuario.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean esAdmin = radioAdmin.isSelected();
        
        try {
            boolean exito;
            if (usuarioEditar == null) {
                // Crear nuevo usuario
                Usuario nuevoUsuario = controladorUsuario.registrarUsuario(
                        nombre, apellido, email, password, esAdmin);
                exito = (nuevoUsuario != null && nuevoUsuario.getId() > 0);
            } else {
                // Actualizar usuario existente
                usuarioEditar.setNombre(nombre);
                usuarioEditar.setApellido(apellido);
                usuarioEditar.setEmail(email);
                usuarioEditar.setEsAdmin(esAdmin);
                
                // Actualizar contraseña solo si se proporcionó una nueva
                if (!password.isEmpty()) {
                    usuarioEditar.setPassword(password);
                }
                
                exito = controladorUsuario.actualizarUsuario(usuarioEditar);
            }
            
            if (exito) {
                usuarioGuardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Error al guardar el usuario. Por favor, inténtelo de nuevo. " +
                        "Es posible que el email ya esté registrado.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error al procesar los datos: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Verifica si el usuario fue guardado exitosamente.
     * @return true si el usuario fue guardado, false en caso contrario.
     */
    public boolean isUsuarioGuardado() {
        return usuarioGuardado;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGuardar) {
            guardarUsuario();
        } else if (e.getSource() == btnCancelar) {
            dispose();
        }
    }
} 
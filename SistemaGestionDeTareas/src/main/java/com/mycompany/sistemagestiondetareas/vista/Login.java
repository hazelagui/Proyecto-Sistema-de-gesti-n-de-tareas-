package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorUsuario;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import com.mycompany.sistemagestiondetareas.util.Cliente;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana de inicio de sesión con diseño moderno y mejorado.
 */
public class Login extends JFrame implements ActionListener {
    private final ControladorUsuario controladorUsuario;
    private Cliente cliente;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    
    /**
     * Constructor de la ventana de inicio de sesión.
     */
    public Login() {
        this.controladorUsuario = new ControladorUsuario();
        configurarVentana();
        inicializarComponentes();
    }
    
    /**
     * Configura las propiedades básicas de la ventana.
     */
    private void configurarVentana() {
        setTitle("Sistema de Gestión de Tareas");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }
    
    /**
     * Inicializa y configura los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Panel principal con fondo
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 120, 212));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Panel superior
        JPanel panelSuperior = new JPanel();
        panelSuperior.setOpaque(false);
        JLabel lblTitulo = new JLabel("Bienvenido");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);
        
        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridLayout(4, 1, 10, 10));
        panelFormulario.setOpaque(false);
        
        // Campos de texto
        Font fontCampos = new Font("Segoe UI", Font.PLAIN, 14);
        Font fontEtiquetas = new Font("Segoe UI", Font.BOLD, 14);
        
        // Campo de email
        JLabel lblEmail = new JLabel("Correo electrónico");
        lblEmail.setFont(fontEtiquetas);
        lblEmail.setForeground(Color.WHITE);
        panelFormulario.add(lblEmail);
        
        txtEmail = crearCampoTexto("ejemplo@correo.com", fontCampos);
        panelFormulario.add(txtEmail);
        
        // Campo de contraseña
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setFont(fontEtiquetas);
        lblPassword.setForeground(Color.WHITE);
        panelFormulario.add(lblPassword);
        
        txtPassword = crearCampoContrasena("", fontCampos);
        panelFormulario.add(txtPassword);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton btnLogin = crearBoton("Iniciar Sesión", new Color(0, 120, 212), Color.WHITE);
        JButton btnSalir = crearBoton("Salir", new Color(240, 240, 240), new Color(80, 80, 80));
        
        panelBotones.add(btnLogin);
        panelBotones.add(btnSalir);
        
        // Agregar componentes
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    private JTextField crearCampoTexto(String placeholder, Font font) {
        JTextField campo = new JTextField(20);
        campo.setFont(font);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            new EmptyBorder(10, 10, 10, 10)
        ));
        campo.setBackground(new Color(255, 255, 255, 200));
        campo.setForeground(Color.BLACK);
        campo.setPreferredSize(new Dimension(300, 40));
        campo.setText(placeholder);
        campo.setForeground(new Color(150, 150, 150));
        
        campo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        return campo;
    }
    
    private JPasswordField crearCampoContrasena(String placeholder, Font font) {
        JPasswordField campo = new JPasswordField(20);
        campo.setFont(font);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            new EmptyBorder(10, 10, 10, 10)
        ));
        campo.setBackground(new Color(255, 255, 255, 200));
        campo.setForeground(Color.BLACK);
        campo.setPreferredSize(new Dimension(300, 40));
        campo.setEchoChar('•');
        
        return campo;
    }
    
    private JButton crearBoton(String texto, Color fondo, Color textoColor) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(fondo);
        boton.setForeground(textoColor);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(180, 45));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(this);
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(fondo.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(fondo);
            }
        });
        
        return boton;
    }
    
    /**
     * Maneja eventos de acción de los componentes.
     * @param e Evento de acción.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        if (comando.equals("Iniciar Sesión")) {
            iniciarSesion();
        } else if (comando.equals("Salir")) {
            System.exit(0);
        }
    }
    
    /**
     * Inicia sesión con las credenciales proporcionadas.
     */
    private void iniciarSesion() {
        String email = txtEmail.getText();
        String password = new String(txtPassword.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, complete todos los campos.", 
                "Campos incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            cliente = new Cliente(email, password);
            Usuario usuario = controladorUsuario.autenticarUsuario(email, password);
            
            if (usuario != null) {
                if (usuario.isEsAdmin()) {
                    new PanelAdmin(usuario).setVisible(true);
                } else {
                    new PanelUsuario(usuario).setVisible(true);
                }
                dispose();
            } else {
                cliente.cerrarConexion();
                JOptionPane.showMessageDialog(this, 
                    "Credenciales inválidas.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al conectar con el servidor.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 
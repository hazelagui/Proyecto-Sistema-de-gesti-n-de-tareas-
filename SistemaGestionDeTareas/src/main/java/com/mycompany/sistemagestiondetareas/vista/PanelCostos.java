package com.mycompany.sistemagestiondetareas.vista;

import com.mycompany.sistemagestiondetareas.controlador.ControladorCosto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.modelo.Costo;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;

public class PanelCostos extends JPanel {
    private final ControladorCosto controladorCosto;
    private final ControladorProyecto controladorProyecto;
    private final JTable tablaCostos;
    private final DefaultTableModel modeloTabla;
    private final JComboBox<String> comboProyecto;
    private final JTextField txtMonto;
    private final JTextField txtDescripcion;
    private final JTextField txtPresupuestoTotal;
    private final JLabel lblSaldoDisponible;
    private final JComboBox<String> comboTipoCosto;
    private final boolean esAdmin;
    private final Usuario usuario;

    public PanelCostos(boolean esAdmin, Usuario usuario) {
        this.esAdmin = esAdmin;
        this.usuario = usuario;
        this.controladorCosto = new ControladorCosto();
        this.controladorProyecto = new ControladorProyecto();
        
        // Configurar el modelo de la tabla
        String[] columnas = {"ID", "Descripción", "Monto", "Tipo Costo", "Fecha", "Usuario"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Crear la tabla
        tablaCostos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaCostos);
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel(new GridLayout(1, 2));
        comboProyecto = new JComboBox<>();
        
        panelFiltros.add(new JLabel("Proyecto:"));
        panelFiltros.add(comboProyecto);
        
        // Panel de formulario para nuevos costos
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Nuevo Costo"));
        
        txtDescripcion = new JTextField();
        txtMonto = new JTextField();
        txtPresupuestoTotal = new JTextField();
        lblSaldoDisponible = new JLabel("Saldo disponible: $0.00");
        comboTipoCosto = new JComboBox<>(new String[]{"RETRASO", "ADELANTO", "GASTO_PLANIFICADO"});
        
        panelFormulario.add(new JLabel("Descripción:"));
        panelFormulario.add(txtDescripcion);
        panelFormulario.add(new JLabel("Monto:"));
        panelFormulario.add(txtMonto);
        panelFormulario.add(new JLabel("Presupuesto Total:"));
        panelFormulario.add(txtPresupuestoTotal);
        panelFormulario.add(new JLabel("Tipo de Costo:"));
        panelFormulario.add(comboTipoCosto);
        panelFormulario.add(new JLabel("Saldo Disponible:"));
        panelFormulario.add(lblSaldoDisponible);
        
        // Botones
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Costo");
        JButton btnActualizar = new JButton("Actualizar");
        
        btnAgregar.addActionListener(e -> agregarCosto());
        btnActualizar.addActionListener(e -> actualizarTabla());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        
        // Panel principal con BorderLayout
        setLayout(new BorderLayout(10, 10));
        add(panelFiltros, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel sur que contiene el formulario y los botones
        JPanel panelSur = new JPanel(new BorderLayout(5, 5));
        panelSur.add(panelFormulario, BorderLayout.CENTER);
        panelSur.add(panelBotones, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);
        
        // Cargar datos iniciales
        actualizarComboProyecto();
        actualizarTabla();
    }
    
    private void actualizarComboProyecto() {
        comboProyecto.removeAllItems();
        
        List<Proyecto> proyectos = esAdmin ? 
            controladorProyecto.obtenerTodosLosProyectos() : 
            controladorProyecto.obtenerProyectosPorResponsable(usuario.getId());
        
        for (Proyecto proyecto : proyectos) {
            comboProyecto.addItem(proyecto.getNombre() + " (ID: " + proyecto.getId() + ")");
        }
    }
    
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        String seleccion = (String) comboProyecto.getSelectedItem();
        
        if (seleccion == null || seleccion.isEmpty()) {
            return;
        }
        
        // Extraer el ID del texto seleccionado
        int idProyecto = Integer.parseInt(seleccion.substring(seleccion.lastIndexOf("ID: ") + 4, seleccion.length() - 1));
        
        // Cargar el presupuesto del proyecto
        Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
        if (proyecto != null) {
            txtPresupuestoTotal.setText(String.format("%.2f", proyecto.getPresupuestoTotal()));
        }
        
        List<Costo> costos = controladorCosto.obtenerCostosPorReferencia("PROYECTO", idProyecto);
        
        double totalGastos = 0;
        double totalAdelantos = 0;
        double totalRetrasos = 0;
        
        for (Costo costo : costos) {
            Object[] fila = {
                costo.getId(),
                costo.getDescripcion(),
                String.format("$%.2f", costo.getMonto()),
                costo.getTipoCosto(),
                costo.getFechaRegistro(),
                costo.getIdUsuarioRegistro()
            };
            modeloTabla.addRow(fila);
            
            // Calcular totales
            switch (costo.getTipoCosto()) {
                case "GASTO_PLANIFICADO":
                    totalGastos += costo.getMonto();
                    break;
                case "ADELANTO":
                    totalAdelantos += costo.getMonto();
                    break;
                case "RETRASO":
                    totalRetrasos += costo.getMonto();
                    break;
            }
        }
        
        // Actualizar saldo disponible
        double presupuestoTotal = 0;
        try {
            presupuestoTotal = Double.parseDouble(txtPresupuestoTotal.getText().trim());
        } catch (NumberFormatException e) {
            // Si no hay presupuesto definido, no actualizamos el saldo
            return;
        }
        
        double saldoDisponible = presupuestoTotal - totalGastos + totalAdelantos - totalRetrasos;
        lblSaldoDisponible.setText(String.format("Saldo disponible: $%.2f", saldoDisponible));
    }
    
    private void agregarCosto() {
        try {
            String descripcion = txtDescripcion.getText().trim();
            String montoStr = txtMonto.getText().trim();
            String tipoCosto = (String) comboTipoCosto.getSelectedItem();
            String seleccion = (String) comboProyecto.getSelectedItem();
            
            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La descripción no puede estar vacía", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (montoStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El monto no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (seleccion == null || seleccion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un proyecto", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validar y convertir el monto
            double monto;
            try {
                monto = Double.parseDouble(montoStr.replace(",", "."));
                if (monto <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El monto debe ser un número válido mayor a 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Extraer el ID del texto seleccionado
            int idProyecto = Integer.parseInt(seleccion.substring(seleccion.lastIndexOf("ID: ") + 4, seleccion.length() - 1));
            
            // Actualizar el presupuesto del proyecto
            try {
                double nuevoPresupuesto = Double.parseDouble(txtPresupuestoTotal.getText().trim());
                Proyecto proyecto = controladorProyecto.obtenerProyectoPorId(idProyecto);
                if (proyecto != null) {
                    proyecto.setPresupuestoTotal(nuevoPresupuesto);
                    controladorProyecto.actualizarProyecto(proyecto);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El presupuesto debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verificar saldo disponible para gastos planificados
            if (tipoCosto.equals("GASTO_PLANIFICADO")) {
                double saldoDisponible = Double.parseDouble(lblSaldoDisponible.getText().replace("Saldo disponible: $", "").replace(",", ""));
                if (monto > saldoDisponible) {
                    JOptionPane.showMessageDialog(this, 
                        "No hay suficiente saldo disponible para este gasto", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Registrar el costo
            Costo costo = controladorCosto.registrarCosto(
                "PROYECTO", 
                idProyecto, 
                descripcion, 
                monto, 
                tipoCosto,
                usuario.getId()
            );
            
            if (costo != null) {
                JOptionPane.showMessageDialog(this, "Costo registrado exitosamente");
                actualizarTabla();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el costo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al procesar el costo: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarFormulario() {
        txtDescripcion.setText("");
        txtMonto.setText("");
        txtPresupuestoTotal.setText("");
        comboTipoCosto.setSelectedIndex(0);
    }
} 
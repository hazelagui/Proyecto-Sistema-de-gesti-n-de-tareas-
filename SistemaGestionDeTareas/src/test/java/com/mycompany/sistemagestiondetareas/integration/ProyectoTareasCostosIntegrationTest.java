package com.mycompany.sistemagestiondetareas.integration;

import com.mycompany.sistemagestiondetareas.controlador.ControladorProyecto;
import com.mycompany.sistemagestiondetareas.controlador.ControladorTarea;
import com.mycompany.sistemagestiondetareas.controlador.ControladorCosto;
import com.mycompany.sistemagestiondetareas.dao.ProyectoDAO;
import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.dao.CostoDAO;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.modelo.Costo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de Integración: Flujo Proyecto → Tareas → Costos
 * 
 * Verifica el flujo completo de:
 * 1. Crear un proyecto
 * 2. Asociar tareas al proyecto
 * 3. Registrar costos asociados a proyecto y tareas
 * 4. Verificar consistencia de IDs
 * 5. Validar agregaciones (suma de costos)
 * 
 * IMPORTANTE: Usa base de datos REAL (no mocks)
 */
public class ProyectoTareasCostosIntegrationTest extends IntegrationTestBase {

    private ProyectoDAO proyectoDAO;
    private TareaDAO tareaDAO;
    private CostoDAO costoDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        proyectoDAO = new ProyectoDAO();
        tareaDAO = new TareaDAO();
        costoDAO = new CostoDAO();
    }

    // ========================================
    // FLUJO COMPLETO: PROYECTO → TAREAS → COSTOS
    // ========================================

    @Test
    public void flujoCompleto_crearProyectoTareasYCostos_verificaConsistencia() throws Exception {
        // ============ FASE 1: CREAR PROYECTO ============
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Sistema E-Commerce");
        proyecto.setDescripcion("Desarrollo de tienda online");
        proyecto.setFechaInicio(new Date());
        proyecto.setFechaFin(new Date(System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000)); // +90 días
        proyecto.setIdResponsable(1); // Admin Test
        proyecto.setNivelRiesgo("MEDIO");
        proyecto.setPresupuestoTotal(50000.0);
        
        // Insertar proyecto
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        
        // Verificar que se generó el ID
        assertNotNull(proyectoCreado, "El proyecto debe crearse correctamente");
        assertTrue(proyectoCreado.getId() > 0, "Debe generar un ID válido");
        int idProyecto = proyectoCreado.getId();
        
        System.out.println("✅ Proyecto creado con ID: " + idProyecto);
        
        // ============ FASE 2: CREAR TAREAS ============
        Tarea tarea1 = new Tarea();
        tarea1.setNombre("Diseño de Base de Datos");
        tarea1.setDescripcion("Diseñar schema completo");
        tarea1.setFechaCreacion(new Date());
        tarea1.setFechaVencimiento(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)); // +7 días
        tarea1.setIdProyecto(idProyecto); // Asociar al proyecto
        tarea1.setIdResponsable(2); // Usuario Regular
        tarea1.setEstado("PENDIENTE");
        tarea1.setComentarios("Prioridad alta");
        
        Tarea tarea2 = new Tarea();
        tarea2.setNombre("Implementar API REST");
        tarea2.setDescripcion("Desarrollar endpoints");
        tarea2.setFechaCreacion(new Date());
        tarea2.setFechaVencimiento(new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000)); // +14 días
        tarea2.setIdProyecto(idProyecto); // Asociar al proyecto
        tarea2.setIdResponsable(2);
        tarea2.setEstado("PENDIENTE");
        tarea2.setComentarios("");
        
        // Insertar tareas
        Tarea tarea1Creada = tareaDAO.insertar(tarea1);
        Tarea tarea2Creada = tareaDAO.insertar(tarea2);
        
        assertNotNull(tarea1Creada, "Tarea 1 debe crearse correctamente");
        assertNotNull(tarea2Creada, "Tarea 2 debe crearse correctamente");
        assertTrue(tarea1Creada.getId() > 0, "Tarea 1 debe tener ID válido");
        assertTrue(tarea2Creada.getId() > 0, "Tarea 2 debe tener ID válido");
        
        int idTarea1 = tarea1Creada.getId();
        int idTarea2 = tarea2Creada.getId();
        
        System.out.println("✅ Tareas creadas con IDs: " + idTarea1 + ", " + idTarea2);
        
        // Verificar que las tareas están asociadas al proyecto
        List<Tarea> tareasDelProyecto = tareaDAO.listarPorProyecto(idProyecto);
        assertEquals(2, tareasDelProyecto.size(), "Debe haber 2 tareas asociadas al proyecto");
        
        // ============ FASE 3: REGISTRAR COSTOS ============
        // Costo del proyecto (gasto planificado)
        Costo costoProyecto1 = new Costo();
        costoProyecto1.setTipo("PROYECTO");
        costoProyecto1.setIdReferencia(idProyecto);
        costoProyecto1.setDescripcion("Licencias de software");
        costoProyecto1.setMonto(5000.0);
        costoProyecto1.setTipoCosto("GASTO_PLANIFICADO");
        costoProyecto1.setFechaRegistro(new Date());
        costoProyecto1.setIdUsuarioRegistro(1);
        
        Costo costoProyecto2 = new Costo();
        costoProyecto2.setTipo("PROYECTO");
        costoProyecto2.setIdReferencia(idProyecto);
        costoProyecto2.setDescripcion("Infraestructura cloud");
        costoProyecto2.setMonto(3000.0);
        costoProyecto2.setTipoCosto("GASTO_PLANIFICADO");
        costoProyecto2.setFechaRegistro(new Date());
        costoProyecto2.setIdUsuarioRegistro(1);
        
        // Costo de tarea 1
        Costo costoTarea1 = new Costo();
        costoTarea1.setTipo("TAREA");
        costoTarea1.setIdReferencia(idTarea1);
        costoTarea1.setDescripcion("Horas extras diseño");
        costoTarea1.setMonto(1500.0);
        costoTarea1.setTipoCosto("ADELANTO");
        costoTarea1.setFechaRegistro(new Date());
        costoTarea1.setIdUsuarioRegistro(2);
        
        // Insertar costos
        Costo costoProyecto1Creado = costoDAO.insertar(costoProyecto1);
        Costo costoProyecto2Creado = costoDAO.insertar(costoProyecto2);
        Costo costoTarea1Creado = costoDAO.insertar(costoTarea1);
        
        assertNotNull(costoProyecto1Creado, "Costo proyecto 1 debe crearse");
        assertNotNull(costoProyecto2Creado, "Costo proyecto 2 debe crearse");
        assertNotNull(costoTarea1Creado, "Costo tarea 1 debe crearse");
        
        System.out.println("✅ Costos registrados correctamente");
        
        // ============ FASE 4: VERIFICAR CONSISTENCIA ============
        
        // 4.1 Verificar IDs válidos
        assertTrue(existeRegistro("proyectos", "id", idProyecto), 
                "Proyecto debe existir en BD");
        assertTrue(existeRegistro("tareas", "id", idTarea1), 
                "Tarea 1 debe existir en BD");
        assertTrue(existeRegistro("tareas", "id", idTarea2), 
                "Tarea 2 debe existir en BD");
        
        // 4.2 Verificar asociaciones (Foreign Keys)
        List<Tarea> tareasAsociadas = tareaDAO.listarPorProyecto(idProyecto);
        assertEquals(2, tareasAsociadas.size(), "Deben existir 2 tareas asociadas");
        assertTrue(tareasAsociadas.stream().anyMatch(t -> t.getId() == idTarea1), 
                "Tarea 1 debe estar en la lista");
        assertTrue(tareasAsociadas.stream().anyMatch(t -> t.getId() == idTarea2), 
                "Tarea 2 debe estar en la lista");
        
        // 4.3 Verificar costos del proyecto
        List<Costo> costosProyecto = costoDAO.listarPorReferencia("PROYECTO", idProyecto);
        assertEquals(2, costosProyecto.size(), "Debe haber 2 costos del proyecto");
        
        // 4.4 Verificar costos de la tarea
        List<Costo> costosTarea1 = costoDAO.listarPorReferencia("TAREA", idTarea1);
        assertEquals(1, costosTarea1.size(), "Debe haber 1 costo de la tarea 1");
        
        // ============ FASE 5: VERIFICAR AGREGACIONES ============
        
        // 5.1 Suma total de costos del proyecto (solo tipo GASTO_PLANIFICADO)
        double totalGastosPlanificadosProyecto = costoDAO.calcularTotalPorTipo(
                "PROYECTO", idProyecto, "GASTO_PLANIFICADO");
        
        assertEquals(8000.0, totalGastosPlanificadosProyecto, 0.01, 
                "Total de gastos planificados debe ser 5000 + 3000 = 8000");
        
        // 5.2 Suma total de costos de la tarea (tipo ADELANTO)
        double totalAdelantosTarea1 = costoDAO.calcularTotalPorTipo(
                "TAREA", idTarea1, "ADELANTO");
        
        assertEquals(1500.0, totalAdelantosTarea1, 0.01, 
                "Total de adelantos de tarea 1 debe ser 1500");
        
        // 5.3 Verificar presupuesto vs costos reales
        double costosRealesProyecto = costosProyecto.stream()
                .mapToDouble(Costo::getMonto)
                .sum();
        
        assertEquals(8000.0, costosRealesProyecto, 0.01, 
                "Costos reales del proyecto deben ser 8000");
        
        assertTrue(costosRealesProyecto < proyecto.getPresupuestoTotal(), 
                "Costos reales (" + costosRealesProyecto + 
                ") deben ser menores al presupuesto (" + proyecto.getPresupuestoTotal() + ")");
        
        System.out.println("✅ Todas las verificaciones de consistencia pasaron");
        
        // ============ FASE 6: VERIFICAR INTEGRIDAD REFERENCIAL ============
        
        // 6.1 Intentar crear tarea con proyecto inexistente (debe fallar)
        Tarea tareaInvalida = new Tarea();
        tareaInvalida.setNombre("Tarea con proyecto inválido");
        tareaInvalida.setDescripcion("No debe crearse");
        tareaInvalida.setFechaCreacion(new Date());
        tareaInvalida.setFechaVencimiento(new Date());
        tareaInvalida.setIdProyecto(99999); // Proyecto inexistente
        tareaInvalida.setIdResponsable(1);
        tareaInvalida.setEstado("PENDIENTE");
        
        Tarea tareaInvalidaResultado = tareaDAO.insertar(tareaInvalida);
        assertNull(tareaInvalidaResultado, 
                "No debe permitir crear tarea con proyecto inexistente (FK constraint)");
        
        System.out.println("✅ Integridad referencial verificada");
    }

    // ========================================
    // FLUJO: ACTUALIZAR Y VERIFICAR CASCADA
    // ========================================

    @Test
    public void flujo_actualizarProyectoYVerificarTareas_mantieneLaAsociacion() throws Exception {
        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Original");
        proyecto.setDescripcion("Descripción original");
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(1);
        proyecto.setNivelRiesgo("BAJO");
        proyecto.setPresupuestoTotal(10000.0);
        
        Proyecto proyectoCreado = proyectoDAO.insertar(proyecto);
        int idProyecto = proyectoCreado.getId();
        
        // Crear tarea asociada
        Tarea tarea = new Tarea();
        tarea.setNombre("Tarea del proyecto");
        tarea.setDescripcion("Descripción");
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(idProyecto);
        tarea.setIdResponsable(1);
        tarea.setEstado("PENDIENTE");
        
        Tarea tareaCreada = tareaDAO.insertar(tarea);
        
        // Actualizar proyecto
        proyectoCreado.setNombre("Proyecto Actualizado");
        proyectoCreado.setPresupuestoTotal(15000.0);
        boolean actualizado = proyectoDAO.actualizar(proyectoCreado);
        
        assertTrue(actualizado, "Proyecto debe actualizarse correctamente");
        
        // Verificar que la tarea sigue asociada
        Tarea tareaRecuperada = tareaDAO.buscarPorId(tareaCreada.getId());
        assertNotNull(tareaRecuperada, "Tarea debe seguir existiendo");
        assertEquals(idProyecto, tareaRecuperada.getIdProyecto(), 
                "Tarea debe seguir asociada al proyecto");
        
        // Verificar datos actualizados del proyecto
        Proyecto proyectoRecuperado = proyectoDAO.buscarPorId(idProyecto);
        assertEquals("Proyecto Actualizado", proyectoRecuperado.getNombre(), 
                "Nombre del proyecto debe estar actualizado");
        assertEquals(15000.0, proyectoRecuperado.getPresupuestoTotal(), 0.01, 
                "Presupuesto debe estar actualizado");
    }

    // ========================================
    // FLUJO: LISTAR Y FILTRAR
    // ========================================

    @Test
    public void flujo_listarTareasPorProyectoYCostosPorUsuario_filtraCorrectamente() throws Exception {
        // Crear 2 proyectos
        Proyecto proyecto1 = crearProyecto("Proyecto A", 1);
        Proyecto proyecto2 = crearProyecto("Proyecto B", 2);
        
        int idProyecto1 = proyecto1.getId();
        int idProyecto2 = proyecto2.getId();
        
        // Crear tareas para cada proyecto
        crearTarea("Tarea A1", idProyecto1, 1);
        crearTarea("Tarea A2", idProyecto1, 1);
        crearTarea("Tarea B1", idProyecto2, 2);
        
        // Listar tareas por proyecto
        List<Tarea> tareasProyecto1 = tareaDAO.listarPorProyecto(idProyecto1);
        List<Tarea> tareasProyecto2 = tareaDAO.listarPorProyecto(idProyecto2);
        
        assertEquals(2, tareasProyecto1.size(), "Proyecto 1 debe tener 2 tareas");
        assertEquals(1, tareasProyecto2.size(), "Proyecto 2 debe tener 1 tarea");
        
        // Crear costos de diferentes usuarios
        crearCosto("PROYECTO", idProyecto1, 1000.0, 1);
        crearCosto("PROYECTO", idProyecto1, 2000.0, 1);
        crearCosto("PROYECTO", idProyecto2, 1500.0, 2);
        
        // Listar costos por usuario
        List<Costo> costosUsuario1 = costoDAO.listarPorUsuario(1);
        List<Costo> costosUsuario2 = costoDAO.listarPorUsuario(2);
        
        assertEquals(2, costosUsuario1.size(), "Usuario 1 debe tener 2 costos");
        assertEquals(1, costosUsuario2.size(), "Usuario 2 debe tener 1 costo");
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private Proyecto crearProyecto(String nombre, int idResponsable) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(nombre);
        proyecto.setDescripcion("Descripción de " + nombre);
        proyecto.setFechaInicio(new Date());
        proyecto.setIdResponsable(idResponsable);
        proyecto.setNivelRiesgo("MEDIO");
        proyecto.setPresupuestoTotal(10000.0);
        return proyectoDAO.insertar(proyecto);
    }

    private Tarea crearTarea(String nombre, int idProyecto, int idResponsable) {
        Tarea tarea = new Tarea();
        tarea.setNombre(nombre);
        tarea.setDescripcion("Descripción de " + nombre);
        tarea.setFechaCreacion(new Date());
        tarea.setFechaVencimiento(new Date());
        tarea.setIdProyecto(idProyecto);
        tarea.setIdResponsable(idResponsable);
        tarea.setEstado("PENDIENTE");
        return tareaDAO.insertar(tarea);
    }

    private Costo crearCosto(String tipo, int idReferencia, double monto, int idUsuario) {
        Costo costo = new Costo();
        costo.setTipo(tipo);
        costo.setIdReferencia(idReferencia);
        costo.setDescripcion("Costo de prueba");
        costo.setMonto(monto);
        costo.setTipoCosto("GASTO_PLANIFICADO");
        costo.setFechaRegistro(new Date());
        costo.setIdUsuarioRegistro(idUsuario);
        return costoDAO.insertar(costo);
    }
}
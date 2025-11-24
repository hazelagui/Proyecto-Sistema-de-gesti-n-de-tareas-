package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.TareaDAO;
import com.mycompany.sistemagestiondetareas.modelo.Tarea;
import com.mycompany.sistemagestiondetareas.util.Notificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorTareaTest {

    private ControladorTarea controlador;
    private TareaDAO mockDAO;
    private Notificador mockNotificador;

    @BeforeEach
    public void setUp() throws Exception {
        controlador = new ControladorTarea();
        mockDAO = mock(TareaDAO.class);
        mockNotificador = mock(Notificador.class);

        // Inyectar mocks
        Field fDao = ControladorTarea.class.getDeclaredField("tareaDAO");
        fDao.setAccessible(true);
        fDao.set(controlador, mockDAO);

        Field fNotif = ControladorTarea.class.getDeclaredField("notificador");
        fNotif.setAccessible(true);
        fNotif.set(controlador, mockNotificador);
    }

    // -----------------------------------------------------
    //   CREACIÓN DE TAREAS
    // -----------------------------------------------------

    @Test
    public void crearTarea_datosValidos_retornaTarea() {
        Date hoy = new Date();
        Date mañana = new Date(hoy.getTime() + 86400000L);

        Tarea mockRetorno = new Tarea("A", "B", hoy, mañana, 1, 1, "PENDIENTE", "");
        mockRetorno.setId(10);

        when(mockDAO.insertar(any(Tarea.class))).thenReturn(mockRetorno);

        Tarea t = controlador.crearTarea(
                "A", "B", hoy, mañana, 1, 1, "PENDIENTE", ""
        );

        assertNotNull(t);
        assertEquals(10, t.getId());
        verify(mockDAO, times(1)).insertar(any());
    }

    @Test
    public void crearTarea_datosInvalidos_retornaNull() {
        Date hoy = new Date();

        assertNull(controlador.crearTarea("", "desc", hoy, hoy, 1, 1, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "", hoy, hoy, 1, 1, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "desc", null, hoy, 1, 1, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "desc", hoy, null, 1, 1, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "desc", hoy, hoy, 0, 1, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "desc", hoy, hoy, 1, 0, "PENDIENTE", ""));
        assertNull(controlador.crearTarea("nom", "desc", hoy, hoy, 1, 1, "", ""));

        verify(mockDAO, never()).insertar(any());
    }

    @Test
    public void crearTarea_estadoInvalido_defaultPendiente() {
        Date hoy = new Date();
        Date mañana = new Date(hoy.getTime() + 86400000L);

        when(mockDAO.insertar(any())).thenAnswer(inv -> inv.getArgument(0));

        Tarea t = controlador.crearTarea("A", "B", hoy, mañana, 1, 1, "XXX", null);

        assertNotNull(t);
        assertEquals("PENDIENTE", t.getEstado());
    }

    // -----------------------------------------------------
    //   ACTUALIZAR TAREA
    // -----------------------------------------------------

    @Test
    public void actualizarTarea_preservaFechaCreacion() {
        Date creacion = new Date();
        Tarea existente = new Tarea(5, "Old", "Old", creacion,
                new Date(), 1, 1, "PENDIENTE", "");

        when(mockDAO.buscarPorId(5)).thenReturn(existente);
        when(mockDAO.actualizar(any())).thenReturn(true);

        Date nuevaFechaV = new Date(creacion.getTime() + 8640000L);

        boolean ok = controlador.actualizarTarea(
                5, "Nuevo", "Nuevo", nuevaFechaV, 1, 1, "COMPLETADA", "coment"
        );

        assertTrue(ok);
        verify(mockDAO, times(1)).actualizar(argThat(t ->
                t.getFechaCreacion().equals(creacion) &&
                t.getEstado().equals("COMPLETADA")
        ));
    }

    @Test
    public void actualizarTarea_invalida_retornaFalse() {
        assertFalse(controlador.actualizarTarea(0, "A", "B", new Date(), 1, 1, "PENDIENTE", ""));
        assertFalse(controlador.actualizarTarea(1, "", "B", new Date(), 1, 1, "PENDIENTE", ""));
        assertFalse(controlador.actualizarTarea(1, "A", "", new Date(), 1, 1, "PENDIENTE", ""));
        assertFalse(controlador.actualizarTarea(1, "A", "B", null, 1, 1, "PENDIENTE", ""));
        verify(mockDAO, never()).actualizar(any());
    }

    @Test
    public void actualizarTarea_noExiste_retornaFalse() {
        when(mockDAO.buscarPorId(5)).thenReturn(null);
        boolean res = controlador.actualizarTarea(
                5, "A", "B", new Date(), 1, 1, "PENDIENTE", ""
        );
        assertFalse(res);
        verify(mockDAO, never()).actualizar(any());
    }

    // -----------------------------------------------------
    //   ACTUALIZAR ESTADO
    // -----------------------------------------------------

    @Test
    public void actualizarEstadoTarea_valido_enviaNotificacion() {
        Tarea mockT = new Tarea(5, "A", "B", new Date(),
                new Date(), 1, 1, "PENDIENTE", "");

        when(mockDAO.buscarPorId(5)).thenReturn(mockT);
        when(mockDAO.actualizarEstado(5, "COMPLETADA", "ok")).thenReturn(true);

        boolean ok = controlador.actualizarEstadoTarea(5, "COMPLETADA", "ok");

        assertTrue(ok);
        verify(mockDAO, times(1)).actualizarEstado(5, "COMPLETADA", "ok");
        verify(mockNotificador, times(1))
                .notificarCambioEstadoTarea(any(), eq("PENDIENTE"));
    }

    @Test
    public void actualizarEstadoTarea_invalido_retornaFalse() {
        assertFalse(controlador.actualizarEstadoTarea(0, "A", null));
        assertFalse(controlador.actualizarEstadoTarea(5, "", null));
        verify(mockDAO, never()).actualizarEstado(anyInt(), any(), any());
    }

    @Test
    public void actualizarEstadoTarea_noExiste_retornaFalse() {
        when(mockDAO.buscarPorId(5)).thenReturn(null);
        assertFalse(controlador.actualizarEstadoTarea(5, "PENDIENTE", ""));
        verify(mockDAO, never()).actualizarEstado(anyInt(), any(), any());
    }

    // -----------------------------------------------------
    //   OBTENER TAREAS
    // -----------------------------------------------------

    @Test
    public void obtenerTareaPorId_invalido_retornaNull() {
        assertNull(controlador.obtenerTareaPorId(0));
        verify(mockDAO, never()).buscarPorId(anyInt());
    }

    @Test
    public void obtenerTareaPorId_valido() {
        Tarea t = new Tarea("A", "B", new Date(), new Date(), 1, 1, "PENDIENTE", "");
        t.setId(3);

        when(mockDAO.buscarPorId(3)).thenReturn(t);

        Tarea res = controlador.obtenerTareaPorId(3);
        assertNotNull(res);
        assertEquals(3, res.getId());
    }

    @Test
    public void obtenerTareasPorProyecto_invalido_retornaVacia() {
        List<Tarea> res = controlador.obtenerTareasPorProyecto(0);
        assertTrue(res.isEmpty());
        verify(mockDAO, never()).listarPorProyecto(anyInt());
    }

    @Test
    public void obtenerTareasPorProyecto_valido() {
        when(mockDAO.listarPorProyecto(2)).thenReturn(Collections.emptyList());
        controlador.obtenerTareasPorProyecto(2);
        verify(mockDAO, times(1)).listarPorProyecto(2);
    }

    @Test
    public void obtenerTareasPorResponsable_invalido_retornaVacia() {
        assertTrue(controlador.obtenerTareasPorResponsable(0).isEmpty());
        verify(mockDAO, never()).listarPorResponsable(anyInt());
    }

    @Test
    public void obtenerTareasPorResponsable_valido() {
        when(mockDAO.listarPorResponsable(2)).thenReturn(Collections.emptyList());
        controlador.obtenerTareasPorResponsable(2);
        verify(mockDAO, times(1)).listarPorResponsable(2);
    }

    // -----------------------------------------------------
    //   ELIMINAR
    // -----------------------------------------------------

    @Test
    public void eliminarTarea_invalido() {
        assertFalse(controlador.eliminarTarea(0));
        verify(mockDAO, never()).eliminar(anyInt());
    }

    @Test
    public void eliminarTarea_valido() {
        when(mockDAO.eliminar(5)).thenReturn(true);
        assertTrue(controlador.eliminarTarea(5));
        verify(mockDAO, times(1)).eliminar(5);
    }
}



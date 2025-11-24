package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.ProyectoDAO;
import com.mycompany.sistemagestiondetareas.modelo.Proyecto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorProyectoTest {

    private ControladorProyecto controlador;
    private ProyectoDAO mockDao;

    @BeforeEach
    public void setUp() throws Exception {
        controlador = new ControladorProyecto();
        mockDao = mock(ProyectoDAO.class);

        // Inyectar mock al campo privado final proyectoDAO
        Field f = ControladorProyecto.class.getDeclaredField("proyectoDAO");
        f.setAccessible(true);
        f.set(controlador, mockDao);
    }

    // -------------------------
    // CREACIÓN DE PROYECTOS
    // -------------------------

    @Test
    public void crearProyecto_datosValidos_retornaProyecto() {
        Date inicio = new Date();
        Date fin = new Date(inicio.getTime() + 86400000L);

        Proyecto mockRetorno = new Proyecto(
                "Proyecto X",
                "Desc X",
                inicio,
                fin,
                1,
                "VERDE",
                1000.0
        );
        mockRetorno.setId(10);

        when(mockDao.insertar(any(Proyecto.class))).thenReturn(mockRetorno);

        Proyecto res = controlador.crearProyecto(
                "Proyecto X",
                "Desc X",
                inicio,
                fin,
                1,
                "VERDE",
                1000.0
        );

        assertNotNull(res);
        assertEquals(10, res.getId());
        assertEquals("Proyecto X", res.getNombre());
        assertEquals("VERDE", res.getNivelRiesgo());
        verify(mockDao, times(1)).insertar(any(Proyecto.class));
    }

    @Test
    public void crearProyecto_datosInvalidos_retornaNull() {
        // nombre vacío
        Proyecto res1 = controlador.crearProyecto(
                "", "desc", new Date(), null, 1, "VERDE", 0
        );
        assertNull(res1);

        // responsable inválido
        Proyecto res2 = controlador.crearProyecto(
                "N", "D", new Date(), null, 0, "VERDE", 0
        );
        assertNull(res2);

        verify(mockDao, never()).insertar(any());
    }

    @Test
    public void crearProyecto_nivelRiesgoInvalido_seReemplazaPorVerde() {
        when(mockDao.insertar(any())).thenAnswer(inv -> inv.getArgument(0));

        Proyecto res = controlador.crearProyecto(
                "P", "D",
                new Date(), null,
                1,
                "XXX",   // inválido
                0
        );

        assertNotNull(res);
        assertEquals("VERDE", res.getNivelRiesgo());
        verify(mockDao, times(1)).insertar(any());
    }

    // -------------------------
    // OBTENER POR ID
    // -------------------------

    @Test
    public void obtenerProyectoPorId_invalido_retornaNull() {
        assertNull(controlador.obtenerProyectoPorId(0));
        verify(mockDao, never()).buscarPorId(anyInt());
    }

    @Test
    public void obtenerProyectoPorId_existe_retornaProyecto() {
        Proyecto p = new Proyecto("P", "D", new Date(), null, 1, "VERDE", 100);
        p.setId(5);
        when(mockDao.buscarPorId(5)).thenReturn(p);

        Proyecto res = controlador.obtenerProyectoPorId(5);

        assertNotNull(res);
        assertEquals(5, res.getId());
        verify(mockDao, times(1)).buscarPorId(5);
    }

    // -------------------------
    // LISTAR TODOS
    // -------------------------

    @Test
    public void obtenerTodosLosProyectos_retornaLista() {
        Proyecto p = new Proyecto("A", "B", new Date(), null, 1, "VERDE", 0);
        when(mockDao.listarTodos()).thenReturn(Arrays.asList(p));

        List<Proyecto> list = controlador.obtenerTodosLosProyectos();

        assertEquals(1, list.size());
        verify(mockDao, times(1)).listarTodos();
    }

    // -------------------------
    // LISTAR POR RESPONSABLE
    // -------------------------

    @Test
    public void obtenerProyectosPorResponsable_invalido_retornaListaVacia() {
        List<Proyecto> list = controlador.obtenerProyectosPorResponsable(0);
        assertTrue(list.isEmpty());
        verify(mockDao, never()).listarPorResponsable(anyInt());
    }

    @Test
    public void obtenerProyectosPorResponsable_valido() {
        Proyecto p = new Proyecto("A", "B", new Date(), null, 1, "VERDE", 0);
        when(mockDao.listarPorResponsable(1)).thenReturn(Collections.singletonList(p));

        List<Proyecto> list = controlador.obtenerProyectosPorResponsable(1);

        assertEquals(1, list.size());
        verify(mockDao, times(1)).listarPorResponsable(1);
    }

    // -------------------------
    // ACTUALIZAR
    // -------------------------

    @Test
    public void actualizarProyecto_llamaDAO() {
        Proyecto p = new Proyecto("A", "B", new Date(), null, 1, "VERDE", 0);
        when(mockDao.actualizar(p)).thenReturn(true);

        boolean ok = controlador.actualizarProyecto(p);
        assertTrue(ok);

        verify(mockDao, times(1)).actualizar(p);
    }

    // -------------------------
    // ELIMINAR
    // -------------------------

    @Test
    public void eliminarProyecto_idInvalido_retornaFalse() {
        assertFalse(controlador.eliminarProyecto(0));
        verify(mockDao, never()).eliminar(anyInt());
    }

    @Test
    public void eliminarProyecto_valido() {
        when(mockDao.eliminar(5)).thenReturn(true);

        assertTrue(controlador.eliminarProyecto(5));
        verify(mockDao, times(1)).eliminar(5);
    }
}
